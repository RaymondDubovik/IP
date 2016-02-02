package com.fergus.esa.backend;

import com.fergus.esa.backend.OLD_DATAOBJECTS.ESAEvent;
import com.fergus.esa.backend.OLD_DATAOBJECTS.ESANews;
import com.fergus.esa.backend.OLD_DATAOBJECTS.ESATweet;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.repackaged.com.google.common.collect.ArrayListMultimap;
import com.google.appengine.repackaged.com.google.common.collect.Multimap;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.fergus.esa.backend.OLD_DATAOBJECTS.OfyService.ofy;


@SuppressWarnings("serial")
public class ESAEventServlet extends HttpServlet {
    HashSet<String> events = new HashSet<>();
    Long minusOneHour = System.currentTimeMillis() - 3600000;


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        getEvents();
        try {
            addEvents();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }


    public void addEvents() throws NotFoundException, IOException {
        for (String e : events) {

            List<ESANews> eventNews = listEventNews(e);

            List<ESATweet> eventTweets = listEventTweets(e);

            if (eventTweets.isEmpty() || eventNews.isEmpty()) {
                /*  don't add the event as the empty list would cause the app to crash
                    the event will be added the next time the servlet runs if there
                    are tweets or news associated with it.
                 */
            } else {
                List<String> eventSummaries = summarise(eventNews);

                Long timestamp = getMostRecentTweetTime(eventTweets);


                ESAEvent esaEvent = new ESAEvent();
                esaEvent.setEvent(e);
                esaEvent.setNews(eventNews);
                esaEvent.setTweets(eventTweets);
                esaEvent.setImageUrls(getImageUrls(eventTweets));
                esaEvent.setSummaries(eventSummaries);
                esaEvent.setTimestamp(timestamp);
                insertESAEvent(esaEvent);

                String docID = e.replace(" ", "_");

                String summaries = "";
                String titles = "";

                for (String sum : eventSummaries) {
                    summaries += sum + " ";
                }
                for (ESANews en : eventNews) {
                    titles += en.getTitle() + " ";
                }

                String searchPool = e + " " + summaries + " " + titles;

                Document eventDoc = createDocument(docID, searchPool);
                IndexDocument("eventIndex", eventDoc);

            }
        }
    }


    public void getEvents() throws IOException {

		// TODO: // FIXME: 02/02/2016
		List<ESANews> allNews = listNews();
		// List<ESANews> allNews = new ArrayList<>();

        for (ESANews en : allNews) {
            String event;
            if (en.getTimestamp() > minusOneHour) {
                event = en.getEvent();
                events.add(event);
            }
        }
    }


    public List<String> getImageUrls(List<ESATweet> esaTweets) {
        Set<String> imageUrlsSet = new HashSet<>();
        List<String> imageUrls = new ArrayList<>();
        for (ESATweet t : esaTweets) {
            if (!Objects.equals(t.getImageUrl(), "")) {
                imageUrlsSet.add(t.getImageUrl());
            }
        }

        for (String imgUrl : imageUrlsSet) {
            imageUrls.add(imgUrl);
        }
        return imageUrls;
    }


    // Method to produce a short summary of a group of news articles from a particular day,
    // relating to a particular event.
    public List<String> summarise(List<ESANews> news) {
        List<String> summaries = new ArrayList<>();
        long dayInMillis = 86400000;

        //http://stackoverflow.com/questions/28578072/split-java-util-date-collection-by-days
        Multimap<Long, ESANews> newsByDay = ArrayListMultimap.create();

        for (ESANews en : news) {
            long newsDateInMillis = en.getTimestamp();
            long day = newsDateInMillis / dayInMillis;
            newsByDay.put(day, en);
        }

        for (long day : newsByDay.keySet()) {
            String summaryText = "";
            long dateInMillis = day * dayInMillis;
            Collection<ESANews> dailyNews = newsByDay.get(day);
            Set<String> urls = new HashSet<>();

            for (ESANews dn : dailyNews) {
                urls.add(dn.getUrl());
            }
            for (String url : urls) {

                summaryText = getSummary(url);
                if (!summaryText.equals("")) {
                    break;
                }
            }


            Date date = new Date(dateInMillis);
            DateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.UK);
            String newsDate = dateformat.format(date);
            String summary = summaryText + "esaseparator" + newsDate;
            summaries.add(summary);
        }

        return summaries;
    }


    public String getSummary(String url) {
        String summary = "";


		summary = "this is a summary placeholder here!!!!";
		// TODO: this piece of crap is giving me headache
		/*
        try {

            Client client = Client.create();

            WebResource esaRESTSummariser = client.resource("http://ec2-52-17-235-186.eu-west-1.compute.amazonaws.com:8080/SummarisationRESTServer/SumServ/esarestsummariser/post");

            ClientResponse response = esaRESTSummariser.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, url);
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            summary = response.getEntity(String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return summary;
    }


    public Document createDocument(String event, String searchPool) {
        Document doc = Document.newBuilder()
                .setId(event)
                .addField(Field.newBuilder().setName("content").setText(searchPool))
                .build();

        return doc;
    }


    public void IndexDocument(String indexName, Document document) {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
        Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        try {
            index.put(document);
        } catch (PutException e) {
        }
    }


    public long getMostRecentTweetTime(List<ESATweet> tweets) {
        long timestamp = 0;

        for (ESATweet t : tweets) {
            long tweetTimestamp = t.getTimestamp();
            if (tweetTimestamp > timestamp) {
                timestamp = tweetTimestamp;
            }
        }

        return timestamp;
    }


    // Method to insert ESAEvent entities into the datastore
    public ESAEvent insertESAEvent(ESAEvent esaEvent) throws NotFoundException, IOException {
        // Return the events currently featured as topics on news.google.co.uk

        if (findESAEvent(esaEvent.getEvent()) != null) {
            updateESAEvent(esaEvent.getEvent(), esaEvent);
        } else {
            ofy().save().entity(esaEvent).now();
        }
        // Returns the newly created/updated esaEvent entity
        return esaEvent;
    }


    // Method to update a pre-existing ESAEvent entity
    public ESAEvent updateESAEvent(String event, ESAEvent esaEvent) throws NotFoundException {
        // Loads the existing event from the datastore
        ofy().load().type(ESAEvent.class).id(event).safe();
        // Saves the updated event
        ofy().save().entity(esaEvent).now();
        // Returns the updated event
        return ofy().load().entity(esaEvent).now();
    }


    // Method to check if an event already exists in the datastore
    private ESAEvent findESAEvent(String event) {
        return ofy().load().type(ESAEvent.class).id(event).now();
    }


    public List<ESANews> listNews() {
        List<ESANews> news = ofy().load().type(ESANews.class).list();

        return news;
    }


    public List<ESANews> listEventNews(String event) {
        List<ESANews> eventNews = ofy().load().type(ESANews.class).filter("event", event).list();
        return eventNews;
    }


    public List<ESATweet> listEventTweets(String event) {
        List<ESATweet> eventTweets = ofy().load().type(ESATweet.class).filter("event", event).list();
        return eventTweets;
    }


    public List<ESATweet> listTweets() {
        List<ESATweet> tweets = ofy().load().type(ESATweet.class).list();

        return tweets;
    }
}