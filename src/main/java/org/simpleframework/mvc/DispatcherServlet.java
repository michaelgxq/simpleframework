package org.simpleframework.mvc;

import com.imooc.controller.frontend.MainPageController;
import com.imooc.controller.superadmin.HeadLineOperationController;
import org.simpleframework.aop.AspectWeaver;
import org.simpleframework.core.BeanContainer;
import org.simpleframework.inject.DependencyInjector;
import org.simpleframework.mvc.processor.RequestProcessor;
import org.simpleframework.mvc.processor.impl.ControllerRequestProcessor;
import org.simpleframework.mvc.processor.impl.JspRequestProcessor;
import org.simpleframework.mvc.processor.impl.PreRequestProcessor;
import org.simpleframework.mvc.processor.impl.StaticResourceRequestProcessor;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 该 Servlet 是所有请求的入口，所有 HTTP 请求都会经由该 Servlet 交给 RequestProcessorChain 类处理
 * 因此
 * 该类的 @WebServlet 注解的值为 /*，表示所有类型的 HTTP 请求（即包括请求静态资源，JSP，JSON 数据等）
 * 都会交由该 Servlet 进行处理
 */
@WebServlet("/*")
public class DispatcherServlet extends HttpServlet {
    // 该成员变量就是一个存放 RequestProcessor 接口实现子类的容器（我们就可以把它看做是一个 RequestProcessor 矩阵）
    List<RequestProcessor> PROCESSOR = new ArrayList<>();

    /**
     * 实现 Servlet 接口中的 init() 方法，该方法的主要功能有 2 个（已在下面的代码注释中用序号标出）
     *
     */
    @Override
    public void init(){
        // 1. 初始化容器
        //    即
        //    这一步的功能就是通过调用我们自己实现的 BeanContainer 类中的 loadBeans() 方法来加载 com.imooc 包下的所有类
        //   （即 这一步相当于实现了 Spring IOC 的功能（即创建所有 Bean 实例（仅仅是实例化，还没有初始化）））
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.loadBeans("com.imooc");

        // 调用 AspectWeaver 类中的 doAop() 方法，对上一步实例化好的 Bean 进行 AOP 操作
        new AspectWeaver().doAop();

        // 调用 DependencyInjector 类中的 doIoc() 方法，对实例化好的 Bean 进行 IOC 操作（即 依赖注入）
        new DependencyInjector().doIoc();

        // 2. 初始化请求处理器责任链
        //    即
        //    把 RequestProcessor 实现类对象存放到容器中
        //   （注意，这里存放这些对象的顺序就是按照笔记中 “自研 MVC 的架构” 中的图中的 RequestProcessor 矩阵中各实现类的箭头顺序存放的）
        PROCESSOR.add(new PreRequestProcessor());
        PROCESSOR.add(new StaticResourceRequestProcessor(getServletContext()));
        PROCESSOR.add(new JspRequestProcessor(getServletContext()));
        PROCESSOR.add(new ControllerRequestProcessor());
    }

    /**
     * 实现 Servlet 接口的 service() 方法，该方法的功能主要有 3 个（已在下面的代码注释中用序号标出）
     * @param req
     * @param resp
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        // 1. 创建责任链 RequestProcessorChain 类对象实例
        //    构造方法中传入集合 PROCESSOR 的迭代器（以便于对集合进行遍历），HttpServletRequest 类对象 和 HttpServletResponse 类对象
        RequestProcessorChain requestProcessorChain = new RequestProcessorChain(PROCESSOR.iterator(), req, resp);

        // 2. 调用 RequestProcessorChain 类中的 doRequestProcessorChain() 方法来依次调用 RequestProcessor 实现类对请求进行处理
        //    该方法内部会遍历集合 PROCESSOR，分别调用该集合中的 RequestProcessor 类对象
        requestProcessorChain.doRequestProcessorChain();

        // 3. 对处理结果进行渲染
        requestProcessorChain.doRender();
    }
}
