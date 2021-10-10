package demo.pattern.proxy;

import demo.pattern.proxy.cglib.AlipayMethodInterceptor;
import demo.pattern.proxy.cglib.CglibUtil;
import demo.pattern.proxy.impl.*;
import demo.pattern.proxy.jdkproxy.AlipayInvocationHandler;
import demo.pattern.proxy.jdkproxy.JdkDynamicProxyUtil;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;

public class ProxyDemo {
    public static void main(String[] args) {
//        ToCPayment toCProxy = new AlipayToC(new ToCPaymentImpl());
//        toCProxy.pay();
//        ToBPayment toBProxy = new AlipayToB(new ToBPaymentImpl());
//        toBProxy.pay();
//        ToCPayment toCPayment = new ToCPaymentImpl();
//        InvocationHandler handler = new AlipayInvocationHandler(toCPayment);
//        ToCPayment toCProxy = JdkDynamicProxyUtil.newProxyInstance(toCPayment,handler);
//        toCProxy.pay();


        // 创建 ToBPaymentImpl 这个被代理类对象
        ToBPayment toBPayment = new ToBPaymentImpl();

        // 创建 AlipayInvocationHandler 类对象
        InvocationHandler handlerToB = new AlipayInvocationHandler(toBPayment);

        // 获取代理类对象
        ToBPayment toBProxy = JdkDynamicProxyUtil.newProxyInstance(toBPayment, handlerToB);

        // 调用代理类对象的 pay() 方法
        // 这里，本质其实就是调用的我们创建的 AlipayInvocationHandler 类中所实现的 invoke() 方法
        //（具体见 Spring_AOP 中的 “JDK 动态代理实现原理”）
        toBProxy.pay();


        // 创建 CommonPayment 这个被代理类的对象
//        CommonPayment commonPayment = new CommonPayment();
////        AlipayInvocationHandler invocationHandler = new AlipayInvocationHandler(commonPayment);
////        CommonPayment commonPaymentProxy = JdkDynamicProxyUtil.newProxyInstance(commonPayment, invocationHandler);
//
//        // 创建 MethodInterceptor 接口的实现子类 AlipayMethodInterceptor 类的对象
        MethodInterceptor methodInterceptor = new AlipayMethodInterceptor();
//
//        // 获取代理类对象
//        CommonPayment commonPaymentProxy = CglibUtil.createProxy(commonPayment, methodInterceptor);
//
//        // 调用代理类对象的 pay() 方法
//        commonPaymentProxy.pay();
//
        // 调用代理类对象的 payOne() 方法
//        commonPaymentProxy.payOne();

        ToCPaymentImpl toCPayment = new ToCPaymentImpl();
        ToCPaymentImpl toCProxy = CglibUtil.createProxy(toCPayment, methodInterceptor);
        toCProxy.pay();
    }
}
