package com.fergus.esa.backend;

import com.fergus.esa.backend.OLD_DATAOBJECTS.ESAEvent;
import com.fergus.esa.backend.OLD_DATAOBJECTS.ESANews;
import com.fergus.esa.backend.OLD_DATAOBJECTS.ESATweet;
import com.fergus.esa.backend.dataObjects.CategoryObject;
import com.fergus.esa.backend.dataObjects.EventObject;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fergus.esa.backend.OLD_DATAOBJECTS.OfyService.ofy;

/*
    Endpoint class that handles storing ESAEvent entities in the Cloud Datastore on Google App Engine
 */

@Api(name = "esaEventEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.esa.fergus.com", ownerName = "backend.esa.fergus.com", packagePath = ""))
public class ESAEventEndpoint {

    /*
        Method to list events for display on the app, only lists events from the last 6 hours.
        Older events will be accessible through a search interface
     */
    @ApiMethod(name = "listEvents")
    public List<ESAEvent> listEvents() {
        // Long minusThreeHours = System.currentTimeMillis() - 10800000;
        List<ESAEvent> events = ofy().load().type(ESAEvent.class).order("timestamp").list();

        List<String> images = new ArrayList<>();
        images.add("http://www.gannett-cdn.com/-mm-/d186fe2344ab4f71ba561d52d784138c332b6857/c=0-177-1873-1585&r=x404&c=534x401/local/-/media/2015/02/04/USATODAY/USATODAY/635586464035076487-AFP-527752260.jpg");
        images.add("http://www.gannett-cdn.com/-mm-/d186fe2344ab4f71ba561d52d784138c332b6857/c=0-177-1873-1585&r=x404&c=534x401/local/-/media/2015/02/04/USATODAY/USATODAY/635586464035076487-AFP-527752260.jpg");

        List<ESANews> news = new ArrayList<>();
        news.add(new ESANews());
        List<String> summaries = new ArrayList<>();
        summaries.add("summ1");
        summaries.add("summ2");
        List<ESATweet> tweets = new ArrayList<>();
        tweets.add(new ESATweet());

        events.add(new ESAEvent().setEvent("TestEvent").setImageUrls(images).setNews(news).setSummaries(summaries).setTimestamp(111111111111L).setTweets(tweets));

        return events;
    }


    @ApiMethod(name = "getUserObject")
    public List<UserObject> getUserObject() {
        return null;
    }


    @ApiMethod(name = "getEventObject")
    public List<EventObject> getEventObject() {
        return null;
    }


    @ApiMethod(name = "getCategories")
    public List<CategoryObject> getCategories() {
        List<CategoryObject> categories = new ArrayList<>();
        categories.add(new CategoryObject().setId(1).setName("Category 1"));
        categories.add(new CategoryObject().setId(2).setName("Category 2"));
        categories.add(new CategoryObject().setId(3).setName("Category 3"));
        categories.add(new CategoryObject().setId(4).setName("Category 4"));
        categories.add(new CategoryObject().setId(5).setName("Category 5"));
        categories.add(new CategoryObject().setId(6).setName("Category 6"));
        categories.add(new CategoryObject().setId(7).setName("Category 7"));
        categories.add(new CategoryObject().setId(8).setName("Category 8"));
        categories.add(new CategoryObject().setId(9).setName("Category 9"));
        categories.add(new CategoryObject().setId(10).setName("Category 10"));

        return categories;
    }


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
