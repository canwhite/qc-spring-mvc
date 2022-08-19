package com.qc.mvc.web;

import com.qc.mvc.entity.User;
import com.qc.mvc.service.MailService;
import com.qc.mvc.service.MessagingService;
import com.qc.mvc.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: user controller
 *
 * @author lijinhua
 * @date 2022/8/2 18:11
 */
@Controller
public class UserController {
    public  static  final  String KEY_USER = "__user__";
    final  Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    MessagingService messagingService;


    @GetMapping("/")
    public ModelAndView index(HttpSession session){
        //通过session直接拿到user
        User user = (User) session.getAttribute(KEY_USER);
        Map<String,Object> modal= new HashMap<>();
        if(user != null){
            modal.put("user",modal);
        }
        /** ModelAndView可以只给html，给html和返回map，或者给一个重定向地址，register里就是重定向地址 */
        return  new ModelAndView("index.html",modal);
    }
    //此处用来返回静态文件
    @GetMapping("/register")
    public ModelAndView register(){
        return  new ModelAndView("register.html");
    }

    //此处用来完成登录操作
    @PostMapping("/register")
    public ModelAndView doRegister(@RequestParam("email") String email,@RequestParam("password") String password,@RequestParam("name") String name){
        try {
            User user = userService.register(email,password,name);
            logger.info("user registered : {}",user.getEmail());

            /** 在注册完之后新开一个线程去并行发邮件 */
            //用lambda去简写run方法或者runnable接口实现，最后start就可以了
            /** Authentication failed;  用的测试的，估计帐号密码认证失败了，这里注释一下*/
            /**
            new Thread(()->{
                mailService.sendRegistrationMail(user);
            }).start();
             */

            try {
                messagingService.sendMailMessage(MailMessage.registration(user.getEmail(), user.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch (RuntimeException e){
            return  new ModelAndView("register.html",Map.of("email",email,"error","Register failed"));
        }
        return  new ModelAndView("redirect:/signin");
    }

    @GetMapping("/signin")
    public ModelAndView signin(HttpSession session){
        User user = (User) session.getAttribute(KEY_USER);
        if(user != null){
            return  new ModelAndView("redirect:/profile");
        }
        return  new ModelAndView("signin.html");
    }

    @PostMapping("/signin")
    public ModelAndView doSignin(@RequestParam("email") String email,@RequestParam("password") String password,HttpSession  session){
        /** 登录之后在session里存一下user */
        try{
            User user = userService.signin(email,password);
            session.setAttribute(KEY_USER,user);
        }catch (RuntimeException e){
            /** 失败了之后的兜底页面 */
            return  new ModelAndView("signin.html",Map.of("email", email, "error", "Signin failed"));
        }
        /** 跳转到文本内容显示*/
        return new ModelAndView("redirect:/profile");
    }

    @GetMapping("/profile")
    public ModelAndView profile(HttpSession session){
        User user = (User) session.getAttribute(KEY_USER);
        if (user == null){
            return new ModelAndView("redirect:/signin");
        }
        return new ModelAndView("profile.html", Map.of("user", user));
    }

    @GetMapping("/signout")
    public String signout(HttpSession session){
        session.removeAttribute(KEY_USER);
        return  "redirect:/signin";
    }


    /**
     * 有一种情况是输入和输出都是json
     * 这种@Controller也提供有解决方案，需要注入三方包jackson
     * 当然使用起来比较复杂，
     * so，Spring提供的有@RestController，当然也需要搭配jackson
     * 这里我们先来说一下@Controller这种情况
     * Rest在ApiController再说
     * */

    @PostMapping(
            value = "/rest",
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8"
    )
    @ResponseBody
    //参数 @RequestBody User user
    public String rest() {
        return "{\"restSupport\":true}";
    }

    /**
     * 可以使用curl验证请求
     * curl -v -H "Content-Type: application/json" -d '{"email":"bob@example.com"}' http://localhost:8080/rest
     * -v 输出请求的整个过程，用于调试
     * -H 请求头设置
     * -d 用于发送post请求的数据体
     * 最后是请求地址
     * */






}
