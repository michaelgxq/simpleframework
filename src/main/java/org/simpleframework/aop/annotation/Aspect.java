package org.simpleframework.aop.annotation;

import java.lang.annotation.*;

/**
 * 定义 @Aspect 注解，它的功能和 Spring AOP 中的 @Aspect 注解一样
 * 该注解也只能写在类上
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    // 该属性就是用于定义切入点的（它的值就是一个能够被 AspectJ 解析的表达式）
    String pointcut();
    //"execution(* com.imooc.controller.frontend..*.*(..))"以及within(com.imooc.controller.frontend.*)
}
