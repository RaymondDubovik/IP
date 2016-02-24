package com.fergus.esa.backend.categorizer;

import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 23.02.2016
 */
public interface CategoryPicker {
	public void addCategories(List<ScoredCategory> categories);
	public String getBestMatch();
	public List<String> getRelevantCategories();
}
