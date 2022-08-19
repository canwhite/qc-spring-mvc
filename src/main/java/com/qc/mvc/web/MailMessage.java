package com.qc.mvc.web;

/**
 * 功能描述: mail message
 *
 * @author lijinhua
 * @date 2022/8/19 14:44
 */

/**  用于给message提供用于转化的java对象*/
public class MailMessage {
    //在这里加一个状态机,两个状态，一个注册，一个登录
    public  static  enum  Type{
        REGISTRATION, SIGNIN;
    }

    //type是作为一个类型存在的
    public  Type type;
    public  String email;
    public  String  name;
    public  long timestamp;

    //静态方法
    public static MailMessage registration(String email, String name) {
        var msg = new MailMessage();
        msg.email = email;
        msg.name = name;
        msg.type = Type.REGISTRATION;
        msg.timestamp = System.currentTimeMillis();
        return msg;
    }





}
