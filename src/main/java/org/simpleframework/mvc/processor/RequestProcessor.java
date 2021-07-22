package org.simpleframework.mvc.processor;

import org.simpleframework.mvc.RequestProcessorChain;

/**
 * 定义 RequestProcessor 接口
 */
public interface RequestProcessor {
    /**
     * 该抽象方法就是用于对客户端发送的 HTTP 请求进行处理的
     * @param requestProcessorChain
     * @return
     * @throws Exception
     */
    boolean process(RequestProcessorChain requestProcessorChain) throws Exception;
}
