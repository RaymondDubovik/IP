package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.EventHelper;
import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.MySQLHelpers.TweetHelper;
import com.fergus.esa.backend.dataObjects.EventObject;
import com.fergus.esa.backend.dataObjects.NewsObject;
import com.fergus.esa.backend.dataObjects.SummaryObject;
import com.fergus.esa.backend.dataObjects.TweetObject;
import com.google.api.server.spi.response.ConflictException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import twitter4j.JSONObject;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


@SuppressWarnings("serial")
public class ESAEventServlet extends HttpServlet {
	private static final String SUMMARISATION_SERVER_URL = "http://127.0.0.1:10000/test";
	private Long minusOneHour = System.currentTimeMillis() - 3600000;

	private Connection connection;


	private Configuration getTwitterConfiguration() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("AAfSJDnui3xQTegXr2GOogBAp");
		cb.setOAuthConsumerSecret("pVqlDf3ySpwRtz9ePr0mvYmx0ob9HwIF17IwpfsgfLRdo5VBKI");
		cb.setOAuthAccessToken("3345365331-7amtdWHIU2U98JJyTLWwm2ewFpxJ61YIkRraEWh");
		cb.setOAuthAccessTokenSecret("Mm9Efm05yZqALZ0bkSVVRwrK08j9NZ6YQryAZiUuPgY4a");

