package com.wondertek.mybatis.lock.annotation;

import java.lang.annotation.*;

/**
 *  mybatis乐观锁注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface VersionLocker {

    boolean value() default true;
}
