package com.qc.mvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qc.mvc.web.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * 功能描述: mail message listener
 *
 * @author lijinhua
 * @date 2022/8/19 16:26
 */

@Component
public class MailMessageListener {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MailService mailService;


    @Autowired
    MessagingService messagingService;

    @JmsListener(destination = "jms/queue/mail", concurrency = "10")
    public void onMailMessageReceived(Message message) throws  Exception{
        logger.info("received message: " + message);
        if(message instanceof TextMessage){
            //先拿到，然后转成java实例
            String text = ((TextMessage) message).getText();
            //将json转化为java实例
            MailMessage mm = objectMapper.readValue(text,MailMessage.class);
            mailService.sendRegistrationMail(mm);

        }else{
            logger.error("unable to process non-text message!");

        }
    }










}
