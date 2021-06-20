package demo.pattern.proxy.impl;

import demo.pattern.proxy.ToBPayment;

/**
 * 该类为被代理类，它实现 ToBPayment 接口
 */
public class ToBPaymentImpl implements ToBPayment {
    @Override
    public void pay() {
        System.out.println("以公司的名义进行支付");
    }


}
