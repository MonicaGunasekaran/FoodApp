package com.foodapp.foodapp.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodapp.config.DbConfig;
import com.foodapp.foodapp.records.FoodAppRecords.CreateAdminRequest;
import com.foodapp.foodapp.records.FoodAppRecords.LoginResult;
import com.foodapp.foodapp.records.FoodAppRecords.RegisterUserRequest;
import com.foodapp.foodapp.records.FoodAppRecords.SendOtpRequest;
import com.foodapp.foodapp.records.FoodAppRecords.SuperAdminLoginRequest;
import com.foodapp.foodapp.records.FoodAppRecords.VerifyOtpRequest;
import com.foodapp.foodapp.service.AuthService;
import com.foodapp.foodapp.utils.JwtUtil;
import com.foodapp.foodapp.utils.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AuthController implements HttpHandler {

    private final AuthService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthController(AuthService service){
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {

            String requestBody = "";

            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(exchange.getRequestBody()));

                StringBuilder body = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }

                requestBody = body.toString();
            }

            switch (path) {

                case "/api/send-otp" -> {

                    if ("POST".equalsIgnoreCase(method)) {

                        SendOtpRequest req =
                                mapper.readValue(requestBody, SendOtpRequest.class);

                        boolean sent = service.sendOtp(req.email());

                        if (sent) {
                            Response.success(exchange, HttpURLConnection.HTTP_OK, "OTP sent", null);
                        } else {
                            Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to send OTP");
                        }

                    } else {
                        Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                    }
                }

                case "/api/verify-otp" -> {

                    if ("POST".equalsIgnoreCase(method)) {
                        VerifyOtpRequest req =
                                mapper.readValue(requestBody, VerifyOtpRequest.class);
                        LoginResult result = service.verifyOtp(req);
                        if (result != null) {
                            String token = JwtUtil.generateToken(
                                    result.userId(),
                                    result.role()
                            );
                            Map<String, Object> data = new HashMap<>();
                            data.put("access_token", token);
                            Response.success(exchange,
                                    HttpURLConnection.HTTP_OK,
                                    "Login success",
                                    data);
                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "Invalid OTP, expired OTP, or user not registered");
                        }
                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }
                
                case "/api/create-user" -> {
                    if ("POST".equalsIgnoreCase(method)) {
                        RegisterUserRequest req =
                                mapper.readValue(requestBody, RegisterUserRequest.class);

                        boolean created = service.register(req);

                        if (created) {
                            Response.success(exchange, HttpURLConnection.HTTP_CREATED, "User created", null);
                        } else {
                            Response.error(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "User creation failed");
                        }

                    } else {
                        Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                    }
                }

                case "/api/create-admin" -> {

                    if ("POST".equalsIgnoreCase(method)) {

                        CreateAdminRequest req =
                                mapper.readValue(requestBody, CreateAdminRequest.class);

                        boolean created = service.createAdmin(req);

                        if (created) {
                            Response.success(exchange,
                                    HttpURLConnection.HTTP_CREATED,
                                    "Admin created successfully. Please login via OTP",
                                    null);
                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "Admin creation failed (possible duplicate email)");
                        }

                    } else {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_METHOD,
                                "Method not allowed");
                    }
                }

                case "/api/super-admin-login" -> {

                    if ("POST".equalsIgnoreCase(method)) {

                        SuperAdminLoginRequest req =
                                mapper.readValue(requestBody, SuperAdminLoginRequest.class);

                        LoginResult result = service.superAdminLogin(req);

                        if (result != null) {

                            String token = JwtUtil.generateToken(
                                    result.userId(),
                                    result.role()
                            );

                            Map<String, Object> data = new HashMap<>();
                            data.put("access_token", token);

                            Response.success(exchange,
                                    HttpURLConnection.HTTP_OK,
                                    "Super admin login success",
                                    data);

                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_UNAUTHORIZED,
                                    "Invalid credentials");
                        }

                    } else {
                        Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
                    }
                }
//
//                case "/api/verify-admin" -> {
//
//                    if ("POST".equalsIgnoreCase(method)) {
//
//                        Map<String, String> req =
//                                mapper.readValue(requestBody, Map.class);
//
//                        if (req.containsKey("user_id") && !req.containsKey("otp")) {
//
//                            UUID userId = UUID.fromString(req.get("user_id"));
//
//                            boolean sent = service.sendAdminVerificationOtp(userId);
//
//                            if (sent) {
//                                Response.success(exchange,
//                                        HttpURLConnection.HTTP_OK,
//                                        "OTP sent to admin email",
//                                        null);
//                            } else {
//                                Response.error(exchange,
//                                        HttpURLConnection.HTTP_BAD_REQUEST,
//                                        "Failed to send OTP");
//                            }
//                        }
//
//                        else if (req.containsKey("user_id") && req.containsKey("otp")) {
//
//                            UUID userId = UUID.fromString(req.get("user_id"));
//
//                            boolean verified = service.verifyAdminWithOtp(
//                                    userId,
//                                    req.get("otp")
//                            );
//
//                            if (verified) {
//                                Response.success(exchange,
//                                        HttpURLConnection.HTTP_OK,
//                                        "Admin verified successfully",
//                                        null);
//                            } else {
//                                Response.error(exchange,
//                                        HttpURLConnection.HTTP_BAD_REQUEST,
//                                        "Invalid OTP or already verified");
//                            }
//                        }
//
//                        else {
//                            Response.error(exchange,
//                                    HttpURLConnection.HTTP_BAD_REQUEST,
//                                    "Invalid request");
//                        }
//
//                    } else {
//                        Response.error(exchange,
//                                HttpURLConnection.HTTP_BAD_METHOD,
//                                "Method not allowed");
//                    }
//                }
//                case "/api/unverified-admins" -> {
//
//                    if ("GET".equalsIgnoreCase(method)) {
//
//                        var admins = service.getAdminsByVerification(false);
//
//                        Response.success(exchange,
//                                HttpURLConnection.HTTP_OK,
//                                "Unverified admins",
//                                admins);
//
//                    } else {
//                        Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
//                    }
//                }
//
//                case "/api/verified-admins" -> {
//
//                    if ("GET".equalsIgnoreCase(method)) {
//
//                        var admins = service.getAdminsByVerification(true);
//
//                        Response.success(exchange,
//                                HttpURLConnection.HTTP_OK,
//                                "Verified admins",
//                                admins);
//
//                    } else {
//                        Response.error(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed");
//                    }
//                }

                default -> Response.error(exchange,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        "Endpoint not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Response.error(exchange,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "Internal server error");
        }
    }
}