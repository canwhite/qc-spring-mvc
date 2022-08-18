package com.qc.mvc.service;

import com.qc.mvc.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;


/**
 * 功能描述: mail
 *
 * @author lijinhua
 * @date 2022/8/17 11:50
 */
@Component
public class MailService {
    //如果从发邮箱的角度来说，这个相当于发的源头
    @Value("${smtp.from}")
    String from;

    //引入mailSender
    @Autowired
    JavaMailSender mailSender;


    //写发送服务
    public  void  sendRegistrationMail(User user){
        try {
            /**MimeMessage是JavaMail的邮件对象，
             *而MimeMessageHelper是Spring提供的用于简化设置MimeMessage的类
             *通过我们的mailSender实例创建
            */
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,"utf-8");
            //设置发邮件的源头
            helper.setFrom(from);
            //设置发邮件的方向
            helper.setTo(user.getEmail());
            //设置邮件主题
            helper.setSubject("Welcome to Java course!");
            //设置邮件内容
            String html = String.format("<p> Hi,%s,</p><p>Welcome to Java course!</p> <p> send at %s</p>",user.getName(), LocalDateTime.now());
            //发送邮件
            helper.setText(html);

            /** 最后用mailSender发送message*/
            mailSender.send(mimeMessage);




        }catch (MessagingException e){
            throw  new RuntimeException();
        }

    }


}
