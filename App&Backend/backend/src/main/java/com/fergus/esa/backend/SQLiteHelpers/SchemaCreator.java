package com.fergus.esa.backend.SQLiteHelpers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// TODO: think about
/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.11.2015
 */
public class SchemaCreator {
    private static final String queryCategories =
        "CREATE TABLE IF NOT EXISTS `categories` (" +
            "`id` INTEGER PRIMARY KEY," +
            "`name` INTEGER" +
        ")";

    private static final String queryEvents =
        "CREATE TABLE IF NOT EXISTS `events` (" +
            "`id` INTEGER PRIMARY KEY," +
            "`timestamp` INTEGER, " +
            "`heading` TEXT" +
        ")";

    private static final String queryImages =
        "CREATE TABLE IF NOT EXISTS `images` (" +
            "`url` TEXT," +
            "`eventId` INTEGER" +
        ")";

    private static final String queryNews =
        "CREATE TABLE IF NOT EXISTS `news` (" +
            "`id` INTEGER PRIMARY KEY," +
            "`title` TEXT," +
            "`url` TEXT," +
            "`logoUrl` TEXT," +
            "`timestamp` INTEGER" +
        ")";

    private static final String queryTweets =
        "CREATE TABLE IF NOT EXISTS `tweets` (" +
            "`id` INTEGER PRIMARY KEY," +
            "`username` TEXT," +
            "`screenName` TEXT," +
            "`profileImgUrl` TEXT," +
            "`imageUrl` INTEGER," +
            "`text` TEXT," +
            "`timestamp` INTEGER" +
        ")";

    private static final String queryUsers =
        "CREATE TABLE IF NOT EXISTS `users` (" +
            "`id` INTEGER PRIMARY KEY," +
            "`gcmToken` TEXT" +
        ")";

    private static final String querySummaries =
        "CREATE TABLE IF NOT EXISTS `summaries` (" +
                "`length` INTEGER," +
                "`text` INTEGER," +
            ")";

    private static final String queryEventsUsers =
        "CREATE TABLE IF NOT EXISTS `eventsUsers` (" +
            "`userId` INTEGER REFERENCES `users`(`id`)" +
            "`eventId` INTEGER REFERENCES `events`(`id`)," +
            "`hits` INTEGER DEFAULT 0" +
            "`time` INTEGER DEFAULT 0" +
        ")";

    private static final String queryEventsCategories =
        "CREATE TABLE IF NOT EXISTS `eventsCategories` (" +
            "`categoryId` INTEGER REFERENCES `categories`(`id`)" +
            "`eventId` INTEGER REFERENCES `events`(`id`)," +
        ")";

    private static final String queryForeignKeyNews = "ALTER TABLE `news` ADD COLUMN `eventId` INTEGER REFERENCES `events`(`id`);";

    private static final String queryForeignKeyTweets = "ALTER TABLE `tweets` ADD COLUMN `eventId` INTEGER REFERENCES `events`(`id`);";

    private static final String queryForeignKeySummaries = "ALTER TABLE `summaries` ADD COLUMN `eventId` INTEGER REFERENCES `events`(`id`);";

    private static final String queryForeignKeyCategories = "ALTER TABLE `events` ADD COLUMN `eventId` INTEGER REFERENCES `events`(`id`);";

    private static final String queryDropTableEvents = "DROP TABLE `events`";
    private static final String queryDropTableCategories = "DROP TABLE `events`";
    private static final String queryDropTableNews = "DROP TABLE `events`";
    private static final String queryDropTableTweets = "DROP TABLE `events`";
    private static final String queryDropTableUsers = "DROP TABLE `events`";
    private static final String queryDropTableImages = "DROP TABLE `events`";
    private static final String queryDropTableSummaries = "DROP TABLE `events`";

    private static final String queryDropEventsUsers = "DROP TABLE `eventUsers`";
    private static final String queryDropEventsCategories = "DROP TABLE `eventCategories`";


    public void create(Connection connection) throws SQLException {
        createTables(connection);
        createConnectionTable(connection);
        addForeignKeys(connection);
    }


    public void drop(Connection connection) throws SQLException {
        dropTables(connection);
        dropConnectionTables(connection);
    }


    private void dropConnectionTables(Connection connection) throws SQLException {
        dropConnectionTableEventsUsers(connection);
        dropConnectionTableEventsCategories(connection);
    }


    private void dropConnectionTableEventsUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropEventsUsers);
        statement.close();
    }


    private void dropConnectionTableEventsCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropEventsCategories);
        statement.close();
    }


    private void dropTables(Connection connection) throws SQLException {
        dropTableEvents(connection);
        dropTableCategories(connection);
        dropTableNews(connection);
        dropTableTweets(connection);
        dropTableUsers(connection);
        dropTableImages(connection);
        dropTableSummaries(connection);
    }


    private void dropTableSummaries(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableSummaries);
        statement.close();
    }


    private void dropTableImages(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableImages);
        statement.close();
    }


    private void dropTableUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableUsers);
        statement.close();
    }


    private void dropTableTweets(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableTweets);
        statement.close();
    }


    private void dropTableNews(Connection connection) throws SQLException{
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableNews);
        statement.close();
    }


    private void dropTableCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableCategories);
        statement.close();
    }


    private void dropTableEvents(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryDropTableEvents);
        statement.close();
    }


    private void addForeignKeys(Connection connection) throws SQLException {
        addForeignKeysNews(connection);
        addForeignKeysTweets(connection);
        addForeignKeysSummaries(connection);
        addForeignKeysCategories(connection);
    }


    private void createTables(Connection connection) throws SQLException {
        createTableEvents(connection);
        createTableCategories(connection);
        createTableNews(connection);
        createTableTweets(connection);
        createTableUsers(connection);
        createTableImages(connection);
        createTableSummaries(connection);
    }


    private void createConnectionTable(Connection connection) throws SQLException {
        createConnectionTableEventsUsers(connection);
        createConnectionTableEventsCategories(connection);
    }


    private void createConnectionTableEventsCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryEventsCategories);
        statement.close();
    }


    private void createTableSummaries(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(querySummaries);
        statement.close();
    }


    private void createTableImages(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryImages);
        statement.close();
    }


    private void createConnectionTableEventsUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryEventsUsers);
        statement.close();
    }


    private void createTableUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryUsers);
        statement.close();
    }


    private void createTableTweets(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryTweets);
        statement.close();
    }


    private void createTableNews(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryNews);
        statement.close();
    }


    private void createTableEvents(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryEvents);
        statement.close();
    }


    private void createTableCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryCategories);
        statement.close();
    }


    private void addForeignKeysNews(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryForeignKeyNews);
        statement.close();
    }


    private void addForeignKeysTweets(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryForeignKeyTweets);
        statement.close();
    }


    private void addForeignKeysSummaries(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryForeignKeySummaries);
        statement.close();
    }


    private void addForeignKeysCategories(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryForeignKeyCategories);
        statement.close();
    }
}
