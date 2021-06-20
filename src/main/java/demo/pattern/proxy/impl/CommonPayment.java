package demo.pattern.proxy.impl;

/**
 * 定义 CommonPayment 类
 * 由于该类将通过 CGLib 来实现代理，因此它无需实现任何接口
 */
public class CommonPayment {
    public void pay() {
        System.out.println("个人名义或者公司名义都可以走这个支付通道");
    }

    public void payOne() {
        System.out.println("个人名义或者公司名义都可以走这个支付通道One");
    }
}
