package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.UserObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 15.01.2016
 */
public class UserHelper {
    private Connection connection;


    public UserHelper(Connection connection) {
        this.connection = connection;
    }


    public UserObject getUser(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `id`, `gcmToken`" +
                    " FROM `users`" +
                    " WHERE `id` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            results = statement.executeQuery();
            if (results.isBeforeFirst()) {
                return new UserObject().setId(results.getInt("id")).setGcmToken(results.getString("gcmToken"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlEx) {} // ignore

                results = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlEx) {} // ignore

                statement = null;
            }
        }

        return null;
    }


    public UserObject createUser(String gcmToken) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "INSERT INTO `users` (`gcmToken`)" +
                        " VALUES(?)";

        try {
            statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, gcmToken);
            statement.executeUpdate();

            results = statement.getGeneratedKeys();
            if (results.next()) {
                return getUser(results.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlEx) {} // ignore

                results = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlEx) {} // ignore

                statement = null;
            }
        }

        return null;
    }
}
