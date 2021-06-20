package org.simpleframework.aop.aspect;

import java.lang.reflect.Method;

/**
 * 该抽象类用于定义我们自研的 AOP 框架支持几种通知（即 Advice）
 * 即
 * 这里支持 before（即 前置通知），afterReturning（即 后置通知），afterThrowing（即 异常通知） 这几种类型的通知
 * 这里之所以通过定义抽象类来实现通知的定义，而不是像 Spring AOP 那样，定义 @Before，@After 等注解，主要就是为了实现方便
 */
public abstract class DefaultAspect {
    /**
     * 前置通知（即 前置拦截）
     * @param targetClass 被代理的目标类（即被代理类）
     * @param method 被代理类中的目标方法（即被代理方法）
     * @param args 被代理类中的目标方法（即被代理方法）对应的参数列表
     * @throws Throwable
     */
    public void before(Class<?> targetClass, Method method, Object[] args) throws Throwable{

    }
    /**
     * 后置通知（即 后置拦截）
     * @param targetClass 被代理的目标类（即被代理类）
     * @param method 被代理类中的目标方法（即被代理方法）
     * @param args 被代理类中的目标方法（即被代理方法）对应的参数列表
     * @param returnValue 被代理的目标方法（即被代理方法）执行后的返回值
     * @throws Throwable
     */
    public Object afterReturning(Class<?> targetClass, Method method, Object[] args, Object returnValue) throws Throwable{
        return returnValue;
    }
    /**
     * 异常通知（即 异常拦截）
     * @param targetClass 被代理的目标类（即被代理类）
     * @param method 被代理类中的目标方法（即被代理方法）
     * @param args 被代理类中的目标方法（即被代理方法）对应的参数列表
     * @param e 被代理的目标方法（即被代理方法）抛出的异常
     * @throws Throwable
     */
    public void afterThrowing(Class<?> targetClass, Method method, Object[] args,  Throwable e) throws Throwable{

    }
}
