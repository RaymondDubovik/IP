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
            if (results.next()) {
                return new UserObject().setId(results.getInt("id")).setGcmToken(results.getString("gcmToken"));
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

        return null;
    }


    public UserObject create(String gcmToken) {
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


    public boolean updateToken(int userId, String gcmToken) {
        PreparedStatement statement = null;

        String query =
                "UPDATE `users`" +
                        " SET `gcmToken` = ?" +
                        " WHERE `id` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, gcmToken);
            statement.setInt(1, userId);
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


	public void registerHit(int userId, int eventId, double milliseconds) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT EXISTS(SELECT 1 FROM `eventsUsers` WHERE `userid`=? AND `eventId`=?) AS `exists`";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userId);
			statement.setInt(2, eventId);
			results = statement.executeQuery();

			if (results.next()) {
				boolean exists = results.getBoolean("exists");
				if (exists) {
					updateHit(userId, eventId, milliseconds);
				} else {
					createHit(userId, eventId, milliseconds);
				}
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
	}


	private boolean createHit(int userId, int eventId, double milliseconds) {
		PreparedStatement statement = null;

		String query = "INSERT INTO `eventsUsers`(`userId`, `eventId`, `timestamp`, `hits`, `time`) VALUES (?, ?, NOW(), 1, ?)";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userId);
			statement.setInt(2, eventId);
			statement.setDouble(3, milliseconds);

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


	private boolean updateHit(int userId, int eventId, double milliseconds) {
		PreparedStatement statement = null;

		String query =
				"UPDATE `eventsUsers`" +
						" SET `hits` = `hits` + 1," +
						" `time` = `time` + ?," +
						" `timestamp`=NOW()" +
						" WHERE `userId` = ?" +
						" AND `eventId` = ?" ;

		try {
			statement = connection.prepareStatement(query);
			statement.setDouble(1, milliseconds);
			statement.setInt(2, userId);
			statement.setInt(3, eventId);
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
}
