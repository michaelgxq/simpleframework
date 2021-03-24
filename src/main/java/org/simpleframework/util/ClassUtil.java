package org.simpleframework.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClassUtil {

    public static final String FILE_PROTOCOL = "file";

    /**
     * 获取指定包下的所有类
     *
     * @parampackageName包名
     * @return 该包下所有类的集合
     */
    public static Set<Class<?>> extractPackageClass(String packageName){
        // 1.调用下面定义的 getClassLoader() 方法，获取到类的加载器（即 ClassLoader 类对象），以便获取该包的真实的绝对路径
        ClassLoader classLoader = getClassLoader();

        // 2.调用 ClassLoader 类中的 getResource() 方法获取到要加载的资源（即对应资源的 URL）
        //   由于
        //   在 Java 中包与它的子包之间是用 . 分隔的
        //   但是
        //   包与子包其实就是文件夹与子文件夹之间的关系
        //   所以这里要把 . 换成资源定位符用到 /
        URL url = classLoader.getResource(packageName.replace(".", "/"));

        if (url == null){
            log.warn("unable to retrieve anything from package: " + packageName);
            return  null;
        }

        // 3.依据不同的资源类型，采用不同的方式获取资源的集合
        Set<Class<?>> classSet = null;

        // 调用 URL 类中的 getProtocol() 方法，判断当前 URL 的协议是否是 file（即过滤出文件类型的资源）
        if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)){
            classSet = new HashSet<Class<?>>();

            // 获取该 URL 所对应的 File 类对象，构造方法中传入通过调用 URL 类中的 getPath() 方法获取的该 URL 中所包含的对应文件的绝对路径
            File packageDirectory = new File(url.getPath());

            // 调用下面定义的 extractClassFile() 方法，获取该包下的所有类
            extractClassFile(classSet, packageDirectory, packageName);
        }
        //TODO 此处可以加入针对其他类型资源的处理

        return classSet;
    }

    /**
     * 递归获取目标包里面的所有 .class 文件(包括子包里的 .class 文件)
     *
     * @param emptyClassSet 装载目标类的集合
     * @param fileSource    文件或者目录
     * @param packageName   包名
     * @return 类集合
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
        // 调用 File 类中的 isDirectory() 方法，判断该 File 类对象是否是一个文件夹
        if(!fileSource.isDirectory()){
            return;
        }

        // 如果是一个文件夹，则调用 File 类中的 listFiles() 方法过滤出该文件夹下的所有文件夹
        // 该 listFiles() 方法会遍历当前文件夹（即形参 fileSource 所指向那个文件夹）
        // 然后
        // 返回所以符合 FileFilter 类的过滤条件的 File 类对象
        // 即
        // 在 listFiles() 方法中传入 FileFilter 类对象
        // 通过重写该 FileFilter 类中的 accept() 方法来判断当前遍历到的 File 类对象（即形参 fileSource 所指向的文件夹中的文件或文件夹）是否是文件夹
        // 如果是就返回 true
        // 此时
        // listFiles() 方法就会把当前文件夹（即形参 fileSource 所指向那个文件夹）中所有的文件夹过过滤出来，放到一个数据类型为 File 类的数组中
        // 如果不是就是返回 false
        // 此时
        // 该 File 类对象就不会被放到一个数据类型为 File 类的数组中
        // 最后
        // 该数组就会赋值给下面这个变量 files
        File[] files = fileSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {

                if(file.isDirectory()) {
                    return true;
                }
                // 如果此时 listFiles 遍历到的 File 类对象不是文件就先判断该文件是否是 .class 文件，并处理 .class 文件
                else {
                    // 获取文件的绝对值路径
                    String absoluteFilePath = file.getAbsolutePath();

                    // 判断该绝对路径指向的文件是否是 class 文件
                    if(absoluteFilePath.endsWith(".class")) {
                        // 若是 class 文件，则调用下面定义的 addToClassSet() 方法获取 Class 类对象，并存放如 Set 集合中
                        addToClassSet(absoluteFilePath);
                    }
                }

                return false;
            }

            /**
             * 根据 Class 文件的绝对路径，获取并生成 Class 对象，并放入 Set 集合中
             * @param absoluteFilePath
             */
            private void addToClassSet(String absoluteFilePath) {
                // 下面三行代码是用于把该 Class 文件的绝对值路径转换成对应的包含了包名的全类名
                // 如
                // 把 /Users/baidu/imooc/springframework/sampleframework/target/classes/com/imooc/entity/dto/MainPageInfoDTO.class
                // 转换成
                // com.imooc.entity.dto.MainPageInfoDTO（即把前面那部分的到项目根目录的路径去掉了，并且把最后面的 .class 这个后缀名去掉）
                // 注意
                // 下面的 File.separator 这个常量会根据不同的操作系统，选用不同的文件分隔符（即 Linux 系统的文件分隔符是 /，Windows 里的文件分隔符是 \）
                // 这样就不用我们自己去判断操作系统，然后去使用不同的文件分隔符了
                absoluteFilePath = absoluteFilePath.replace(File.separator, ".");
                String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className = className.substring(0, className.lastIndexOf("."));

                // 调用下面定义的 loadClass() 方法，获取该 Class 文件对应类的 Class 类对象
                Class targetClass = loadClass(className);

                // 把 Class 类对象存放如集合中
                emptyClassSet.add(targetClass);
            }
        });

        // 如果该 files 变量不为空（即当前文件夹下仍然有文件夹），就遍历该 files 数组中的所有 File 类对象
        // 然后
        // 递归调用当前的 extractClassFile() 方法，直到当前文件夹下没有文件夹为止
        if(files != null) {
            for(File f : files) {
                //递归调用
                extractClassFile(emptyClassSet, f, packageName);
            }
        }
    }
    /**
     * 获取Class对象
     *
     * @param className class全名=package + 类名
     * @return Class
     */
    public static Class<?> loadClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error:", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 创建类实例
     *
     * @param clazz Class
     * @param <T>   class的类型
     * @param accessible   是否支持创建出构造方法为私有的类的实例
     * @return 类的实例化
     */
    public static <T> T newInstance(Class<?> clazz, boolean accessible){
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(accessible);
            return (T)constructor.newInstance();
        } catch (Exception e) {
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 获取classLoader
     *
     * @return 当前ClassLoader
     */
    public static  ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }
    /**
     * 设置类的属性值
     *
     * @param field      成员变量
     * @param target     类实例
     * @param value      成员变量的值
     * @param accessible 是否允许设置私有属性
     */
    public static void setField(Field field, Object target, Object value, boolean accessible){
        field.setAccessible(accessible);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.error("setField error", e);
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        //extractPackageClass("com.imooc.entity");
        File[] files = null;
        for(File f : files){
            System.out.printf("haha");
        }
        files.getClass().getClassLoader();

    }
}
