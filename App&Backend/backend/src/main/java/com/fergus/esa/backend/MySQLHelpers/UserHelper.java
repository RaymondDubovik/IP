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
    private Connection conn;


    public UserHelper(Connection conn) {
        this.conn = conn;
    }


    public UserObject getUser(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `id`, `gcmToken`" +
                    " FROM `users`" +
                    " WHERE `id` = ?";

        try {
            statement = conn.prepareStatement(query);

            try {
                //looking for dogs with the same parents
                statement.setInt(1, id);
                results = statement.executeQuery();
                while (results.next()) {
                    return new UserObject().setId(results.getInt("id")).setGcmToken(results.getString("gcmToken"));
                }
            } catch(SQLException e) {
                // TODO: failed to execute query QUERYNAME
            }
        } catch (SQLException e) {
            // TODO: failed to compile statement STATEMENTNAME
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlEx) { } // ignore

                results = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlEx) { } // ignore

                statement = null;
            }
        }

        return null;
    }
}
