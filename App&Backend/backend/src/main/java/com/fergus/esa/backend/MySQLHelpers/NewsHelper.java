package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.NewsObject;

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
public class NewsHelper {
    private Connection connection;


    public NewsHelper(Connection connection) {
        this.connection = connection;
    }


    public List<NewsObject> getEventNews(int id) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `title`, `url`, `logoUrl`, `timestamp`" +
                        " FROM `news`" +
                        " WHERE `eventId` = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            results = statement.executeQuery();
            List<NewsObject> news = new ArrayList<>();
            while (results.next()) {
                news.add(
                        new NewsObject()
                                .setTitle(results.getString("title"))
                                .setUrl(results.getString("url"))
                                .setLogoUrl(results.getString("logoUrl"))
                                .setTimestamp(results.getDate("timestamp"))
                                .setEventId(id)
                );
            }
            return news;
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


	public boolean exists(String url) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "SELECT EXISTS(SELECT 1 FROM `news` WHERE `url`=?) AS `exists`";

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


	public int create(NewsObject news) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "INSERT INTO `news` (`title`, `url`, `logoUrl`, `timestamp`, `eventId`) VALUES(?, ?, ?, ?, ?);";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, news.getTitle());
			statement.setString(2, news.getUrl());
			statement.setString(3, news.getLogoUrl());
			statement.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(news.getTimestamp()));
			statement.setInt(5, news.getEventId());

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
