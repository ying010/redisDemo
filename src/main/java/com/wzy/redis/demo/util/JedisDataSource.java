package com.wzy.redis.demo.util;

import io.codis.jodis.JedisResourcePool;
import io.codis.jodis.RoundRobinJedisPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Package com.wzy.redis.demo.util
 * @ClassName JedisDataSource
 * @Description TODO
 * @Author W.Z.King
 * @Date 2019/11/5 11:41
 */
@Configuration
public class JedisDataSource {

    @Value("${spring.redis.host:148.70.65.167}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.jedis.pool.max-active}")
    private String maxActive;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;
    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWait;

//    @Bean(name = "jedisResourcePool")
    public JedisResourcePool jedisResourcePool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);

        JedisResourcePool pool = RoundRobinJedisPool.create().poolConfig(poolConfig)
                .curatorClient("148.70.65.167:6379", 30000)
                .password("").zkProxyDir("").build();

        return pool;
    }

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);

        JedisPool jedisPool = new JedisPool(poolConfig, host, port);
        return jedisPool;
    }

}
