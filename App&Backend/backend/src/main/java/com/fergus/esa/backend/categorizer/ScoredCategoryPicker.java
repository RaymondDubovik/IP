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
public class ScoredCategoryPicker implements CategoryPicker {
	// key stores category, value stores the total entropy of the category
	private Map<String, Double> map;


	public ScoredCategoryPicker() {
		map = new HashMap<>();
	}


	@Override
	public void addCategories(List<ScoredCategoryObject> categories) {
		for (ScoredCategoryObject category : categories) {
			Double categoryTotalEntropy = map.get(category.getName());
			double crossEntropy = category.getScore();

			if (categoryTotalEntropy == null) { // if there is no value for given key, then this category is new (add it to the map)
				categoryTotalEntropy = 0d;
			}

			map.put(category.getName(), categoryTotalEntropy + crossEntropy);
		}
	}


	// Note, that if there are several categories with equal max count, it will return random one.
	@Override
	public String getBestMatch() {
		List<String> maxCategories = new ArrayList<>();
		double max = Integer.MIN_VALUE;
		for (String category : map.keySet()) {
			double current = map.get(category);
			if (current == max) {
				maxCategories.add(category);
			} else if (current > max) {
				max = current;
				maxCategories = new ArrayList<>();
				maxCategories.add(category);
			}
		}

		// return random one from the list, if several categories received the same overall score
		return maxCategories.get(new Random().nextInt(maxCategories.size()));
	}


	/*
		this is the method, where it is decided which categories the topic belongs to
		algorithm is to keep only categories that contained at least THRESHOLD of all rated categories
	*/
	@Override
	public List<String> getRelevantCategories () {
		// TODO: I could not find an algorithm, that would reliably decide which categories to consider as applicable
		return null;
	}
}
