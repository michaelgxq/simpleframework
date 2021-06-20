package demo.pattern.proxy.impl;

import demo.pattern.proxy.ToCPayment;

/**
 * 该类为被代理类，它实现 ToCPayment 接口
 */
public class ToCPaymentImpl implements ToCPayment {
    @Override
    public void pay() {
        System.out.println("以用户的名义进行支付");
    }

    public void show() {
        System.out.println("测试测试");
    }
}
