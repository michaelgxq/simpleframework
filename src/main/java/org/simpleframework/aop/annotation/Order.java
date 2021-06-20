package org.simpleframework.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 @Order 注解，该注解的功能和 Spring AOP 中的 @Order 注解一样
 * 如果定义了多个切面，那么就可以用该注解定义每个切面的执行顺序（即优先级）
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    /**
     * 该属性就是用于定义执行顺序的，值越小优先级越高
     */
    int value();
}
