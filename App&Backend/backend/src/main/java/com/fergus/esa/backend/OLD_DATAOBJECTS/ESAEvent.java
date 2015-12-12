package com.fergus.esa.backend.OLD_DATAOBJECTS;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

@Entity
public class ESAEvent {
    @Id
    private String event;
    private List<ESATweet> tweets;
    private List<ESANews> news;
    private List<String> imageUrls;
    private List<String> summaries;
    @Index
    private Long timestamp;


    public ESAEvent() {}


    public String getEvent() {
        return event;
    }


    public ESAEvent setEvent(String event) {
        this.event = event;
        return this;
    }


    public List<ESATweet> getTweets() {
        return tweets;
    }


    public ESAEvent setTweets(List<ESATweet> tweets) {
        this.tweets = tweets;
        return this;
    }


    public List<ESANews> getNews() {
        return news;
    }


    public ESAEvent setNews(List<ESANews> news) {
        this.news = news;
        return this;
    }


    public List<String> getImageUrls() {
        return imageUrls;
    }


    public ESAEvent setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        return this;
    }


    public List<String> getSummaries() {
        return summaries;
    }


    public ESAEvent setSummaries(List<String> summaries) {
        this.summaries = summaries;
        return this;
    }


    public Long getTimestamp() {
        return timestamp;
    }


    public ESAEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}