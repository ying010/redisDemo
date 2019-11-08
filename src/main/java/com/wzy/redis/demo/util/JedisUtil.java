package com.wzy.redis.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Package com.wzy.redis.demo.util
 * @ClassName JedisUtil
 * @Description TODO
 * @Author W.Z.King
 * @Date 2019/11/7 11:08
 */
public class JedisUtil {
    @Autowired
    private static JedisPool jedisPool;

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
