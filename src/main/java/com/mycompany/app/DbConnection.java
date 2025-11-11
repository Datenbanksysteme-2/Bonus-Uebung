package com.mycompany.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DbConnection {

    // lib dotenv um Benutzerdaten verstecken
    private static final Dotenv dotenv = Dotenv.load();
    private final String uri;
    private final String userName;
    private final String password;

    public DbConnection() {
        this.userName = dotenv.get("DB_USER");
        this.password = dotenv.get("DB_PASSWORD");
        this.uri = dotenv.get("DB_URI");
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(this.uri, this.userName, this.password);
        System.out.println("DB connected successfully");
        return conn;
    }
}
