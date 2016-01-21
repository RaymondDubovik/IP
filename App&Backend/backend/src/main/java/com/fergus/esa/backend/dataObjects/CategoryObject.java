package com.fergus.esa.backend.dataObjects;

import com.googlecode.objectify.annotation.Entity;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 2015.08.11.
 */
@Entity
public class CategoryObject {
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
}
