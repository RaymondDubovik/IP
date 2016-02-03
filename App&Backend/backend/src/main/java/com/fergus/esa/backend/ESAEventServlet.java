package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.EventHelper;
import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.dataObjects.EventObject;
import com.fergus.esa.backend.dataObjects.NewsObject;
import com.google.api.server.spi.response.ConflictException;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class ESAEventServlet extends HttpServlet {
    // private  events = new HashSet<>();
    private Long minusOneHour = System.currentTimeMillis() - 3600000;

	private Connection connection;


	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		connection = (new MySQLJDBC()).getConnection();

		// 1) get event URLs
		// 2) get news from those urls
		// 3) get tweets from those urls

		// getting something
		HashSet<String> eventHeadings = getEventHeadings();

		try {
			getData(eventHeadings);
		} catch (FeedException e) {
			e.printStackTrace();
		}
	}


	public HashSet<String> getEventHeadings() throws IOException {
		HashSet<String> eventHeadings = new HashSet<>();

		org.jsoup.nodes.Document doc = Jsoup.connect("https://news.google.co.uk/").get();

		Elements topics = doc.select("div.topic");

		for (Element t : topics) {
			eventHeadings.add(t.text());
		}

		return eventHeadings;
	}


	public void getData(HashSet<String> evebtHeadings) throws FeedException, IOException {
		String feedUrl;

		for (String heading : evebtHeadings) {
			heading = removeSuffix(removeAccents(heading));

			// TODO: add event here and retrieve event Id:
			int eventId = addEvent(heading);

			addNews(heading, eventId);

			EventObject event = new EventObject()
					.setId(eventId)
					.setHeading(heading);

			// TODO: after adding tweets, update the event with image urls
			// esaEvent.setImageUrls(getImageUrls(eventTweets));
			// TODO: Long timestamp = getMostRecentTweetTime(eventTweets);

			// TODO: update the event here
		}
	}


	private void addNews(String event, int eventId) throws IOException, FeedException {
		URL url = new URL(getEventUrl(event));
		Reader r = new InputStreamReader(url.openStream());
		SyndFeed feed = new SyndFeedInput().build(r);

		List<SyndEntry> entryList = feed.getEntries();

		if (entryList.size() > 0) {
			for (int i = 0; i < 2; i++) {
				SyndEntry entry = entryList.get(i);
				String entryUrl = entry.getUri().substring(33);
				String title = entry.getTitle();
				Date date = entry.getPublishedDate();

				NewsObject news = new NewsObject()
						.setEventId(eventId) // TODO: implement
						.setEventId(1)
						.setTitle(title)
						.setUrl(entryUrl)
						.setLogoUrl("")
						.setTimestamp(date);

				// System.out.println(title);

				insertNews(news);
			}
		}
	}


	public NewsObject insertNews(NewsObject news) throws ConflictException {
		NewsHelper helper = new NewsHelper(connection);

		// If if is not null, then check if it exists. If yes, throw an Exception, that it is already present
		if (news.getUrl() != null && helper.exists(news.getUrl())) {
			throw new ConflictException("Object already exists");
		}

		helper.create(news);
		return news;
	}


	private int addEvent(String eventHeading) {
		EventObject event = new EventObject().setHeading(eventHeading);
		return insertEvent(event);
	}


	private String getEventUrl(String event) {
		String cleanEvent;
		if (event.contains(" ")) {
			cleanEvent = event.replace(" ", "+");
		} else {
			cleanEvent = event;
		}

		return "http://news.google.com/news?q=" + cleanEvent + "&output=rss";
	}


	//http://drillio.com/en/software/java/remove-accent-diacritic/
	public String removeAccents(String text) {
		return text == null ? null :
				Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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


	public int insertEvent(EventObject event) {
		EventHelper helper = new EventHelper(connection);

		if (event.getHeading() == null) {
			return 0;
		}

		// If if is not null, then check if it exists. If yes, throw an Exception, that it is already present
		int id = helper.getIdByHeading(event.getHeading()); // TODO: return an ID here

		return (id != 0) ? id :	helper.create(event);
	}



    public void addEvents() throws NotFoundException, IOException {
        for (String e : events) {
            List<ESANews> eventNews = listEventNews(e);
            List<ESATweet> eventTweets = listEventTweets(e);

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


    public void getEvents() throws IOException {
		List<ESANews> allNews = listNews();

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


/*
    public long getMostRecentTweetTime(List<ESATweet> tweets) {
        long timestamp = 0;

        for (ESATweet t : tweets) {
            long tweetTimestamp = t.getTimestamp();
            if (tweetTimestamp > timestamp) {
                timestamp = tweetTimestamp;
            }
        }

        return timestamp;



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

    */
}