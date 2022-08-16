package com.qc.mvc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


/**
 * 功能描述: filter
 *
 * @author lijinhua
 * @date 2022/8/16 10:02
 */
public class AsyncFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.info("start AsyncFilter...");
        filterChain.doFilter(servletRequest, servletResponse);
        logger.info("end AsyncFilter.");
    }
}
