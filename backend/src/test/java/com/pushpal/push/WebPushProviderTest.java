package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import nl.martijndwars.webpush.Notification;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebPushProviderTest {

    @Test
    void sendsPayloadWhenOptionalBodyIsNull() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        nl.martijndwars.webpush.PushService client =
                mock(nl.martijndwars.webpush.PushService.class);
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(client.send(any(Notification.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(201);

        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        subscription.setEndpoint("https://push.example.test/subscription");
        subscription.setP256dh(generatePublicKey());
        subscription.setAuthKey(base64Url(new byte[16]));

        WebPushProvider provider = new WebPushProvider(client);
        NotificationProvider.SendResult result = provider.send(
                subscription,
                new NotificationProvider.NotificationPayload("Title", null, Map.of()));

        assertThat(result.success()).isTrue();
    }

    private String generatePublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", "BC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        ECPublicKey publicKey = (ECPublicKey) generator.generateKeyPair().getPublic();
        return base64Url(publicKey.getQ().getEncoded(false));
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
