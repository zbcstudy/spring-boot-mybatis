package com.wondertek.mybatis.lock.interceptor;

import com.wondertek.mybatis.lock.annotation.VersionLocker;
import com.wondertek.mybatis.lock.uitl.Constent;
import com.wondertek.mybatis.lock.uitl.PluginUtil;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author zbc
 * @Date 23:05-2019/2/19
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = ParameterHandler.class, method = "setParameters", args = PreparedStatement.class)
})
public class OptimisticLocker implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(OptimisticLocker.class);
    private String versionColumn;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取方法名称
        String invocationMethod = invocation.getMethod().getName();
        if ("prepare".equals(invocationMethod)) {
            StatementHandler routingHandler = (StatementHandler) PluginUtil.processTarget(invocation.getTarget());
            MetaObject routingMeta = SystemMetaObject.forObject(routingHandler);
            MetaObject metaObject = routingMeta.metaObjectForProperty("delegate");
            VersionLocker versionLocker = VersionLockerResolver.resolve(metaObject);
            //非更新操作
            if (versionLocker != null && !versionLocker.value()) {
                //继续执行调用链
                return invocation.proceed();
            }
            String originalSql = (String) metaObject.getValue("boundSql.sql");
            StringBuilder builder = new StringBuilder(originalSql);
            builder.append(" AND ");
            builder.append(versionColumn);
            builder.append(" = ?");
            metaObject.setValue("boundSql.sql", builder.toString());
        } else if ("setParameters".equals(invocationMethod)) {
            ParameterHandler parameterHandler = (ParameterHandler) PluginUtil.processTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);

            VersionLocker versionLocker = VersionLockerResolver.resolve(metaObject);
            //非更新操作
            if (versionLocker != null && !versionLocker.value()) {
                return invocation.proceed();
            }

            BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
            Object parameterObject = boundSql.getParameterObject();
            if (parameterObject instanceof MapperMethod.ParamMap<?>) {
                MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) parameterObject;
                if (!paramMap.containsKey(versionColumn)) {
                    throw new TypeException("All the primitive type parameters must add MyBatis's @Param Annotaion");
                }
            }

            Configuration configuration = ((MappedStatement) metaObject.getValue("mappedStatement")).getConfiguration();
            MetaObject mo = configuration.newMetaObject(parameterObject);
            Object value = mo.getValue(versionColumn);
            ParameterMapping parameterMapping = new ParameterMapping.Builder(configuration, versionColumn,
                    Object.class).build();
            TypeHandler typeHandler = parameterMapping.getTypeHandler();

            JdbcType jdbcType = parameterMapping.getJdbcType();
            if (value == null && jdbcType == null) {
                jdbcType = configuration.getJdbcTypeForNull();
            }

            int versionLocation = boundSql.getParameterMappings().size() + 1;

            try {
                PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
                typeHandler.setParameter(ps, versionLocation, value, jdbcType);
            } catch (TypeException | SQLException e) {
                throw new TypeException("set parameter 'version' failed, Cause: " + e, e);
            }

            if (!Objects.equals(value.getClass(), Long.class) && Objects.equals(value.getClass(), long.class) && log.isDebugEnabled()) {
                log.error(Constent.LOG_PREFIX + "property type error, the type of version property must be Long or long.");
            }

            // increase version
            mo.setValue(versionColumn, (long) value + 1);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler || target instanceof ParameterHandler)
            return Plugin.wrap(target, this);
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        versionColumn = properties.getProperty("versionColumn", "version");
    }
}
