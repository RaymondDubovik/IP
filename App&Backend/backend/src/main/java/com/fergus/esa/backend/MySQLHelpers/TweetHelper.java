package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.TweetObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.01.2016
 */
public class TweetHelper {
    private Connection connection;


    public TweetHelper(Connection connection) {
        this.connection = connection;
    }


    public List<TweetObject> getEventTweets(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `username`, `screenName`, `profileImgUrl`, `imageUrl`, `text`, `timestamp`" +
                        " FROM `tweets`" +
                        " WHERE `eventId` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            results = statement.executeQuery();
            List<TweetObject> tweets = new ArrayList<>();
            while (results.next()) {
                tweets.add(
                    new TweetObject()
                        .setScreenName(results.getString("screenName"))
                        .setUsername(results.getString("username"))
                        .setProfileImgUrl(results.getString("profileImgUrl"))
                        .setImageUrl(results.getString("imageUrl"))
                        .setText(results.getString("text"))
                        .setTimestamp(results.getDate("timestamp"))
                        .setEventId(id)

                );
            }
            return tweets;
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


	public boolean exists(long id) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT EXISTS(SELECT 1 FROM `tweets` WHERE `id`=?) AS `exists`";

		try {
			statement = connection.prepareStatement(query);
			statement.setLong(1, id);
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


	public int create(TweetObject tweet) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "INSERT INTO `tweets` (`username`, `screenName`, `profileImgUrl`, `imageUrl`, `text`, `timestamp`, `eventId`) VALUES (?, ?, ?, ?, ?, ?, ?);";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, tweet.getUsername());
			statement.setString(2, tweet.getScreenName());
			statement.setString(3, tweet.getProfileImgUrl());
			statement.setString(4, tweet.getImageUrl());
			statement.setString(5, tweet.getText());
			statement.setString(6, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tweet.getTimestamp()));
			statement.setInt(7, tweet.getEventId());

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
}
