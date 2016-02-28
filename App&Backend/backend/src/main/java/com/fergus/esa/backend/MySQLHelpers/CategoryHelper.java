package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.CategoryObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

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

            List<CategoryObject> categories = new ArrayList<>();
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
                } catch (SQLException ignore) {}

                results = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {}

                statement = null;
            }
        }

        return null;
    }


	public boolean deleteCagetogies(int eventId) {
		PreparedStatement statement = null;

		String query =
				"DELETE FROM `eventsCategories`" +
						" WHERE `eventId` = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, eventId);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignore) {}

				statement = null;
			}
		}

		return false;
	}


	// TODO: use
	public int addCategory(int categoryId, int eventId) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = query = "INSERT INTO `eventsCategories` (`categoryId`, `eventId`) VALUES (?, ?)";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setInt(1, categoryId);
			statement.setInt(2, eventId);

			statement.executeUpdate();
			results = statement.getGeneratedKeys();
			if (results.next()) {
				return results.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (SQLException ignore) {}

				results = null;
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignore) {}

				statement = null;
			}
		}

		return 0;
	}


	public Multimap<CategoryObject, String> getBestCategoriesForEachUser() {
		ListMultimap<CategoryObject, String> multimap = ArrayListMultimap.create();

		PreparedStatement statement = null;
		ResultSet results = null;

		String query =
				"SELECT `sub`.`gcmToken`, `sub`.`categoryId`, `sub`.`categoryName`, MAX(`sub`.`score`)" +
				" FROM" +
				"	(SELECT `u`.`gcmToken` AS `gcmToken`, `c`.`id` AS `categoryId`, `c`.`name` AS `categoryName`, SUM(`eu`.`hits` * 12000 + `eu`.`time`) AS `score`" +
				" 	FROM `users` AS `u`" +
				"	JOIN `eventsUsers` AS `eu` ON `eu`.`userId` = `u`.`id`" +
				" 	JOIN `events` AS `e` ON `e`.`id`=`eu`.`eventId`" +
				" 	JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
				"	JOIN `categories` AS `c` ON `c`.`id` = `ec`.`categoryId`" +
				"	WHERE `eu`.`timestamp` > DATE_SUB(NOW(), INTERVAL 2 WEEK) " + // we are interested only in user actions not older than 2 weeks
				" 	GROUP BY `c`.`id`, `u`.`id`" +
				"	ORDER BY `score` DESC) AS `sub`" +
				" GROUP BY `sub`.`gcmToken`";

		try {
			statement = connection.prepareStatement(query);
			results = statement.executeQuery();
			while (results.next()) {
				/** http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Multimap.html **/
				CategoryObject categoryObject = new CategoryObject()
						.setId(results.getInt("categoryId"))
						.setName(results.getString("categoryName"));

				multimap.put(categoryObject, results.getString("gcmToken"));
			}

			return multimap;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (SQLException ignore) {}

				results = null;
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignore) {}

				statement = null;
			}
		}

		return null;
	}
}
