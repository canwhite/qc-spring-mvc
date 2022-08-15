package com.qc.mvc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 功能描述: mvc interceptor
 *
 * @author lijinhua
 * @date 2022/8/15 09:12
 */
@Component
public class MvcInterceptor implements HandlerInterceptor {
    @Autowired
    LocaleResolver localeResolver;

    //引入messageSource
    @Autowired
    @Qualifier("i18n")
    MessageSource messageSource;


    /** postHandle controller方法正常返回后执行 */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        if(modelAndView != null){
            //解析用户的locale，这里要用到我们注册的localeResolver
            Locale locale = localeResolver.resolveLocale(request);//从请求头获取相关数据
            //放入Modal
            modelAndView.addObject("__messageSource__",messageSource);
            modelAndView.addObject("__locale__",locale);
            //PS：这里改过之后前端也需要修改
            //<a href="/signin">{{ __messageSource__.getMessage('signin', null, __locale__) }}</a>
            //当然上述比较麻烦，我们也可以通过view引擎的拓展改造一下

        }
    }
}
