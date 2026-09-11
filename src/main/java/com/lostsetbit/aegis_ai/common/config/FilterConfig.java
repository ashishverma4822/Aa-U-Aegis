package com.lostsetbit.aegis_ai.common.config;

import com.lostsetbit.aegis_ai.ratelimit.filter.RateGuardFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateGuardFilter> rateGuardFilterRegistration(RateGuardFilter filter) {
        FilterRegistrationBean<RateGuardFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}