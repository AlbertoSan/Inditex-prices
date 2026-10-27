package com.priceservice.app.infrastructure.adapter.in.web.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class FilterConfig {

    /**
     * Registers the rate limiter on the /v1/** endpoints. Enabled by default;
     * disable with ratelimit.enabled=false (e.g. during load testing).
     */
    @Bean
    @ConditionalOnProperty(name = "ratelimit.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitingFilter(
            @Value("${ratelimit.capacity:5}") long capacity,
            @Value("${ratelimit.refill-tokens:5}") long refillTokens,
            @Value("${ratelimit.refill-period-seconds:1}") long refillPeriodSeconds) {

        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter(capacity, refillTokens, refillPeriodSeconds));
        registrationBean.addUrlPatterns("/v1/*");
        return registrationBean;
    }

    /**
     * Genera el ETag de la respuesta y atiende las peticiones condicionales
     * (If-None-Match -> 304 Not Modified), reduciendo el tráfico de respuestas
     * idénticas. Combinado con el Cache-Control que fija el controlador.
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
        FilterRegistrationBean<ShallowEtagHeaderFilter> registrationBean =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registrationBean.addUrlPatterns("/v1/*");
        return registrationBean;
    }
}
