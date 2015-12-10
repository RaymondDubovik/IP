package com.fergus.esa.dataObjects;

import java.util.Date;
/*
    A class that models an individual summary
 */

public class ESASummary implements Comparable<ESASummary> {
    private String summary;
    private Date date;


    public ESASummary(String summary, Date date) {
        this.summary = summary;
        this.date = date;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }


    public Date getDate() {
        return date;
    }


    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(ESASummary other) {
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
