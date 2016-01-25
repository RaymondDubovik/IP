package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.CategoryObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 16.01.2016
 */
public class CategoryHelper {
    private Connection connection;


    public CategoryHelper(Connection conn) {
        this.connection = conn;
    }


    public List<CategoryObject> getCategories() {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `id`, `name`" +
                        " FROM `categories`" +
                        " ORDER BY `name`";

        try {
            statement = connection.prepareStatement(query);

            ArrayList<CategoryObject> categories = new ArrayList<>();
            results = statement.executeQuery();
            while (results.next()) {
                categories.add(new CategoryObject()
                        .setId(results.getInt("id"))
                        .setName(results.getString("name"))
                );
            }

            return categories;
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
