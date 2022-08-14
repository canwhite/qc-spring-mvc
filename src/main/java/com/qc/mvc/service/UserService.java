package com.qc.mvc.service;

import com.qc.mvc.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

/**
 * 功能描述: user service
 *
 * @author lijinhua
 * @date 2022/8/2 18:10
 */
@Component
public class UserService {
    final  Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    JdbcTemplate jdbcTemplate;

    //将查询结果映射为Bean,一般作为jdbcTemplate方法的最后一个参数
    RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public  User getUserById(long id){
        return  jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?",new Object[] {id} ,userRowMapper);
    }

    public User getUserByEmail(String email){
        return  jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?",new Object[]{email},userRowMapper);
    }

    //登录
    public User signin(String email,String password){
        //这个{}是个标识符
        logger.info("try login by {}...", email);
        //用上边的方法获取user
        User user = getUserByEmail(email);
        if(user.getPassword().equals(password)){
            return  user;
        }
        throw  new RuntimeException("login failed.");
    }

    //注册
    public  User register(String email,String password,String name){
        //彻底解决这个问题
        logger.info("try register by {}...", email);
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setCreatedAt(System.currentTimeMillis());

        //holder为了后期拿到自增id
        KeyHolder holder = new GeneratedKeyHolder();
        if(1 != jdbcTemplate.update((conn)->{
            var ps = conn.prepareStatement("INSERT INTO users(email,password,name,createdAt) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getPassword());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getCreatedAt());
            return ps;
        },holder)){
            //抛出错误
            throw new RuntimeException("Insert failed.");
        }
        //从holder拿到值
        user.setId(holder.getKey().longValue());
        return user;
    }


    //更新
    public void  updateUser(User user){
        //如果查询查不到，就抛出错误
        if(1 != jdbcTemplate.update("UPDATE user SET name = ? WHERE id=?",user.getName(),user.getId())){
            throw new RuntimeException("User not found by id");
        }
    }

    //获取所有用户
    public List<User> getUsers(){
        return  jdbcTemplate.query("SELECT * FROM users",userRowMapper);
    }










}
