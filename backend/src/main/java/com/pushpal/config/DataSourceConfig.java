package com.pushpal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    public static BeanPostProcessor databaseUrlParsingBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSourceProperties properties) {
                    apply(properties);
                    if (properties.getUrl() != null) {
                        log.info("Resolved datasource URL: {}", sanitize(properties.getUrl()));
                    }
                }
                return bean;
            }
        };
    }

    static void apply(DataSourceProperties properties) {
        String url = properties.getUrl();
        if (url == null) {
            return;
        }
        String stripped = url.startsWith("jdbc:") ? url.substring("jdbc:".length()) : url;
        if (!stripped.startsWith("postgresql://") && !stripped.startsWith("postgres://")) {
            return;
        }

        int schemeEnd = stripped.indexOf("://") + 3;
        int slash = stripped.indexOf('/', schemeEnd);
        String authority = slash >= 0 ? stripped.substring(schemeEnd, slash) : stripped.substring(schemeEnd);
        String rest = slash >= 0 ? stripped.substring(slash) : "";

        int at = authority.lastIndexOf('@');
        if (at < 0) {
            if (!url.startsWith("jdbc:")) {
                properties.setUrl("jdbc:" + url);
            }
            return;
        }

        String userInfo = authority.substring(0, at);
        String hostPort = authority.substring(at + 1);
        int colon = userInfo.indexOf(':');
        properties.setUsername(userInfo.substring(0, colon >= 0 ? colon : userInfo.length()));
        if (colon >= 0) {
            properties.setPassword(userInfo.substring(colon + 1));
        }
        properties.setUrl("jdbc:postgresql://" + hostPort + rest);
    }

    static String sanitize(String url) {
        int at = url.indexOf('@');
        return at >= 0 ? "jdbc:postgresql://" + url.substring(at + 1) : url;
    }
}
