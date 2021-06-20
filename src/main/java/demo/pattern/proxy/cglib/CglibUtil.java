package demo.pattern.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * 该类中的 createProxy() 方法其实就是对 Enhancer 类中的 create() 方法进行了简单的封装
 */
public class CglibUtil {
    public static <T>T createProxy(T targetObject, MethodInterceptor methodInterceptor){

        // 调用 Enhancer 类中的 create() 方法
        // 方法中传入被代理类的 Class 类对象，以及 MethodInterceptor 的实现子类
        // 由于该方法返回的是一个 Object 类对象，所以这里要进行一下强转
        return (T)Enhancer.create(targetObject.getClass(), methodInterceptor);
    }
}
