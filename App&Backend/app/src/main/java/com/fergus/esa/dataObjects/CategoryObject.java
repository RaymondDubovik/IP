package com.fergus.esa.dataObjects;

import java.io.Serializable;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 2015.08.11.
 */
public class CategoryObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public static String ALL_CATEGORIES_NAME = "All Categories";
    public static int ALL_CATEGORIES_ID = -1;

    private int id = 0;
    private String name = "";


    public CategoryObject() {}


    public CategoryObject(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public int getId() {
        return id;
    }


    public CategoryObject setId(int id) {
        this.id = id;
        return this;
    }


    public String getName() {
        return name;
    }


    public CategoryObject setName(String name) {
        this.name = name;
        return this;
    }


    public String toJson() {
        return "{id:\"" + id + "\",name:\" " + name + " \"}";
    }
}
