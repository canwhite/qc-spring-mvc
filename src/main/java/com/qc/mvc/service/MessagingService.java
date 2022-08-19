package com.qc.mvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qc.mvc.web.MailMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 功能描述: send message
 *
 * @author lijinhua
 * @date 2022/8/19 14:08
 */
@Component
public class MessagingService {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JmsTemplate jmsTemplate;


    public void  sendMailMessage(MailMessage message) throws  Exception{
        /** write将java对象转化为json字符串
         * 注意这个MailMessage是我们自己定义的java对象
         * 具体位置是在web文件夹下
         * */
        String text = objectMapper.writeValueAsString(message);

        //用template发送信息
        /**
         * 最常用的是发送基于JSON的文本消息，上述代码通过JmsTemplate创建一个TextMessage并发送到名称为jms/queue/mail的Queue
         * 注意Artemis会自动创建Queue，因此不必手动创建一个名为jms/queue/mail的Queue
         * */
        jmsTemplate.send("jms/queue/mail", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                /** Jsm的消息类型支持以下几种
                 * TextMessage：文本消息；
                 * BytesMessage：二进制消息；
                 * MapMessage：包含多个Key-Value对的消息；
                 * ObjectMessage：直接序列化Java对象的消息；
                 * StreamMessage：一个包含基本类型序列的消息。
                 * */
                //这里创建的textMessage类型又是json
                return session.createTextMessage(text);
            }
        });
    }
}
