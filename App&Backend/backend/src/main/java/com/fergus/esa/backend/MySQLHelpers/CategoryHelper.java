package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.CategoryObject;
import com.fergus.esa.backend.dataObjects.CategoryRatingObject;

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


	// TODO: use this method for each user to get their category ratings.
	public List<CategoryRatingObject> getUserCategoryRating(int userId, List<Integer> categories) {
		PreparedStatement statement = null;
		ResultSet results = null;

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

		String query =
				"SELECT `c`.`id`, SUM(`eu`.`hits` * 12000 + `eu`.`time`) AS `score`" +
						" FROM `users` AS `u`" +
						" JOIN `eventsUsers` AS `eu` ON `eu`.`userId` = `u`.`id`" +
						" JOIN `events` AS `e` ON `e`.`id`=`eu`.`eventId`" +
						" JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
						" JOIN `categories` AS `c` ON `c`.`id` = `ec`.`categoryId`" +
						" WHERE `u`.`id` = ?" +
						"	AND `eu`.`timestamp` > DATE_SUB(NOW(), INTERVAL 2 WEEK) " + // we are interested only in user actions not older than 2 weeks
						categorySqlPart +
						" GROUP BY `c`.`id` " +
						" ORDER BY `score` DESC";

		try {
			statement = connection.prepareStatement(query);

			int param = 1;
			statement.setInt(param++, userId);
			if (categories != null) {
				for (int categoryId : categories) {
					statement.setInt(param++, categoryId);
				}
			}

			List<CategoryRatingObject> categoryRatings = new ArrayList<>();
			results = statement.executeQuery();
			while (results.next()) {
				categoryRatings.add(
						new CategoryRatingObject()
								.setCategoryId(results.getInt("id"))
								.setScore(results.getInt("score"))
				);

				System.out.println(results.getInt("id") + " " + results.getInt("score"));
			}

			return categoryRatings;
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
