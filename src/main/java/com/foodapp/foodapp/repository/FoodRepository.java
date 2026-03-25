package com.foodapp.foodapp.repository;

public class FoodRepository {
	 public static final String VALIDATE_RESTAURANT_OWNER = """
		        SELECT id FROM restaurants
		        WHERE id = ? AND owner_id = ? and is_verified= true
		    """;

		    public static final String CREATE_FOOD = """
		        INSERT INTO foods
		        (id, name, quantity, restaurant_id, price, created_at, updated_at)
		        VALUES (?,?,?,?,?,NOW(),NOW())
		    """;
		    
		    public static final String GET_RESTAURANTS_BY_ADMIN = """
		    	    SELECT id, name, location
		    	    FROM restaurants
		    	    WHERE owner_id = ?
		    	""";
		    
		    public static final String GET_ALL_FOODS_BY_RESTAURANT_ID="""
		    		SELECT id, name, quantity, price from foods where restaurant_id=?
		    		""";
		    
		    public static final String ORDER_FOOD= """
		    	    UPDATE foods
		    	    SET quantity = quantity - ?
		    	    WHERE restaurant_id=? AND id = ? AND quantity >= ?
		    	    """;
}
