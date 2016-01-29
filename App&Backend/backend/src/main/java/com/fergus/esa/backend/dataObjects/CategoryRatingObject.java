package com.fergus.esa.backend.dataObjects;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public class CategoryRatingObject {
	private int categoryId;
	private int score;
	private int time;


	public CategoryRatingObject() {}


	public int getCategoryId() {
		return categoryId;
	}


	public CategoryRatingObject setCategoryId(int categoryId) {
		this.categoryId = categoryId;
		return this;
	}


	public int getScore() {
		return score;
	}


	public CategoryRatingObject setScore(int score) {
		this.score = score;
		return this;
	}
}
