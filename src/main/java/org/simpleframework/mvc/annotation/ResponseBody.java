package org.simpleframework.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于标识是否需要把返回客户端的响应数据转为为 JSON 格式字符串
 * 该注解和 Spring MVC 中的 @ResponseBody 注解的功能基本一样
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBody {
}
