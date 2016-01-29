package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

import java.util.Date;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class EventObject {
    private int id;
    private String heading;
    private Date timestamp;
    private String mainImageUrl;


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


    public String getImageUrl() {
        return mainImageUrl;
    }


    public EventObject setImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
        return this;
    }
}
