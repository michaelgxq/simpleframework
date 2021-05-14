package demo.pattern.eventmode;

public class EventModeDemo {
    public static void main(String[] args) {
        // 创建事件源对象
        EventSource eventSource = new EventSource();

        // 创建两个事件监听器对象
        SingleClickEventListener singleClickEventListener = new SingleClickEventListener();
        DoubleClickEventListener doubleClickEventListener = new DoubleClickEventListener();

        // 创建事件对象
        Event event = new Event();

        // 为事件对象设置 type 属性值
        event.setType("doubleclick");

        // 往事件源对象中注册事件监听器对象
        eventSource.register(singleClickEventListener);
        eventSource.register(doubleClickEventListener);

        // 事件源对象发布事件
        eventSource.publishEvent(event);
    }
}
