package org.simpleframework.aop;

import lombok.Getter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.simpleframework.aop.annotation.Aspect;
import org.simpleframework.aop.annotation.Order;
import org.simpleframework.aop.aspect.AspectInfo;
import org.simpleframework.util.ValidationUtil;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 该类主要就是往被代理类中的方法添加横切关注点
 * 由于自研的 AOP 使用的是 CGLib，所以这里要实现 MethodInterceptor 接口
 */
public class AspectListExecutor implements MethodInterceptor {
    // 该成员变量接收被代理的类
    private Class<?> targetClass;

    // 该集合中存放的是按照 @Order 注解排好序的切面类对应的 AspectInfo 类对象
    //（即这里该集合中元素是存放切面类相关信息的 AspectInfo 类型）
    @Getter
    private List<AspectInfo> sortedAspectInfoList;

    // 构造方法，调用此构造方法时，该构造方法就会根据形参 aspectInfoList 接收到的集合对里面的 AspectInfo 类对象进行排序
    //（本质上也就是对 AspectInfo 类对象对应的切面类排序）
    public AspectListExecutor(Class<?> targetClass, List<AspectInfo> aspectInfoList){
        this.targetClass = targetClass;

        // 调用 sortAspectInfoList() 方法，根据加在切面（即使用了 @Aspect 注解的类）上的 @Order 注解的值对多个切面进行排序
        this.sortedAspectInfoList = sortAspectInfoList(aspectInfoList);
    }


    /**
     * 对成员变量 sortedAspectInfoList 这个集合中的元素按照 @Order 注解的值进行升序排序，确保 order 值小的 Aspect 先被织入
     *
     * @param aspectInfoList
     * @return
     */
    private List<AspectInfo> sortAspectInfoList(List<AspectInfo> aspectInfoList) {
        Collections.sort(aspectInfoList, new Comparator<AspectInfo>() {
            @Override
            public int compare(AspectInfo o1, AspectInfo o2) {
                // 按照 @Order 注解的值的大小进行升序排序
                return o1.getOrderIndex() - o2.getOrderIndex();
            }
        });
        return aspectInfoList;
    }

