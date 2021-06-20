package org.simpleframework.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class ProxyCreator {
    /**
     * 该方法就是用于创建动态代理对象并返回
     * @param targetClass 被代理的Class对象
     * @param methodInterceptor 方法拦截器
     * @return
     */
    public static Object createProxy(Class<?> targetClass, MethodInterceptor methodInterceptor){
        // 调用 CGLib 提供的 Enhancer 类中的 create() 方法创建代理类对象
        return Enhancer.create(targetClass, methodInterceptor);
    }
}
