package com.fergus.esa.backend.dataObjects;

import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 03.12.2015
 */
public class UserObject {
    private int id;
    private String gcmToken;
    private List<CategoryObject> selectedCategories;


    public int getId() {
        return id;
    }


    public UserObject setId(int id) {
        this.id = id;
        return this;
    }


    public String getGcmToken() {
        return gcmToken;
    }


    public UserObject setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
        return this;
    }


    public List<CategoryObject> getSelectedCategories() {
        return selectedCategories;
    }


    public UserObject setSelectedCategories(List<CategoryObject> selectedCategories) {
        this.selectedCategories = selectedCategories;
        return this;
    }
}
