package com.pawlak.subscription.currency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExchangeRateConfig {
    @Bean
    public RestClient nbpRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.nbp.pl/api")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
