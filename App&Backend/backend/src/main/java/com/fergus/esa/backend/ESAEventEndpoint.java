package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.CategoryHelper;
import com.fergus.esa.backend.MySQLHelpers.EventHelper;
import com.fergus.esa.backend.MySQLHelpers.ImageHelper;
import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.MySQLHelpers.SchemaCreator;
import com.fergus.esa.backend.MySQLHelpers.SummaryHelper;
import com.fergus.esa.backend.MySQLHelpers.TweetHelper;
import com.fergus.esa.backend.MySQLHelpers.UserHelper;
import com.fergus.esa.backend.OLD_DATAOBJECTS.ESAEvent;
import com.fergus.esa.backend.dataObjects.CategoryObject;
import com.fergus.esa.backend.dataObjects.EventObject;
import com.fergus.esa.backend.dataObjects.GcmObject;
import com.fergus.esa.backend.dataObjects.ImageObject;
import com.fergus.esa.backend.dataObjects.NewsObject;
import com.fergus.esa.backend.dataObjects.SummaryObject;
import com.fergus.esa.backend.dataObjects.TweetObject;
import com.fergus.esa.backend.dataObjects.UserObject;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.MatchScorer;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortOptions;
import com.google.gson.Gson;
import com.googlecode.objectify.repackaged.gentyref.TypeToken;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fergus.esa.backend.OLD_DATAOBJECTS.OfyService.ofy;

/*
    Endpoint class that handles storing ESAEvent entities in the Cloud Datastore on Google App Engine
 */

@Api(name = "esaEventEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.esa.fergus.com", ownerName = "backend.esa.fergus.com", packagePath = ""))
public class ESAEventEndpoint {
    private Connection connection;

    public ESAEventEndpoint() {
        connection = (new MySQLJDBC()).getConnection();

        try {
            SchemaCreator schemaCreator = new SchemaCreator();
            schemaCreator.drop(connection);
            schemaCreator.create(connection);
            schemaCreator.populateWithMockData(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, name = "registerGcmToken")
    public UserObject registerGcmToken(@Named("gcmToken") String gcmToken) {
        // TODO: check, if token is unique (in the database)

        GcmObject gcmObject = new GcmObject(gcmToken, "SomeTextHere", "SomeTitleHere").setData("{\"id\":5}"); // TODO: change
        new GcmSender().sendNotification(gcmObject.toJson());
        return new UserHelper(connection).create(gcmToken);

    }


    @ApiMethod(httpMethod = ApiMethod.HttpMethod.GET, name = "updateGcmToken")
    public void updateGcmToken(@Named("userId") int userId, @Named("gcmToken") String gcmToken) {
        // TODO: check, if user with given ID exists
        new UserHelper(connection).updateToken(userId, gcmToken);
        // TODO: return true or false in a wrapper....
    }




    // TODO: implement real thing here
    @ApiMethod(name = "getEvents")
    public List<EventObject> getEvents(@Named("from") int from, @Named("count") int count, @Named("categories") String categoriesJson) {

        categoriesJson = categoriesJson.trim();
        List<Integer> categoryIds = categoriesJson.equals("") ? null : (List<Integer>) new Gson().fromJson(categoriesJson, new TypeToken<ArrayList<Integer>>() {}.getType());

        List<EventObject> eventsTmp = new EventHelper(connection).getEvents(categoryIds, from, count);

        // TODO: supply random /first image here
        ImageObject image = new ImageObject().setUrl("http://staging.mediawales.co.uk/_files/images//jun_10/mw__1276511479_News_Image.jpg");
        List<ImageObject> images = new ArrayList<>();
        images.add(image);

        EventObject event = new EventObject().setId(from + count).setImages(images).setHeading("Event title");
        List<EventObject> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(event);
        }

        // todo: check, if categories == null;

        return events;
    }


    @ApiMethod(name = "getCategories")
    public List<CategoryObject> getCategories() {
        return new CategoryHelper(connection).getCategories();
    }


    @ApiMethod(name="getTweets")
    public List<TweetObject> getTweets(@Named("eventId") int id) {
        // TODO: remove from and count parameters
        return new TweetHelper(connection).getEventTweets(id);
    }


    @ApiMethod(name="getImages")
    public List<ImageObject> getImages(@Named("eventId") int id) {
        return new ImageHelper(connection).getEventImages(id);
    }


    @ApiMethod(name="getNews")
    public List<NewsObject> getNews(@Named("eventId") int id) {
        return new NewsHelper(connection).getEventNews(id);
    }


    @ApiMethod(name="getSummaries")
    public List<SummaryObject> getSummaries(@Named("eventId") int id) {
        return new SummaryHelper(connection).getEventSummaries(id);
    }







    // SOMETHING THERE.....



    @ApiMethod(name = "listSearchedEvents")
    public List<ESAEvent> listSearchedEvents(@Named("query") String query) {

        List<ScoredEvent> scoredEvents = getEventsByQuery(query);
        Collections.sort(scoredEvents);
        List<ESAEvent> events = new ArrayList<>();

        for (ScoredEvent se : scoredEvents) {

            ESAEvent matchingEvent = ofy().load().type(ESAEvent.class).id(se.getEvent()).safe();
            matchingEvent.setEvent(se.getEvent() + "esaseparator" + se.getScore());
            events.add(matchingEvent);
        }

        return events;
    }


    @ApiMethod(name = "returnESAEvent")
    public ESAEvent returnESAEvent(@Named("event") String event) {
        ESAEvent esaEvent = ofy().load().type(ESAEvent.class).id(event).now();
        return esaEvent;
    }


    private List<ScoredEvent> getEventsByQuery(String userQuery) {
        List<ScoredEvent> scoredEvents = new ArrayList<>();
        Index index = SearchServiceFactory.getSearchService()
                .getIndex(IndexSpec.newBuilder().setName("eventIndex"));

        Query query = Query.newBuilder()
                .setOptions(QueryOptions.newBuilder()
                        .setSortOptions(SortOptions.newBuilder()
                                .setMatchScorer(MatchScorer.newBuilder())))
                .build(userQuery);
        Results<ScoredDocument> results = index.search(query);

        for (ScoredDocument document : results) {
            String event = document.getId().replace("_", " ");
            Double score = document.getSortScores().get(0);
            ScoredEvent se = new ScoredEvent(event, score);
            scoredEvents.add(se);
        }

        return scoredEvents;
    }


    private class ScoredEvent implements Comparable<ScoredEvent> {
        private String event;
        private Double score;


        public ScoredEvent(String event, Double score) {
            this.event = event;
            this.score = score;
        }


        public String getEvent() {
            return event;
        }


        public void setEvent(String event) {
            this.event = event;
        }


        public Double getScore() {
            return score;
        }


        public void setScore(Double score) {
            this.score = score;
        }


        @Override
        public int compareTo(ScoredEvent other) {
            Double thisScore = this.getScore();
            Double otherScore = other.getScore();

            if (thisScore > otherScore)
                return -1;
            else if (thisScore == otherScore)
                return 0;
            else
                return 1;
        }
    }
}
