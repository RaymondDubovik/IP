package com.fergus.esa.backend.MySQLHelpers;

import com.fergus.esa.backend.dataObjects.SummaryObject;

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
public class SummaryHelper {
    private Connection connection;


    public SummaryHelper(Connection connection) {
        this.connection = connection;
    }


    public List<SummaryObject> getEventSummaries(int id, int length) {
        PreparedStatement statement = null;
        ResultSet results = null;

        String query =
                "SELECT `length`, `text`, `timestamp`" +
                        " FROM `summaries`" +
                        " WHERE `eventId` = ?" +
						" AND `length` = ?" +
						" ORDER BY `timestamp` DESC";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
			statement.setInt(2, length);
            results = statement.executeQuery();
            List<SummaryObject> summaries = new ArrayList<>();
            while (results.next()) {
                summaries.add(
                        new SummaryObject()
                                .setLength(results.getInt("length"))
                                .setText(results.getString("text"))
								.setTimestamp(results.getTimestamp("timestamp"))
                                .setEventId(id)

                );
            }
            return summaries;
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


	public int create(SummaryObject summary, int eventId) {
		PreparedStatement statement = null;
		ResultSet results = null;

		String query = "INSERT INTO `summaries` (`text`, `length`, `timestamp`, `eventId`) VALUES (?, ?, NOW(), ?);";

		try {
			statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, summary.getText());
			statement.setInt(2, summary.getLength());
			statement.setInt(3, eventId);

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
