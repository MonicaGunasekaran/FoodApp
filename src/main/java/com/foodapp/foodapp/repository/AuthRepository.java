package com.foodapp.foodapp.repository;

public class AuthRepository {

    public static final String FIND_USER_BY_EMAIL = """
        SELECT u.id, r.role_name
        FROM users u
        JOIN roles r ON u.role_id = r.id
        WHERE u.email = ?
        """;

    public static final String FIND_USER_WITH_ROLE = """
        SELECT u.id, u.password, r.role_name
        FROM users u
        JOIN roles r ON u.role_id = r.id
        WHERE u.email = ?
        """;

    public static final String GET_ROLE_ID = """
        SELECT id
        FROM roles
        WHERE role_name = ?
        """;

    public static final String CREATE_USER = """
    	    INSERT INTO users
    	    (id,name,phone,email,password,location,role_id,is_verified)
    	    VALUES (?,?,?,?,?,?,?,false)
    	    """;

    public static final String GET_ADMIN_ROLE_ID = """
        SELECT id
        FROM roles
        WHERE role_name = 'ADMIN'
        """;

    public static final String CREATE_ADMIN = """
    	    INSERT INTO users
    	    (id,name,phone,email,password,location,role_id,is_verified,created_at)
    	    VALUES (?,?,?,?,?,?,?,false,NOW())
    	    """;
    public static final String VERIFY_ADMIN = """
        UPDATE users
        SET is_verified = true,updated_at=now()
        WHERE id = ?
        """;

    public static final String VERIFY_RESTAURANT = """
        UPDATE restaurants
        SET is_verified = true,updated_at=now()
        WHERE owner_id = ?
        """;

    public static final String GET_ADMINS_BY_VERIFICATION = """
        SELECT u.id,
               u.name,
               u.email,
               u.phone,
               u.location,
               u.created_at
        FROM users u
        JOIN roles r ON u.role_id = r.id
        WHERE r.role_name = 'ADMIN'
        AND u.is_verified = ?
        ORDER BY u.created_at DESC
        """;
    public static final String GET_EMAIL_BY_USER_ID = """
    	    SELECT email
    	    FROM users
    	    WHERE id = ?
    	    """;

    	public static final String GET_USER_VERIFICATION_STATUS = """
    	    SELECT is_verified FROM users WHERE id = ?
    	""";
    
    public static final String GET_USER_ID_BY_EMAIL = """
    	    SELECT id, is_verified FROM users WHERE email = ?
    	""";
    public static final String CREATE_RESTAURANT = """
        INSERT INTO restaurants
        (id, name, fssai_id, owner_id,location, is_verified, created_at)
        VALUES (?,?,?,?,?,false,NOW())
        """;
}