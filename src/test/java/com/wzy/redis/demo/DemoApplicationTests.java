package com.wzy.redis.demo;

import com.wzy.redis.demo.util.JedisDataSource;
import io.codis.jodis.JedisResourcePool;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SpringBootTest
class DemoApplicationTests {

//    @Autowired
//    StringRedisTemplate stringRedisTemplate;
//    @Autowired @Qualifier("jedisResourcePool")
//    JedisResourcePool jedisResourcePool;
    @Autowired
    private JedisPool jedisPool;

    @Test
    void contextLoads() {
        Jedis jedis = jedisPool.getResource();
        assert jedis.isConnected();
//        String a = stringRedisTemplate.opsForValue().get("key1");
//        Assert.assertEquals("val1", a);
//        System.out.println(port);
    }

}
