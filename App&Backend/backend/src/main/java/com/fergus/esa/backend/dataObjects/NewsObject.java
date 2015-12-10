package com.fergus.esa.backend.dataObjects;

import java.util.Date;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
public class NewsObject {
    private int id;
    private String title;
    private String url;
    private String logoUrl;
    private Date timestamp;


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
}
