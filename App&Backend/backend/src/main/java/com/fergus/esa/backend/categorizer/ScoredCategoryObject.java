package com.fergus.esa.backend.categorizer;

import com.fergus.esa.backend.dataObjects.CategoryObject;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.02.2016
 */
public class ScoredCategoryObject extends CategoryObject {
	private double score;


	public ScoredCategoryObject() {}


	public ScoredCategoryObject(String category, double score) {
		this.score = score;
		this.setName(category);
	}


	public double getScore() {
		return score;
	}


	public ScoredCategoryObject setScore(double score) {
		this.score = score;
		return this;
	}
}