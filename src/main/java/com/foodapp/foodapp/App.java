package com.foodapp.foodapp;

import com.foodapp.foodapp.controller.AuthController;
import com.foodapp.foodapp.controller.FoodController;
import com.foodapp.foodapp.controller.HotelController;
import com.foodapp.foodapp.security.JwtAuthHandler;
import com.foodapp.foodapp.service.AuthService;
import com.foodapp.foodapp.service.FoodService;
import com.foodapp.foodapp.service.HotelService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {
        AuthService authService = new AuthService();
        FoodService foodService=new FoodService();
        HotelService hotelService=new HotelService();
        HttpServer server =HttpServer.create(new InetSocketAddress(8080),0);
        server.createContext("/api/login", new AuthController(authService));
        server.createContext("/api/verify-otp", new AuthController(authService));
        server.createContext("/api/create-user", new AuthController(authService));
        server.createContext("/api/create-admin",new AuthController(authService));
        server.createContext("/api/super-admin-login",new AuthController(authService));
        server.createContext("/api/create-hotel", new JwtAuthHandler(new HotelController(hotelService)));
        server.createContext("/api/verify-hotel", new JwtAuthHandler(new HotelController(hotelService)));
        server.createContext("/api/verified-hotels", new JwtAuthHandler(new HotelController(hotelService)));
        server.createContext("/api/unverified-hotels", new JwtAuthHandler(new HotelController(hotelService)));
        server.createContext("/api/add-food", new JwtAuthHandler(new FoodController(foodService)));
        server.createContext("/api/my-restaurants",new JwtAuthHandler(new FoodController(foodService)));
        server.createContext("/api/get-foods",new FoodController(foodService));
        server.createContext("/api/place-order",new JwtAuthHandler(new FoodController(foodService)));
        server.start();
    }
}