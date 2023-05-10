package com.example.findHataNotificationServer.services.sender;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

interface FictitiousEmail extends Sender{

}

@Service
@Slf4j
public class EmailSender implements FictitiousEmail {

    @Component
    static class EmailSenderSupport {

        @Autowired
        SenderStore senderStore;

        @Autowired
        public void init(FictitiousEmail sender) {
            senderStore.register(SenderType.EMAIL, sender);
        }
    }

    @Autowired
    SenderStore senderStore;

    @Autowired
    JavaMailSender emailSender;


    @Override
    @Async
    public void send(String to, String title, String message) {
        log.info("send message to: " + to);
        log.info("send message: " + message);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(title);
        simpleMailMessage.setText(message);
        emailSender.send(simpleMailMessage);
        log.info("successful sending of the message");
    }
}
