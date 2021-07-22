package org.simpleframework.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.mvc.RequestProcessorChain;
import org.simpleframework.mvc.annotation.RequestMapping;
import org.simpleframework.mvc.annotation.RequestParam;
import org.simpleframework.mvc.annotation.ResponseBody;
import org.simpleframework.mvc.processor.RequestProcessor;
import org.simpleframework.mvc.render.JsonResultRender;
import org.simpleframework.mvc.render.ResourceNotFoundResultRender;
import org.simpleframework.mvc.render.ResultRender;
import org.simpleframework.mvc.render.ViewResultRender;
import org.simpleframework.mvc.type.ControllerMethod;
import org.simpleframework.mvc.type.RequestPathInfo;
import org.simpleframework.util.ConverterUtil;
import org.simpleframework.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该 RequestProcessor 接口实现类用于把客户端请求派发到对应的 Controller 类中的方法进行处理
 *（该方法的作用和 Spring MVC 中，把客户端请求派发给对应的 Controller 类中的方法进行处理是一样的）
 *
 * 该类的主要功能如下
 * 1. 根据客户端请求的 URI 定位到相应的 Controller 类中的方法进行
 *
 * 2. 解析请求中的查询字符串（即 请求参数）（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）
 *    并把这些参数对应的值赋值给上面定位的方法的形参上
 *   （即 该功能是针对方法形参加了 @RequestParam 注解的方法的）
 *
 * 3. 通过反射调用该方法对客户端请求进行处理
 *
 * 4. 选择合适的 ResultRender 类对象，对请求处理结果进行渲染（即 包装）
 *
 */
