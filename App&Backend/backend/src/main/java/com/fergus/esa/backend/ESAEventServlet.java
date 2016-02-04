package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.EventHelper;
import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.MySQLHelpers.TweetHelper;
import com.fergus.esa.backend.dataObjects.EventObject;
import com.fergus.esa.backend.dataObjects.NewsObject;
import com.fergus.esa.backend.dataObjects.TweetObject;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
public class ESAEventServlet extends HttpServlet {
    // private  events = new HashSet<>();
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
		String feedUrl;

		for (String heading : eventHeading) {
			heading = removeSuffix(removeAccents(heading));

			// TODO: add event here and retrieve event Id:
			int eventId = addEvent(heading);

			new NewsModel().addNews(heading, eventId);
			TweetModel tweetModel = new TweetModel(getTwitterConfiguration());
			List<TweetObject> tweets = tweetModel.getTweets(eventId, heading);
			insertTweets(tweets);

			for (TweetObject tweet: tweets) {
				// TODO: store the tweet in the database
			}

			Set<String> imageUrls = getImagesFromTweets(tweets);
			for (String image : imageUrls) {
				// TODO insert images for the event here;
			}

			EventObject event = new EventObject()
					.setId(eventId)
					.setHeading(heading)
					.setTimestamp(getMostRecentTweetTime(tweets));

			// TODO: store event summaries
			// List<String> eventSummaries = summarise(eventNews);

			/*
			List<ESANews> eventNews = listEventNews(e);
            List<ESATweet> eventTweets = listEventTweets(e);



			Long timestamp = getMostRecentTweetTime(eventTweets);

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


/*

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

    */


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






	














	private class NewsModel {
		public void addNews(String event, int eventId) throws IOException, FeedException {
			URL url = new URL(getEventUrl(event));
			Reader r = new InputStreamReader(url.openStream());
			SyndFeed feed = new SyndFeedInput().build(r);

			List<SyndEntry> entryList = feed.getEntries();

			if (entryList.size() > 0) {
				for (int i = 0; i < 2; i++) { // TODO: what is 2 in this case?
					SyndEntry entry = entryList.get(i);
					String entryUrl = entry.getUri().substring(33); // TODO: where 33 comes from?
					String title = entry.getTitle();
					Date date = entry.getPublishedDate();

					NewsObject news = new NewsObject()
							.setEventId(eventId)
							.setEventId(1)
							.setTitle(title)
							.setUrl(entryUrl)
							.setLogoUrl("")
							.setTimestamp(date);

					try {
						insertNews(news);
					} catch (ConflictException e) {
						// TODO: do nothing, if it exists
					}
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
}