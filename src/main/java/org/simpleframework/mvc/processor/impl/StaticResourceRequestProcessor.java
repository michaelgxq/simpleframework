package org.simpleframework.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.mvc.RequestProcessorChain;
import org.simpleframework.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * 该 RequestProcessor 接口实现类用于对客户端请求的静态资源进行处理（包括但不限于图片、css、以及js文件等）
 */
@Slf4j
public class StaticResourceRequestProcessor implements RequestProcessor {
    // 定义两个常量
    public static final String DEFAULT_TOMCAT_SERVLET = "default";
    public static final String STATIC_RESOURCE_PREFIX = "/static/";

    RequestDispatcher defaultDispatcher;


    public StaticResourceRequestProcessor(ServletContext servletContext) {
        // 调用 ServletContext 接口中的 getNamedDispatcher() 方法
        // 用于获取 Tomcat 实现的 Servlet 类 --- DefaultServlet 类所对应的 RequestDispatcher 类对象
        this.defaultDispatcher = servletContext.getNamedDispatcher(DEFAULT_TOMCAT_SERVLET);

        if(this.defaultDispatcher == null){
            throw new RuntimeException("There is no default tomcat servlet");
        }

        log.info("The default servlet for static resource is {}", DEFAULT_TOMCAT_SERVLET);
    }

    /**
     * 实现 RequestProcessor 接口中的 process() 方法，该方法主要功能就是判断客户端请求是否是请求静态资源的
     * 如果是就交给 Tomcat 实现的 DefaultServlet 类去处理（具体实现见下面的代码注释中的序号）
     * @param requestProcessorChain
     * @return
     * @throws Exception
     */
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        // 1. 通过请求路径判断是否是请求的静态资源
        if(isStaticResource(requestProcessorChain.getRequestPath())){
            // 2. 如果是静态资源，则将请求转发给 Tomcat 实现的 DefaultServlet 类
            defaultDispatcher.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());

            // 在 DefaultServlet 类处理完之后，执行控制权会返回到该 process() 方法，此时就需要返回 false
            // 以便让 RequestProcessorChain 类中的 doRequestProcessorChain() 方法中的 while 循环能够退出
            return false;
        }

        // 如果当前这个请求不会被交由 DefaultServlet 类处理，那么最后就返回 true
        // 以便让 RequestProcessorChain 类中的 doRequestProcessorChain() 方法中的 while 循环能遍历下一个 RequestProcessor 类对象
        return true;
    }

    // 通过请求路径判断客户端请求的是否为静态资源（即判断客户端的请求 URI 是否以 /static/ 开头）
    private boolean isStaticResource(String path){
        return path.startsWith(STATIC_RESOURCE_PREFIX);
    }
}
