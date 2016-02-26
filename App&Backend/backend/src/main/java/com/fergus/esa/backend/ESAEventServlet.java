package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.CategoryHelper;
import com.fergus.esa.backend.MySQLHelpers.EventHelper;
import com.fergus.esa.backend.MySQLHelpers.ImageHelper;
import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.MySQLHelpers.SummaryHelper;
import com.fergus.esa.backend.MySQLHelpers.TweetHelper;
import com.fergus.esa.backend.categorizer.CategoryPicker;
import com.fergus.esa.backend.categorizer.ESACategoryPicker;
import com.fergus.esa.backend.categorizer.ScoredCategory;
import com.fergus.esa.backend.dataObjects.CategoryObject;
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
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	private Connection connection;


	private static Configuration getTwitterConfiguration() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("AAfSJDnui3xQTegXr2GOogBAp");
		cb.setOAuthConsumerSecret("pVqlDf3ySpwRtz9ePr0mvYmx0ob9HwIF17IwpfsgfLRdo5VBKI");
		cb.setOAuthAccessToken("3345365331-7amtdWHIU2U98JJyTLWwm2ewFpxJ61YIkRraEWh");
		cb.setOAuthAccessTokenSecret("Mm9Efm05yZqALZ0bkSVVRwrK08j9NZ6YQryAZiUuPgY4a");

		return cb.build();
	}


	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		connection = (new MySQLJDBC()).getConnection();

		/*
		try {
            SchemaCreator schemaCreator = new SchemaCreator();
            schemaCreator.drop(connection);
            schemaCreator.create(connection);
            // schemaCreator.populateWithMockData(connection);
            schemaCreator.populate(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
		*/

		try {
			storeData(getEventHeadings());
		} catch (IOException | FeedException | TwitterException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException ignored) {}
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
		EventHelper eventHelper = new EventHelper(connection);
		CategoryHelper categoryHelper = new CategoryHelper(connection);
		SummaryHelper summaryHelper = new SummaryHelper(connection);
		ImageHelper imageHelper = new ImageHelper(connection);

		List<CategoryObject> allCategoriesList =  categoryHelper.getCategories();

		Map<String, Integer> allCategories = new HashMap<>();
		for (CategoryObject category : allCategoriesList) {
			allCategories.put(category.getName().toLowerCase(), category.getId());
		}

		for (String heading : eventHeading) {
			boolean newEvent = false;
			heading = removeSuffix(removeAccents(heading));
			System.out.println("----------" + heading + "----------");

			EventObject event = new EventObject()
					.setHeading(heading);
			int eventId = eventHelper.getIdByHeading(event.getHeading());

			if (eventId == 0) {
				eventId = eventHelper.create(event.setImageUrl(""));
				newEvent = true;
			} else {
				categoryHelper.deleteCagetogies(eventId);
			}
			System.out.println("eventId: " + eventId);

			TweetModel tweetModel = new TweetModel(getTwitterConfiguration());
			List<TweetObject> tweets = tweetModel.getTweets(eventId, heading);
			tweetModel.insertTweets(tweets);

			Set<String> imageUrls = tweetModel.getImagesFromTweets(tweets);
			String mainImageUrl = null;
			for (String imageUrl : imageUrls) {
				if (newEvent && mainImageUrl == null) {
					mainImageUrl = imageUrl;
				}

				if (!imageHelper.exists(imageUrl)) {
					imageHelper.create(imageUrl, eventId);
				}
			}

			CategoryPicker categoryPicker = new ESACategoryPicker();
			List<NewsObject> news = new NewsModel().addNews(heading, eventId);
			List<ResponseJsonObject> responses = summarise(news);
			for (ResponseJsonObject response: responses) {
				List<SummaryObject> summaries = response.getSummaries();
				if (summaries != null) {
					for (SummaryObject summary : summaries) {
						summaryHelper.create(summary, eventId);
					}
				}

				categoryPicker.addCategories(response.getCategories());
			}

			List<String> relevantCategories = categoryPicker.getRelevantCategories();
			for (String relevantCategory : relevantCategories) {
				categoryHelper.addCategory(allCategories.get(relevantCategory), eventId);
			}

			System.out.println();

			if (newEvent) {
				// categoryPicker.getBestMatch();
				// TODO: send a push notification to the relevant recipients

				/*
				GcmObject gcmObject = new GcmObject(gcmToken, "SomeTextHere", "SomeTitleHere").setData("{\"id\":5}"); // TODO: change
				new GcmSender().sendNotification(gcmObject.toJson());
				*/
			}

			// finish populating the event object
			event.setId(eventId)
					.setHeading(heading)
					.setImageUrl(mainImageUrl)
					.setTimestamp(tweetModel.getMostRecentTweetTime(tweets));

			if (mainImageUrl != null) {
				event.setImageUrl(mainImageUrl);
			}

			eventHelper.update(event);
		}
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
			ResponseJsonObject retrievedSummaries;
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
			client.setConnectTimeout(8000);
			client.setReadTimeout(8000);

            WebResource summariser = client.resource(SUMMARISATION_SERVER_URL + "?url=" + url);

            ClientResponse response = summariser.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

			// TODO: try to use only GSON, because it makes sense ... but I can't understand why there is an exception and how to solve it, when try to decode to ResponseJsonObject
			String stringResponse = response.getEntity(String.class);
			if (stringResponse == null || stringResponse.trim().equals("")) {
				return null;
			}

			JSONObject jsonObject = new JSONObject(stringResponse);

			String categoriesJson = jsonObject.getString("categoriesJson");
			if (categoriesJson == null || categoriesJson.trim().equals("")) {
				return null;
			}

			List<ScoredCategory> scoredCategoryObjects = new Gson().fromJson(categoriesJson, new TypeToken<ArrayList<ScoredCategory>>(){}.getType());
			summaries.setCategories(scoredCategoryObjects);
			List<SummaryObject> summaryObjects = new Gson().fromJson(jsonObject.getString("summaries"), new TypeToken<ArrayList<SummaryObject>>(){}.getType());
			summaries.setSummaries(summaryObjects);

        } catch (Exception e) {
            e.printStackTrace();
			return null;
        }

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
				// TODO:
				// TODO:
				// TODO:
				// TODO:
				for (int i = 0; i < 2; i++) { // was: for (int i = 0; i < 2; i++) // TODO: what is 2 and why in this case?
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
					} catch (ConflictException ignored) {} // does nothing, if news article already exists in the database
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
		private List<ScoredCategory> categories;
		private List<SummaryObject> summaries;
		private Date date;


		public List<ScoredCategory> getCategories() {
			return categories;
		}


		public ResponseJsonObject setCategories(List<ScoredCategory> category) {
			this.categories = category;
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