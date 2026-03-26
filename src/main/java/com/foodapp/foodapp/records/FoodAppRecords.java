package com.foodapp.foodapp.records;

import java.util.List;
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
            String password,
            String phone,
            String location
    ){}

    public record SuperAdminLoginRequest(
            String email,
            String password
    ){}
    public record CreateHotelRequest(
    	    String name,
    	    String location,
    	    String fssaiId
    	) {}
    public record LoginResult(UUID userId, String role) {}
    
    public record AddFoodRequest(
            String name,
            int quantity,
            double price,
            UUID restaurantId
    ) {}
    
    public record PlaceOrderRequest(
    	    UUID restaurantId,
    	    List<OrderItem> items
    	) {}

    	public record OrderItem(
    	    UUID foodId,
    	    int quantity
    	) {}
    	
    	public record UserSignupVerifyRequest(
    	        String email,
    	        String name,
    	        String password,
    	        String phone,
    	        String location,
    	        String otp
    	){}
    	
    	public record CreateAdminRequest(
    		    String name,
    		    String phone,
    		    String email,
    		    String password,
    		    String location,
    		    String otp   
    		){}
}