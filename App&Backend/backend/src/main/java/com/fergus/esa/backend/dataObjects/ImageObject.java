package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class ImageObject {
    private String url;
    private int eventId;


    public ImageObject() {}


    public String getUrl() {
        return url;
    }


    public ImageObject setUrl(String url) {
        this.url = url;
        return this;
    }


    public int getEventId() {
        return eventId;
    }


    public ImageObject setEventId(int eventId) {
        this.eventId = eventId;
        return this;
    }
}
