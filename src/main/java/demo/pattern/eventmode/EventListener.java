package demo.pattern.eventmode;

/**
 * 该接口为事件监听器（即抽象观察者）
 */
public interface EventListener {
    /**
     * 该方法用于对事件进行相应处理
     * @param event 该形参接收的就是事件对象（即此时的 Event 类对象）
     */
    void processEvent(Event event);
}
