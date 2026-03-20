package com.foodapp.foodapp.utils;

import com.foodapp.foodapp.config.DbConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil {

    private static final JedisPool pool = DbConfig.getRedisPool();

    public static void storeOtp(String email,String otp){

        try(Jedis jedis = pool.getResource()){

            jedis.setex("otp:"+email,300,otp);
        }
    }

    public static String getOtp(String email){

        try(Jedis jedis = pool.getResource()){

            return jedis.get("otp:"+email);
        }
    }

    public static void deleteOtp(String email){

        try(Jedis jedis = pool.getResource()){

            jedis.del("otp:"+email);
        }
    }
}