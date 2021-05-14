package demo.pattern.eventmode;

import java.util.ArrayList;
import java.util.List;

/**
 * 该 EventSource 类即为事件源对象（它就相当于目标类）
 */
public class EventSource {
    // 该成员变量作为容器用于存放事件监听器对象
    private List<EventListener> listenerList = new ArrayList<>();

    /**
     * 该方法用于往成员变量 listenerList 中存放事件监听器对象（即存放观察者）
     * @param listener
     */
    public void register(EventListener listener){
        listenerList.add(listener);
    }

    /**
     * 该方法用于发布事件
     * @param event 该形参接收的就是事件对象（即此时的 Event 类对象）
     */
    public void publishEvent(Event event){
        // 遍历容器 listenerList 中的所有事件监听器对象（即观察者），调用它们的事件处理方法 processEvent()
        for(EventListener listener: listenerList){
            listener.processEvent(event);
        }
    }
}
