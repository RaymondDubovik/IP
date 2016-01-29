package com.fergus.esa.backend.dataObjects;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 29/01/2016
 */
public class CategoryRatingObject {
	private int categoryId;
	private int hits;
	private int time;


	public CategoryRatingObject() {}


	public int getCategoryId() {
		return categoryId;
	}


	public CategoryRatingObject setCategoryId(int categoryId) {
		this.categoryId = categoryId;
		return this;
	}


	public int getHits() {
		return hits;
	}


	public CategoryRatingObject setHits(int hits) {
		this.hits = hits;
		return this;
	}


	public int getTime() {
		return time;
	}


	public CategoryRatingObject setTime(int time) {
		this.time = time;
		return this;
	}
}
