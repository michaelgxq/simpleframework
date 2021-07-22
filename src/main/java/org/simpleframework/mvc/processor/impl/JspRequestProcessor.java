package org.simpleframework.mvc.processor.impl;

import org.simpleframework.mvc.RequestProcessorChain;
import org.simpleframework.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * 该 RequestProcessor 接口实现类用于对客户端请求的 jsp 资源进行处理
 */
public class JspRequestProcessor implements RequestProcessor {
    // 定义两个常量
    private static final String JSP_SERVLET = "jsp";
    private static final String  JSP_RESOURCE_PREFIX = "/templates/";


    private RequestDispatcher jspServlet;

    public JspRequestProcessor(ServletContext servletContext) {
        // 调用 ServletContext 接口中的 getNamedDispatcher() 方法
        // 用于获取 Tomcat 实现的 Servlet 类 --- JspServlet 类所对应的 RequestDispatcher 类对象
        jspServlet = servletContext.getNamedDispatcher(JSP_SERVLET);
        if (null == jspServlet) {
            throw new RuntimeException("there is no jsp servlet");
        }
    }

    /**
     * 实现 RequestProcessor 接口中的 process() 方法，该方法主要功能就是判断客户端请求是否是请求 JSP 资源的
     * 如果是就交给 Tomcat 实现的 JspServlet 类去处理
     * @param requestProcessorChain
     * @return
     * @throws Exception
     */
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        // 判断客户端请求是否是请求 JSP 资源的
        // 如果是就通过调用 JspServlet 类所对应的 RequestDispatcher 类中的 forward() 方法交给 Tomcat 实现的 JspServlet 类去处理
        if (isJspResource(requestProcessorChain.getRequestPath())) {
            jspServlet.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());

            // 在 JspServlet 类处理完之后，执行控制权会返回到该 process() 方法，此时就需要返回 false
            // 以便让 RequestProcessorChain 类中的 doRequestProcessorChain() 方法中的 while 循环能够退出
            return false;
        }

        // 如果当前这个请求不会被交由 JspServlet 类处理，那么最后就返回 true
        // 以便让 RequestProcessorChain 类中的 doRequestProcessorChain() 方法中的 while 循环能遍历下一个 RequestProcessor 类对象
        return true;
    }

    /**
     * 判断客户端请求的是是否是 JSP 资源（即判断除去项目名部分的 URI 是否是以 /templates/ 开头）
     */
    private boolean isJspResource(String url) {
        return url.startsWith(JSP_RESOURCE_PREFIX);
    }

}

