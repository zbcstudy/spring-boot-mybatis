package com.wondertek.mybatis.lock.uitl;

import com.wondertek.mybatis.lock.exception.LockException;
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

    /**
     * MetaObject是用于包装Object的，将Object包装成ObjectWrapper，MapWrapper，CollectionWrapper，BeanWrapper等，
     * ObjectWrapper是对应属性及方法的一个包装类，MetaObject获取obejct的属性值，以及给属性设置，是委托给
     * ObjectWrapper，ObjectWrapper在委托给MetaClass，MetaClass最后交Reflector；
     * Reflector包含Obejct的属性名，set方法，以及get方法的描述，MetaObject获取属性值和设置属性值方法，
     * 本质上是，从Reflector获取属性对应方法的Invoker，然后invoke。下面一节，我们讲Reflector。
     * @param target
     * @return
     */
    public static Object processTarget(Object target) {
        //判断目标类是否是代理类
        if (Proxy.isProxyClass(target.getClass())) {
            //获取代理类的原始类，使用MetaObject进行包装
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