		// Create a Configuration instance which can be reused
		Configuration configuration = cb.build();
		return configuration;
	}


	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		connection = (new MySQLJDBC()).getConnection();

		try {
			storeData(getEventHeadings());
		} catch (IOException | FeedException | TwitterException e) {
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


	public void storeData(HashSet<String> eventHeading) throws FeedException, IOException, TwitterException {
		for (String heading : eventHeading) {
			System.out.println("NEW EVENT NOW:");
			heading = removeSuffix(removeAccents(heading));

			int eventId = addEvent(heading);

			TweetModel tweetModel = new TweetModel(getTwitterConfiguration());
			List<TweetObject> tweets = tweetModel.getTweets(eventId, heading);
			tweetModel.insertTweets(tweets);

			Set<String> imageUrls = tweetModel.getImagesFromTweets(tweets);
			for (String image : imageUrls) {
				// TODO insert images for the event here;
			}

			// TODO: refactor NewsModel
			List<NewsObject> news = new NewsModel().addNews(heading, eventId);
			List<ResponseJsonObject> responses = summarise(news);
			for (ResponseJsonObject response: responses) {
				List<SummaryObject> summaries = response.getSummaries();
				for (SummaryObject summary : summaries) {
					if (summary == null || summary.equals("")) {
						System.out.println("empty summary");
						break;
					}
					//System.out.println(summary.getText());
					// TODO: if summary text is empty, then don't store the category
				}

				System.out.println("category: " + response.getCategory());
				//System.out.println(summary.getText());
				// TODO: category
				// when deciding on category, take it into account only when summaries are not empty
			}


			EventObject event = new EventObject()
					.setId(eventId)
					.setHeading(heading)
					.setTimestamp(tweetModel.getMostRecentTweetTime(tweets));

			// TODO: store event summaries
			// List<String> eventSummaries = summarise();

			/*
			List<ESANews> eventNews = listEventssNews(e);
            List<ESATweet> eventTweets = listEventTweets(e);

			ESAEvent esaEvent = new ESAEvent();
			esaEvent.setEvent(e);
			esaEvent.setNews(eventNews);

			insertESAEvent(esaEvent);
			 */



			// TODO: update the event here
		}
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


	// TODO: look into (one hour)
	/*
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
    */


	// Method to produce a short summary of a group of news articles from a particular day,
	// relating to a particular event.
	public List<ResponseJsonObject> summarise(List<NewsObject> news) {
		List<ResponseJsonObject> summaries = new ArrayList<>();
		long dayInMillis = 86400000;

		//http://stackoverflow.com/questions/28578072/split-java-util-date-collection-by-days
		Multimap<Long, NewsObject> newsByDay = ArrayListMultimap.create();

		for (NewsObject newsObject : news) { // TODO: fix in a list
			long newsDateInMillis = newsObject.getTimestamp().getTime();
			long day = newsDateInMillis / dayInMillis;
			newsByDay.put(day, newsObject);
		}

		for (long day : newsByDay.keySet()) {
			ResponseJsonObject retrievedSummaries = null;
			long dateInMillis = day * dayInMillis;
			Collection<NewsObject> dailyNews = newsByDay.get(day);
			Set<String> urls = new HashSet<>();

			for (NewsObject newsObject : dailyNews) {
				urls.add(newsObject.getUrl());
			}

			for (String url : urls) {
				retrievedSummaries = getSummaries(url);
				if (retrievedSummaries != null) { // adding summary only if it is meaningful
					retrievedSummaries.setDate(new Date(dateInMillis));
					summaries.add(retrievedSummaries);
				}
			}
		}

		return summaries;
	}


    public ResponseJsonObject getSummaries(String url) {
		ResponseJsonObject summaries = new ResponseJsonObject();
        try {
            Client client = Client.create();

            WebResource Summariser = client.resource(SUMMARISATION_SERVER_URL + "?url=" + url);

            ClientResponse response = Summariser.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

			// TODO: try to use only GSON, because it makes sense ... but I can't understand why there is an exception and how to solve it, when try to decode to ResponseJsonObject

			String stringResponse = response.getEntity(String.class);
			if (stringResponse == null || stringResponse.trim().equals("")) {
				return null;
			}

			System.out.println("response: '" + stringResponse + "'");
			JSONObject jsonObject = new JSONObject(stringResponse);
			summaries.setCategory(jsonObject.getString("category"));
			List<SummaryObject> summaryObjects = new Gson().fromJson(jsonObject.getString("summaries"), new TypeToken<ArrayList<SummaryObject>>(){}.getType());
			summaries.setSummaries(summaryObjects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**/

        return summaries;
    }






	














	private class NewsModel {
		public List<NewsObject> addNews(String event, int eventId) throws IOException, FeedException {
			List<NewsObject> news = new ArrayList<>();

			URL url = new URL(getEventUrl(event));
			Reader r = new InputStreamReader(url.openStream());
			SyndFeed feed = new SyndFeedInput().build(r);

			List<SyndEntry> entryList = feed.getEntries();

			if (entryList.size() > 0) {
				for (int i = 0; i < entryList.size(); i++) { // was: for (int i = 0; i < 2; i++) // TODO: what is 2 and why in this case?
					SyndEntry entry = entryList.get(i);
					String entryUrl = entry.getUri().substring(33); // TODO: where 33 comes from?
					String title = entry.getTitle();
					Date date = entry.getPublishedDate();

					NewsObject newsObject = new NewsObject()
							.setEventId(eventId)
							.setEventId(1)
							.setTitle(title)
							.setUrl(entryUrl)
							.setLogoUrl("")
							.setTimestamp(date);


					try {
						insertNews(newsObject);
						news.add(newsObject);
					} catch (ConflictException e) {
						// TODO: do nothing, if it exists
					}
				}
			}

			return news;
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
	}


	// TODO: refactor this class
	private class TweetModel {
		private Configuration twitterConfiguration;


		public TweetModel(Configuration twitterConfiguration) {
			this.twitterConfiguration = twitterConfiguration;
		}


		private void insertTweets(List<TweetObject> tweets) {
			TweetHelper helper = new TweetHelper(connection);
			for (TweetObject tweet : tweets) {
				try {
					insertTweet(helper, tweet);
				} catch (ConflictException e) {
					e.printStackTrace();
				}

				helper.create(tweet);
			}
		}


		private void insertTweet(TweetHelper helper, TweetObject tweet) throws ConflictException {
			//If if is not null, then check if it exists. If yes, throw an Exception that it is already present
			if (tweet.getId() != 0 && helper.exists(tweet.getId())) {
				throw new ConflictException("Object already exists");
			}
		}


		public Date getMostRecentTweetTime(List<TweetObject> tweets) {
			Date timestamp = new Date(0);

			for (TweetObject tweet : tweets) {
				Date tweetTimestamp = tweet.getTimestamp();
				if (tweetTimestamp.after(timestamp)) {
					timestamp = tweetTimestamp;
				}
			}

			return timestamp;
		}


		private Set<String> getImagesFromTweets(List<TweetObject> tweets) {
			Set<String> images = new HashSet<>();
			for (TweetObject tweet : tweets) {
				if (!Objects.equals(tweet.getImageUrl(), "")) {
					images.add(tweet.getImageUrl());
				}
			}

			return images;
		}


		private List<TweetObject> getTweets(int eventId, String eventHeading) throws TwitterException {
			// Create a new instance of a TwitterFactory to pull data from twitter
			TwitterFactory tf = new TwitterFactory(twitterConfiguration);
			Twitter twitter = tf.getInstance();

			// For each trending event pull the top 10 most popular tweets

			twitter4j.Query query = new twitter4j.Query(eventHeading);
			query.count(10);
			query.lang("en");
			query.resultType(Query.ResultType.mixed);

			List<TweetObject> tweets = new ArrayList<>();
			QueryResult result = twitter.search(query);
			for (Status status : result.getTweets()) {
				if (!status.isRetweet() && !status.isPossiblySensitive()) {
					String imageUrl = "";

					if (status.getMediaEntities().length > 0) {
						imageUrl = status.getMediaEntities()[0].getMediaURL();
					}

					tweets.add(new TweetObject()
							.setEventId(eventId) // TODO fix;
							.setId(status.getId())
							.setUsername(status.getUser().getName())
							.setScreenName(status.getUser().getScreenName())
							.setProfileImgUrl(status.getUser().getBiggerProfileImageURL())
							.setImageUrl(imageUrl)
							.setText(status.getText())
							.setTimestamp(status.getCreatedAt())
					);
				}
			}

			return tweets;
		}
	}


	private class ResponseJsonObject {
		private String category;
		private List<SummaryObject> summaries;
		private Date date;


		public String getCategory() {
			return category;
		}


		public ResponseJsonObject setCategory(String category) {
			this.category = category;
			return this;
		}


		public List<SummaryObject> getSummaries() {
			return summaries;
		}


		public ResponseJsonObject setSummaries(List<SummaryObject> summaries) {
			this.summaries = summaries;
			return this;
		}


		public ResponseJsonObject setDate(Date date) {
			this.date = date;
			return this;
		}


		public Date getDate() {
			return date;
		}
	}
}