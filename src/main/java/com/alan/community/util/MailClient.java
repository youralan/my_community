package com.alan.community.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件的工具类
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    //spring自带的邮件发送类
    @Autowired
    JavaMailSender mailSender;
    //邮件发送方
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content){
        try {
            //创建信息
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            //创建信息helper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            //设置信息值
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("邮件发送失败：" + e.getMessage());
        }

    }
}
