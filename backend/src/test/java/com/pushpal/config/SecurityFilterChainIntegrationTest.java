package com.pushpal.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.scheduler.enabled=false",
        "app.api-base-url=https://api.pushpal.test"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedApiRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointRemainsPublicAndReflectsMissingPushConfiguration() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void deliveryCallbackPassesAuthenticationChainButRequiresScopedToken() throws Exception {
        mockMvc.perform(post("/api/notifications/{id}/delivered", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
