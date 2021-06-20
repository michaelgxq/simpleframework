package demo.pattern.proxy.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 该工具类中的 newProxyInstance() 方法其实就是对 Proxy 类的 newProxyInstance() 方法进行了一下封装
 * 让我们可以不用直接调用该 newProxyInstance() 方法
 */
public class JdkDynamicProxyUtil {
    /**
     * 该 newProxyInstance() 方法其实就是对 Proxy 类的 newProxyInstance() 方法进行了一下封装
     * 让我们可以不用直接调用该 newProxyInstance() 方法
     * @param targetObject 该形参接收被代理类对象
     * @param handler 该形参接收 InvocationHandler 接口实现类对象
     * @return
     */
    public static <T>T newProxyInstance(T targetObject, InvocationHandler handler){
        // 获取被代理类的 ClassLoader 类对象
        ClassLoader classLoader = targetObject.getClass().getClassLoader();

        // 获取被代理类所实现的所有接口的 Class 类对象
        Class<?>[]  interfaces = targetObject.getClass().getInterfaces();

        // 调用 Proxy 类的 newProxyInstance() 方法获取代理对象
        // 由于
        // Proxy 类的 newProxyInstance() 方法返回的是 Object 类对象
        // 所以这里要进行强转，把 Object 类对象强转成被代理类对象
        return (T)Proxy.newProxyInstance(classLoader, interfaces, handler);
    }
}