    /**
     * 实现 MethodInterceptor 接口中的 intercept() 方法
     * 该方法主要做了以下几件事
     * 1. 按照 @Order 注解值的顺序升序执行完所有切面中定义的 before() 方法（即 DefaultAspect 类中定义的 before() 方法）
     *   （即这一步主要是针对实现了多个切面的情况的）
     *
     * 2. 执行被代理类中的目标方法（即被代理方法）
     *
     * 3. 如果被代理方法正常返回，则按照 @Order 注解值的顺序降序执行完所有切面的 afterReturning() 方法
     *   （即 DefaultAspect 类中定义的 afterReturning() 方法）
     *    注意
     *    这里是降序调用，因为上面 before() 方法是升序执行，所以最后执行的就是 @Order 注解值最大的那个切面中定义的 before() 方法
     *    因此
     *    为了与之对应，afterReturning() 方法在执行时就需要先执行 @Order 注解值最大的那个切面中定义的 afterReturning() 方法
     *
     * 4. 如果被代理方法抛出异常，则按照 @Order 注解值的顺序降序执行完所有切面中定义的 afterThrowing() 方法
     *   （即 DefaultAspect 类中定义的 afterThrowing() 方法）
     *    注意
     *    这里也是降序调用（原因同上）
     *
     *
     * @param proxy
     * @param method
     * @param args
     * @param methodProxy
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        // 该变量用于接收被代理方法的返回值
        Object returnValue = null;

        // 该方法用于判断形参 method 接收到的方法可以被集合 sortedAspectInfoList 中的哪些切入点表达式定位到
        collectAccurateMatchedAspectList(method);

        // 如果集合 sortedAspectInfoList 为空，那么说明当前这个方法无需进行 AOP 操作
        // 因此
        // 这里就直接执行被代理方法，并返回（后面的执行增强方法的逻辑（即执行如 before() 方法等）就不会再被执行）
        if(ValidationUtil.isEmpty(sortedAspectInfoList)){
            returnValue = methodProxy.invokeSuper(proxy, args);
            return returnValue;
        }

        // 1. 按照 @Order 注解值的顺序升序执行完所有切面中定义的 before() 方法（即 DefaultAspect 类中定义的 before() 方法）
        invokeBeforeAdvices(method, args);
        try{
            // 2. 执行被代理类中的目标方法（即被代理方法）
            returnValue = methodProxy.invokeSuper(proxy, args);

            // 3. 如果被代理方法正常返回，则按照 @Order 注解值的顺序降序执行完所有切面的 afterReturning() 方法
            returnValue = invokeAfterReturningAdvices(method, args, returnValue);
        } catch (Exception e){

            // 4. 如果被代理方法抛出异常，则按照 @Order 注解值的顺序降序执行完所有切面中定义的 afterThrowing() 方法
            invokeAfterThrowingAdvides(method, args, e);
        }
        return returnValue;
    }


    /**
     * 该方法用于判断形参 method 接收到的方法可以被集合 sortedAspectInfoList 中的哪些切入点表达式定位到
     *（即形参 method 接收到的方法符合哪些切入点表达式的筛选条件）
     * 然后
     * 在集合 sortedAspectInfoList 中只保留能够定为到形参 method 接收到的方法的 AspectInfo 类对象
     * 其他无法定位到该方法的 AspectInfo 类对象都从集合中去除
     *（集合 sortedAspectInfoList 中存放的是排序好的 AspectInfo 类对象）
     *
     * 注意
     * 这一步看似在筛选切面类（即切面类对应的 AspectInfo 类对象）
     * 但是
     * 其实也是在筛选被代理方法
     * 因为
     * 如果经过筛选之后的集合 sortedAspectInfoList 为空
     * 那么
     * 就说明该方法无需进行 AOP
     * 那么
     * 之后也就不会对该方法进行织入操作
     * @param method
     */
    private void collectAccurateMatchedAspectList(Method method) {

        if(ValidationUtil.isEmpty(sortedAspectInfoList)){
            return;
        }

        Iterator<AspectInfo> it = sortedAspectInfoList.iterator();
        // 遍历 sortedAspectInfoList 集合
        while (it.hasNext()){
            AspectInfo aspectInfo = it.next();
            // 调用 PointcutLocator 类中的 accurateMatches() 方法，判断形参 method 接收的方法是否符合当前这个切入点表达式的筛选规则
            if(!aspectInfo.getPointcutLocator().accurateMatches(method)){
                it.remove();
            }
        }
    }


    /**
     * 该方法就是对按照 @Order 注解值的顺序降序执行完所有切面的 afterThrowing() 方法 这个功能的简单封装
     * @param method
     * @param args
     * @param e
     * @throws Throwable
     */
    private void invokeAfterThrowingAdvides(Method method, Object[] args, Exception e) throws Throwable {
        // 倒序遍历成员变量 sortedAspectInfoList 中的值，并执行 DefaultAspect 类中定义的 afterThrowing() 方法
        for (int i =  sortedAspectInfoList.size() - 1; i >=0 ; i--){
            sortedAspectInfoList.get(i).getAspectObject().afterThrowing(targetClass, method, args, e);
        }
    }


    /**
     * 该方法就是对按照 @Order 注解值的顺序降序执行完所有切面的 afterReturning() 方法 这个功能的简单封装
     * @param method
     * @param args
     * @param returnValue
     * @return
     * @throws Throwable
     */
    private Object invokeAfterReturningAdvices(Method method, Object[] args, Object returnValue) throws Throwable {
        Object result = null;

        // 倒序遍历成员变量 sortedAspectInfoList 中的值，并执行 DefaultAspect 类中定义的 afterReturning() 方法
        for (int i =  sortedAspectInfoList.size() - 1; i >=0 ; i--){
            result = sortedAspectInfoList.get(i).getAspectObject().afterReturning(targetClass, method, args, returnValue);
        }
        return result;
    }

    /**
     * 该方法就是对按照 @Order 注解值的顺序升序执行完所有切面中定义的 before() 方法 这个功能的简单封装
     * @param method
     * @param args
     * @throws Throwable
     */
    private void invokeBeforeAdvices(Method method, Object[] args) throws Throwable {
        // 遍历成员变量 sortedAspectInfoList 中的值，并执行 DefaultAspect 类中定义的 before() 方法
        for(AspectInfo aspectInfo : sortedAspectInfoList){
            aspectInfo.getAspectObject().before(targetClass, method, args);
        }
    }
}
