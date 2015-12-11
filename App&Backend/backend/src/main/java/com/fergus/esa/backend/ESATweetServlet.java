package com.fergus.esa.backend;

import com.fergus.esa.backend.OLD_DATAOBJECTS.ESATweet;
import com.google.api.server.spi.response.ConflictException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Format;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

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

import static com.fergus.esa.backend.OLD_DATAOBJECTS.OfyService.ofy;

@SuppressWarnings("serial")
public class ESATweetServlet extends HttpServlet {

    // An array of strings to hold trending events
    HashSet<String> events = new HashSet<>();


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

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

                    Long timestamp = System.currentTimeMillis();

                    String imageUrl = "";

                    if (status.getMediaEntities().length > 0) {

                        imageUrl = status.getMediaEntities()[0].getMediaURL();
                    }

                    Date date = status.getCreatedAt();
                    Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String sDate = formatter.format(date);

                    ESATweet esaTweet = new ESATweet();
                    esaTweet.setId(status.getId());
                    esaTweet.setEvent(event);
                    esaTweet.setUsername(status.getUser().getName());
                    esaTweet.setScreenname(status.getUser().getScreenName());
                    esaTweet.setProfileImgUrl(status.getUser().getBiggerProfileImageURL());
                    esaTweet.setImageUrl(imageUrl);
                    esaTweet.setText(status.getText());
                    esaTweet.setDate(sDate);
                    esaTweet.setTimestamp(timestamp);

                    if (findESATweet(status.getId()) == null) {
                        insertESATweet(esaTweet);
                    }
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
                Normalizer.normalize(text, Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }


    public ESATweet insertESATweet(ESATweet esaTweet) throws ConflictException {
        //If if is not null, then check if it exists. If yes, throw an Exception
        //that it is already present
        if (esaTweet.getId() != null) {
            if (findESATweet(esaTweet.getId()) != null) {
                throw new ConflictException("Object already exists");
            }
        }
        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(esaTweet).now();
        return esaTweet;
    }


    public ESATweet findESATweet(Long id) {
        return ofy().load().type(ESATweet.class).id(id).now();
    }
}

