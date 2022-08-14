package com.qc.mvc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 功能描述: user model
 *
 * @author lijinhua
 * @date 2022/8/2 18:08
 */
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private long createdAt;

    public Long getId() {
        return id;
    }
    public void  setId(Long id){
        this.id = id;
    }

    public long getCreatedAt(){
        return createdAt;
    }

    public void  setCreatedAt(long createdAt){
        this.createdAt = createdAt;
    }

    //获取dateTime
    public String getCreatedDateTime(){
        return  Instant.ofEpochMilli(this.createdAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getEmail(){
        return  email;
    }

    public void setEmail(String email){
        this.email = email;
    }


    public void  setPassword(String password){
        this.password = password;
    }

    @JsonIgnore //JsonIgnore
    /**
     * 如果注册的时候可以，但是读数据的时候不可以读到
     * @JsonProperty(access = Access.WRITE_ONLY)
     *
     * 如果读数据的时候可以读到，但是写入的时候拿不到
     * @JsonProperty(access = Access.READ_ONLY)
     *
     * */
    public String getPassword(){
        return  password;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    //获取imageUrl
    public String getImageUrl(){
        try {
            //拿到md5转换器
            MessageDigest md = MessageDigest.getInstance("MD5");
            //转换并返回接结果
            byte[] hash = md.digest(this.email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            //拼接出url字符串
            return "https://www.gravatar.com/avatar/" + String.format("%032x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            //java.security.NoSuchAlgorithmException: Cannot find any provider supporting RSA/ECB/PKCS1Padding
            throw new RuntimeException(e); //如果上述不满足，这里就抛出runtime exception
        }

    }

    @Override
    public String toString() {
        return String.format("User[id=%s, email=%s, name=%s, password=%s, createdAt=%s, createdDateTime=%s]", getId(),
                getEmail(), getName(), getPassword(), getCreatedAt(), getCreatedDateTime());
    }

}
