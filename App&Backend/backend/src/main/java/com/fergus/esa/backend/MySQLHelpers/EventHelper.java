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

    // NOTE: it is assumed, that in the database, record with smaller ID will have a smaller timestamp

    public EventHelper(Connection connection) {
        this.connection = connection;
    }


    // TODO: use categories
    public List<EventObject> getEvents(List<Integer> categories, int from, int count) {
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            String categorySqlPart = "";
            if (categories != null && categories.size() > 0) {
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

            statement = connection.prepareStatement(query);
            int param = 1;
            statement.setInt(param++, from);
            if (categories != null) {
				for (int categoryId : categories) {
					statement.setInt(param++, categoryId);
				}
			}
            statement.setInt(param++, count);
            results = statement.executeQuery();
            if (!results.next()) {
                System.out.println("No minId");
                return null;
            }

            return getEventInterval(results.getInt("minId"), from, categories, categorySqlPart);
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


    private List<EventObject> getEventInterval(int minId, int maxId, List<Integer> categories, String categorySqlPart) {
		if (minId == 0) {
			return null;
		}

		PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT DISTINCT `e`.`id`, `e`.`timestamp`, `e`.`heading`, `e`.`mainImageUrl`" +
                        " FROM `events` AS `e`" +
						" JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
						" JOIN `categories` AS `c` ON `c`.`id`=`ec`.`categoryId`" +
						" WHERE `e`.`id` >= ? AND `e`.`id` < ?" + categorySqlPart +
                        " ORDER BY `e`.`id` DESC";

        try {
            statement = connection.prepareStatement(query);
			int param = 1;
			statement.setInt(param++, minId);
            statement.setInt(param++, maxId);
			if (categories != null) {
				for (int categoryId : categories) {
					statement.setInt(param++, categoryId);
				}
			}

            List<EventObject> events = new ArrayList<>();
            results = statement.executeQuery();
            int i = 0;
			while (results.next()) {
				events.add(new EventObject()
                        .setId(results.getInt("id"))
                        .setTimestamp(results.getDate("timestamp"))
                        .setHeading(results.getString("heading"))
						.setImageUrl(results.getString("mainImageUrl"))
                );
            }

            return events;
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
