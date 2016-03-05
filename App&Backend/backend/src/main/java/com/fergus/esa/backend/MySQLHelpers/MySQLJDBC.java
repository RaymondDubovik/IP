package com.fergus.esa.backend.MySQLHelpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.11.2015
 */
public class MySQLJDBC implements JDBC {
    private static final boolean PRODUCTION = false;
	private static final String MYSQL_PROPERTIES_PATH = "WEB-INF/mysql.config.properties";

	private static String HOST;
    private static int PORT;
    private static String DATABASE_NAME;
    private static String USER;
	private static String PASSWORD;
	private static boolean configRead = false;


	private static void readConfigs() {
		if (configRead) { // if config is already read, skip
			return;
		}

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(MYSQL_PROPERTIES_PATH));
			HOST = properties.getProperty("host");
			PORT = Integer.parseInt(properties.getProperty("port"));
			DATABASE_NAME = properties.getProperty("databaseName");
			USER = properties.getProperty("user");
			PASSWORD = properties.getProperty("password");

			configRead = true;
		} catch (IOException e) {
			System.err.println("Could not read property file!");
			e.printStackTrace();
			System.exit(-1);
		} catch (NumberFormatException e) {
			System.err.println("Could not parse integer in the property file!");
			System.exit(-1);
		}
	}


    public Connection getConnection() {
        readConfigs();

		try {
            String url;
            if (PRODUCTION) {
                // Load the class that provides the new "jdbc:google:mysql://" prefix.
                Class.forName("com.mysql.jdbc.GoogleDriver");
                url = "jdbc:google:mysql://your-project-id:your-instance-name/" + DATABASE_NAME + "?user=" + USER + "&password=" + PASSWORD;
            } else {
                // Local MySQL instance to use during development.
                Class.forName("com.mysql.jdbc.Driver");
                url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?user=" + USER + "&password=" + PASSWORD;
                // Alternatively, connect to a Google Cloud SQL instance using:
                // jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
            }

            System.out.println("Opened database successfully");

            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
