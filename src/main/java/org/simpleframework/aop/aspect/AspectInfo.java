package org.simpleframework.aop.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.simpleframework.aop.PointcutLocator;

/**
 * 该类用于存放一些与切面相关的信息
 */
@AllArgsConstructor
@Getter
public class AspectInfo {
    // 该成员变量存放的就是 @Order 注解的值
    private int orderIndex;

    // 该成员变量用于存放，当前切面使用了哪些通知类型（即在 DefaultAspect 类中定义的通知类想）
    private DefaultAspect aspectObject;

    // 该成员变量用于接收 PointcutLocator 类对象，该对象中存放的就是经过解析后的切入点表达式
    private PointcutLocator pointcutLocator;
}
