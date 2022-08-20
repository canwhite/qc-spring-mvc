package com.qc.mvc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.mitchellbosecke.pebble.spring.extension.SpringExtension;
import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
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

import javax.jms.ConnectionFactory;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;


/**
 * 功能描述: app config
 *
 * @author lijinhua
 * @date 2022/8/2 18:03
 */
@Configuration
@ComponentScan
@EnableJms
@EnableWebMvc /** 启用spring MVC*/
@EnableTransactionManagement
@EnableScheduling
//classpath:target->classes即为classpath，任何我们需要在classpath前缀中获取的资源都必须在target->classes文件夹中找到
//正常项目resource里的资源，会放在target->classes的根目录下
//@PropertySource("classpath:/jdbc.properties")
//下边是添加多个
@PropertySource({ "classpath:/jdbc.properties", "classpath:/smtp.properties","classpath:/jms.properties","classpath:/task.properties"  })
public class AppConfig {

    final Logger logger = LoggerFactory.getLogger(getClass());

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

    // -- javamail configuration ----------------------------------------------
    @Bean
    JavaMailSender createJavaMailSender(
            // properties:
            @Value("${smtp.host}") String host,
            @Value("${smtp.port}") int port,
            @Value("${smtp.auth}") String auth,
            @Value("${smtp.username}") String username,
            @Value("${smtp.password}") String password,
            @Value("${smtp.debug:true}") String debug
    ){
        //创建mailSender实例
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        //属性设置
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        if (port == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (port == 465) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        props.put("mail.debug", debug);
        return  mailSender;
    }

    // -- jms configuration ----------------------------------------------
    //创建jsm的dataSource
    @Bean
    //@Value冒号后边可以加默认值
    ConnectionFactory createJMSConnectionFactory(@Value("${jms.uri:tcp://localhost:61616}") String uri,
                                                 @Value("${jms.username:admin}") String username, @Value("${jms.password:123456}") String password){

        logger.info("create JMS connection factory for standalone activemq artemis server...");
        return new ActiveMQJMSConnectionFactory(uri, username, password);
    }




    //然后创建模版模型
    @Bean
    //根据返回值名字去create不失为一个好办法
    JmsTemplate createJmsTemplate(@Autowired ConnectionFactory connectionFactory){
        return  new JmsTemplate(connectionFactory);
    }

    //设置一个带名字的listener factory
    @Bean("jmsListenerContainerFactory")
    DefaultJmsListenerContainerFactory createJmsListenerContainerFactory(@Autowired ConnectionFactory connectionFactory){
        var factory = new  DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return  factory;
    }


    //---jackson bean-----------------------------------------------------
    @Bean
    ObjectMapper createObjectMapper(){
        var om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return  om;
    }


    //-----i18n-----------------------
    /** 这部分主要是考察对顺序的理解，
     * 是从controller=> interceptor =>view Extension
     * 这样的顺序执行的，message搭配着locale参数做的语言切换
     * 目前已经基本实现了，默认是否浏览器语言还要验证
     * controller切换时更改为en_US去生成locale，然后走的后续流程
     * */
    @Bean
    LocaleResolver createLocaleResolver() {
        var clr = new CookieLocaleResolver();
        clr.setDefaultLocale(Locale.ENGLISH);
        clr.setDefaultTimeZone(TimeZone.getDefault());
        return clr;
    }

    //然后写messageSource
    @Bean("i18n") /** i18n，区分于其他messageSource */
    MessageSource createMessageSource(){
        var messageSource = new ResourceBundleMessageSource();
         //指定文件名是utf-8编码
         messageSource.setDefaultEncoding("UTF-8");
         //指定主文件名
         messageSource.setBasename("messages");
         return  messageSource;
    }


    // -- pebble view configuration -------------------------------------------
    /** Spring MVC允许集成任何模板引擎，使用哪个模板引擎，就实例化一个对应的ViewResolver */
    @Bean
    ViewResolver createViewResolver(@Autowired ServletContext servletContext,@Autowired @Qualifier("i18n") MessageSource messageSource){
        PebbleEngine engine = new PebbleEngine.Builder().autoEscaping(true)
                .cacheActive(false)
                .loader(new ServletLoader(servletContext))
                .extension(createExtension(messageSource))
                .build();
        PebbleViewResolver viewResolver = new PebbleViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix("");
        viewResolver.setPebbleEngine(engine);
        return  viewResolver;
    }

    //这个方法没咋起效果
    private Extension createExtension(MessageSource messageSource) {
        return new AbstractExtension() {
            @Override
            public Map<String, Function> getFunctions() {
                return Map.of("_", new Function() {
                    @Override
                    public List<String> getArgumentNames() {
                        return null;
                    }

                    @Override
                    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context,
                                          int lineNumber) {
                        /**
                         * 注意我们改写的实体：
                         * String text = messageSource.getMessage("signin", null, locale);
                         * 第一个参数signin是我们在.properties文件中定义的key，
                         * 第二个参数是Object[]数组作为格式化时传入的参数，
                         * 最后一个参数就是获取的用户Locale实例。
                         * */
                        //第一个参数拿到key
                        String key = (String) args.get("0");
                        System.out.println("=========="+key+"============");
                        //第二个参数，拿到arguments
                        List<Object> arguments = this.extractArguments(args);
                        System.out.println("-------"+arguments + "--------");

                        //第三个参数拿到Locale
                        Locale locale = (Locale) context.getVariable("__locale__");
                        //一开始等于en,后来等于en_US，会不会是这个造成的问题，目前来看一开始默认的是浏览器默认语言

                        System.out.println("$$$$$$$$$" + locale + "$$$$$$$$$");
                        //返回messageSource，toArray将ArrayList转化为数组
                        return  messageSource.getMessage(key,arguments.toArray(),"???" + key + "???", locale);

                    }

                    private List<Object> extractArguments(Map<String, Object> args) {
                        int i = 1;
                        List<Object> arguments = new ArrayList<>();
                        while (args.containsKey(String.valueOf(i))) {
                            //valueOf返回原始值
                            Object param = args.get(String.valueOf(i));
                            arguments.add(param);
                            i++;
                        }
                        return arguments;
                    }
                });
            }
        };
    }





    // -- jdbc configuration --------------------------------------------------

    @Value("${jdbc.url}")
    String jdbcUrl;

    @Value("${jdbc.username}")
    String jdbcUsername;

    @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean
    HikariDataSource createDataSource() {
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
