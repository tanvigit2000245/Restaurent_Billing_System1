package com.restaurent.db;
import java.sql.*;

public class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/restaurent_db";
    static final String USER = "root";
    static final String PASS = "Quity2002@@$$";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try {
            Connection con = getConnection();
            System.out.println("✅ Connected to database!");
            con.close();
        } catch (Exception e) {
            System.out.println("❌ Connection failed");
            e.printStackTrace();
        }
    }
} 