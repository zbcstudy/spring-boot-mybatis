package com.wondertek.mybatis.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.Properties;

/**
 * 完成插件签名
 *  告诉mybatis当前插件用来拦截哪个对象的哪个方法
 */
@Intercepts({
        @Signature(type = StatementHandler.class,method = "parameterize",args = Statement.class)
})
public class FirstPlugin implements Interceptor {

    /**
     * 拦截目标对象的执行
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("FirstPlugin----intercept " + invocation.getMethod());
        //执行目标方法
        Object proceed = invocation.proceed();

        return proceed;
    }

    /**
     * 包装目标对象
     *  为目标对象创建代理对象
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {

        //借助Plugin的wrap的方法来使用当前interceptor包装我们的目标对象
        Object wrap = Plugin.wrap(target, this);

        //返回当前target创建的动态代理
        return wrap;
    }

    /**
     * 设置插件注册是的properties属性
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件配置的信息" + properties);
    }
}
