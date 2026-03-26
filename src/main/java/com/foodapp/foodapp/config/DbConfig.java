package com.foodapp.foodapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import redis.clients.jedis.JedisPool;

public class DbConfig {
// new comment added
    public static final String DB_URL =
            "jdbc:postgresql://localhost:5432/foodapp_db";

    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "password";

    public static Connection getConnection() throws Exception {

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }
    
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private static final JedisPool jedisPool =
            new JedisPool(REDIS_HOST, REDIS_PORT);

    public static JedisPool getRedisPool(){
        return jedisPool;
    }
}