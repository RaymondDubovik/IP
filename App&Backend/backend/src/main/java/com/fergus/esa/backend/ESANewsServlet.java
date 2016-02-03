package com.fergus.esa.backend;

import com.fergus.esa.backend.MySQLHelpers.MySQLJDBC;
import com.fergus.esa.backend.MySQLHelpers.NewsHelper;
import com.fergus.esa.backend.dataObjects.NewsObject;
import com.google.api.server.spi.response.ConflictException;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ESANewsServlet extends HttpServlet {
    // An array of strings to hold current events
    private HashSet<String> events = new HashSet<>();

	private Connection connection;


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		connection = (new MySQLJDBC()).getConnection();

		getEvents();
        try {
            getNewsArticles();
        } catch (IllegalArgumentException | FeedException | ConflictException e) {
            e.printStackTrace();
        }
    }


    public void getEvents() {
        try {
            Document doc = Jsoup.connect("https://news.google.co.uk/").get();

            Elements topics = doc.select("div.topic");

            for (Element t : topics) {
                events.add(t.text());
            }
        } catch (IOException e) {
            getEvents();
        }
    }


    public void getNewsArticles() throws IOException, IllegalArgumentException, FeedException, ConflictException {
        String feedUrl;
        String cleanEvent;

        for (String event : events) {
            cleanEvent = getCleanEvent(event);
            event = removeSuffix(removeAccents(event));
            feedUrl = "http://news.google.com/news?q=" + cleanEvent + "&output=rss";

            URL url = new URL(feedUrl);
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
							//.setEventId(event.get) // TODO: implement
							.setEventId(1)
							.setTitle(title)
							.setUrl(entryUrl)
							.setLogoUrl("")
							.setTimestamp(date);

					insertNews(news);
                }
            }
        }
    }


    public String getCleanEvent(String event) {
        String cleanEvent;
        if (event.contains(" ")) {
            cleanEvent = event.replace(" ", "+");
        } else {
            cleanEvent = event;
        }
        return cleanEvent;
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

