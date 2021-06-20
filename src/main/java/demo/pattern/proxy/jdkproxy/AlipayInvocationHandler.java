package demo.pattern.proxy.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 创建 AlipayInvocationHandler 类，该类实现 InvocationHandler 接口
 * 该类有点像 Spring AOP 中的切面（即加了 @Aspect 注解的类）
 * 因为
 * 我们是在该类中定义用于增强被代理类中方法的方法的（如此时的 beforePay() 和 afterPay() 方法就是增强方法）
 *（即这些增强方法类似 Spring AOP 中的横切关注点（即那些加了如 @Before 注解的方法））
 *
 */
public class AlipayInvocationHandler implements InvocationHandler {
    // 该成员变量就是接收被代理类的对象的（如 此时的 ToBPaymentImpl 和 ToCPaymentImpl 类对象）
    private Object targetObject;

    // 有参构造方法
    public AlipayInvocationHandler(Object targetObject){
        this.targetObject = targetObject;
    }

    /**
     * 实现 InvocationHandler 接口中的 invoke() 方法
     * 我们就是通过调用该方法中的形参 method 的 invoke() 方法实现调用代理类中的方法的（其实就是反射调用）
     * 并且
     * 我们也是在该方法中调用增强方法对被代理方法进行增强
     * @param proxy 该形参接收的是代理类对象（即通过调用 Proxy 类的 newProxyInstance() 方法返回的对象）
     * @param method 该形参接收的是被代理类中的被代理方法的 Method 类对象
     * @param args 该形参接收的是被代理方法的形参
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 调用 beforePay() 方法对被代理方法进行前置增强
        beforePay();
        // 通过反射调用被代理方法，方法从传入被代理类对象，以及被代理方法所需要的参数
        Object result = method.invoke(targetObject, args);
        // 调用 afterPay() 方法对被代理方法进行后置增强
        afterPay();

        return result;
    }

    // 定义 beforePay() 这个增强方法
    private void beforePay() {
        System.out.println("从招行取款");
    }

    // 定义 afterPay() 这个增强方法
    private void afterPay() {
        System.out.println("支付给慕课网");
    }
}
