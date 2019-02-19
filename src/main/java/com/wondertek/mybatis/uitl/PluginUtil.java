package com.wondertek.mybatis.uitl;

import com.wondertek.mybatis.exception.LockException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @Author zbc
 * @Date 20:20-2019/2/19
 */
public class PluginUtil {

    private static final Logger log = LoggerFactory.getLogger(PluginUtil.class);

    private PluginUtil() {
    }

    public static Object processTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return processTarget(metaObject.getValue("h.target"));
        }

        if (!(target instanceof StatementHandler) && !(target instanceof ParameterHandler)) {
            if (log.isDebugEnabled()) {
                log.error("{}plugin init failed",Constent.LOG_PREFIX);
            }
            throw new LockException(Constent.LOG_PREFIX + "plugin init failed");
        }
        return target;
    }
}
