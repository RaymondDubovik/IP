package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
@Entity
public class SummaryObject {
    private int length;
    private String text;
    private int eventId;


    public SummaryObject() {}


    public int getLength() {
        return length;
    }


    public SummaryObject setLength(int length) {
        this.length = length;
        return this;
    }


    public String getText() {
        return text;
    }


    public SummaryObject setText(String text) {
        this.text = text;
        return this;
    }


    public int getEventId() {
        return eventId;
    }


    public SummaryObject setEventId(int eventId) {
        this.eventId = eventId;
        return this;
    }
}
