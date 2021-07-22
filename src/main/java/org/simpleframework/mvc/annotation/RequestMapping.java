package org.simpleframework.mvc.annotation;

import org.simpleframework.mvc.type.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于标识 Controller 类中的方法与客户端请求路径和请求方法之间的映射关系
 *（该注解的功能基本和 Spring MVC 中的 @RequestMapping 注解的功能基本一致）
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    // 该属性值用于设置请求路径的，默认值为 空
    String value() default "";
    // 该属性用于设置客户端请求方法的（即是 GET 请求还是 POST 请求），默认值为 GET
    RequestMethod method() default RequestMethod.GET;
}
