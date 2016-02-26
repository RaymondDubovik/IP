package main.java.uk.ac.gla.student.raymond2039897d;

import com.google.gson.Gson;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by svchost on 08/02/16.
 * <p/>
 * First run Install.PL in MEAD directory
 *
 * /mead/bin/addons/formatting/*.pm files need to be added to the perl path (read /mead/bin/addons/formatting/Readme)
 * the above didn't work for me, so I added them manually to the perl directory ( /usr/lib/perl/_perl_version_here )
 *
 * next, give full permissions to the data folder for the user that tomcat and perl are running on (your user)
 */
@WebServlet(name = "Servlet")
public class Servlet extends HttpServlet {
    /** Path to categorisation */
    private static final String CATEGORIZATION_LOCATION = "/home/svchost/Desktop/shared/categorizer/categorizer.jar";
    /** Path to MEAD */
    private static final String MEAD_LOCATION = "/home/svchost/Desktop/mead/";
    public static final int ARTICLE_RESOLVE_TIMEOUT = 1000;
    public static final int RETRY_COUNT = 2;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter("url");
        boolean summarize = new Boolean(request.getParameter("summarize"));

        System.out.println("summarize: " + summarize);

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        for (int retryCount = 0; retryCount < RETRY_COUNT; retryCount++) {
            try {
                out.println(summarise(url, summarize));
                return; // if we were able to summarise, then exit here
            } catch (Exception e) { // if we were not able to summarise, then output exception and try again, if retryCount < RETRY_COUNT
                System.out.println("Could not summarise: " + e.getMessage());
                // e.printStackTrace();
            }
        }

        out.println(""); // if above failed, then output empty string;
    }


    public String summarise(String url, boolean summarize) throws Exception {
        String article;
        try {
            article = getArticle(url);
        } catch (Exception e) {
            throw new Exception(e);
        }

        if (article.length() == 0) { // if could not retrieve article, then throw an exception
            throw new Exception("Could not retrieve article");
        }

        List<SummaryObject> summaries;
        if (summarize) {
            String folderName = UUID.randomUUID().toString();
            final String folderAbsolutePath = MEAD_LOCATION + "data/" + folderName;

            initFiles(article, folderName, folderAbsolutePath);

            execute("perl " + MEAD_LOCATION + "bin/addons/formatting/text2cluster.pl " + folderAbsolutePath);

            summaries = summarize(folderAbsolutePath);

            execute("rm -r " + MEAD_LOCATION + "data/" + folderName);
        } else {
            summaries = null;
        }

        String categoriesJson = getCategory(article);
        return new Gson().toJson(new ResponseJsonObject(categoriesJson, summaries));
    }


    private List<SummaryObject> summarize(String folderAbsolutePath) throws IOException {
        List<SummaryObject> summaries = new ArrayList<>();
        for (int length = 75; length <= 135; length += 15) {
            String summary = "";
            Process meadSummarise = Runtime.getRuntime().exec("perl " + MEAD_LOCATION + "bin/mead.pl -w -a " + length + " " + folderAbsolutePath);

            BufferedReader in = new BufferedReader(new InputStreamReader(meadSummarise.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                line = line.substring(5);
                if (!line.equals("")) {
                    summary = summary + " " + line;
                }
            }

            summaries.add(new SummaryObject().setText(summary).setLength(length));
        }
        return summaries;
    }


    private void initFiles(String article, String folderName, String folderAbsolutePath) throws IOException {
        execute("mkdir " + folderAbsolutePath);

        File articleFile = new File(folderAbsolutePath + "/0");

        articleFile.createNewFile();

        FileWriter writer = new FileWriter(articleFile);
        writer.write(article);
        writer.flush();
        writer.close();

        String clusterXML = "";
        StringBuilder clusterBuilder = new StringBuilder();
        clusterBuilder.append("<?xml version='1.0'?>\n");
        clusterBuilder.append("<CLUSTER LANG='ENG'>\n");
        clusterBuilder.append("\t<D DID='0' />\n");
        clusterBuilder.append("</CLUSTER>\n");

        clusterXML = clusterBuilder.toString();

        File clusterFile = new File(folderAbsolutePath + "/" + folderName + ".cluster");

        clusterFile.createNewFile();

        FileWriter clusterWriter = new FileWriter(clusterFile);
        clusterWriter.write(clusterXML);
        clusterWriter.flush();
        clusterWriter.close();
    }


    private String getCategory(String article) throws IOException {
        String categoriesJson = "";

        String command = "java -cp " + CATEGORIZATION_LOCATION + " uk.ac.gla.student.raymond2039897d.Categorize -c \"" + article + "\"";
        Process categorize = Runtime.getRuntime().exec(command);

        BufferedReader in = new BufferedReader(new InputStreamReader(categorize.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("line: " + line);
            if (!line.equals("")) {
                categoriesJson = line;
            }
        }

        System.out.println(categoriesJson);
        return categoriesJson;
    }


    private static String execute(String command) {
        StringBuilder sb = new StringBuilder();
        String[] commands = new String[]{"/bin/bash", "-c", command};
        try {
            Process proc = Runtime.getRuntime().exec(commands);
            //Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            while ((s = stdError.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e) {
            return e.getMessage();
        }

        return sb.toString();
    }


    private String getArticle(String url) throws Exception {
        JResult result = new HtmlFetcher().fetchAndExtract(url, ARTICLE_RESOLVE_TIMEOUT, true);
        return result.getText();
    }


    private class ResponseJsonObject {
        private String categoriesJson;
        private List<SummaryObject> summaries;


        public ResponseJsonObject(String categoriesJson, List<SummaryObject> summaries) {
            this.categoriesJson = categoriesJson;
            this.summaries = summaries;
        }
    }

    
    private class SummaryObject {
        private int length;
        private String text;


        public SummaryObject() {}


        public int getLength() {
            return length;
        }


        public SummaryObject setLength(int length) {
            this.length = length;
            return this;
        }


        public String getText() {
            return text;
        }


        public SummaryObject setText(String text) {
            this.text = text;
            return this;
        }
    }
}
