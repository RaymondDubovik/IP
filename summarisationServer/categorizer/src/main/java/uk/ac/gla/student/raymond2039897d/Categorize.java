package uk.ac.gla.student.raymond2039897d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class Categorize {
    private static final String SWITCH_TRAIN = "-t";
    private static final String SWITCH_EVALUATE = "-e";
    private static final String SWITCH_GUESS = "-g";
    private static final String SWITCH_CATEGORIZE = "-c";


    public static void main(String[] args) throws Exception {
        if (args.length <= 0) {
            printHelp();
            return;
        }

        NewsCategorizer categorizer = new NewsCategorizer();
        switch (args[0]) {
            case SWITCH_TRAIN:
                if (args.length <= 1) {
                    categorizer.train();
                } else {
                    int ngramSize;
                    try {
                        ngramSize = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Ngram size must be an integer.");
                        return;
                    }

                    if (ngramSize < 2 || ngramSize > 16) {
                        System.out.println("Invalid ngram size. Allowed values are between 2 and 16.");
                        return;
                    }

                    System.out.println("Training! Please wait.");
                    categorizer.train(ngramSize);
                    System.out.println("Finished training.");
                }
                break;
            case SWITCH_EVALUATE:
                if (!categorizer.exists()) {
                    System.out.println("Categorizer is not trained. Please, train it first.");
                    return;
                }
                System.out.println("Evaluating, please wait");
                categorizer.evaluate();
                break;
            case SWITCH_GUESS:
                if (args.length < 2) {
                    System.out.println("Invalid parameter count. Did you forget to specify the text?");
                    return;
                }
                System.out.println(categorizer.guess(args[1]));
                break;
            case SWITCH_CATEGORIZE:
                if (args.length < 2) {
                    System.out.println("Invalid parameter count. Did you forget to specify the text?");
                    return;
                }

                System.out.print(new Gson().toJson(categorizer.getCategoryScores(args[1])));
                break;
            default:
                printHelp();
        }
    }


    private static void printHelp() {
        System.out.println("Command unknown, please, try again:");
        System.out.println("Guess the category: -g \"text\" ");
        System.out.println("Categorize text: -c \"text\" ");
        System.out.println("Train categorizer with new data: -t [ngram size]");
        System.out.println("Evaluate categorizer on test dataset: -e");
    }
}