@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {
    // IOC 容器
    private BeanContainer beanContainer;

    // 该集合中 Key 为 @RequestMapping 注解所设置的 URI 所对应的 RequestPathInfo 类对象
    // Value 为该 @RequestMapping 注解所在方法所对应的 ControllerMethod 类对象
    private Map<RequestPathInfo, ControllerMethod> pathControllerMethodMap = new ConcurrentHashMap<>();

    /**
     * 构造方法
     * 该构造方法的功能主要是解析加了 @RequestMapping 注解的类以及这些类中加了 @RequestMapping 注解的方法
     * 并把 @RequestMapping 注解的值对应的 RequestPathInfo 类对象
     * 和
     * 加了 @RequestMapping 注解的方法所对应的 ControllerMethod 类对象存放到集合 pathControllerMethodMap 中
     */
    public ControllerRequestProcessor() {
        // 获取 IOC 容器实例
        // 注意
        // 这里只需要获取 IOC 容器实例即可，因为当 DispatcherServlet 类在初始化的时候（即 Tomcat 在调用该类的 init() 方法的时候）
        // 我们已经在 init() 方法中对该 IOC 容器实例进行初始化了（即加载了 Bean，以及对这些 Bean 进行依赖注入）
        //（即 init() 方法中的步骤 1）
        // 而该 ControllerRequestProcessor 类的实例化是在 init() 方法的步骤 2 中进行的
        // 因此
        // 我们在该构造方法中获取的 IOC 容器实例就是已经初始化好的 IOC 容器实例
        this.beanContainer = BeanContainer.getInstance();

        // 获取 IOC 容器中，所有加了 @RequestMapping 注解的类（即 Controller 类）所对应的 Class 类对象
        Set<Class<?>> requestMappingSet = beanContainer.getClassesByAnnotation(RequestMapping.class);

        // 调用 initPathControllerMethodMap() 方法，方法传入的是上面获取的集合 requestMappingSet
        // 解析加了 @RequestMapping 注解的类以及这些类中加了 @RequestMapping 注解的方法
        // 并把 @RequestMapping 注解的值对应的 RequestPathInfo 类对象
        // 和
        // 加了 @RequestMapping 注解的方法所对应的 ControllerMethod 类对象存放到集合 pathControllerMethodMap 中
        initPathControllerMethodMap(requestMappingSet);
    }

    /**
     * 该方法的主要功能就是
     * 该构造方法的功能主要是解析加了 @RequestMapping 注解的类以及这些类中加了 @RequestMapping 注解的方法
     * 并把 @RequestMapping 注解的值对应的 RequestPathInfo 类对象
     * 和
     * 加了 @RequestMapping 注解的方法所对应的 ControllerMethod 类对象存放到集合 pathControllerMethodMap 中
     *（主要实现步骤见下面代码注释中的序号）
     *
     * @param requestMappingSet
     */
    private void initPathControllerMethodMap(Set<Class<?>> requestMappingSet) {
        if (ValidationUtil.isEmpty(requestMappingSet)) {
            return;
        }
        // 1. 遍历所有被 @RequestMapping 注解标记的类（即 Controller 类）（即遍历形参 requestMappingSet 接收的容器）
        //    获取这些类上面加的该 @RequestMapping 注解的属性值作为一级路径
        for (Class<?> requestMappingClass : requestMappingSet) {
            RequestMapping requestMapping = requestMappingClass.getAnnotation(RequestMapping.class);
            String basePath = requestMapping.value();
            // 如果 @RequestMapping 注解中的值不是以 / 开头，就在开头加上 /
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }

            // 2. 遍历当前类里所有被 @RequestMapping 注解标记的方法，获取方法上面该注解的属性值，作为二级路径
            Method[] methods = requestMappingClass.getDeclaredMethods(); // 通过反射获取该类中的所有方法对应的 Method 类对象

            if (ValidationUtil.isEmpty(methods)) {
                continue;
            }

            // 遍历上面获取到的所有 Method 类对象
            for (Method method : methods) {
                // 判断该方法是否加了 @RequestMapping 注解
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    // 获取该方法上的 @RequestingMapping 注解
                    RequestMapping methodRequest = method.getAnnotation(RequestMapping.class);

                    String methodPath = methodRequest.value();
                    if (!methodPath.startsWith("/")) {
                        methodPath = "/" + methodPath;
                    }

                    // 把加在 Controller 类上的 @RequestMapping 注解中的路径和加在该方法上的 @RequestMapping 注解中的路径拼接起来
                    String url = basePath + methodPath;

                    // 3. 解析该方法里被 @RequestParam 注解标记的形参
                    //    然后
                    //    把该注解的属性值作为 Key，把该形参的数据类型对应的 Class 类对象作为 Value，存放到集合中
                    Map<String, Class<?>> methodParams = new HashMap<>();

                    // 通过反射获取该方法中所有的形参
                    Parameter[] parameters = method.getParameters();

                    if (!ValidationUtil.isEmpty(parameters)) {

                        // 遍历上面获取的所有形参
                        for (Parameter parameter : parameters) {
                            // 获取该形参上的 @RequestParam 注解
                            RequestParam param = parameter.getAnnotation(RequestParam.class);

                            // 判断变量 param 是否为空（即判断该形参是否加了 @RequestParam 注解）
                            if (param == null) {
                                // 由于
                                // 目前暂定为 Controller 类中的方法里面所有的参数都需要 @RequestParam 注解
                                // 所以
                                // 一旦有形参没加该注解，就抛出异常
                                throw new RuntimeException("The parameter must have @RequestParam");
                            }

                            // 把 @RequestParam 注解的属性值作为 Key，把该形参的数据类型对应的 Class 类对象作为 Value，存放到集合中
                            methodParams.put(param.value(), parameter.getType());
                        }
                    }

                    // 4. 将上面几步获取到的信息封装成 RequestPathInfo 类实例和 ControllerMethod 类实例
                    //    然后放置到映射表（即集合 pathControllerMethodMap）里
                    String httpMethod = String.valueOf(methodRequest.method());

                    // 创建 RequestPathInfo 类对象
                    RequestPathInfo requestPathInfo = new RequestPathInfo(httpMethod, url);

                    // 判断当前集合 pathControllerMethodMap 中是否有以该 RequestPathInfo 类对象为 Key 的键值对
                    // 有就打印一个警告日志
                    // 这样可以让用户知道，此时下面调用 put() 方法是对该集合中原先的值进行覆盖操作
                    if (this.pathControllerMethodMap.containsKey(requestPathInfo)) {
                        log.warn("duplicate url:{} registration，current class {} method{} will override the former one",
                                requestPathInfo.getHttpPath(), requestMappingClass.getName(), method.getName());
                    }

                    // 创建 ControllerMethod 类实例
                    ControllerMethod controllerMethod = new ControllerMethod(requestMappingClass, method, methodParams);

                    // 把 RequestPathInfo 类实例和 ControllerMethod 类实例放置到映射表（即集合 pathControllerMethodMap）里
                    this.pathControllerMethodMap.put(requestPathInfo, controllerMethod);
                }
            }
        }

    }

    /**
     * 实现 RequestProcessor 接口中的 process() 方法，该方法的功能如下
     *
     * 1. 获取客户端发送的请求 URI（即请求路径），和请求方法，并将它们封装成 RequestPathInfo 类对象
     *
     * 2. 以该 RequestPathInfo 类对象为 Key，从集合 pathControllerMethodMap 中获取对应的 Value（即 ControllerMethod 类对象）
     *    如果能获取到，那么就说明该 URI ，以及该请求对应的请求方法能匹配到对应的 Controller 类中的方法
     *
     * 3. 解析请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）
     *    并通过调用封装在该 ControllerMethod 类对象中的方法（即与该请求相匹配的 Controller 类中的方法）来处理该请求
     *
     * 4. 将处理结果使用对应的 ResultRender 类进行包装
     *
     *
     * @param requestProcessorChain
     * @return
     * @throws Exception
     */
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {

        // 1. 获取客户端发送的请求 URI（即请求路径），和请求方法，并将它们封装成 RequestPathInfo 类对象
        String method = requestProcessorChain.getRequestMethod();
        String path = requestProcessorChain.getRequestPath();
        RequestPathInfo requestPathInfo = new RequestPathInfo(method, path);

        // 2. 以该 RequestPathInfo 类对象为 Key，从集合 pathControllerMethodMap 中获取对应的 Value（即 ControllerMethod 类对象）
        //    如果能获取到，那么就说明该 URI ，以及该请求对应的请求方法能匹配到对应的 Controller 类中的方法
        ControllerMethod controllerMethod = this.pathControllerMethodMap.get(requestPathInfo);

        // 如果 controllerMethod 为 null，表示该客户端请求的 URI 没有对应的 Controller 类中的方法与之匹配
        if (controllerMethod == null) {
            // 使用 ResourceNotFoundResultRender 类对该客户端请求进行处理
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(method, path));

            return false;
        }

        // 3. 解析请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）
        //    并通过调用封装在该 ControllerMethod 类对象中的方法（即与该请求相匹配的 Controller 类中的方法）来处理该请求
        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest());

        // 4. 将处理结果使用对应的 ResultRender 类进行包装
        setResultRender(result, controllerMethod, requestProcessorChain);

        return true;
    }

    /**
     * 根据不同情况设置不同的渲染器
     */
    private void setResultRender(Object result, ControllerMethod controllerMethod, RequestProcessorChain requestProcessorChain) {
        if (result == null) {
            return;
        }

        ResultRender resultRender;

        // 判断当前方法上是否加了 @ResponseBody 注解
        boolean isJson = controllerMethod.getInvokeMethod().isAnnotationPresent(ResponseBody.class);

        // 如果加了 @ResponseBody 注解，就创建 JsonResultRender 类对象，以便后面使用该类对象对处理结果进行包装
        if (isJson) {
            resultRender = new JsonResultRender(result);
        }
        // 如果没加，就创建 ViewResultRender 类对象，以便后面使用该类对象对处理结果进行包装
        else {
            resultRender = new ViewResultRender(result);
        }

        // 调用 RequestProcessorChain 类中的 setResultRender() 方法为 RequestProcessorChain 类设置对应的 ResultRender 类对象
        requestProcessorChain.setResultRender(resultRender);
    }

    /**
     * 该方法的主要功能就是解析请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）
     * 并通过调用封装在该 ControllerMethod 类对象中的方法（即与该请求相匹配的 Controller 类中的方法）来处理该请求
     *（具体见下面代码注释中的序号）
     *
     * @param controllerMethod
     * @param request
     * @return
     */
    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request) {

        // 该集合用于存放从请求参数（该请求参数可以是 GET 请求参数和 POST 请求中，键值对形式的请求参数）解析出来的参数
        Map<String, String> requestParamMap = new HashMap<>();

        // 1. 调用 HttpServletRequest 接口中的 getParameterMap() 方法获取 GET 或者 POST 请求的请求参数
        //    注意
        //    该 Map 集合的 Value 是一个字符串数组，这是因为请求参数会存在一个 Key 对应多个 Value 的请求（如 表单中的复选框）
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            if (!ValidationUtil.isEmpty(parameter.getValue())) {
                // 把请求参数的 Key 和 Value 存放到集合 requestParamMap 中
                //（由于我们这里只支持一个 Key 对应一个 Value 的形式，所以这里只取 Value （即上面 Map 集合中的字符串数组）中的第一个值
                requestParamMap.put(parameter.getKey(), parameter.getValue()[0]);
            }
        }

        // 2. 根据获取到的请求参数
        //    以及
        //    形参 controllerMethod 所接收的 ControllerMethod 类对象里存放的方法形参和它的数据类型对应的 Class 类对象的 Map 集合
        //    去实例化出该 Controller 类中方法对应的实参

        // 该集合中存放经过类型转化后的客户端请求参数中的 Value
        List<Object> methodParams = new ArrayList<>();

        // 获取形参 controllerMethod 所接收的 ControllerMethod 类对象里存放的方法形参和它的数据类型对应的 Class 类对象的 Map 集合
        Map<String, Class<?>> methodParamMap = controllerMethod.getMethodParameters();

        // 遍历集合 methodParamMap
        for (String paramName : methodParamMap.keySet()) {
            // 获取该形参对应的数据类型的 Class 类对象（该 paramName 变量的值就是 @RequestParam 注解中的值）
            Class<?> type = methodParamMap.get(paramName);

            // 尝试从存放客户端请求参数的集合 requestParamMap 中获取与 @RequestParam 注解中的值同名的 Key 所对应的 Value
            String requestValue = requestParamMap.get(paramName);

            // 该变量接收经过转化的请求参数中的 Value
            // 因为
            // 我们上面存放在集合 requestParamMap 中的都是 String 类型的请求参数，而该 Controller 类中方法的形参数据类型是各种各样的
            //（即 可能是 String，int，double 等（这里我们只支持基本数据类型））
            // 所以
            // 我们要把客户端传送过来的 String 类型的参数转换成对应的数据类型
            // 以便后面通过反射调用该方法时使用
            Object value;

            // 如果存放客户端请求参数的集合 requestParamMap 中没有该请求参数（即客户端没有传送该请求参数）
            // 那么
            // 就为变量 value 赋一个空值
            if (requestValue == null) {
                // 根据方法形参的数据类型，给变量 value 赋上适配于参数类型的空值（即 如果形参的数据类型是 int，那它对应的空值就是 0）
                value = ConverterUtil.primitiveNull(type);
            } else {
                // 根据该 Controller 类中方法形参对应的数据类型
                // 把变量 requestValue 接收的客户端请求参数对应的 Value 转换成该数据类型
                //（这里我们只支持转换成 String 以及基础类型 char,int,short,byte,double,long,float,boolean,及它们的包装类型）
                value = ConverterUtil.convert(type, requestValue);
            }

            // 把转化后的数据放入到集合 methodParams 中
            methodParams.add(value);
        }


        // 3. 通过反射执行该 Controller 类中的方法并获取返回结果

        // 通过封装在 ControllerMethod 类中的该方法所在 Controller 类的 Class 类对象，或者该类在 IOC 容器中对应的实例
        Object controller = beanContainer.getBean(controllerMethod.getControllerClass());

        // 获取封装在 ControllerMethod 类中的该方法所对应的 Method 类对象
        Method invokeMethod = controllerMethod.getInvokeMethod();

        // 这里为了防止该方法是私有的，这里要设置以下该方法的访问权限
        invokeMethod.setAccessible(true);

        Object result;

        try {
            // 如果集合 methodParams 为空（即 该方法为空参方法）
            if (methodParams.size() == 0) {
                result = invokeMethod.invoke(controller);
            }
            else {
                result = invokeMethod.invoke(controller, methodParams.toArray());
            }
        }
        catch (InvocationTargetException e) {
            // 如果是调用异常的话，需要通过e.getTargetException() 去获取执行方法抛出的异常
            throw new RuntimeException(e.getTargetException());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
