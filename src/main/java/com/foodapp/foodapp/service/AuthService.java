package com.foodapp.foodapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.mindrot.jbcrypt.BCrypt;

import com.foodapp.foodapp.config.DbConfig;
import com.foodapp.foodapp.records.FoodAppRecords.CreateAdminRequest;
import com.foodapp.foodapp.records.FoodAppRecords.LoginResult;
import com.foodapp.foodapp.records.FoodAppRecords.RegisterUserRequest;
import com.foodapp.foodapp.records.FoodAppRecords.SuperAdminLoginRequest;
import com.foodapp.foodapp.records.FoodAppRecords.VerifyOtpRequest;
import com.foodapp.foodapp.repository.AuthRepository;
import com.foodapp.foodapp.utils.MailUtil;
import com.foodapp.foodapp.utils.RedisUtil;

public class AuthService {

    public boolean sendOtp(String email){
        if(email == null || email.isBlank()){
            return false;
        }
        try{
            String otp = String.valueOf(
                    ThreadLocalRandom.current().nextInt(100000,1000000)
            );

            RedisUtil.storeOtp(email, otp);
            MailUtil.sendOtp(email, otp);

            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public LoginResult verifyOtp(VerifyOtpRequest req) {
        try {

            if (req == null || req.email() == null || req.otp() == null) {
                return null;
            }
            String storedOtp = RedisUtil.getOtp(req.email());
            if (storedOtp == null) {
                return null;
            }
            if (!storedOtp.equals(req.otp())) {
                RedisUtil.deleteOtp(req.email()); 
                return null;
            }
            try (Connection connection = DbConfig.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         AuthRepository.FIND_USER_BY_EMAIL)) {
                ps.setString(1, req.email());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID userId = rs.getObject("id", UUID.class);
                        String role = rs.getString("role_name");
                        RedisUtil.deleteOtp(req.email());
                        return new LoginResult(userId, role);
                    }
                }
            }

            RedisUtil.deleteOtp(req.email());
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createAdmin(CreateAdminRequest req){
        if(req == null || req.email() == null || req.email().isBlank()){
            return false;
        }
        try(Connection connection = DbConfig.getConnection()){
            UUID roleId;
            try(PreparedStatement ps =
                    connection.prepareStatement(AuthRepository.GET_ADMIN_ROLE_ID);
                ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    throw new RuntimeException("ADMIN role not found");
                }
                roleId = rs.getObject("id", UUID.class);
            }
            try(PreparedStatement ps =
                    connection.prepareStatement(AuthRepository.CREATE_ADMIN)){
                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setString(3, req.phone());
                ps.setString(4, req.email());
                ps.setString(5, req.location());
                ps.setObject(6, roleId);
                return ps.executeUpdate() > 0;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(RegisterUserRequest req){
        if(req == null || req.email() == null || req.email().isBlank()){
            return false;
        }
        try(Connection connection = DbConfig.getConnection()){
            UUID roleId = null;
            try(PreparedStatement ps =
                    connection.prepareStatement(AuthRepository.GET_ROLE_ID)){
                ps.setString(1, "USER");
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        roleId = rs.getObject("id", UUID.class);
                    }
                }
            }
            if(roleId == null){
                return false;
            }
            try(PreparedStatement ps =
                    connection.prepareStatement(AuthRepository.CREATE_USER)){
                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, req.name());
                ps.setString(3, req.phone());
                ps.setString(4, req.email());
                ps.setString(5, req.location());
                ps.setObject(6, roleId);
                return ps.executeUpdate() > 0;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public LoginResult superAdminLogin(SuperAdminLoginRequest req){
        try(Connection connection = DbConfig.getConnection()){
            try(PreparedStatement ps =
                    connection.prepareStatement(
                            AuthRepository.FIND_USER_WITH_ROLE)){
                ps.setString(1, req.email());
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()){
                        UUID userId = rs.getObject("id", UUID.class);
                        String role = rs.getString("role_name");
                        String hashedPassword = rs.getString("password");
                        if(!"SUPER_ADMIN".equals(role)){
                            return null;
                        }
                        if(hashedPassword == null || hashedPassword.isBlank()){
                            return null;
                        }
                        boolean valid = BCrypt.checkpw(
                                req.password(),
                                hashedPassword
                        );
                        if(valid){
                            return new LoginResult(userId, role);
                        }
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifyAdmin(UUID userId) throws Exception {

        try(Connection con = DbConfig.getConnection();
            PreparedStatement ps =
                con.prepareStatement(AuthRepository.VERIFY_ADMIN)) {
            ps.setObject(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String,Object>> getAdminsByVerification(boolean verified) throws Exception {

        List<Map<String,Object>> admins = new ArrayList<>();
        try(Connection con = DbConfig.getConnection();
            PreparedStatement ps =
                con.prepareStatement(AuthRepository.GET_ADMINS_BY_VERIFICATION)) {
            ps.setBoolean(1, verified);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String,Object> row = new HashMap<>();
                row.put("id", rs.getObject("id"));
                row.put("name", rs.getString("name"));
                admins.add(row);
            }
        }
        return admins;
    }
}