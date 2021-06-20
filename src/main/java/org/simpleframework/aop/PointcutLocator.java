package org.simpleframework.aop;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 该类用于解析切入点表达式，并根据切入点表达式筛选出符合条件的目标类或者目标方法
 */
public class PointcutLocator {
    /**
     * 该成员变量接收的是 AspectJ 中的切入点表达式解析器
     * 这里给它赋上了一个默认值（即 AspectJ 中的切入点表达式解析器）
     */
    private PointcutParser pointcutParser= PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(
            PointcutParser.getAllSupportedPointcutPrimitives()
    );

    /**
     * 该成员变量用于存放被解析后的切入点表达式
     * 我们可以该成员变量来判断某个类，某个方法是否匹配切入点表达式
     */
    private PointcutExpression pointcutExpression;

    // 构造方法
    public PointcutLocator(String expression){
        this.pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }
    /**
     *
     * 该方法用于判断某个类是否符合切入点表达式的筛选规则
     * 由于
     * 该方法中调用的 couldMatchJoinPointsInType() 方法所能校验的切入点表达式只能精确到类
     *（即 我们写的 execution(XXX) 切入点表达式只能精确到类的那种，而不能写只精确到包的）
     * 否则
     * 该方法会把它所无法识别的表达式都当作正确的表达式，而返回 true
     * 因此
     * 该 roughMatches() 方法只能进行初步筛选出那些符合切入点表达式规则的类
     *
     * @param targetClass 该形参接收需要进行判断的类
     * @return 是否匹配
     */
    public boolean roughMatches(Class<?> targetClass){
        return pointcutExpression.couldMatchJoinPointsInType(targetClass);
    }


    /**
     * 该方法用于判断某个类中的方法是否符合切入点表达式的筛选规则
     * 即
     * 该方法是在上面 roughMatches() 方法筛选出的类中，再筛选出符合条件的方法
     *
     * @param method 该形参接收需要进行判断的方法的 Method 类对象
     * @return
     */
    public boolean accurateMatches(Method method){
        ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(method);
        // 如果是完全匹配就返回 true
        if(shadowMatch.alwaysMatches()){
            return true;
        }
        else {
            return false;
        }
    }
}
