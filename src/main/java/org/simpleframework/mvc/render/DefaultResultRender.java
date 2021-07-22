package org.simpleframework.mvc.render;

import org.simpleframework.mvc.RequestProcessorChain;

/**
 * 该类为默认渲染器（当客户端不需要获取请求结果时，就会调用该类）
 */
public class DefaultResultRender implements ResultRender {
    /**
     * 实现 ResultRender 接口中的 render() 方法
     * @param requestProcessorChain
     * @throws Exception
     */
    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        // 获取 HttpServletResponse 类对象，并使用该对象中的 setStatus() 方法设置响应状态码
        requestProcessorChain.getResponse().setStatus(requestProcessorChain.getResponseCode());
    }
}
