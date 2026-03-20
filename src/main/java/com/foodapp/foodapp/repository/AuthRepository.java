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
        (id,name,phone,email,location,role_id)
        VALUES (?,?,?,?,?,?)
        """;

    public static final String GET_ADMIN_ROLE_ID = """
        SELECT id
        FROM roles
        WHERE role_name = 'ADMIN'
        """;

    public static final String CREATE_ADMIN = """
        INSERT INTO users
        (id,name,phone,email,location,role_id,is_verified,created_at)
        VALUES (?,?,?,?,?,?,false,NOW())
        """;

    public static final String VERIFY_ADMIN = """
        UPDATE users
        SET is_verified = true
        WHERE id = ?
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
}