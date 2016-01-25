package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.EventObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 25.01.2016
 */
public class EventHelper {
    private Connection connection;


    public EventHelper(Connection connection) {
        this.connection = connection;
    }


    // TODO: use categories
    public List<EventObject> getEvents(List<Integer> categories, int from, int count) {
        PreparedStatement statement = null;
        ResultSet results = null;

        int minId;

        try {
            String categorySqlPart = "";
            if (categories != null) {
                StringBuilder builder = new StringBuilder(" AND `c`.`id` IN (");

                String prefix = "";
                for (int categoryId : categories) {
                    builder.append(prefix).append('?');
                    prefix = ",";
                }
                builder.append(") ");
                categorySqlPart = builder.toString();
            }

            String query = "SELECT MIN(`mId`) AS `minId` FROM" +
                    " (SELECT `e`.`id` AS `mId`" +
                    " FROM `events` AS `e`" +
                    " JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
                    " JOIN `categories` AS `c` ON `c`.`id`=`ec`.`categoryId`" +
                    " WHERE `e`.`id` < ?" + categorySqlPart +
                    " GROUP BY `e`.`id`" +
                    " ORDER BY `e`.`id` DESC" +
                    " LIMIT ?) AS `eventAlias`";


            System.out.println(query);

            statement = connection.prepareStatement(query);
            int param = 1;
            statement.setInt(param++, from);
            for (int categoryId : categories) {
                statement.setInt(param++, categoryId);
            }
            statement.setInt(param++, count);
            results = statement.executeQuery();
            if (!results.next()) {
                System.out.println("No minId");
                return null;
            }

            minId = results.getInt("minId");
            System.out.println(minId);

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


        List<EventObject> events = new ArrayList<>();
        return events;
    }
}
