package com.pushpal.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:PushPal <noreply@pushpal.app>}")
    private String from;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Autowired
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendMagicLink(String to, String url) {
        if (mailSender == null || smtpHost == null || smtpHost.isBlank()) {
            log.warn("SMTP not configured; magic link email was not sent");
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Your PushPal sign-in link");
        message.setText("""
                Hello,

                Use this link to sign in to PushPal (valid for 15 minutes, single use):

                %s

                If you didn't request this link, you can ignore this email.
                """.formatted(url));
        mailSender.send(message);
        log.info("Magic link email sent");
        return true;
    }
}
