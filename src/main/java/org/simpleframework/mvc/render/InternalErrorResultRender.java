package org.simpleframework.mvc.render;

import org.simpleframework.mvc.RequestProcessorChain;

import javax.servlet.http.HttpServletResponse;

/**
 * 该类用于渲染（即包装）处理客户端请求时抛出的异常的
 */
public class InternalErrorResultRender implements ResultRender{
    private String errorMsg;

    public InternalErrorResultRender(String  errorMsg){
        this.errorMsg = errorMsg;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        // 直接向客户端发送一个错误码以及错误信息
        requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
    }
}
