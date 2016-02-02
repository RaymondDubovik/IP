package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.TweetHelper;
import com.fergus.esa.backend.dataObjects.TweetObject;
import com.google.api.server.spi.response.ConflictException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.text.Normalizer;
import java.util.HashSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@SuppressWarnings("serial")
public class ESATweetServlet extends HttpServlet {
    // An array of strings to hold trending events
    private HashSet<String> events = new HashSet<>();

	private Connection connection;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		connection = (new MySQLJDBC()).getConnection();

		// Create a ConfigurationBuilder which links the program to a twitter account using the various keys below
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("AAfSJDnui3xQTegXr2GOogBAp");
        cb.setOAuthConsumerSecret("pVqlDf3ySpwRtz9ePr0mvYmx0ob9HwIF17IwpfsgfLRdo5VBKI");
        cb.setOAuthAccessToken("3345365331-7amtdWHIU2U98JJyTLWwm2ewFpxJ61YIkRraEWh");
        cb.setOAuthAccessTokenSecret("Mm9Efm05yZqALZ0bkSVVRwrK08j9NZ6YQryAZiUuPgY4a");

        // Create a Configuration instance which can be reused
        Configuration c = cb.build();

        getEvents();

        try {
            getTweets(c);
        } catch (TwitterException | ConflictException e) {
            e.printStackTrace();
        }
    }


    public void getEvents() throws IOException {
        Document doc = Jsoup.connect("https://news.google.co.uk/").get();

        Elements topics = doc.select("div.topic");

        for (Element t : topics) {
            events.add(t.text());
        }
    }


    public void getTweets(Configuration c) throws TwitterException, ConflictException {
        // Create a new instance of a TwitterFactory to pull data from twitter
        TwitterFactory tf = new TwitterFactory(c);
        Twitter twitter = tf.getInstance();

        // For each trending event pull the top 10 most popular tweets
        for (String event : events) {
            event = removeSuffix(event);
            event = removeAccents(event);
            twitter4j.Query query = new twitter4j.Query(event);
            query.count(10);
            query.lang("en");
            query.resultType(Query.ResultType.mixed);

            QueryResult result = twitter.search(query);
            for (Status status : result.getTweets()) {
                if (!status.isRetweet() && !status.isPossiblySensitive()) {
                    String imageUrl = "";

                    if (status.getMediaEntities().length > 0) {
                        imageUrl = status.getMediaEntities()[0].getMediaURL();
                    }

					TweetObject tweetObject = new TweetObject()
							.setEventId(1) // TODO fix;
							.setId(status.getId())
							.setUsername(status.getUser().getName())
							.setScreenName(status.getUser().getScreenName())
							.setProfileImgUrl(status.getUser().getBiggerProfileImageURL())
							.setImageUrl(imageUrl)
							.setText(status.getText())
							.setTimestamp(status.getCreatedAt());

					insertTweet(tweetObject);
                }
            }
        }
    }


    public String removeSuffix(String event) {
        String suffixRemovedEvent;

        if (event.contains("FC")) {
            suffixRemovedEvent = event.replace("FC", "");
        } else if (event.contains("F.C.")) {
            suffixRemovedEvent = event.replace("F.C.", "");
        } else if (event.contains("PLC")) {
            suffixRemovedEvent = event.replace("PLC", "");
        } else if (event.contains("LLC")) {
            suffixRemovedEvent = event.replace("LLC", "");
        } else if (event.contains("Corporation")) {
            suffixRemovedEvent = event.replace("Corporation", "");
        } else if (event.contains("Inc.")) {
            suffixRemovedEvent = event.replace("Inc.", "");
        } else if (event.contains("Ltd.")) {
            suffixRemovedEvent = event.replace("Ltd.", "");
        } else if (event.contains("AFC")) {
            suffixRemovedEvent = event.replace("AFC", "");
        } else if (event.contains("A.F.C.")) {
            suffixRemovedEvent = event.replace("A.F.C.", "");
        } else {
            suffixRemovedEvent = event;
        }

        return suffixRemovedEvent;
    }


    //http://drillio.com/en/software/java/remove-accent-diacritic/
    public String removeAccents(String text) {
        return text == null ? null :
                Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }


    public TweetObject insertTweet(TweetObject tweet) throws ConflictException {
		TweetHelper helper = new TweetHelper(connection);
		//If if is not null, then check if it exists. If yes, throw an Exception that it is already present
        if (tweet.getId() != 0 && helper.exists(tweet.getId())) {
			throw new ConflictException("Object already exists");
        }

        helper.create(tweet);
        return tweet;
    }
}

