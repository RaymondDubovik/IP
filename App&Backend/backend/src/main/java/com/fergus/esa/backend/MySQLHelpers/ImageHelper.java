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


	public boolean exists(String url) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT EXISTS(SELECT 1 FROM `images` WHERE `url`=?) AS `exists`";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
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

		return false;
	}


	public int create(String url, int eventId) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "INSERT INTO `images` (`eventId`, `url`) VALUES (?, ?)";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setInt(1, eventId);
			statement.setString(2, url);

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
}