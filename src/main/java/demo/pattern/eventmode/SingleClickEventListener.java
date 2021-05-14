package demo.pattern.eventmode;

/**
 * 该类为事件监听器（即观察者）EventListener 接口的实现子类
 * 即
 * 该类为具体观察者
 */
public class SingleClickEventListener implements EventListener {
    /**
     * 实现 EventListener 接口中的 processEvent() 方法
     * @param event 该形参接收的就是事件对象（即此时的 Event 类对象）
     */
    @Override
    public void processEvent(Event event) {
        if("singleclick".equals(event.getType())){
            System.out.println("单击被触发了");
        }
    }
}
