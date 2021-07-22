package org.simpleframework.mvc.render;

import org.simpleframework.mvc.RequestProcessorChain;

import javax.servlet.http.HttpServletResponse;

/**
 * 该类用于渲染（即包装）处理客户端请求时出现的客户端要访问的资源找不到的情况
 */
public class ResourceNotFoundResultRender implements ResultRender {
    private String httpMethod;
    private String httpPath;
    public ResourceNotFoundResultRender(String method, String path) {
        this.httpMethod = method;
        this.httpPath = path;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        // 直接向客户端发送一个错误码以及错误信息
        requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND,
                "获取不到对应的请求资源：请求路径[" + httpPath + "]" + "请求方法[" + httpMethod + "]");
    }
}
