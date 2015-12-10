package com.fergus.esa.backend.dataObjects;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
public class SummaryObject {
    private int length;
    private String text;


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
}
