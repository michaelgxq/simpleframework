package org.simpleframework.mvc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 该类是对客户端发送的 HTTP 请求 以及 @RequestMapping 注解中所设置的 URI 的封装
 * 即
 * 该类中存储的是 URI 和请求方法（即是 POST 请求还是 GET 请求）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPathInfo {
    // HTTP 请求方法
    private String httpMethod;
    // HTTP 请求路径
    private String httpPath;
}
