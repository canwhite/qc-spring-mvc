package com.qc.mvc.web;

import com.qc.mvc.mbean.BlacklistMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 功能描述: blacklist interceptor
 *
 * @author lijinhua
 * @date 2022/8/22 14:11
 */
@Order(1)
@Component
public class BlacklistInterceptor implements HandlerInterceptor {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BlacklistMBean blacklistMBean;

    //拦截器,注意返回值是boolean，主要是看能不能继续往下走
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**获取到请求的ip地址 */
        String ip = request.getRemoteAddr();
        logger.info("check ip address {}...", ip);
        if(blacklistMBean.shouldBlock(ip)){
            //todo
            logger.warn("will block ip {} for it is in blacklist.", ip);
            //respone可以直接返回403，
            /** 严谨了，403本身的意思就有ip被拉入黑名单的意思*/
            response.sendError(403);
            /** 对拦截器来说，如果返回false，就不往下走了 */
            return false;
        }
        return  true;
    }
}
