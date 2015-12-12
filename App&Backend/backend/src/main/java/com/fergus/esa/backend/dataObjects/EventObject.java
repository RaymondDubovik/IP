package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

import java.util.Date;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class EventObject {
    private int id;
    private String heading;
    private Date timestamp;
    // TODO: think where users need to be put
    private List<CategoryObject> categories;
    private List<NewsObject> news;
    private List<TweetObject> tweets;
    private List<SummaryObject> summaries;


    public EventObject() {}


    public int getId() {
        return id;
    }


    public EventObject setId(int id) {
        this.id = id;
        return this;
    }


    public String getHeading() {
        return heading;
    }


    public EventObject setHeading(String heading) {
        this.heading = heading;
        return this;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public EventObject setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }


    public List<CategoryObject> getCategories() {
        return categories;
    }


    public EventObject setCategories(List<CategoryObject> categories) {
        this.categories = categories;
        return this;
    }


    public List<NewsObject> getNews() {
        return news;
    }


    public EventObject setNews(List<NewsObject> news) {
        this.news = news;
        return this;
    }


    public List<TweetObject> getTweets() {
        return tweets;
    }


    public EventObject setTweets(List<TweetObject> tweets) {
        this.tweets = tweets;
        return this;
    }


    public List<SummaryObject> getSummaries() {
        return summaries;
    }


    public EventObject setSummaries(List<SummaryObject> summaries) {
        this.summaries = summaries;
        return this;
    }
}
