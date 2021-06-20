package org.simpleframework.aop;

import org.simpleframework.aop.annotation.Aspect;
import org.simpleframework.aop.annotation.Order;
import org.simpleframework.aop.aspect.AspectInfo;
import org.simpleframework.aop.aspect.DefaultAspect;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 该类就是织入器
 */
public class AspectWeaver {
    // 该成员变量是一个 IOC 容器（即 Bean 实例容器）
    private BeanContainer beanContainer;

    // 构造方法
    public AspectWeaver() {
        this.beanContainer = BeanContainer.getInstance();
    }

    /**
     * 该方法是真正进行织入操作的
     * 该方法主要做了以下几件事
     * 1. 获取 IOC 容器（即 Bean 实例容器）中所有加了 @Aspect 注解的类（即 切面类），并把这些类存放到一个 Set 集合中
     *
     * 2. 把上一步获取到的切面类对应的相关信息封装成一个个 AspectInfo 类实例，存放到一个集合中
     *
     * 3. 遍历 IOC 容器（即 Bean 实例容器）中所有的类，筛选出遍历的到每一个类都能被哪些切入点表达式定位到
     *    然后，把这些符合条件的切入点表达式所属切面类对应的 AspectInfo 类对象存放到一个新的容器中（即进行初筛）
     *
     * 4.尝试把经过筛选的 AspectInfo 类对应的切面类中的那些横切关注点织入到目标（即 被代理的方法）上，并生成对应的代理类对象
     *
     */
    public void doAop() {
        // 1.  获取 IOC 容器（即 Bean 实例容器）中所有加了 @Aspect 注解的类（即 切面类），并把这些类存放到一个 Set 集合中
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);

        if(ValidationUtil.isEmpty(aspectSet)){
            return;
        }

        // 2. 把上一步获取到的所有切面类对应的相关信息（如 切入点表达式）封装成一个 AspectInfo 类实例，存放到一个集合中
        List<AspectInfo> aspectInfoList = packAspectInfoList(aspectSet);

        // 获取 IOC 容器中的所有类（这里面也包括了加了 @Aspect 注解的切面类）
        Set<Class<?>> classSet = beanContainer.getClasses();

