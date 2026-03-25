package com.foodapp.foodapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.foodapp.foodapp.config.DbConfig;
import com.foodapp.foodapp.records.FoodAppRecords.CreateAdminRequest;
import com.foodapp.foodapp.records.FoodAppRecords.LoginResult;
import com.foodapp.foodapp.records.FoodAppRecords.RegisterUserRequest;
import com.foodapp.foodapp.records.FoodAppRecords.SuperAdminLoginRequest;
import com.foodapp.foodapp.records.FoodAppRecords.VerifyOtpRequest;
import com.foodapp.foodapp.repository.AuthRepository;
import com.foodapp.foodapp.utils.MailUtil;
import com.foodapp.foodapp.utils.RedisUtil;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    public boolean sendOtp(String email) {
        if (email == null || email.isBlank()) return false;
        try {
            String otp = String.valueOf(
                    ThreadLocalRandom.current().nextInt(100000, 1000000)
            );

            RedisUtil.storeOtp(email, otp);
            MailUtil.sendOtp(email, otp);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public LoginResult verifyOtp(VerifyOtpRequest req) {

        if (req == null || req.email() == null || req.otp() == null) {
            return null;
        }

        try {

            String key = "otp:login:" + req.email();

            String storedOtp = RedisUtil.getValue(key);

            if (storedOtp == null) return null;

            if (!storedOtp.equals(req.otp())) return null;

            try (Connection con = DbConfig.getConnection();
                 PreparedStatement ps =
                         con.prepareStatement(AuthRepository.FIND_USER_BY_EMAIL)) {

                ps.setString(1, req.email());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    UUID userId = rs.getObject("id", UUID.class);
                    String role = rs.getString("role_name");

                    RedisUtil.deleteValue(key);

                    return new LoginResult(userId, role);
                }
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean loginWithPassword(String email, String password) {

        if (email == null || password == null) return false;

        try (Connection con = DbConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(AuthRepository.FIND_USER_WITH_ROLE)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return false;

            String hashedPassword = rs.getString("password");

            if (hashedPassword == null || !BCrypt.checkpw(password, hashedPassword)) {
                return false;
            }

            String otp = String.valueOf(
                    ThreadLocalRandom.current().nextInt(100000, 1000000)
            );

            String key = "otp:login:" + email;

            RedisUtil.setValue(key, otp, 300);

            MailUtil.sendOtp(email, otp);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createAdmin(CreateAdminRequest req) {

        if (req == null || req.email() == null || req.password() == null) {
            return false;
        }

        try (Connection con = DbConfig.getConnection()) {

            UUID roleId = null;

            try (PreparedStatement ps =
                    con.prepareStatement(AuthRepository.GET_ADMIN_ROLE_ID);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    roleId = rs.getObject("id", UUID.class);
                }
            }

            if (roleId == null) return false;

            String hashedPassword = BCrypt.hashpw(req.password(), BCrypt.gensalt());

            try (PreparedStatement ps =
                    con.prepareStatement(AuthRepository.CREATE_ADMIN)) {

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setString(3, req.phone());
                ps.setString(4, req.email());
                ps.setString(5, hashedPassword);
                ps.setString(6, req.location());
                ps.setObject(7, roleId);

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(RegisterUserRequest req) {

        if (req == null || req.email() == null || req.password() == null) {
            return false;
        }

        try (Connection con = DbConfig.getConnection()) {

            UUID roleId = null;

            try (PreparedStatement ps =
                    con.prepareStatement(AuthRepository.GET_ROLE_ID)) {

                ps.setString(1, "USER");

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    roleId = rs.getObject("id", UUID.class);
                }
            }

            if (roleId == null) return false;

            String hashedPassword = BCrypt.hashpw(req.password(), BCrypt.gensalt());

            try (PreparedStatement ps =
                    con.prepareStatement(AuthRepository.CREATE_USER)) {

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setString(3, req.phone());
                ps.setString(4, req.email());
                ps.setString(5, hashedPassword);
                ps.setString(6, req.location());
                ps.setObject(7, roleId);

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public LoginResult superAdminLogin(SuperAdminLoginRequest req) {

        try (Connection con = DbConfig.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(AuthRepository.FIND_USER_WITH_ROLE)) {

            ps.setString(1, req.email());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                UUID userId = rs.getObject("id", UUID.class);
                String role = rs.getString("role_name");
                String hashedPassword = rs.getString("password");

                if (!"SUPER_ADMIN".equals(role)) return null;
                if (hashedPassword == null) return null;

                boolean valid = BCrypt.checkpw(req.password(), hashedPassword);

                if (valid) {
                    return new LoginResult(userId, role);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}