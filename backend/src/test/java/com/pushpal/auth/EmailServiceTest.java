package com.pushpal.auth;

import com.pushpal.common.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class EmailServiceTest {

    @Test
    void rejectsMagicLinkWhenSmtpIsNotConfigured() {
        EmailService service = new EmailService(null);

        assertThatThrownBy(() -> service.sendMagicLink(
                "user@example.com", "https://pushpal.example/auth/callback/?token=token"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("temporarily unavailable");
    }

    @Test
    void reportsSmtpDeliveryFailure() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        doThrow(new MailSendException("delivery failed"))
                .when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        EmailService service = new EmailService(mailSender);
        ReflectionTestUtils.setField(service, "smtpHost", "smtp.example.com");
        ReflectionTestUtils.setField(service, "from", "noreply@example.com");

        assertThatThrownBy(() -> service.sendMagicLink(
                "user@example.com", "https://pushpal.example/auth/callback/?token=token"))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Unable to send");
    }
}
