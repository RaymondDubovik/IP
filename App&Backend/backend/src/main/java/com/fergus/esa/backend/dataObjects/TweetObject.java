package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

import java.util.Date;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class TweetObject {
    private long id;
    private String username;
    private String screenName;
    private String profileImgUrl;
    private String imageUrl;
    private String text;
    private Date timestamp;
	private String url;
    private int eventId;


    public TweetObject() {}


    public long getId() {
        return id;
    }


    public TweetObject setId(long id) {
        this.id = id;
        return this;
    }


    public String getUsername() {
        return username;
    }


    public TweetObject setUsername(String username) {
        this.username = username;
        return this;
    }


    public String getScreenName() {
        return screenName;
    }


    public TweetObject setScreenName(String screenName) {
        this.screenName = screenName;
        return this;
    }


    public String getProfileImgUrl() {
        return profileImgUrl;
    }


    public TweetObject setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
        return this;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    public TweetObject setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }


    public String getText() {
        return text;
    }


    public TweetObject setText(String text) {
        this.text = text;
        return this;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public TweetObject setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }


	public String getUrl() {
		return url;
	}


	public TweetObject setUrl(String url) {
		this.url = url;
		return this;
	}


	public int getEventId() {
        return eventId;
    }


    public TweetObject setEventId(int eventId) {
        this.eventId = eventId;
        return this;
    }
}
