package org.simpleframework.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.aop.annotation.Aspect;
import org.simpleframework.core.annotation.Component;
import org.simpleframework.core.annotation.Controller;
import org.simpleframework.core.annotation.Repository;
import org.simpleframework.core.annotation.Service;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类就是 IoC 容器（该类中使用了枚举类型的单例模式）
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanContainer {
    /**
     * 定义 Map 集合用于存放所有被配置标记的目标对象（如被 @Controller，@Component 等注解标记的类的对象）
     *（该集合使用了线程安全的 ConcurrentHashMap）
     */
    private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap();

    /**
     * 定义一个 List 集合用于存放我们创建的那些注解的 Class 文件对象
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION
            = Arrays.asList(Component.class, Controller.class, Service.class, Repository.class, Aspect.class);

    /**
     * 获取 Bean 容器实例
     *
     * @return BeanContainer
     */
    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    private enum ContainerHolder {
        HOLDER;
        private BeanContainer instance;

        ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    /**
     * bean 容器（即上面创建的 Map 集合 beanMap ）是否被加载过
     */
    private boolean loaded = false;

    /**
     * bean 容器（即上面创建的 Map 集合 beanMap ）是否被加载过
     *
     * @return 是否已加载
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Bean实例数量
     *
     * @return 数量
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * 该方法用于根据包名，获取该包下所有使用了我们定义的那些注解的类的实例，然后把这些实例放入到上面创建的 Bean 容器 ---- beanMap 集合中
     * 以便用户后续获取这些实例
     *（该方法为同步方法）
     * @param packageName 该形参用于接收包名
     */
    public synchronized void loadBeans(String packageName) {
        // 判断 bean 容器（即上面创建的 Map 集合 beanMap ）是否被加载过（其实就是是否以及往成员变量 beanMap 中存放过元素了）
        // 其实就是判断这个 loadBeans() 方法是否被执行过一次了
        if (isLoaded()) {
            log.warn("BeanContainer has been loaded.");

            return;
        }

        // 调用我们定义的 extractPackageClass() 方法获取形参 packageName 所指向的包下的所有的类的 Class 类对象
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);

        // 调用我们定义的 isEmpty() 方法，判断 Set 结合是否为空
        if (ValidationUtil.isEmpty(classSet)) {
            log.warn("extract nothing from packageName" + packageName);
            return;
        }

        // 遍历上面获取到的所有的 Class 类对象，检查它们是否使用了我们定义的注解（即 Component, Controller, Service 等注解）
        // 如果使用了，就调用我们定义的 newInstance() 方法创建它们的实例
        // 然后
        // 以该类的 Class 类对象为 Key，该类的实例为 Value，把它们存放到 bean 容器（即当前 beanMap 这个集合）中
        for (Class<?> clazz : classSet) {
            for (Class<? extends Annotation> annotation : BEAN_ANNOTATION) {
                //如果类上面标记了定义的注解
                if (clazz.isAnnotationPresent(annotation)) {
                    //将目标类本身作为键，目标类的实例作为值，放入到beanMap中
                    beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
                }
            }
        }

        loaded = true;
    }

    /**
     * 添加一个 class 类对象及它对应的类的实例
     *
     * @param clazz Class对象
     * @param bean  Bean实例
     * @return 原有的Bean实例, 没有则返回null
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 移除一个 IOC 容器管理的对象（即从 Bean 容器中移除传入该方法的 Class 类对象以及它对应的类的实例）
     *
     * @param clazz Class对象
     * @return 删除的Bean实例, 没有则返回null
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * 根据 Class 对象获取 Bean 实例
     *
     * @param clazz Class对象
     * @return Bean实例
     */
    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }
    /**
     * 获取容器管理的所有Class对象集合
     *
     * @return Class集合
     */
    public Set<Class<?>> getClasses(){
        return beanMap.keySet();
    }
    /**
     * 获取所有Bean集合
     *
     * @return Bean集合
     */
    public Set<Object> getBeans(){
        return new HashSet<>( beanMap.values());
    }

    /**
     * 该方法用于 bean 容器中获取使用了指定注解的 bean 的 Class类对象集合
     *
     * @param annotation 注解
     * @return Class集合
     */
    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation){
        // 1.获取 bean 容器中的所有 Class 对象
        Set<Class<?>> keySet = getClasses();

        if(ValidationUtil.isEmpty(keySet)){
            log.warn("nothing in beanMap");
            return null;
        }
        // 2.通过注解筛选被注解标记的class对象，并添加到classSet里


        Set<Class<?>> classSet = new HashSet<>();

        // 获取 bean 容器中的所有 Class 对象
        for(Class<?> clazz : keySet){
            //类是否有相关的注解标记
            if(clazz.isAnnotationPresent(annotation)){
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet: null;
    }
    /**
     * 该方法用于获取指定 接口所对应的实现子类 或者 父类所对应的子类 的 Class 类对象集合（不包括该类和接口本身）
     *
     * @param interfaceOrClass 接口Class或者父类Class
     * @return Class集合
     */
    public Set<Class<?>> getClassesBySuper(Class<?> interfaceOrClass){
        // 1.获取 bean 容器中的所有 Class 对象
        Set<Class<?>> keySet = getClasses();
        if(ValidationUtil.isEmpty(keySet)){
            log.warn("nothing in beanMap");
            return null;
        }
        // 2.判断keySet里的元素是否是传入的接口或者类的子类，如果是，就将其添加到classSet里


        Set<Class<?>> classSet = new HashSet<>();

        // 遍历这些 Class 类对象
        for(Class<?> clazz : keySet){
            // 调用 Class 类的 isAssignableFrom() 方法，判断 keySet 集合里的元素是否是传入的接口或者类的子类
            // 并且判断当前遍历到的 Class 类对象不是传入该方法的类或者接口本身
            if(interfaceOrClass.isAssignableFrom(clazz) && !clazz.equals(interfaceOrClass)){
                classSet.add(clazz);
            }
        }
        return classSet.size() > 0 ? classSet: null;
    }

}
