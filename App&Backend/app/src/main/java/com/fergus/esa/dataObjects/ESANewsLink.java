package com.fergus.esa.dataObjects;

import java.util.Date;

/*
A class that models a news link
 */
public class ESANewsLink implements Comparable<ESANewsLink> {
    private String title;
    private String url;
    private Date date;


    public ESANewsLink(String title, String url, Date date) {
        this.title = title;
        this.url = url;
        this.date = date;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public Date getDate() {
        return date;
    }


    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(ESANewsLink other) {
        long thisDate = this.getDate().getTime();
        long otherDate = other.getDate().getTime();

        if (thisDate < otherDate)
            return 1;
        else if (thisDate == otherDate)
            return 0;
        else
            return -1;
    }
}