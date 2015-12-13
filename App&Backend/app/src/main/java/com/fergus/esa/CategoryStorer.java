package com.fergus.esa;

import android.content.Context;

import com.fergus.esa.backend.esaEventEndpoint.model.CategoryObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 05.11.2015
 */
public class CategoryStorer {

    private static final String KEY = "categories";
    private static final String FILENAME = "categories";
    private static final String DIR = "data";
    // private static final String KEY = "categories";


    private Context context;


    // TODO: category object is NOT SERIALIZABLE, cant work. either create a wrapper or store as JSON!

    // TODO:
    // TODO:
    // TODO:
    // TODO:
    // TODO:
    // create a constructor that checks if the file exists
    // if not, it creates a file and writes an empty HashSet to it
    // TODO:
    // TODO:
    // TODO:
    // TODO:
    // TODO:


    public CategoryStorer(Context context) {
        this.context = context;
        if (!fileExists()) {
            storeCategories(new HashSet<CategoryObject>());
        }
    }


    public HashSet<CategoryObject> getSelectedCategories() {
        HashSet categories = null;
        try {
            // File file = new File(context.getFilesDir(), KEY);
            ObjectInputStream inputStream = new ObjectInputStream(context.openFileInput(FILENAME));
            categories = (HashSet) inputStream.readObject(); // TODO: check
            inputStream.close();
        } catch (IOException|ClassNotFoundException e) {
            // TODO: fix
            e.printStackTrace();
            return new HashSet<>();
        }

        return categories;
    }


    public void addCategory(CategoryObject category) {
        HashSet<CategoryObject> categories = getSelectedCategories();
        categories.add(category);
        storeCategories(categories);
    }


    public void removeCategory(CategoryObject category) {
        HashSet<CategoryObject> categories = getSelectedCategories();
        categories.remove(category);
        storeCategories(categories);
    }


    public int getCount() {
        return getSelectedCategories().size();
    }


    private void storeCategories(HashSet<CategoryObject> categories) {
        try {
            //File file = new File(context.getFilesDir(), FILENAME);
            ObjectOutputStream outputStream = new ObjectOutputStream(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
            outputStream.writeObject(categories);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO: fix
            e.printStackTrace();
        }
    }


    private boolean fileExists() {
        return context.getFileStreamPath(FILENAME).exists();
    }
}