package com.priceservice.app.infrastructure.config;

import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.application.port.out.LoadPricePort;
import com.priceservice.app.application.service.GetPriceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the application services (which are framework-free) as Spring beans.
 * Keeping this here means the application and domain layers carry no Spring
 * annotations: the only place that knows about the container is infrastructure.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public GetPriceUseCase getPriceUseCase(LoadPricePort loadPricePort) {
        return new GetPriceService(loadPricePort);
    }
}
