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

    public ESAEvent() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<ESATweet> getTweets(){
        return tweets;
    }

    public void setTweets(List<ESATweet> tweets){
        this.tweets = tweets;
    }

    public List<ESANews> getNews(){
        return news;
    }

    public void setNews(List<ESANews> news){
        this.news = news;
    }

    public List<String> getImageUrls(){
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls){
        this.imageUrls = imageUrls;
    }

    public List<String> getSummaries(){
        return summaries;
    }

    public void setSummaries(List<String> summaries){
        this.summaries = summaries;
    }

    public Long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(Long timestamp){
        this.timestamp = timestamp;
    }
}