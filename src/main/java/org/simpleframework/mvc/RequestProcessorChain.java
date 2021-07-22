package org.simpleframework.mvc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.mvc.processor.RequestProcessor;
import org.simpleframework.mvc.render.DefaultResultRender;
import org.simpleframework.mvc.render.InternalErrorResultRender;
import org.simpleframework.mvc.render.ResultRender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 该类（即 请求处理器链）主要功能有以下 2 个
 * 1. 通过迭代器遍历存放 RequestProcessor 类对象（即 请求处理器）的集合，以便以责任链的模式执行这些请求处理器
 * 2. 把请求结果分派给特定的 ResultRender 类实例对请求处理器处理后的结果进行渲染
 */
@Data
@Slf4j
public class RequestProcessorChain {
    // RequestProcessor 类对象（即 请求处理器）的集合的迭代器
    private Iterator<RequestProcessor> requestProcessorIterator;
    // HttpServletRequest 类对象
    private HttpServletRequest request;
    // HttpServletResponse 类对象
    private HttpServletResponse response;
    // HTTP 请求方法（即是 POST 请求还是 GET 请求）
    private String requestMethod;
    // HTTP 请求路径（即 URI）
    private String requestPath;
    // HTTP 响应状态码（即 请求成功的 200，系统错误的 500 等）
    private  int responseCode;
    // 请求结果渲染器（即 ResultRender 类对象）
    private ResultRender resultRender;

    // 构造方法
    public RequestProcessorChain(Iterator<RequestProcessor> iterator, HttpServletRequest req, HttpServletResponse resp) {
        this.requestProcessorIterator = iterator;
        this.request = req;
        this.response = resp;
        this.requestMethod = req.getMethod();
        // 调用 HttpServletRequest 接口中的 getPathInfo() 方法，获取客户端请求的额外信息
        // 由于
        // 形参 req 接收的 HttpServletRequest 类对象是从 DispatcherServlet 类中传递过来的
        // 并且由于 DispatcherServlet 类的 @WebService 注解的值为 /*，因此此时该方法返回的就是一个不包含项目名那部分的 URI
        //（具体原因见 Servlet 笔记）
        this.requestPath = req.getPathInfo();

        // 设置默认的 HTTP 响应状态码（即 200）
        this.responseCode  = HttpServletResponse.SC_OK;
    }

    /**
     * 该方法是以责任链的模式来按需调用请求处理器（即 RequestProcessor 类对象）
     *（实现的步骤见下面代码注释中的序号）
     */
    public void doRequestProcessorChain() {
        try{
            // 1. 通过一个 RequestProcessor 集合的迭代器来遍历该 RequestProcessor 集合
            //   （该集合就是 DispatcherServlet 类中的成员变量 PROCESSOR）
            while(requestProcessorIterator.hasNext()){

                // 2. 调用遍历到的 RequestProcessor 类对象中的 process() 方法
                //    直到某个 RequestProcessor 类对象的 process() 方法返回为 false 为止
                if(!requestProcessorIterator.next().process(this)){
                    break;
                }
            }
        }
        catch (Exception e){
            // 3. 在调用 process() 方法期间如果出现异常，就创建一个内部异常渲染器（即 InternalErrorResultRender 类）对象
            //    以便之后由该渲染器对异常信息进行包装
            this.resultRender = new InternalErrorResultRender(e.getMessage());
            log.error("doRequestProcessorChain error:", e);
        }


    }

    /**
     * 该方法就是用于执行渲染器的（即调用 ResultRender 接口中的 render() 方法）
     */
    public void doRender() {
        // 1. 如果请求处理器实现类（即 RequestProcessor 类对象）
        //    没通过调用当前这个 RequestProcessorChain 类的 setResultRender() 方法设置渲染器的话
        //    就为当前这个 RequestProcessorChain 类的成员变量 resultRender 设置一个默认渲染器（即 DefaultResultRender 类对象）
        if(this.resultRender == null){
            this.resultRender = new DefaultResultRender();
        }

        // 2. 调用渲染器（即 ResultRender 类对象）的 render() 方法对结果进行渲染（即 进行包装）
        try {
            this.resultRender.render(this);
        } catch (Exception e) {
            log.error("doRender error: ", e);
            throw new RuntimeException(e);
        }
    }
}
