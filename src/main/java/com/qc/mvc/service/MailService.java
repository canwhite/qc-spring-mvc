package com.qc.mvc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 功能描述: mail
 *
 * @author lijinhua
 * @date 2022/8/17 11:50
 */
@Component
public class MailService {
    @Value("${smtp.from}")
    String from;


}
