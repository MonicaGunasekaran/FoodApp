package com.foodapp.foodapp.records;

import java.util.UUID;

public class FoodAppRecords {

    public record User(
            UUID id,
            String name,
            String phone,
            String email,
            String location,
            String role
    ){}

    public record SendOtpRequest(String email){}

    public record VerifyOtpRequest(
            String email,
            String otp
    ){}

    public record RegisterUserRequest(
            String email,
            String name,
            String phone,
            String location
    ){}
    
    public record CreateAdminRequest(
            String name,
            String phone,
            String email,
            String location
    ){}
    
    public record SuperAdminLoginRequest(
            String email,
            String password
    ){}
    
    public record LoginResult(UUID userId, String role) {}
}