package org.simpleframework.inject;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.inject.annotation.Autowired;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.ValidationUtil;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 该类用于实现依赖注入
 */
@Slf4j
public class DependencyInjector {
    /**
     * 定义数据类型为 BeanContainer 类（即我们定义的 IoC 容器）的成员变量 beanContainer
     */
    private BeanContainer beanContainer;

    // 构造方法
    public DependencyInjector(){
        // 获取 BeanContainer 类实例
        beanContainer = BeanContainer.getInstance();
    }
    /**
     * 该方式就是用于实现依赖注入功能的
     * 依赖注入的实现步骤如下
     * 1.获取 Bean 容器中的所有的 Key（即那些 Class 类对象），并遍历
     * 2.获取当前遍历到的 Class 类对象所对应的类中的所有成员变量
     * 3.遍历这些成员变量，找出这些成员变量中被 Autowired 注解标记的成员变量
     * 4.获取该成员变量的数据类型
     * 5.获取这些成员变量的数据类型在 Bean 容器里对应的实例
     * 6.通过反射将该实例注入到成员变量所在类的实例里（即把该实例赋值给对应类对象中的对应的成员变量）
     *
     * 从上面的步骤可以看出，无论是要被注入依赖的类对象，还是那些注入的类对象，它们首先都需要在 Bean 容器中存在
     */
    public void doIoc() {
        // 获取 IoC 容器里面的 Bean 容器里的所有的 Key（即那些 Class 类对象），并判断是否为空
        if(ValidationUtil.isEmpty(beanContainer.getClasses())){
            log.warn("empty classset in BeanContainer");

            return;
        }

        // 1.获取 Bean 容器中的所有的 Key（即那些 Class 类对象），并遍历
        for(Class<?> clazz : beanContainer.getClasses()){

            // 2.获取当前遍历到的 Class 类对象所对应的类中的所有成员变量
            Field[] fields = clazz.getDeclaredFields();

            if (ValidationUtil.isEmpty(fields)){
                continue;
            }

            // 遍历上面获取到的所有成员变量
            for(Field field : fields){

                // 3.找出被 Autowired 注解标记的成员变量
                if(field.isAnnotationPresent(Autowired.class)){

                    Autowired autowired = field.getAnnotation(Autowired.class);

                    // 获取 Autowired 注解的属性值
                    String autowiredValue = autowired.value();

                    // 4.获取该成员变量的数据类型
                    Class<?> fieldClass = field.getType();

                    //5.获取这些成员变量的数据类型在 Bean 容器里对应的实例（这里调用的是我们下面定义的 getFieldInstance() 方法）
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);

                    if(fieldValue == null){
                        throw new RuntimeException("unable to inject relevant type，target fieldClass is:" + fieldClass.getName() + " autowiredValue is : " + autowiredValue);
                    }
                    else {
                        // 6.通过反射将该实例注入到成员变量所在类的实例里（即把该实例赋值给对应类对象中的对应的成员变量）
                        Object targetBean =  beanContainer.getBean(clazz);

                        // 调用我们定义的 setField() 方法进行注入操作
                        ClassUtil.setField(field, targetBean, fieldValue, true);
                    }
                }
            }
        }


    }

    /**
     * 根据 Class 类对象从 Bean 容器里获取其对应的实例或实现类（即如果该 Class 类对象对应的是一个接口，就获取它的实现类）
     * @param fieldClass 该形参用于接收类中对应成员变量的 Class 类对象
     * @param autowiredValue 该形参用于接收加载该成员变量上的 Autowired 注解的属性值
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredValue) {

        // 获取该 Class 类对象在 Bean 容器中对应的实例
        Object fieldValue = beanContainer.getBean(fieldClass);
        if (fieldValue != null){
            return fieldValue;
        }
        // 如果 Bean 容器中没有对应的实例，那么有可能用户就是利用了多态，使用了该成员变量对应数据类型的父类或者接口作为数据类型
        // 此时
        // 就需要获取该父类或者接口的实现子类在 Bean 容器中的实例
        else {
            // 调用下面定义的 getImplementedClass() 方法，根据 Autowired 注解的属性值，获取该 Class 类对象对应接口的实现类
            Class<?> implementedClass = getImplementedClass(fieldClass, autowiredValue);

            // 如果获取到了该接口或者父类的实现子类，就从 Bean 容器中获取对应的实例
            if(implementedClass != null){

                return beanContainer.getBean(implementedClass);
            } else {
                return null;
            }
        }
    }
    /**
     * 根据 Autowired 注解的属性值，获取 Class 类对象所对应接口的实现子类
     * @param fieldClass 该形参用于接收类中对应成员变量的 Class 类对象
     * @param autowiredValue 该形参用于接收加载该成员变量上的 Autowired 注解的属性值
     */
    private Class<?> getImplementedClass(Class<?> fieldClass, String autowiredValue) {
        // 调用我们定义的 getClassesBySuper() 方法获取指定 接口所对应的实现子类 或者 父类所对应的子类 的 Class 类对象集合（不包括该类和接口本身）
        Set<Class<?>> classSet =  beanContainer.getClassesBySuper(fieldClass);

        // 判断该存放 Class 类对象的 Set 集合是否为空
        if(!ValidationUtil.isEmpty(classSet)){

            // 判断 Autowired 注解的属性值是否为空
            if(ValidationUtil.isEmpty(autowiredValue)){
                // 如果该 Set 集合中只有一个元素，就直接返回该元素
                if(classSet.size() == 1){
                    return classSet.iterator().next();
                }
                else {
                    //如果该 Set 集合中有多于两个的该接口的实现子类且用户未指定是其中哪一个实现子类（即 Autowired 注解的属性值为空），则抛出异常
                    throw new RuntimeException("multiple implemented classes for " + fieldClass.getName() + " please set @Autowired's value to pick one");
                }
            }
            // 如果 Autowired 注解的属性值不为空（此时该属性值因为该接口的实现子类的类名）
            else {
                // 遍历该 Set 集合，判断 Autowired 注解的属性值是否就是该 Class 类对象对应类的类名，是就返回该 Class 类对象
                for(Class<?> clazz : classSet){
                    // 因为我们为 Autowired 注解设置属性值的时候，设置的仅仅是类的简单类名（如 @Autowired("Person")）
                    // 因此
                    // 这里比较的时候也是通过调用 Class 类的 getSimpleName() 方法获取该 Class 类对象对应的类的简单类名
                    if(autowiredValue.equals(clazz.getSimpleName())){
                        return clazz;
                    }
                }
            }
        }

        return null;
    }
}
