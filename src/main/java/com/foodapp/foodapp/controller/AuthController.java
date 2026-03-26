package com.foodapp.foodapp.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodapp.records.FoodAppRecords.CreateAdminRequest;
import com.foodapp.foodapp.records.FoodAppRecords.LoginResult;
import com.foodapp.foodapp.records.FoodAppRecords.SuperAdminLoginRequest;
import com.foodapp.foodapp.records.FoodAppRecords.UserSignupVerifyRequest;
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

            case "/api/login" -> {

                if ("POST".equalsIgnoreCase(method)) {

                    Map<String, String> req =
                            mapper.readValue(requestBody, Map.class);

                    String email = req.get("email");
                    String password = req.get("password");

                    try {

                        boolean sent = service.loginWithPassword(email, password);

                        if (sent) {
                            Response.success(exchange,
                                    HttpURLConnection.HTTP_OK,
                                    "Password Verified. Otp Sent to the registered email.",
                                    null);
                        } else {
                            Response.error(exchange,
                                    HttpURLConnection.HTTP_BAD_REQUEST,
                                    "Invalid credentials");
                        }

                    } catch (RuntimeException e) {
                        Response.error(exchange,
                                HttpURLConnection.HTTP_BAD_REQUEST,
                                e.getMessage());
                    }

                } else {
                    Response.error(exchange,
                            HttpURLConnection.HTTP_BAD_METHOD,
                            "Method not allowed");
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
                                "Invalid or expired OTP");
                    }

                } else {
                    Response.error(exchange,
                            HttpURLConnection.HTTP_BAD_METHOD,
                            "Method not allowed");
                }
            }
                
            case "/api/create-user" -> {

                if ("POST".equalsIgnoreCase(method)) {

                    UserSignupVerifyRequest req =
                            mapper.readValue(requestBody, UserSignupVerifyRequest.class);

                    String message = service.createOrVerifyUser(req);

                    Response.success(exchange,
                            HttpURLConnection.HTTP_OK,
                            message,
                            null);

                } else {
                    Response.error(exchange,
                            HttpURLConnection.HTTP_BAD_METHOD,
                            "Method not allowed");
                }
            }
            case "/api/create-admin" -> {

                if ("POST".equalsIgnoreCase(method)) {

                    CreateAdminRequest req =
                            mapper.readValue(requestBody, CreateAdminRequest.class);

                    String message = service.createOrVerifyAdmin(req);

                    Response.success(exchange,
                            HttpURLConnection.HTTP_OK,
                            message,
                            null);

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
                                    "Login success",
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