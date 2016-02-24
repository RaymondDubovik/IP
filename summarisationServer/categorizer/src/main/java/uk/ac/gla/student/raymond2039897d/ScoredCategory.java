package uk.ac.gla.student.raymond2039897d;

/**
 * Created by svchost on 24/02/16.
 */
public class ScoredCategory {
    private String category;
    private double score;


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
