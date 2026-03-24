package com.foodapp.foodapp.controller;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodapp.records.FoodAppRecords.AddFoodRequest;
import com.foodapp.foodapp.records.FoodAppRecords.PlaceOrderRequest;
import com.foodapp.foodapp.service.FoodService;
import com.foodapp.foodapp.utils.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FoodController implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final FoodService service;

    public FoodController(FoodService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpExchange exchange) {
        String role = (String) exchange.getAttribute("role");
        return "ADMIN".equals(role);
    }

    @Override
    public void handle(HttpExchange exchange) {

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());

            switch (path) {

                case "/api/add-food" -> {

                    if ("POST".equalsIgnoreCase(method)) {

                        if (!isAdmin(exchange)) {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_FORBIDDEN,
                                    "Only admins can add food");
                            return;
                        }

                        AddFoodRequest req =
                                mapper.readValue(requestBody, AddFoodRequest.class);

                        UUID adminId = (UUID) exchange.getAttribute("userId");

                        boolean added = service.addFood(req, adminId);

                        if (added) {
                            Response.success(exchange,
                                    HttpURLConnection.HTTP_OK,
                                    "Food added successfully",
                                    null);
                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "Failed to add food");
                        }

                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }
               
                case "/api/get-foods" -> {
                	
                    if ("POST".equalsIgnoreCase(method)) {
                    	
                        Map<String, String> req =
                                mapper.readValue(requestBody, Map.class);
                        
                        if (!req.containsKey("restaurant_id")) {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "restaurant_id is required");
                            return;
                        }

                        UUID restaurantId =
                                UUID.fromString(req.get("restaurant_id"));

                        List<Map<String, Object>> foods =
                                service.getFoodsByRestaurant(restaurantId);

                        Response.success(exchange,
                                HttpURLConnection.HTTP_OK,
                                "Foods fetched successfully",
                                foods);

                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }

                case "/api/my-restaurants" -> {
                    if ("GET".equalsIgnoreCase(method)) {
                        if (!isAdmin(exchange)) {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_FORBIDDEN,
                                    "Only admins can view restaurants");
                            return;
                        }

                        UUID adminId = (UUID) exchange.getAttribute("userId");

                        List<Map<String, Object>> restaurants =
                                service.getRestaurantsByAdmin(adminId);

                        Response.success(exchange,
                                HttpURLConnection.HTTP_OK,
                                "Restaurants fetched",
                                restaurants);

                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }
                case "/api/place-order" -> {

                    if ("POST".equalsIgnoreCase(method)) {

                        PlaceOrderRequest req =
                                mapper.readValue(requestBody, PlaceOrderRequest.class);

                        boolean success = service.placeOrder(
                                req.restaurantId(),
                                req.items()
                        );

                        if (success) {
                            Response.success(exchange,
                                    HttpURLConnection.HTTP_OK,
                                    "Order placed successfully",
                                    null);
                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "Order failed");
                        }

                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }
                default -> Response.error(exchange,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        "Route not found");
            }

        } catch (RuntimeException e) {
            Response.error(exchange,
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            Response.error(exchange,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "Something went wrong");
        }
    }
    
}