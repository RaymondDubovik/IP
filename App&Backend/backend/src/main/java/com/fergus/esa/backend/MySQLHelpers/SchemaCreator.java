package com.fergus.esa.backend.MySQLHelpers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

// TODO: think about
/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.11.2015
 */
public class SchemaCreator {
    private static final String queryCategories =
        "CREATE TABLE IF NOT EXISTS `categories` (" +
            "`id` INT PRIMARY KEY AUTO_INCREMENT," +
            "`name` VARCHAR(60)" +
        ")";

    private static final String queryEvents =
        "CREATE TABLE IF NOT EXISTS `events` (" +
            "`id` INT PRIMARY KEY AUTO_INCREMENT," +
            "`timestamp` DATETIME, " +
            "`heading` VARCHAR(255)" +
        ")";

    private static final String queryImages =
        "CREATE TABLE IF NOT EXISTS `images` (" +
            "`url` VARCHAR(400)," +
            "`eventId` INT" +
        ")";

    private static final String queryNews =
        "CREATE TABLE IF NOT EXISTS `news` (" +
            "`id` INT PRIMARY KEY AUTO_INCREMENT," +
            "`title` VARCHAR(255)," +
            "`url` VARCHAR(400)," +
            "`logoUrl` VARCHAR(400)," +
            "`timestamp` DATETIME" +
        ")";

    private static final String queryTweets =
        "CREATE TABLE IF NOT EXISTS `tweets` (" +
            "`id` INT PRIMARY KEY AUTO_INCREMENT," +
            "`username` VARCHAR(15)," +
            "`screenName` VARCHAR(40)," +
            "`profileImgUrl` VARCHAR(400)," +
            "`imageUrl` VARCHAR(255)," +
            "`text` TEXT," +
            "`timestamp` DATETIME" +
        ")";

    private static final String queryUsers =
        "CREATE TABLE IF NOT EXISTS `users` (" +
            "`id` INT PRIMARY KEY AUTO_INCREMENT," +
            "`gcmToken` VARCHAR(500)" +
        ")";

    private static final String querySummaries =
        "CREATE TABLE IF NOT EXISTS `summaries` (" +
                "`length` INT," +
                "`text` TEXT" +
            ")";

    private static final String queryEventsUsers =
        "CREATE TABLE IF NOT EXISTS `eventsUsers` (" +
            "`userId` INT REFERENCES `users`(`id`)," +
            "`eventId` INT REFERENCES `events`(`id`)," +
            "`hits` INT DEFAULT 0," +
            "`time` INT DEFAULT 0" +
        ")";

    private static final String queryEventsCategories =
        "CREATE TABLE IF NOT EXISTS `eventsCategories` (" +
            "`categoryId` INT REFERENCES `categories`(`id`)," +
            "`eventId` INT REFERENCES `events`(`id`)" +
        ")";

    private static final String queryForeignKeyNews = "ALTER TABLE `news` ADD COLUMN `eventId` INT REFERENCES `events`(`id`);";
    private static final String queryForeignKeyTweets = "ALTER TABLE `tweets` ADD COLUMN `eventId` INT REFERENCES `events`(`id`);";
    private static final String queryForeignKeySummaries = "ALTER TABLE `summaries` ADD COLUMN `eventId` INT REFERENCES `events`(`id`);";

    private static final String queryDropTableEvents = "DROP TABLE IF EXISTS `events`";
    private static final String queryDropTableCategories = "DROP TABLE IF EXISTS `categories`";
    private static final String queryDropTableNews = "DROP TABLE IF EXISTS `news`";
    private static final String queryDropTableTweets = "DROP TABLE IF EXISTS `tweets`";
    private static final String queryDropTableUsers = "DROP TABLE IF EXISTS `users`";
    private static final String queryDropTableImages = "DROP TABLE IF EXISTS `images`";
    private static final String queryDropTableSummaries = "DROP TABLE IF EXISTS `summaries`";

    private static final String queryDropEventsUsers = "DROP TABLE IF EXISTS `eventsUsers`";
    private static final String queryDropEventsCategories = "DROP TABLE IF EXISTS `eventsCategories`";


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


    public void populateWithMockData(Connection connection) throws SQLException {
        String query = "INSERT INTO `categories` (`name`) VALUES ('Category 1'),('Category 2'),('Category 3'),('Category 4'),('Category 5'),('Category 6'),('Category 7')";
        executeUpdateQuery(connection, query);

        for (int i = 1; i <= 34; i++) {
            // events
            query = "INSERT INTO `events` (`id`, `timestamp`, `heading`) VALUES (" + i + ", NOW(), 'Some awesome event title');";
            executeUpdateQuery(connection, query);

            // news
            for (int j = 0; j < 34; j++) {
                query = "INSERT INTO `news` (`title`, `url`, `logoUrl`, `timestamp`, `eventId`) VALUES ('Some awesome news title', 'http://www.bbc.co.uk/news/entertainment-arts-35366703', NULL, NOW(), " + i + ");";
                executeUpdateQuery(connection, query);
            }

            // images
            query = "INSERT INTO `images` (`eventId`, `url`) VALUES (" + i + ", 'http://staging.mediawales.co.uk/_files/images//jun_10/mw__1276511479_News_Image.jpg'), (" + i + ", 'http://vantage-uk.com/wp-content/uploads/2013/03/breakingnews1.jpg')";
            executeUpdateQuery(connection, query);

            // categories
            for (int j = 0; j < 3; j++) {
                int random = new Random().nextInt((6) + 1) + 1;
                query = "INSERT INTO `eventsCategories` (`categoryId`, `eventId`) VALUES (" + random + ", " + i + ")";
                executeUpdateQuery(connection, query);
            }

            // tweets
            for (int j = 0; j < 30; j++) {
                query = "INSERT INTO `tweets` (`username`, `screenName`, `profileImgUrl`, `imageUrl`, `text`, `timestamp`, `eventId`) VALUES ('someUsername', 'someScreenName', 'https://pbs.twimg.com/profile_images/666407537084796928/YBGgi9BO.png', 'https://pbs.twimg.com/profile_images/666407537084796928/YBGgi9BO.png', 'Some text in twitter here because I can', NOW(), " + i + ");";
                executeUpdateQuery(connection, query);
            }

            // summaries
            for (int j = 0; j < 30; j++) {
                query = "INSERT INTO `summaries` (`text`, `length`, `eventId`) VALUES ('Some long summary text here.', 5, " + i + ");";
                executeUpdateQuery(connection, query);
            }
        }
    }

    private void executeUpdateQuery(Connection connection, String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }
}