        // 3. 遍历 IOC 容器（即 Bean 实例容器）中所有的类，筛选出遍历的到每一个类都能被哪些切入点表达式定位到
        //    然后
        //    把这些符合条件的切入点表达式所属切面类对应的 AspectInfo 类对象存放到一个新的容器中
        //    注意
        //    这步看似在筛选切面类，但其实也是在筛选目标类，因为如果当前类不能被任何切面类的切入点表达式定位到，那么就说明该类不需要进行 AOP
        //   （即该类不是目标类）
        //    那么之后也就不会对该类执行创建代理类等操作了
        for (Class<?> targetClass: classSet) {
            // 因为此时遍历的类中包含了切面类
            // 因此
            // 我们这里需要排除调用这些切面类（即 那些加了 @Aspect 注解的类）（因为对切面类进行织入会造成死循环）
            // 该 isAnnotationPresent() 方法就是用于判断当前遍历到的类是否是加了 @Aspect 注解
            // 如果是就说明它是切面类，就不走下面的逻辑
            if(targetClass.isAnnotationPresent(Aspect.class)){
                continue;
            }

            // 找出集合 aspectInfoList 中，切入点表达式能定位到当前遍历到的这个类的所有 AspectInfo 类对象
            // 并把它们放到一个新的集合 roughMatchedAspectList 中（这一步就是对 AspectInfo 类对象进行初筛）
            // 注意
            // 因为 AspectInfo 类对象中的成员变量 pointcutLocator 就是由对应切面类的切入点表达式转换而成的
            // 因此我们可以使用该成员变量来判断该 AspectInfo 类对象对应的切面类的切入点表达式是否能定位到该当前遍历到的类
            List<AspectInfo> roughMatchedAspectList  = collectRoughMatchedAspectListForSpecificClass(aspectInfoList, targetClass);

            // 4. 尝试把经过筛选的 AspectInfo 类对应的切面类中的那些横切关注点织入到目标（即 被代理的方法）上，并生成对应的代理类对象
            wrapIfNecessary(roughMatchedAspectList,targetClass);
        }

    }

    /**
     * 该方法用于把经过筛选的 AspectInfo 类对应的切面类中的那些横切关注点织入到目标（即 被代理的方法）上
     * 然后
     * 生成一个全新的包含了横切关注点以及被代理方法的代理类对象
     * 然后
     * 把该代理类对象存放入 IOC 容器中（即覆盖原来被代理前的 Bean 实例）
     *
     * @param roughMatchedAspectList
     * @param targetClass
     */
    private void wrapIfNecessary(List<AspectInfo> roughMatchedAspectList, Class<?> targetClass) {
        // 判断集合 roughMatchedAspectList 是否为空
        // 如果为空（即表示当前这个类无法被任何切面类中的切入点表达式给定位到，即这个类不需要进行 AOP）
        // 那么就直接返回（即不再执行之后的创建动态代理的逻辑）
        if(ValidationUtil.isEmpty(roughMatchedAspectList)){
            return;
        }
        // 以下 2 行代码就是创建动态代理对象
        // 即
        // 创建一个 MethodInterceptor 接口的实现子类 AspectListExecutor 类对象
        // 然后
        // 通过 CGLib 生成一个织入了横切关注点的代理类对象
        AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass, roughMatchedAspectList);
        Object proxyBean = ProxyCreator.createProxy(targetClass, aspectListExecutor);

        // 将动态代理类对象添加到我们定义的 IOC 容器（即此时的 beanContainer）中，覆盖原来被代理前的 Bean 实例
        //（即这里是用动态代理对象替换调用了容器中的原来的对象）
        beanContainer.addBean(targetClass, proxyBean);
    }


    /**
     * 该方法用于找出集合 aspectInfoList 中，切入点表达式能定位到当前遍历到的这个类的所有 AspectInfo 类对象
     * 并把它们放到一个新的集合 roughMatchedAspectList 中（这一步就是对 AspectInfo 类对象进行初筛）
     *
     * @param aspectInfoList
     * @param targetClass
     * @return
     */
    private List<AspectInfo> collectRoughMatchedAspectListForSpecificClass(List<AspectInfo> aspectInfoList, Class<?> targetClass) {
        List<AspectInfo> roughMatchedAspectList = new ArrayList<>();
        // 遍历集合 aspectInfoList 中的所有元素
        for(AspectInfo aspectInfo : aspectInfoList){
            // 调用 PointcutLocator 类中的 roughMatches() 方法，判断当前这个类是否能被当前 AspectInfo 类对象所对应的切入点表达式定位到
            // 如果可以，就把该 AspectInfo 类对象放入到一个新的集合中
            if(aspectInfo.getPointcutLocator().roughMatches(targetClass)){

                roughMatchedAspectList.add(aspectInfo);
            }
        }
        return roughMatchedAspectList;
    }

    /**
     * 该方法用于把所有切面类对应的相关信息（如 切入点表达式）封装成一个 AspectInfo 类实例，存放到一个集合中
     * @param aspectSet
     * @return
     */
    private List<AspectInfo> packAspectInfoList(Set<Class<?>> aspectSet) {
        List<AspectInfo> aspectInfoList = new ArrayList<>();

        // 遍历集合
        for(Class<?> aspectClass : aspectSet){
            // 验证我们获取到的类是否符合我们定义的 AOP 规范
            if (verifyAspect(aspectClass)){

                // 下面几行代码就是把切面类的相关信息
                // 即
                // @Order 注解的值
                // 以及
                // 所要进行的通知（即它重写的 DefaultAspect 类中的如 before() 等方法））
                // 以及
                // @Aspect 注解的值（即 切入点）
                // 把这些信息都封装到 AspectInfo 类中
                Order orderTag = aspectClass.getAnnotation(Order.class);
                Aspect aspectTag = aspectClass.getAnnotation(Aspect.class);
                DefaultAspect defaultAspect = (DefaultAspect) beanContainer.getBean(aspectClass);
                // 创建表达式定位器（即 PointcutLocator 类对象），构造方法中存入切入点表达式
                PointcutLocator pointcutLocator = new PointcutLocator(aspectTag.pointcut());
                // 把获取到的相关信息封装到 AspectInfo 类对象中
                AspectInfo aspectInfo = new AspectInfo(orderTag.value(), defaultAspect, pointcutLocator);

                // 把上面创建的 AspectInfo 类对象放入到集合中
                aspectInfoList.add(aspectInfo);
            } else {
                // 如果不符合我们定义的规范则直接抛出异常
                throw new RuntimeException("@Aspect and @Order must be added to the Aspect class, and Aspect class must extend from DefaultAspect");
            }
        }
        return aspectInfoList;
    }


    /**
     * 该方法主要是用于验证，我们获取到的类是否符合我们定义的 AOP 规范
     * 即
     * 切面类必须添加 @Aspect 和 @Order 注解，同时，必须该类必须继承自 DefaultAspect
     * 注意
     * 切面的 @Aspect 注解的值不能是该 @Aspect 注解本身（否则给切面做切面就会陷入死循环）
     * (
     *
     * @param aspectClass
     * @return
     */
    private boolean verifyAspect(Class<?> aspectClass) {
        // 判断形参 aspectClass 接收的类是否加上了 @Aspect 和 @Order 注解，并且该类是否是 DefaultAspect 类的子类
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                DefaultAspect.class.isAssignableFrom(aspectClass);
    }
}
