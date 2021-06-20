package demo.pattern.proxy.impl;

import demo.pattern.proxy.ToCPayment;

/**
 * 该类为代理类，和被代理类一样，它实现了 ToCPayment 接口
 * 即
 * 如果我们不使用 JDK 动态代理，我们就需要创建该类，以实现对被代理类中的方法进行增强
 */
public class AlipayToC implements ToCPayment {
    ToCPayment toCPayment;
    public AlipayToC(ToCPayment toCPayment){
        this.toCPayment = toCPayment;
    }
    @Override
    public void pay() {
        beforePay();
        toCPayment.pay();
        afterPay();
    }

    private void beforePay() {
        System.out.println("从招行取款");
    }
    private void afterPay() {
        System.out.println("支付给慕课网");
    }
}
