package com.pushpal.config;

import nl.martijndwars.webpush.Configuration;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class WebPushConfig {

    @Bean
    public PushService pushService(
            @Value("${app.vapid.public-key:}") String publicKey,
            @Value("${app.vapid.private-key:}") String privateKey,
            @Value("${app.vapid.subject:mailto:pushpal@example.com}") String subject) {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            return null;
        }

        Configuration configuration = new Configuration.Builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .subject(subject)
                .build();

        return new PushService(configuration);
    }
}
