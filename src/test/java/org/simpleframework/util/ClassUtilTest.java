package org.simpleframework.util;

import com.imooc.entity.bo.HeadLine;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;

public class ClassUtilTest {
    @DisplayName("提取目标类方法：extractPackageClassTest")
    @Test
    public void extractPackageClassTest() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        Set<Class<?>> classSet =  ClassUtil.extractPackageClass("com.imooc.entity");
//        System.out.println(classSet);
//        Assertions.assertEquals(4, classSet.size());

        Class<?> headLineClass = Class.forName("com.imooc.entity.bo.HeadLine");

        Object o = headLineClass.newInstance();

        Field[] fields = headLineClass.getDeclaredFields();
        Annotation annotation = headLineClass.getAnnotation(Data.class);

        for (Field field : fields) {
            System.out.println("field ============== " + field + " type ============ " + field.getType());

            field.setAccessible(true);

            field.set("headLine", "22");
        }
    }
}
