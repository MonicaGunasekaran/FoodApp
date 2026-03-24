package com.foodapp.foodapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodapp.config.DbConfig;
import com.foodapp.foodapp.records.FoodAppRecords.AddFoodRequest;
import com.foodapp.foodapp.records.FoodAppRecords.OrderItem;
import com.foodapp.foodapp.repository.FoodRepository;
import com.foodapp.foodapp.utils.RedisUtil;

public class FoodService {
	
    public boolean addFood(AddFoodRequest req, UUID adminId) {

        if (req == null) {
            throw new RuntimeException("Invalid request");
        }

        if (req.quantity() <= 0) {
            throw new RuntimeException("Invalid quantity of food");
        }

        if (req.price() <= 0) {
            throw new RuntimeException("Invalid price");
        }

        if (req.name() == null || req.name().isBlank()) {
            throw new RuntimeException("Food name cannot be empty");
        }

        try (Connection con = DbConfig.getConnection()) {

            try (PreparedStatement ps =
                    con.prepareStatement(FoodRepository.VALIDATE_RESTAURANT_OWNER)) {

                ps.setObject(1, req.restaurantId());
                ps.setObject(2, adminId);

                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Unauthorized restaurant access");
                }
            }

            try (PreparedStatement ps =
                    con.prepareStatement(FoodRepository.CREATE_FOOD)) {

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setInt(3, req.quantity());
                ps.setObject(4, req.restaurantId());
                ps.setDouble(5, req.price());

                boolean inserted = ps.executeUpdate() > 0;

                if (inserted) {
                    String key = "foods:" + req.restaurantId();
                    RedisUtil.deleteValue(key);
                }

                return inserted;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    public List<Map<String, Object>> getRestaurantsByAdmin(UUID adminId) {

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection con = DbConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(FoodRepository.GET_RESTAURANTS_BY_ADMIN)) {

            ps.setObject(1, adminId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getObject("id"));
                row.put("name", rs.getString("name"));
                row.put("location", rs.getString("location"));
                list.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public List<Map<String, Object>> getFoodsByRestaurant(UUID restaurantId) {

        List<Map<String, Object>> foods = new ArrayList<>();

        if (restaurantId == null) return foods;

        String key = "foods:" + restaurantId;

        try {
            String cached = RedisUtil.getValue(key);

            if (cached != null) {
                return new ObjectMapper().readValue(
                        cached,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection con = DbConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(FoodRepository.GET_ALL_FOODS_BY_RESTAURANT_ID)) {

            ps.setObject(1, restaurantId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getObject("id"));
                row.put("name", rs.getString("name"));
                row.put("quantity", rs.getInt("quantity"));
                row.put("price", rs.getDouble("price"));

                foods.add(row);
            }

            try {
                String json = new ObjectMapper().writeValueAsString(foods);
                RedisUtil.setValue(key, json, 300);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return foods;
    }
    
    public boolean placeOrder(UUID restaurantId, List<OrderItem> items) {

        if (restaurantId == null || items == null || items.isEmpty()) {
            throw new RuntimeException("Invalid order");
        }

        try (Connection con = DbConfig.getConnection()) {

            con.setAutoCommit(false);

            for (OrderItem item : items) {

                try (PreparedStatement ps =
                        con.prepareStatement(FoodRepository.UPDATE_FOOD_QUANTITY)) {

                    ps.setInt(1, item.quantity());
                    ps.setObject(2, item.foodId());
                    ps.setInt(3, item.quantity());

                    int updated = ps.executeUpdate();

                    if (updated == 0) {
                        con.rollback();
                        throw new RuntimeException("Insufficient quantity for food: " + item.foodId());
                    }
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}