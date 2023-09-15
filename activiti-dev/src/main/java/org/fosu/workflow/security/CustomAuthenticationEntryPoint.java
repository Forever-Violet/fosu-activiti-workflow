package org.fosu.workflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 当未认证请求接口，会默认响应302，而前端无法处理，
 * 于是我们就响应一个正常的json字符串，其中 code: 50008 状态，前端接收到这个后跳转到登录页
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        String json = objectMapper.writeValueAsString(Result.build(50008, "请先登录再访问"));
        response.getWriter().write(json);

    }
}
