package uk.ac.gla.student.raymond2039897d;

public class Categorize {
    public static void main(String[] args) throws Exception {
        NewsCategorizer categorizer = new NewsCategorizer();
        if (!categorizer.exists()) {
            System.out.println("does not exist");
        }

        // categorizer.evaluate();
        System.out.println(categorizer.guess(args[0]));
    }
}
