package org.simpleframework.mvc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 该类是对 Controller 类（即加了 @RequestMapping 注解的类）中
 * 所有加了 @RequestMapping 注解的方法的相关信息的封装（这些信息我们可以看作是该方法的元信息）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControllerMethod {
    // 该方法所在的 Controller 类的 Class 对象
    private Class<?> controllerClass;
    // 该方法所对应的 Method 类对象
    private Method invokeMethod;
    // 该集合中存放的是该方法中，加了 @RequestParams 注解的形参（这里我们实现的时候规定所有形参都需要加 @RequestParams 注解）
    // 即
    // 该集合的 Key 为加了 @RequestParams 注解的形参的名字，Value 为该形参的数据类型对应的 Class 类对象
    // 注意
    // 为了实现方便
    // 这里我们实现的时候规定所有形参都需要加 @RequestParams 注解（因为后面通过反射调用该方法时要用到这些参数）
    // 同样为了实现方便
    // 我们这里规定方法的形参只能是 String 以及基础类型 char,int,short,byte,double,long,float,boolean,及它们的包装类型
    //（这样我们之后对客户端传来的 String 类型的请求参数进行转化时就会方便不少）
    private Map<String, Class<?>> methodParameters;
}
