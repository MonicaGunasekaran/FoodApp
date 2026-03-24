package com.foodapp.foodapp.repository;

public class HotelRepository {

    public static final String CREATE_HOTEL = """
        INSERT INTO restaurants
        (id, name, location, fssai_id, owner_id, is_verified, created_at)
        VALUES (?, ?, ?, ?, ?, false, NOW())
        """;

    public static final String CHECK_FSSAI_EXISTS = """
        SELECT 1 FROM restaurants WHERE fssai_id = ?
        """;

    public static final String GET_OWNER_EMAIL_BY_RESTAURANT = """
        SELECT u.email
        FROM restaurants r
        JOIN users u ON r.owner_id = u.id
        WHERE r.id = ?
        """;

    public static final String VERIFY_HOTEL = """
        UPDATE restaurants
        SET is_verified = true, updated_at = NOW()
        WHERE id = ? AND is_verified = false
        """;
    public static final String GET_RESTAURANT_OWNER = """
    	    SELECT r.owner_id, u.email
    	    FROM restaurants r
    	    JOIN users u ON r.owner_id = u.id
    	    WHERE r.id = ?
    	    """;

    public static final String GET_HOTELS_BY_VERIFICATION = """
        SELECT 
            r.id,
            r.name,
            r.location,
            r.fssai_id,
            r.owner_id,
            r.created_at
        FROM restaurants r
        WHERE r.is_verified = ?
        ORDER BY r.created_at DESC
        """;
}