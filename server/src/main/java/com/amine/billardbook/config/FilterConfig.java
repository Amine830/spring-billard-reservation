package com.amine.billardbook.config;

import com.amine.billardbook.filter.DateCacheFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * Configuration de l'application.
 */
@Configuration
public class FilterConfig {

    @Bean
    @ApplicationScope
    public FilterRegistrationBean<DateCacheFilter> registerDateCacheFilter(DateCacheFilter dateCacheFilter) {
        FilterRegistrationBean<DateCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(dateCacheFilter);
        registrationBean.addUrlPatterns("/reservations", "/reservations/*");
        return registrationBean;
    }
}

