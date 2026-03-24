package com.foodapp.foodapp.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodapp.records.FoodAppRecords.CreateHotelRequest;
import com.foodapp.foodapp.service.HotelService;
import com.foodapp.foodapp.utils.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HotelController implements HttpHandler {

    private final HotelService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public HotelController(HotelService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) {

        try {

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            UUID userId = (UUID) exchange.getAttribute("userId");
            String role = (String) exchange.getAttribute("role");

            if (userId == null || role == null) {
                Response.error(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
                return;
            }

            if ("/api/create-hotel".equals(path)) {

                if (!"ADMIN".equals(role)) {
                    Response.error(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
                    return;
                }

                if ("POST".equalsIgnoreCase(method)) {

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(exchange.getRequestBody()));

                    StringBuilder body = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    CreateHotelRequest req =
                            mapper.readValue(body.toString(), CreateHotelRequest.class);

                    boolean created = service.createHotel(userId, req);

                    if (created) {
                        Response.success(exchange, HttpURLConnection.HTTP_CREATED, "Hotel created", null);
                    } else {
                        Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "FSSAI already exists");
                    }

                } else {
                    Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                }
            }

            else if ("/api/verify-hotel".equals(path)) {

                if (!"SUPER_ADMIN".equals(role)) {
                    Response.error(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
                    return;
                }

                if ("POST".equalsIgnoreCase(method)) {

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(exchange.getRequestBody()));

                    StringBuilder body = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    Map<String, String> req =
                            mapper.readValue(body.toString(), Map.class);

                    if (req.containsKey("restaurant_id") && !req.containsKey("otp")) {

                        UUID restaurantId =
                                UUID.fromString(req.get("restaurant_id"));

                        boolean sent =
                                service.sendHotelVerificationOtp(restaurantId);

                        if (sent) {
                            Response.success(exchange, HttpURLConnection.HTTP_OK, "OTP sent", null);
                        } else {
                            Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to send OTP");
                        }
                    }

                    else if (req.containsKey("restaurant_id") && req.containsKey("otp")) {

                        UUID restaurantId =
                                UUID.fromString(req.get("restaurant_id"));

                        boolean verified =
                                service.verifyHotelWithOtp(
                                        restaurantId,
                                        req.get("otp")
                                );

                        if (verified) {
                            Response.success(exchange, HttpURLConnection.HTTP_OK, "Hotel verified", null);
                        } else {
                            Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid OTP");
                        }
                    }

                    else {
                        Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid request");
                    }

                } else {
                    Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                }
            }

            else if ("/api/unverified-hotels".equals(path)) {

                if (!"SUPER_ADMIN".equals(role)) {
                    Response.error(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
                    return;
                }

                if ("GET".equalsIgnoreCase(method)) {

                    var hotels = service.getHotelsByVerification(false);

                    Response.success(exchange,
                            HttpURLConnection.HTTP_OK,
                            "Unverified hotels fetched successfully",
                            hotels);

                } else {
                    Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                }
            }

            else if ("/api/verified-hotels".equals(path)) {

                if (!"SUPER_ADMIN".equals(role)) {
                    Response.error(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Forbidden");
                    return;
                }

                if ("GET".equalsIgnoreCase(method)) {

                    var hotels = service.getHotelsByVerification(true);

                    Response.success(exchange,
                            HttpURLConnection.HTTP_OK,
                            "Verified hotels fetched successfully",
                            hotels);

                } else {
                    Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                }
            }

            else {
                Response.error(exchange, HttpURLConnection.HTTP_NOT_FOUND, "Not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Response.error(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal server error");
        }
    }
}