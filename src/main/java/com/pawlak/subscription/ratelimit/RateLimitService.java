package com.pawlak.subscription.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> proxyManager;

    @Value("${rate-limit.login.capacity:10}")
    private int loginCapacity;

    @Value("${rate-limit.login.refill-tokens:10}")
    private int loginRefillTokens;

    @Value("${rate-limit.login.refill-seconds:60}")
    private int loginRefillSeconds;

    @Value("${rate-limit.register.capacity:5}")
    private int registerCapacity;

    @Value("${rate-limit.register.refill-tokens:5}")
    private int registerRefillTokens;

    @Value("${rate-limit.register.refill-seconds:3600}")
    private int registerRefillSeconds;

    public boolean isAllowed(String ip, RateLimitEndpoint endpoint) {
        String key = buildKey(ip, endpoint);
        Supplier<BucketConfiguration> configSupplier = buildConfig(endpoint);

        return proxyManager
                .builder()
                .build(key, configSupplier)
                .tryConsume(1);
    }

    private String buildKey(String ip, RateLimitEndpoint endpoint) {
        return String.format("rate_limit:%s:%s", endpoint.name().toLowerCase(), ip);
    }

    private Supplier<BucketConfiguration> buildConfig(RateLimitEndpoint endpoint) {
        return switch (endpoint) {
            case LOGIN -> () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(loginCapacity)
                            .refillIntervally(loginRefillTokens, Duration.ofSeconds(loginRefillSeconds))
                            .build())
                    .build();

            case REGISTER -> () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(registerCapacity)
                            .refillIntervally(registerRefillTokens, Duration.ofSeconds(registerRefillSeconds))
                            .build())
                    .build();
        };
    }
}
