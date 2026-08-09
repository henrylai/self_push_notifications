package com.pushpal.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class WebPushConfig {

    @Bean
    @ConditionalOnExpression("!'${app.vapid.public-key:}'.isEmpty() && !'${app.vapid.private-key:}'.isEmpty()")
    public PushService pushService(
            @Value("${app.vapid.public-key:}") String publicKey,
            @Value("${app.vapid.private-key:}") String privateKey,
            @Value("${app.vapid.subject:mailto:pushpal@example.com}") String subject) throws Exception {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        return new PushService(publicKey, privateKey, subject);
    }
}
