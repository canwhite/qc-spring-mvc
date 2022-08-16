package com.qc.mvc.web;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 功能描述: locale controller
 *
 * @author lijinhua
 * @date 2022/8/15 11:08
 */
@Controller
public class LocaleController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    LocaleResolver localeResolver;

    @GetMapping("/locale/{lo}")
    public String setLocale(@PathVariable("lo") String lo, HttpServletRequest request, HttpServletResponse response) {
        // 根据传入的lo创建Locale实例:
        Locale locale = null;
        ///locale/zh_CN
        int pos = lo.indexOf('_');
        System.out.println("---------------"+lo+"-------------------");
        //默认世纪上是看浏览器header放的是什么，然后后续就是我们的选择
        if (pos > 0) {
            //一个取得pos前，zh，一个取pos后是个cn，是个cn国家
            String lang = lo.substring(0, pos);
            String country = lo.substring(pos + 1);
            locale = new Locale(lang, country);

        } else {
            //默认是中文
            locale = new Locale(lo);
        }
        // 设定此Locale:
        localeResolver.setLocale(request, response, locale);
        System.out.println("locale is set to {}."+ locale);
        System.out.println("---------------"+lo+"-------------------");
        // 刷新页面:
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer == null ? "/" : referer);
    }
}