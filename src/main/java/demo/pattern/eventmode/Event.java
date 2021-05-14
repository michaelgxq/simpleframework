package demo.pattern.eventmode;

import lombok.Getter;
import lombok.Setter;

/**
 * 该 Event 类即为事件对象
 */
@Getter
@Setter
public class Event {
    private String type;
}
