package com.fergus.esa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashSet;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 05.11.2015
 */
public class CategoryStorer {
    private SharedPreferences sharedPreferences;
    private Gson gson;


    public CategoryStorer(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }


    public void addCategory(CategoryObject category) {
        HashSet<Integer> categories = getSelectedCategories();
        categories.add(category.getId());
        storeCategories(categories);
    }


    public void removeCategory(CategoryObject category) {
        HashSet<Integer> categories = getSelectedCategories();
        categories.remove(category.getId());
        storeCategories(categories);
    }

    public int getCount() {
        return getSelectedCategories().size();
    }


    public boolean hasCategory(CategoryObject category) {
        return getSelectedCategories().contains(category.getId());
    }


    private HashSet<Integer> getSelectedCategories() {
        String categoriesJson = sharedPreferences.getString(SharedPreferencesKeys.SELECTED_CATEGORIES, null);
        if (categoriesJson == null) {
            return new HashSet<Integer>();
        }

        return gson.fromJson(categoriesJson, new TypeToken<HashSet<Integer>>(){}.getType());
    }


    private void storeCategories(HashSet<Integer> categories) {
        sharedPreferences.edit().putString(SharedPreferencesKeys.SELECTED_CATEGORIES, gson.toJson(categories)).commit();
    }
}