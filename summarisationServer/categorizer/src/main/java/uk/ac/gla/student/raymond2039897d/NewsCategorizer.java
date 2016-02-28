package uk.ac.gla.student.raymond2039897d;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 15.02.2016
 *
 * Here you can change parameters of the categorizer
 * To add more training data to the categorizer, simply add links to the news articles in correct categories
 * To add more tests, simply add links to the news articles in correct categories
 *
 * If you wish to add more categories, simply create a new file within FILENAME_TRAINING_DIRECTORY with the name of the category
 * And place links to the articles in there
 * Do the same for the tests
 */
public class NewsCategorizer {
    private static final String PATH_ROOT = "/home/svchost/Desktop/shared/categorizer/";

    /** Directory, which will be used for training */
    private static final String FILENAME_TRAINING_DIRECTORY = PATH_ROOT + "links";
    /** Directory, which will be used for testing */
    private static final String FILENAME_TEST_DIRECTORY = PATH_ROOT + "testLinks";
    /** Name of the file, where the classifier will be serialized to for reuse (we don't want to train it on every request to categorize an article */
    public static final String FILENAME_SERIALIZABLE = PATH_ROOT + "classifier.ser";
    private static int DEFAULT_NGRM_SIZE = 12; // 12 produces a good result of 18/22 correct guesses

    private Categorizer categorizer;


    public NewsCategorizer() {
        categorizer = new Categorizer(new File(FILENAME_SERIALIZABLE));

        if (categorizer.exists()) {
            try {
                categorizer.init();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Could not initialize categorizer, was it serialized properly?");
            }
        }
    }


    public void train(int ngramSize) {
        try {
            categorizer.trainClassifier(new File(FILENAME_TRAINING_DIRECTORY), ngramSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void train() {
        train(DEFAULT_NGRM_SIZE);
    }


    public void evaluate() {
        categorizer.evaluate(new File(FILENAME_TEST_DIRECTORY));
    }


    public boolean exists() {
        return categorizer.exists();
    }


    public String guess(String text) {
        return categorizer.getBestCategory(text);
    }


    public List<ScoredCategory> getCategoryScores(String text) {
        return categorizer.getCategoryScores(text);
    }
}