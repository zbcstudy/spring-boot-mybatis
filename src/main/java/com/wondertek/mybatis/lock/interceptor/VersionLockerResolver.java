package com.wondertek.mybatis.lock.interceptor;

import com.wondertek.mybatis.lock.annotation.VersionLocker;
import com.wondertek.mybatis.lock.cache.Cache;
import com.wondertek.mybatis.lock.cache.LocalVersionLockerCache;
import com.wondertek.mybatis.lock.cache.VersionLockerCache;
import com.wondertek.mybatis.lock.exception.LockException;
import com.wondertek.mybatis.lock.uitl.Constent;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zbc
 * @Date 22:08-2019/2/19
 */
public class VersionLockerResolver {

    private static final Logger log = LoggerFactory.getLogger(VersionLockerResolver.class);

    private static final VersionLockerCache VERSION_LOCKER_CACHE = new LocalVersionLockerCache();

    private static final Map<String, Class<?>> mapperMap = new ConcurrentHashMap<>();

    private static final VersionLocker trueLocker;

    private static final VersionLocker falseLocker;

    static {
        try {
            trueLocker = VersionLockerResolver.class.getDeclaredMethod("trueVersionValue").getAnnotation(VersionLocker.class);
            falseLocker =
                    VersionLockerResolver.class.getDeclaredMethod("falseVersionValue").getAnnotation(VersionLocker.class);
        } catch (NoSuchMethodException e) {
            throw new LockException("Optimistic Locker Plugin init failed." + e, e);
        }
    }

    @VersionLocker(value = true)
    private void trueVersionValue() {
    }

    @VersionLocker(value = false)
    private void falseVersionValue() {
    }

    static VersionLocker resolve(MetaObject metaObject) {
        //获取方法与xml文件的映射
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");

        //获取执行方法的类型 如果不是update，返回false
        if (!Objects.equals(mappedStatement.getSqlCommandType(), SqlCommandType.UPDATE)) {
            return falseLocker;
        }

        //获取SQL语句
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        //获取参数列表对象
        Object parameterObject = boundSql.getParameterObject();

        Class<?>[] paramClasses = null;

        //参数列表是否是一个map
        if (parameterObject instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) parameterObject;
            if (!paramMap.isEmpty()) {
                paramClasses = new Class[paramMap.size() >> 1];
                int pmSize = paramMap.size() >> 1;
                for (int i = 0; i < pmSize; i++) {
                    Object mapValue = paramMap.get("param" + i);
                    paramClasses[i] = mapValue.getClass();
                }
            }
        }
        //参数类型是map
        else if (parameterObject instanceof Map) {
            paramClasses = new Class<?>[]{Map.class};
        }
        //参数类型是pojo
        else {
            paramClasses = new Class<?>[]{parameterObject.getClass()};
        }

        String id = mappedStatement.getId();
        Cache.MethodSignature methodSignature = new Cache.MethodSignature(id, paramClasses);
        VersionLocker versionLocker = VERSION_LOCKER_CACHE.getVersionLocker(methodSignature);
        if (null != versionLocker) {
            return versionLocker;
        }

        if (mapperMap.isEmpty()) {
            Collection<Class<?>> mappers = mappedStatement.getConfiguration().getMapperRegistry().getMappers();
            if (null != mappers && !mappers.isEmpty()) {
                for (Class<?> mapper : mappers) {
                    mapperMap.put(mapper.getName(), mapper);
                }
            }
        }

        int pos = id.lastIndexOf(".");
        String nameSpace = id.substring(0, pos);
        if (!mapperMap.containsKey(nameSpace) && log.isDebugEnabled()) {
            log.debug(Constent.LOG_PREFIX + "Config info error, maybe you have not config the Mapper interface");
            throw new LockException("Config info error, maybe you have not config the Mapper interface");
        }

        Class<?> mapper = mapperMap.get(nameSpace);
        Method method;
        try {
            method = mapper.getMethod(id.substring(pos + 1), paramClasses);
        } catch (NoSuchMethodException e) {
            throw new LockException("The Map type param error." + e, e);
        }

        versionLocker = method.getAnnotation(VersionLocker.class);

        if (null == versionLocker) {
            versionLocker = trueLocker;
        }

        if (!VERSION_LOCKER_CACHE.containMethodSignature(methodSignature)) {
            VERSION_LOCKER_CACHE.cacheMethod(methodSignature, versionLocker);
        }

        return versionLocker;
    }
}
