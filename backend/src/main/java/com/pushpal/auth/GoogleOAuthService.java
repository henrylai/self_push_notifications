package com.pushpal.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleOAuthService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.google.client-id:}")
    private String clientId;

    @Value("${app.google.client-secret:}")
    private String clientSecret;

    @Value("${app.google.token-uri:https://oauth2.googleapis.com/token}")
    private String tokenUri;

    @Value("${app.google.userinfo-uri:https://www.googleapis.com/oauth2/v2/userinfo}")
    private String userinfoUri;

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public GoogleUserInfo getUserInfo(String code, String redirectUri) {
        if (!isConfigured()) {
            throw new IllegalStateException("Google login is not configured");
        }

        String accessToken = exchangeCode(code, redirectUri);
        return fetchUserInfo(accessToken);
    }

    private String exchangeCode(String code, String redirectUri) {
        String body = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        ).entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUri))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            if (response.statusCode() != 200 || !json.has("access_token")) {
                String error = json.path("error_description").asText(json.path("error").asText("unknown"));
                throw new IllegalStateException("Google token exchange failed: " + error);
            }
            return json.get("access_token").asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token exchange failed", e);
            throw new IllegalStateException("Google token exchange failed");
        }
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(userinfoUri))
                    .header("Authorization", "Bearer " + accessToken)
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Failed to fetch Google user info");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String email = json.path("email").asText(null);
            boolean emailVerified = json.path("verified_email").asBoolean(false);
            String id = json.path("id").asText(null);
            if (email == null || !emailVerified || id == null) {
                throw new IllegalStateException("Google account has no verified email");
            }
            String name = json.path("name").asText(email.split("@")[0]);
            return new GoogleUserInfo(id, email, name, json.path("picture").asText(null));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch Google user info", e);
            throw new IllegalStateException("Failed to fetch Google user info");
        }
    }

    public record GoogleUserInfo(String id, String email, String name, String picture) {}
}
