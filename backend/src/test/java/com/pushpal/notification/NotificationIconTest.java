package com.pushpal.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationIconTest {

    @Test
    void defaultsToBellAndParsesSupportedApiValues() {
        assertThat(NotificationIcon.fromApiValue(null)).isEqualTo(NotificationIcon.BELL);
        assertThat(NotificationIcon.fromApiValue("heart")).isEqualTo(NotificationIcon.HEART);
        assertThat(NotificationIcon.GIFT.apiValue()).isEqualTo("gift");
    }

    @Test
    void rejectsUnknownIconValues() {
        assertThatThrownBy(() -> NotificationIcon.fromApiValue("custom-url"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported notification icon");
    }
}
