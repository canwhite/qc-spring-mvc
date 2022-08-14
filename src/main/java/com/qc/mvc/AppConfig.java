package com.qc.mvc;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Time;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 功能描述: app config
 *
 * @author lijinhua
 * @date 2022/8/2 18:03
 */
@Configuration
@ComponentScan
@EnableWebMvc /** 启用spring MVC*/
@EnableTransactionManagement
//classpath:target->classes即为classpath，任何我们需要在classpath前缀中获取的资源都必须在target->classes文件夹中找到
//正常项目resource里的资源，会放在target->classes的根目录下
@PropertySource("classpath:/jdbc.properties")
public class AppConfig {

    public static void main(String[] args) throws Exception {


        /**
         * 使用Spring MVC时，整个Web应用程序按如下顺序启动：
         * --------------------------------------------
         * 启动Tomcat服务器；
         * Tomcat读取web.xml并初始化DispatcherServlet；
         * DispatcherServlet创建IoC容器并自动注册到ServletContext中。
         * ---------------------------------------------------
         * 启动后，浏览器发出的HTTP请求全部由DispatcherServlet接收，并根据配置转发到指定Controller的指定方法处理。
         * */
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port", 8080));
        tomcat.getConnector();
        //tomcat读取web.xml,并初始化DispatcherServlet
        //xml里创建Ioc容器并自动注册到ServletContext
        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());


        //加载资源
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);


        //启动
        tomcat.start();
        tomcat.getServer().await();
    }

    // -- Mvc configuration ---------------------------------------------------
    @Bean
    WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors){



        return new WebMvcConfigurer() {

            /**
             * 除了在RestController上添加di外，
             * 还可以以全局配置cors
             * */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://127.0.0.1:8080")
                        .allowedMethods("GET", "POST")
                        .maxAge(3600);
                // 可以继续添加其他URL规则:
                // registry.addMapping("/rest/v2/**")...
            }

            //直接只做了添加资源，我们这里在把interceptors添加上去
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                //映射路径为/static/**
                registry.addResourceHandler("/static/**").addResourceLocations("/static/");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                for (var interceptor : interceptors) {
                    registry.addInterceptor(interceptor);
                }
            }

        };
    }

    //-----i18n-----------------------
    @Bean
    LocaleResolver createLocaleResolver(){
        var clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.ENGLISH);
        clr.setDefaultTimeZone(TimeZone.getDefault());
        return  clr;
    }

    //然后写messageSource
    









    // -- pebble view configuration -------------------------------------------
    /** Spring MVC允许集成任何模板引擎，使用哪个模板引擎，就实例化一个对应的ViewResolver */
    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext){
        PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(new SpringExtension())
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return  viewResolver;
    }




    // -- jdbc configuration --------------------------------------------------

    @Value("${jdbc.url}")
    String jdbcUrl;

    @Value("${jdbc.username}")
    String jdbcUsername;

    @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean
    DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);
        config.addDataSourceProperty("autoCommit", "false");
        config.addDataSourceProperty("connectionTimeout", "5");
        config.addDataSourceProperty("idleTimeout", "60");
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate createJdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
