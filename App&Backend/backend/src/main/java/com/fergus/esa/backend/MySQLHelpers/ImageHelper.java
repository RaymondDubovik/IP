package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.ImageObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.01.2016
 */
public class ImageHelper {
    private Connection connection;


    public ImageHelper(Connection connection) {
        this.connection = connection;
    }


    public List<ImageObject> getEventImages(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `url`" +
                        " FROM `images`" +
                        " WHERE `eventId` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            results = statement.executeQuery();
            List<ImageObject> images = new ArrayList<>();
            while (results.next()) {
                images.add(new ImageObject().setEventId(id).setUrl(results.getString("url")));
            }
            return images;
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