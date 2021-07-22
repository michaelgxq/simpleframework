package org.simpleframework.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于获取请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）中对应 Key 的 Value 的
 * 该注解的功能和 Spring MVC 中的 @RequestParam 注解的功能基本一致
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    // 该属性用于设置请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）中的 Key 的
    String value() default "";
    // 该属性用于设置当前这个请求参数是否是必须的
    boolean required() default true;
}
