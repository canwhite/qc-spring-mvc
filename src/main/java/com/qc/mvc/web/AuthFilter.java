package com.qc.mvc.web;

/**
 * 功能描述: auth filter
 *
 * @author lijinhua
 * @date 2022/8/5 10:53
 */

import com.qc.mvc.entity.User;
import com.qc.mvc.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
//import java.util.logging.Filter;
import javax.servlet.Filter; //注意不要引入错误的Filter
import javax.servlet.http.HttpServletRequest;

/**
 * 一个Bean，最终通过在web.xml中配置DelegatingFilterProxy代理执行
 * Filter区别于Interceptor是在这个Servlet之前做筛选，而Interceptor更像一个中间件
 * */
@Component
public class AuthFilter implements Filter {
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            authenticateByHeader(req);
        } catch (RuntimeException e) {
            logger.warn("login by authorization header failed.", e);
        }
        chain.doFilter(request, response);
    }

    //basic auth
    private void  authenticateByHeader(HttpServletRequest req){
        String authHeader = req.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Basic ")){
            logger.info("try authenticate by authorization header...");
            String up = new String(Base64.getDecoder().decode(authHeader.substring(6)), StandardCharsets.UTF_8);
            int pos = up.indexOf(":");
            if(pos > 0){
                String email = URLDecoder.decode(up.substring(0, pos), StandardCharsets.UTF_8);
                String password = URLDecoder.decode(up.substring(pos + 1), StandardCharsets.UTF_8);
                //登录
                User user = userService.signin(email, password);
                //放入session
                req.getSession().setAttribute(UserController.KEY_USER, user);
                logger.info("user {} login by authorization header ok.", email);
            }
        }
    }
}
