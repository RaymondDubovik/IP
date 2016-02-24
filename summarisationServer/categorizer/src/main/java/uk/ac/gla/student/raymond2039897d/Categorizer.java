package uk.ac.gla.student.raymond2039897d;

import com.aliasi.classify.*;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.AbstractExternalizable;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 15.02.2016
 */
public class Categorizer {
    private static final int ARTICLE_RESOLVE_TIMEOUT = 2000; // 2 seconds

    private File serializableFile;
    private JointClassifier<CharSequence> compiledClassifier;


    public Categorizer(File serializableFile) {
        this.serializableFile = serializableFile;
    }


    public Categorizer init() throws IOException, ClassNotFoundException {
        compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable.readObject(serializableFile);
        return this;
    }


    public void trainClassifier(File trainingDirectory, int ngramSize) throws IOException {
        CharSequence article = "";

        String[] categories = trainingDirectory.list();
        DynamicLMClassifier<NGramProcessLM> classifier = DynamicLMClassifier.createNGramProcess(categories, ngramSize);
        for (String category : categories) {
            File file = new File(trainingDirectory, category);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    article = getArticle(line);
                }
            } catch (Exception ignored) {}

            if (article.length() != 0) {
                System.out.println("Training on article with length:" + article.length());
                Classification classification = new Classification(category);
                Classified<CharSequence> classified = new Classified<>(article, classification);
                classifier.handle(classified);
            }
        }

        System.out.println("Compiling");
        AbstractExternalizable.compileTo(classifier, serializableFile);
    }


    public void evaluate(File testDirectory) {
        int correct = 0;
        int total = 0;
        String text;
        String[] categories = testDirectory.list();
        for (String category : categories) {
            File file = new File(testDirectory, category);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    text = getArticle(line);
                    if (text.length() != 0) {
                        String bestCategory = guess(text);

                        total++;
                        if (category.equals(bestCategory)) {
                            correct++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(correct + "/" + total + " guessed correctly");
    }


    public String guess(String text) {
        return compiledClassifier.classify(text).bestCategory();
    }


    public boolean exists() {
        return serializableFile.isFile();
    }


    private static String getArticle(String url) throws Exception {
        JResult result = new HtmlFetcher().fetchAndExtract(url, ARTICLE_RESOLVE_TIMEOUT, true);
        return result.getText();
    }
}
