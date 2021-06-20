package demo.pattern.proxy.cglib;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 定义 AlipayMethodInterceptor 类，它实现了 MethodInterceptor 接口
 * 该类的功能基本和 JDK 动态代理中的 InvocationHandler 接口的实现子类（如当前项目中的 AlipayInvocationHandler 类）的功能一样
 * 即
 * 我们是在该类中定义用于增强被代理类中方法的方法的（如此时的 beforePay() 和 afterPay() 方法就是增强方法）
 *（即这些增强方法类似 Spring AOP 中的横切关注点（即那些加了如 @Before 注解的方法））
 *
 */
public class AlipayMethodInterceptor implements MethodInterceptor {
    /**
     * 实现 MethodInterceptor 接口中的 intercept() 方法
     * 我们就是通过调用该方法中的形参 methodProxy 的 invokeSuper() 方法实现调用代理类中的方法的
     * 并且
     * 我们也是在该方法中调用增强方法对被代理方法进行增强
     *
     * @param o 该形参接收被代理类对象
     * @param method 该形参接收的是被代理类中的被代理方法的 Method 类对象
     * @param args 该形参接收的是被代理方法的形参
     * @param methodProxy 该形参接收的是代理类中的被代理方法
     *                    即
     *                    由于 CGLib 可以通过修改字节码的方式动态生成某个类的子类（这个功能其实是 ASM 技术实现的）
     *                    用在代理模式上就是，CGLib 可以动态生成被代理类的子类
     *                   （即我们可以把这个子类理解为代理类，虽然这么理解不太符合代理模式的规范（代理模式中好像不允许代理类和被代理类是继承关系））
     *                    所以
     *                    这个 methodProxy 形参接收的就是代理类继承自被代理类中的那个被代理方法
     *                   （即 本质上该形参和形参 method 指向的是同一个方法）
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        // 调用 beforePay() 方法对被代理方法进行前置增强
        beforePay();

        // 通过形参 methodProxy 调用 invokeSuper() 方法实现调用被代理类中的被代理方法
        //（如 此时的 CommonPayment 类中的 pay() 方法 和 payOne() 方法）
        Object result = methodProxy.invokeSuper(o, args);

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
