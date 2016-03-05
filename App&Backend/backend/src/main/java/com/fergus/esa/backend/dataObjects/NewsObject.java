package com.fergus.esa.backend.dataObjects;

import com.fergus.esa.backend.categorizer.ScoredCategoryObject;
import com.googlecode.objectify.annotation.Entity;

import java.util.Date;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class NewsObject {
    private int id;
    private String title;
    private String url;
    private String logoUrl;
    private Date timestamp;
    private int eventId;

	private List<ScoredCategoryObject> categories;


    public NewsObject() {}


    public int getId() {
        return id;
    }


    public NewsObject setId(int id) {
        this.id = id;
        return this;
    }


    public String getTitle() {
        return title;
    }


    public NewsObject setTitle(String title) {
        this.title = title;
        return this;
    }


    public String getUrl() {
        return url;
    }


    public NewsObject setUrl(String url) {
        this.url = url;
        return this;
    }


    public String getLogoUrl() {
        return logoUrl;
    }


    public NewsObject setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }


    public Date getTimestamp() {
        return timestamp;
    }


    public NewsObject setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }


    public int getEventId() {
        return eventId;
    }


    public NewsObject setEventId(int eventId) {
        this.eventId = eventId;
        return this;
    }


	public List<ScoredCategoryObject> getCategories() {
		return categories;
	}


	public NewsObject setCategories(List<ScoredCategoryObject> categories) {
		this.categories = categories;
		return this;
	}


	public boolean isNew() {
		return categories == null; // if we don't know the categories of the news article, then the news article was not yet processed and inserted in the database.
	}
}
