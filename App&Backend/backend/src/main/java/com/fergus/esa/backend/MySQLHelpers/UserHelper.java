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
                } catch (SQLException sqlEx) {} // ignore

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
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setInt(1, userId);
			statement.setInt(2, eventId);
			statement.executeUpdate();

			results = statement.getGeneratedKeys();
			if (results.next()) {
				boolean exists = results.getBoolean("exists");
				System.out.println("exists" + exists);
			} else {
				System.out.println("does not exist");
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
	}
}
