package org.fosu.workflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 退出成功，响应json
 */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        String json = objectMapper.writeValueAsString(Result.ok("退出成功"));
        response.getWriter().write(json);
    }
}
