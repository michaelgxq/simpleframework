package org.simpleframework.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.mvc.RequestProcessorChain;
import org.simpleframework.mvc.processor.RequestProcessor;

/**
 * 该 RequestProcessor 接口实现类用于对客户端的 HTTP 请求进行预处理（ 包括编码格式转换以及去掉 URI 最后的 / ）
 */
@Slf4j
public class PreRequestProcessor implements RequestProcessor {
    /**
     * 实现 process() 方法，该方法主要是对客户端请求的字符集，URI 格式进行修改
     *（具体实现方式见下面代码注释的序号）
     * @param requestProcessorChain
     * @return
     * @throws Exception
     */
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        // 1. 设置客户端发送的 HTTP 请求的编码，将其统一设置成 UTF-8
        requestProcessorChain.getRequest().setCharacterEncoding("UTF-8");

        // 获取客户端发送请求中的 URI
        String requestPath = requestProcessorChain.getRequestPath();

        // 2. 判断 URI 是否以 / 结尾，如果是就将请求路径末尾的 / 剔除，为后续使用该 URI 匹配对应的 Controller 类做准备
        //    因为
        //    一般 Controller 类中的 @RequestMapping 注解中设置的 URI 的末尾是没有 / 的（如 /aaa/bbb）
        //    所以
        //    如果客户端发送的请求的 URI 以 / 结尾（如 /aaa/bbb/ ）那么就需要去掉最后的 / （即如 处理成 /aaa/bbb）
        if(requestPath.length() > 1 && requestPath.endsWith("/")){
            requestProcessorChain.setRequestPath(requestPath.substring(0, requestPath.length() - 1));
        }

        log.info("preprocess request {} {}", requestProcessorChain.getRequestMethod(), requestProcessorChain.getRequestPath());

        // 最后返回 true
        // 以便让 RequestProcessorChain 类中的 doRequestProcessorChain() 方法中的 while 循环能遍历下一个 RequestProcessor 类对象
        return true;
    }
}
