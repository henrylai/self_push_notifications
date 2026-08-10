package com.pushpal.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Bean
    public static BeanPostProcessor jdbcUrlPrefixBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSourceProperties properties) {
                    properties.setUrl(normalizeJdbcUrl(properties.getUrl()));
                }
                return bean;
            }
        };
    }

    static String normalizeJdbcUrl(String url) {
        if (url == null || url.startsWith("jdbc:") || !url.contains("://")) {
            return url;
        }
        return "jdbc:" + url;
    }
}
