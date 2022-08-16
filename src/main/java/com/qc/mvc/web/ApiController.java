package com.qc.mvc.web;

import com.qc.mvc.entity.User;
import com.qc.mvc.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 功能描述: api controller
 *
 * @author lijinhua
 * @date 2022/8/4 14:32
 */
//@CrossOrigin(origins = "http://local.liaoxuefeng.com:8080")
@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    UserService userService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /** 写个同步请求的方法 */
    @GetMapping("/version")
    public  Map<String,String> version(){
        logger.info("get version ...");
        return  Map.of("version","1.0");
    }

    @GetMapping("/users")
    /**
    public List<User> users(){
        return  userService.getUsers();
    }
     */
    //第一种asyncc处理方式是返回一个Callable，
    //SpringMVC自动把返回的Callable放入线程池执行，
    //等待结构返回再写入响应
    public Callable<List<User>> users(){
        logger.info("get users...");
        //返回值是callable，这里不用写call了，算是一种简写？
        //有点像promise
        return ()->{
            try {
                //这里实际上可以使用httpClient去请求，然后拿到返回结果在下
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            return userService.getUsers();
        };
    }


    /** 接上
     * get请求可以直接在浏览器中
     * http://localhost:8080/api/users
     * */


    //路径参数PathVariable("xxx")
    @GetMapping("/users/{id}")
    /**
    public  User user(@PathVariable("id") long id){
        return  userService.getUserById(id);
    }
    */

    //修改上述，将此方法改为异步
    public DeferredResult<User> user(@PathVariable("id") long id){
        //初始化一个DeferredResult实例，设置超时时间是3s
        DeferredResult<User> result = new DeferredResult<>(3000L);//3s超时
        //然后新起一个线程
        new Thread(()->{
            // 等待1秒:
            try {
                //实际上可以在这里发起请求
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //pass
            }
            //通过setResult。既可以设置成功也可以设置失败。常用
            try {
                User user = userService.getUserById(id);
                // 设置正常结果并由Spring MVC写入Response:
                result.setResult(user);
            } catch (Exception e) {
                // 设置错误结果并由Spring MVC写入Response:
                result.setErrorResult(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage()));
            }
        }).start();
        return  result;
    }



    /** 接上
     * get请求可以直接在浏览器中
     * http://localhost:8080/api/users/0
     * */


    @PostMapping("/signin")
    public Map<String,Object> signin(@RequestBody SignInRequest signInRequest){
        try {
            User user = userService.signin(signInRequest.email,signInRequest.password);
            return  Map.of("user",user);
        }catch (Exception e){
            return Map.of("error", "SIGNIN_FAILED", "message", e.getMessage());
        }
    }

    //定义一个请求modal
    public static class SignInRequest{
        public  String email;
        public  String password;
    }

    /** 接上
     * 请求登录接口用curl
     * curl -v -H "Content-Type: application/json" -d '{"email":"bob@example.com","password":"bob123"}' http://localhost:8080/api/signin
     * */




}
