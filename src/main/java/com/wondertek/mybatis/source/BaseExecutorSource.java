package com.wondertek.mybatis.source;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ResultExtractor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.apache.ibatis.executor.ExecutionPlaceholder.EXECUTION_PLACEHOLDER;

/**
 * BaseExecutor源码分析
 * @Author zbc
 * @Date 21:21 - 2018/12/30
 */
public abstract class BaseExecutorSource implements Executor {
    // 事务，提交，回滚，关闭事务
    protected Transaction transaction;
    // 底层的 Executor 对象
    protected Executor wrapper;
    // 延迟加载队列
    protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
    // 一级缓存，用于缓存查询结果
    protected PerpetualCache localCache;
    // 一级缓存，用于缓存输出类型参数（存储过程）
    protected PerpetualCache localOutputParameterCache;
    protected Configuration configuration;
    // 用来记录嵌套查询的层数
    protected int queryStack;
    private boolean closed;

    protected BaseExecutorSource(Configuration configuration, Transaction transaction) {
        this.transaction = transaction;
        this.deferredLoads = new ConcurrentLinkedQueue<DeferredLoad>();
        this.localCache = new PerpetualCache("LocalCache");
        this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");
        this.closed = false;
        this.configuration = configuration;
        this.wrapper = this;
    }

    // 4 个抽象方法，由子类实现，模板方法中可变部分
    protected abstract int doUpdate(MappedStatement ms, Object parameter)throws SQLException;
    protected abstract List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException;
    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)throws SQLException;
    protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql)throws SQLException;

    // 执行 insert | update | delete 语句，调用 doUpdate 方法实现,在执行这些语句的时候，会清空缓存
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // ....
        // 清空缓存
        clearLocalCache();
        // 执行SQL语句
        return doUpdate(ms, parameter);
    }

    // 刷新批处理语句，且执行缓存中还没执行的SQL语句
    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return flushStatements(false);
    }
    public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
        // ...
        // doFlushStatements 的 isRollBack 参数表示是否执行缓存中的SQL语句，false表示执行，true表示不执行
        return doFlushStatements(isRollBack);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        // 获取查询SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        // 创建缓存的key，创建逻辑在 CacheKey中已经分析过了
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        // 执行查询
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    // 执行查询逻辑
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        // ....
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            // 如果不是嵌套查询，且 <select> 的 flushCache=true 时才会清空缓存
            clearLocalCache();
        }
        List<E> list;
        try {
            // 嵌套查询层数加1
            queryStack++;
            // 首先从一级缓存中进行查询
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list != null) {
                // 如果命中缓存，则处理存储过程
                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                // 如果缓存中没有对应的数据，则查数据库中查询数据
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        // ... 处理延迟加载的相关逻辑
        return list;
    }

    // 从数据库查询数据
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        // 在缓存中添加占位符
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {
            // 查库操作，由子类实现
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            // 删除占位符
            localCache.removeObject(key);
        }
        // 将从数据库查询的结果添加到一级缓存中
        localCache.putObject(key, list);
        // 处理存储过程
        if (ms.getStatementType() == StatementType.CALLABLE) {
            localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }

    // 查询存储过程
    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameter);
        return doQueryCursor(ms, parameter, rowBounds, boundSql);
    }

    // 事务的提交和回滚
    @Override
    public void commit(boolean required) throws SQLException {
        // 清空缓存
        clearLocalCache();
        // 刷新批处理语句，且执行缓存中的QL语句
        flushStatements();
        if (required) {
            transaction.commit();
        }
    }
    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                // 清空缓存
                clearLocalCache();
                // 刷新批处理语句，且不执行缓存中的SQL
                flushStatements(true);
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            final Object cachedParameter = localOutputParameterCache.getObject(key);
            if (cachedParameter != null && parameter != null) {
                final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
                final MetaObject metaParameter = configuration.newMetaObject(parameter);
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    if (parameterMapping.getMode() != ParameterMode.IN) {
                        final String parameterName = parameterMapping.getProperty();
                        final Object cachedValue = metaCachedParameter.getValue(parameterName);
                        metaParameter.setValue(parameterName, cachedValue);
                    }
                }
            }
        }
    }

    private static class DeferredLoad {

        private final MetaObject resultObject;
        private final String property;
        private final Class<?> targetType;
        private final CacheKey key;
        private final PerpetualCache localCache;
        private final ObjectFactory objectFactory;
        private final ResultExtractor resultExtractor;

        // issue #781
        public DeferredLoad(MetaObject resultObject,
                            String property,
                            CacheKey key,
                            PerpetualCache localCache,
                            Configuration configuration,
                            Class<?> targetType) {
            this.resultObject = resultObject;
            this.property = property;
            this.key = key;
            this.localCache = localCache;
            this.objectFactory = configuration.getObjectFactory();
            this.resultExtractor = new ResultExtractor(configuration, objectFactory);
            this.targetType = targetType;
        }

        public boolean canLoad() {
            return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
        }

        public void load() {
            @SuppressWarnings("unchecked")
            // we suppose we get back a List
                    List<Object> list = (List<Object>) localCache.getObject(key);
            Object value = resultExtractor.extractObjectFromList(list, targetType);
            resultObject.setValue(property, value);
        }
    }
}
