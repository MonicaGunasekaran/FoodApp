package com.foodapp.foodapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

import com.foodapp.foodapp.config.DbConfig;
import com.foodapp.foodapp.records.FoodAppRecords.CreateHotelRequest;
import com.foodapp.foodapp.repository.HotelRepository;
import com.foodapp.foodapp.utils.MailUtil;
import com.foodapp.foodapp.utils.RedisUtil;

public class HotelService {

    public boolean createHotel(UUID ownerId, CreateHotelRequest req) {

        if (ownerId == null || req == null) return false;

        try (Connection con = DbConfig.getConnection()) {

            try (PreparedStatement ps =
                    con.prepareStatement(HotelRepository.CHECK_FSSAI_EXISTS)) {

                ps.setString(1, req.fssaiId());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) return false;
            }

            try (PreparedStatement ps =
                    con.prepareStatement(HotelRepository.CREATE_HOTEL)) {

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setString(3, req.location());
                ps.setString(4, req.fssaiId());
                ps.setObject(5, ownerId);

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendHotelVerificationOtp(UUID restaurantId) {

        try (Connection con = DbConfig.getConnection()) {

            String email = null;

            try (PreparedStatement ps =
                    con.prepareStatement(HotelRepository.GET_OWNER_EMAIL_BY_RESTAURANT)) {

                ps.setObject(1, restaurantId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    email = rs.getString("email");
                } else {
                    return false;
                }
            }

            if (email == null || email.isBlank()) return false;

            String otp = String.valueOf(
                    ThreadLocalRandom.current().nextInt(100000, 1000000)
            );

            String key = "hotel_verify:" + email + ":" + restaurantId;

            RedisUtil.storeOtp(key, otp);
            MailUtil.sendOtp(email, otp);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean verifyHotelWithOtp(UUID restaurantId, String otp) {

        try (Connection con = DbConfig.getConnection()) {

            String email = null;

            try (PreparedStatement ps =
                    con.prepareStatement(HotelRepository.GET_OWNER_EMAIL_BY_RESTAURANT)) {

                ps.setObject(1, restaurantId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    email = rs.getString("email");
                } else {
                    return false;
                }
            }

            if (email == null || email.isBlank()) return false;

            String key = "hotel_verify:" + email + ":" + restaurantId;

            String storedOtp = RedisUtil.getOtp(key);

            if (storedOtp == null || !storedOtp.equals(otp)) {
                return false;
            }

            try (PreparedStatement ps =
                    con.prepareStatement(HotelRepository.VERIFY_HOTEL)) {

                ps.setObject(1, restaurantId);

                int updated = ps.executeUpdate();

                if (updated > 0) {
                    RedisUtil.deleteOtp(key);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Map<String, Object>> getHotelsByVerification(boolean verified) {

        List<Map<String, Object>> hotels = new ArrayList<>();

        try (Connection con = DbConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(HotelRepository.GET_HOTELS_BY_VERIFICATION)) {

            ps.setBoolean(1, verified);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getObject("id"));
                row.put("name", rs.getString("name"));
                row.put("location", rs.getString("location"));
                row.put("fssai_id", rs.getString("fssai_id"));
                row.put("owner_id", rs.getObject("owner_id"));
                row.put("created_at", rs.getTimestamp("created_at"));

                hotels.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotels;
    }
}