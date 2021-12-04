package demo.pattern.proxy.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class AlipayInvocationHandlerTX implements InvocationHandler {
    // 该成员变量就是接收被代理类的对象的
    private Object targetObject;

    // 有参构造方法
    public AlipayInvocationHandlerTX(Object targetObject){
        this.targetObject = targetObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 这一步就相当于开启事务
        startTransaction();

        // 通过反射调用被代理方法（即 那个加了 @Transactional 注解方法中的逻辑）
        Object result = method.invoke(targetObject, args);

        // 这一步就相当于提交事务
        commitTransaction();

        return result;
    }

    // 定义 beforePay() 这个增强方法
    private void startTransaction() {
        System.out.println("开启事务");
    }

    // 定义 afterPay() 这个增强方法
    private void commitTransaction() {
        System.out.println("提交事务");
    }
}
