package com.qc.mvc.web;

import com.qc.mvc.entity.User;
import com.qc.mvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/users")
    public List<User> users(){
        return  userService.getUsers();
    }

    /** 接上
     * get请求可以直接在浏览器中
     * http://localhost:8080/api/users
     * */


    //路径参数PathVariable("xxx")
    @GetMapping("/users/{id}")
    public  User user(@PathVariable("id") long id){
        return  userService.getUserById(id);
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
