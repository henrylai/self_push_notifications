package com.pushpal.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigTest {

    @Test
    void prependsJdbcPrefixToPostgresUrl() {
        String url = "postgresql://postgres:secret@host:5432/pushpal?sslmode=require";
        assertThat(DataSourceConfig.normalizeJdbcUrl(url))
                .isEqualTo("jdbc:postgresql://postgres:secret@host:5432/pushpal?sslmode=require");
    }

    @Test
    void leavesJdbcUrlUntouched() {
        String url = "jdbc:postgresql://localhost:5432/pushpal";
        assertThat(DataSourceConfig.normalizeJdbcUrl(url)).isEqualTo(url);
    }

    @Test
    void leavesNullUntouched() {
        assertThat(DataSourceConfig.normalizeJdbcUrl(null)).isNull();
    }
}
