package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.SummaryObject;

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
public class SummaryHelper {
    private Connection connection;


    public SummaryHelper(Connection connection) {
        this.connection = connection;
    }


    public List<SummaryObject> getEventSummaries(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `length`, `text`" +
                        " FROM `summaries`" +
                        " WHERE `eventId` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            results = statement.executeQuery();
            List<SummaryObject> summaries = new ArrayList<>();
            while (results.next()) {
                summaries.add(
                        new SummaryObject()
                                .setLength(results.getInt("length"))
                                .setText(results.getString("text"))
                                .setEventId(id)

                );
            }
            return summaries;
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
