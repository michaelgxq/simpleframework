package org.simpleframework.mvc.render;

import com.google.gson.Gson;
import org.simpleframework.mvc.RequestProcessorChain;

import java.io.PrintWriter;

/**
 * 该类为 Json 渲染器（即将客户端请求的处理结果转换成 JSON 格式字符串，并将该字符串传入响应流 ）
 */
public class JsonResultRender implements ResultRender {
    private Object jsonData;
    public JsonResultRender(Object jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        // 设置响应头
        requestProcessorChain.getResponse().setContentType("application/json");
        requestProcessorChain.getResponse().setCharacterEncoding("UTF-8");

        // 往响应流 PrintWriter 类对象中写入经过 gson 格式化之后的处理结果
        //（此时数据就会被发送到客户端）
        try(PrintWriter writer = requestProcessorChain.getResponse().getWriter()){
            Gson gson = new Gson();
            writer.write(gson.toJson(jsonData));
            writer.flush();
        }
    }
}
