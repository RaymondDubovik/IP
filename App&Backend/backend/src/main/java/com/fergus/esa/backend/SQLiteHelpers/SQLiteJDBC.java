package com.fergus.esa.backend.SQLiteHelpers;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.11.2015
 */
public class SQLiteJDBC implements JDBC {
    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        return connection;
    }
}
