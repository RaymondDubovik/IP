package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.EventObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
    public List<EventObject> getNewEvents(List<Integer> categories, int from, int count) {
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

		String query = "SELECT MIN(`mId`) AS `minId` FROM" +
				" (SELECT `e`.`id` AS `mId`" +
				" FROM `events` AS `e`" +
				" JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
				" JOIN `categories` AS `c` ON `c`.`id`=`ec`.`categoryId`" +
				" WHERE `e`.`id` < ?" + categorySqlPart +
				" GROUP BY `e`.`id`" +
				" ORDER BY `e`.`id` DESC" +
				" LIMIT ?) AS `eventAlias`";

        try {
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


	public List<EventObject> getRecommendedEvents(int userId, List<Integer> excludedCategories) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String categorySqlPart = "";
		if (excludedCategories != null && excludedCategories.size() > 0) {
			StringBuilder builder = new StringBuilder(" WHERE `c`.`id` IN (");

			String prefix = "";
			for (int categoryId : excludedCategories) {
				builder.append(prefix).append('?');
				prefix = ",";
			}
			builder.append(") ");
			categorySqlPart = builder.toString();
		}

		String query = "SELECT `e`.`id`, `e`.`timestamp`, `e`.`heading`, `e`.`mainImageUrl`, SUM(`categoryScores`.`score`) AS `totalScore` " +
				" FROM `events` AS `e`" +
				" JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
				" JOIN `categories` AS `c` ON `c`.`id`=`ec`.`categoryId`" +
				" JOIN" + // this join gets score for this particular user for each category
				"		(SELECT `c`.`id` AS `categoryId`, SUM(`eu`.`hits` * 12000 + `eu`.`time`) AS `score`" +
				"		FROM `users` AS `u`" +
				"		JOIN `eventsUsers` AS `eu` ON `eu`.`userId` = `u`.`id`" +
				"		JOIN `events` AS `e` ON `e`.`id`=`eu`.`eventId`" +
				"		JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
				"		JOIN `categories` AS `c` ON `c`.`id` = `ec`.`categoryId`" +
				"		WHERE `u`.`id` = ?" +
				"		GROUP BY `c`.`id` " +
				"		ORDER BY `score` DESC) AS `categoryScores` ON `categoryScores`.`categoryId` = `c`.`id`" +
				" WHERE `e`.`timestamp` > DATE_SUB(NOW(), INTERVAL 1 WEEK) " + // we are interested only in events not older than a week
				" GROUP BY `e`.`id`, `e`.`timestamp`, `e`.`heading`, `e`.`mainImageUrl`" +
				" HAVING `e`.`id` NOT IN " + // this subquery filters out all events that are in user selected categories
				"		(SELECT `e`.`id` FROM `events` AS `e`" +
				"		JOIN `eventsCategories` AS `ec` ON `ec`.`eventId` = `e`.`id`" +
				"		JOIN `categories` AS `c` ON `c`.`id`=`ec`.`categoryId`" +
				"		" + categorySqlPart + ")" +
				" ORDER BY `totalScore` DESC";


		System.out.println(query);
		try {
			statement = connection.prepareStatement(query);
			int param = 1;
			statement.setInt(param++, userId);
			if (excludedCategories != null) {
				for (int excludedCategoryId : excludedCategories) {
					statement.setInt(param++, excludedCategoryId);
				}
			}

			results = statement.executeQuery();

			List<EventObject> events = new ArrayList<>();
			results = statement.executeQuery();
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


	public boolean exists(String heading) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT EXISTS(SELECT 1 FROM `events` WHERE `heading`=?) AS `exists`";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, heading);
			results = statement.executeQuery();

			if (results.next()) {
				return results.getBoolean("exists");
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

		return false;
	}


	public int create(EventObject event) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "INSERT INTO `events` (`timestamp`, `heading`, `mainImageUrl`) VALUES (?, ?, ?);";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

			String time;
			if (event.getTimestamp() == null) {
				time = "0000-00-00 00:00:00";
			} else {
				time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp());
			}

			if (event.getImageUrl() == null) {
				event.setImageUrl("");
			}

			statement.setString(1, time);
			statement.setString(2, event.getHeading());
			statement.setString(3, event.getImageUrl());

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

		return 0;
	}


	public int getIdByHeading(String heading) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT `id` FROM `events` WHERE `heading`=?";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, heading);
			results = statement.executeQuery();

			if (results.next()) {
				return results.getInt("id");
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

		return 0;
	}


	public boolean update(EventObject event) {
		PreparedStatement statement = null;

		String query =
				"UPDATE `events`" +
						" SET `mainImageUrl` = ?," +
						" `heading` = ?," +
						" `timestamp` = ?" +
						" WHERE `id` = ?" ;

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, event.getImageUrl());
			statement.setString(2, event.getHeading());
			statement.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp()));
			statement.setInt(4, event.getId());

			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqlEx) {} // ignore

				statement = null;
			}
		}

		return false;
	}


	public EventObject get(int eventId) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query =
				"SELECT `id`, `timestamp`, `heading`, `mainImageUrl`" +
						" FROM `events`" +
						" WHERE `id` = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, eventId);

			results = statement.executeQuery();
			if (results.next()) {
				return new EventObject()
						.setId(results.getInt("id"))
						.setTimestamp(results.getDate("timestamp"))
						.setHeading(results.getString("heading"))
						.setImageUrl(results.getString("mainImageUrl"));
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
