package com.foodapp.foodapp;

import com.foodapp.foodapp.controller.AuthController;
import com.foodapp.foodapp.security.JwtAuthHandler;
import com.foodapp.foodapp.service.AuthService;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {

        AuthService authService = new AuthService();

        HttpServer server =
                HttpServer.create(new InetSocketAddress(8080),0);

        server.createContext("/api/send-otp", new AuthController(authService));
        server.createContext("/api/verify-otp", new AuthController(authService));
        server.createContext("/api/create-user", new AuthController(authService));
        server.createContext("/api/create-admin",new AuthController(authService));
        server.createContext("/api/super-admin-login",new AuthController(authService));
        server.createContext("/api/verify-admin",new JwtAuthHandler(new AuthController(authService)));
        server.createContext("/api/verified-admins",new JwtAuthHandler(new AuthController(authService)));
        server.createContext("/api/unverified-admins",new JwtAuthHandler(new AuthController(authService)));
        server.start();

    }
}