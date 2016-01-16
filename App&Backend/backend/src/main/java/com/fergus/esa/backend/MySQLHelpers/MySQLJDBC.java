package com.fergus.esa.backend.MySQLHelpers;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.11.2015
 */
public class MySQLJDBC implements JDBC {
    private static final boolean PRODUCTION = false;

    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE_NAME = "IP";
    private static final String USER = "root";


    public Connection getConnection() {
        try {
            String url;
            if (PRODUCTION) {
                // Load the class that provides the new "jdbc:google:mysql://" prefix.
                Class.forName("com.mysql.jdbc.GoogleDriver");
                url = "jdbc:google:mysql://your-project-id:your-instance-name/" + DATABASE_NAME + "?user=" + USER;
            } else {
                // Local MySQL instance to use during development.
                Class.forName("com.mysql.jdbc.Driver");
                url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?user=" + USER;
                // Alternatively, connect to a Google Cloud SQL instance using:
                // jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
            }

            System.out.println("Opened database successfully");

            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }




        /*
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
        */
    }
}
