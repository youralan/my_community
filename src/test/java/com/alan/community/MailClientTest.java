package com.alan.community;

import com.alan.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailClientTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void mailSend(){
        Context context = new Context();
        context.setVariable("email", 1153665882);
        context.setVariable("url",123456);

        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail("1153665882@qq.com", "测试邮件", content);
    }
}
