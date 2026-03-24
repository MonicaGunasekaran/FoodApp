package com.foodapp.foodapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Response {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void success(
            HttpExchange exchange,
            int status,
            String message,
            Object data
    ) {

        try {

            Map<String, Object> response = new HashMap<>();

            response.put("status", "success");
            response.put("message", message);
            if (data != null) {
                response.put("data", data);
            }

            byte[] json = mapper.writeValueAsBytes(response);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(status, json.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void error(
            HttpExchange exchange,
            int status,
            String message
    ) {

        try {

            Map<String, Object> response = new HashMap<>();

            response.put("status", "success");
            response.put("message", message);

            byte[] json = mapper.writeValueAsBytes(response);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            exchange.sendResponseHeaders(status, json.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}