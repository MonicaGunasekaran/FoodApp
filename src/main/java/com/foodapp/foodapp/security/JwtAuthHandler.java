package com.foodapp.foodapp.security;
import java.net.HttpURLConnection;

import com.foodapp.foodapp.utils.JwtUtil;
import com.foodapp.foodapp.utils.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JwtAuthHandler implements HttpHandler {

    private final HttpHandler next;

    public JwtAuthHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) {

        try {
            String token = extractToken(exchange);

            if (!JwtUtil.isValid(token)) {
                Response.error(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            exchange.setAttribute(
                    "userId",
                    JwtUtil.getUserId(token)
            );

            exchange.setAttribute(
                    "role",
                    JwtUtil.getRole(token)
            );

            next.handle(exchange);

        } catch (Exception e) {
            Response.error(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized");
        }
    }

    private String extractToken(HttpExchange exchange) {

        String header =
                exchange.getRequestHeaders()
                        .getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Missing token");

        return header.substring(7);
    }
}
