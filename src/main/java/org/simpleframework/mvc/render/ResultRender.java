package org.simpleframework.mvc.render;

import org.simpleframework.mvc.RequestProcessorChain;

/**
 * 该类用于渲染请求结果（即 对请求结果进行包装）
 */
public interface ResultRender {
    // 该方法即用于渲染请求结果（即 对请求结果进行包装）
    void render(RequestProcessorChain requestProcessorChain) throws Exception;
}
