package com.pawlak.subscription.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    public static final int LOGIN_CAPACITY = 10;
    public static final int LOGIN_REFILL_TOKENS = 10;
    public static final int LOGIN_REFILL_SECONDS = 60;

    public static final int REGISTER_CAPACITY = 5;
    public static final int REGISTER_REFILL_TOKENS = 5;
    public static final int REGISTER_REFILL_SECONDS = 3600;

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {

        String redisUri = password.isBlank()
                ? String.format("redis://%s:%d", host, port)
                : String.format("redis://:%s@%s:%d", password, host, port);

        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(connection)
                .build();
    }
}
