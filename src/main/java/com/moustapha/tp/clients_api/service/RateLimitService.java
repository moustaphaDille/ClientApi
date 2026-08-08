package com.moustapha.tp.clients_api.service;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS = 15;

    private final ProxyManager<String> proxyManager;

    public RateLimitService(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(new StringCodec(), new ByteArrayCodec()));

        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .build();
    }

    private BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(MAX_REQUESTS)
                        .refillIntervally(MAX_REQUESTS, Duration.ofHours(1))
                        .build())
                .build();
    }

    public boolean isAllowed(String key) {
        Bucket bucket = proxyManager.builder().build(key, this::bucketConfiguration);
        return bucket.tryConsume(1);
    }
}
