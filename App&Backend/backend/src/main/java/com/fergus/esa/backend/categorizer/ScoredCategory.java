package com.fergus.esa.backend.categorizer;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 24.02.2016
 */
public class ScoredCategory {
	private String category;
	private double score;


	public ScoredCategory() {}


	public ScoredCategory(String category, double score) {
		this.score = score;
		this.category = category;
	}


	public String getCategory() {
		return category;
	}


	public ScoredCategory setCategory(String category) {
		this.category = category;
		return this;
	}


	public double getScore() {
		return score;
	}


	public ScoredCategory setScore(double score) {
		this.score = score;
		return this;
	}
}

