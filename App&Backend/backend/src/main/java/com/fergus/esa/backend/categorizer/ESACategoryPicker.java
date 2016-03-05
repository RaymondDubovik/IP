package com.fergus.esa.backend.categorizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 23.02.2016
 */
public class ESACategoryPicker implements CategoryPicker {
	private static final double THRESHOLD = 0.2;
	private static final int TOP_CATEGORY_COUNT_TO_USE = 3;

	// key stores category, value stores how many sources reported that category
	private Map<String, Integer> map;
	private int categoryCount;


	public ESACategoryPicker() {
		map = new HashMap<>();
		categoryCount = 0;
	}


	@Override
	public void addCategories(List<ScoredCategory> categories) {
		int weight = TOP_CATEGORY_COUNT_TO_USE;
		for (int i = 0; i < TOP_CATEGORY_COUNT_TO_USE; i++) {
			String category = categories.get(i).getName();
			Integer count = map.get(category);

			if (count == null) { // if there is no value for given key, then this category is new (add it to the map)
				count = 0;
			}

			map.put(category, count + weight); // increase the category weight by current weight

			categoryCount += weight;
			weight--;
		}
	}


	// Note, that if there are several categories with equal max count, it will return random one.
	@Override
	public String getBestMatch() {
		List<String> maxCategories = new ArrayList<>();
		int max = Integer.MIN_VALUE;
		for (String category : map.keySet()) {
			int current = map.get(category);
			if (current == max) {
				maxCategories.add(category);
			} else if (current > max) {
				max = current;
				maxCategories = new ArrayList<>();
				maxCategories.add(category);
			}
		}

		// return random one from the list
		return maxCategories.get(new Random().nextInt(maxCategories.size()));
	}


	/*
	this is the method, where it is decided which categories the topic belongs to
	algorithm is to keep only categories that contained at least THRESHOLD of all rated categories
	*/
	@Override
	public List<String> getRelevantCategories () {
		List<String> categories = new ArrayList<>();
		for (String category : map.keySet()) {
			double count = map.get(category);

			double percentage =  count / categoryCount;
			if (percentage >= THRESHOLD) {
				categories.add(category);
			}
		}

		return categories;
	}


	public int getCategoryCount() {
		return categoryCount;
	}
}
