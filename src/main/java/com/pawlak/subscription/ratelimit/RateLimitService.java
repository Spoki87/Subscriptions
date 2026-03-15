package com.pawlak.subscription.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

import static com.pawlak.subscription.ratelimit.RateLimitConfig.*;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final ProxyManager<String> proxyManager;

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
                            .capacity(LOGIN_CAPACITY)
                            .refillIntervally(LOGIN_REFILL_TOKENS, Duration.ofSeconds(LOGIN_REFILL_SECONDS))
                            .build())
                    .build();

            case REGISTER -> () -> BucketConfiguration.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(REGISTER_CAPACITY)
                            .refillIntervally(REGISTER_REFILL_TOKENS, Duration.ofSeconds(REGISTER_REFILL_SECONDS))
                            .build())
                    .build();
        };
    }
}
