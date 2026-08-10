package com.pushpal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConfigTest {

    private DataSourceProperties newProps(String url) {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(url);
        return properties;
    }

    @Test
    void extractsCredentialsAndBuildsJdbcUrl() {
        DataSourceProperties properties =
                newProps("postgresql://postgres:secret@host:5432/railway");
        DataSourceConfig.apply(properties);
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://host:5432/railway");
        assertThat(properties.getUsername()).isEqualTo("postgres");
        assertThat(properties.getPassword()).isEqualTo("secret");
    }

    @Test
    void handlesJdbcPrefixedUrlWithQueryParams() {
        DataSourceProperties properties =
                newProps("jdbc:postgresql://postgres:secret@host:5432/railway?sslmode=require");
        DataSourceConfig.apply(properties);
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://host:5432/railway?sslmode=require");
        assertThat(properties.getUsername()).isEqualTo("postgres");
        assertThat(properties.getPassword()).isEqualTo("secret");
    }

    @Test
    void leavesPlainLocalhostUrlUntouched() {
        DataSourceProperties properties = newProps("jdbc:postgresql://localhost:5432/pushpal");
        DataSourceConfig.apply(properties);
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/pushpal");
        assertThat(properties.getUsername()).isNull();
        assertThat(properties.getPassword()).isNull();
    }

    @Test
    void normalizesUrlWithoutCredentials() {
        DataSourceProperties properties = newProps("postgresql://host:5432/railway");
        DataSourceConfig.apply(properties);
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://host:5432/railway");
    }

    @Test
    void leavesNullUrlUntouched() {
        DataSourceProperties properties = new DataSourceProperties();
        DataSourceConfig.apply(properties);
        assertThat(properties.getUrl()).isNull();
    }
}
