package main.java.uk.ac.gla.student.raymond2039897d;

import com.google.gson.Gson;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 08/02/16
 */
@WebServlet(name = "Servlet")
public class Servlet extends HttpServlet {
    public static final String PROPERTIES_LOCATION = "config.properties";
    private static final String CATEGORIZER_LAUNCH_STRING = "uk.ac.gla.student.raymond2039897d.Categorize -c";

    private static String CATEGORIZATION_LOCATION;
    private static String CATEGORIZER_FILENAME;
    private static String MEAD_LOCATION;
    private static String MEAD_DATA_LOCATION;
    private static int ARTICLE_RESOLVE_TIMEOUT;
    private static int RETRY_COUNT;


    private static boolean configRead = false;


    private static void readConfigs(ServletContext servletContext) {
        if (configRead) { // if config is already read, skip
            return;
        }

        Properties properties = new Properties();
        try {
            properties.load(servletContext.getResourceAsStream(PROPERTIES_LOCATION));
            CATEGORIZATION_LOCATION = properties.getProperty("categorizationRoot");
            CATEGORIZER_FILENAME = properties.getProperty("categorizerFilename");
            MEAD_LOCATION = properties.getProperty("mead");
            MEAD_DATA_LOCATION = properties.getProperty("dataFolder");
            ARTICLE_RESOLVE_TIMEOUT = Integer.parseInt(properties.getProperty("articleResolveTimeout"));
            RETRY_COUNT = Integer.parseInt(properties.getProperty("retryCount"));
            configRead = true;

            System.out.println(MEAD_LOCATION);
            System.out.println(MEAD_DATA_LOCATION);
        } catch (IOException e) {
            System.err.println("Could not read property file!");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.err.println("Could not parse integer in the property file!");
            System.exit(-1);
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        readConfigs(getServletContext());

        String url = request.getParameter("url");
        boolean summarize = new Boolean(request.getParameter("summarize"));

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        for (int retryCount = 0; retryCount < RETRY_COUNT; retryCount++) {
            try {
                out.println(summarize(url, summarize));
                return; // if we were able to summarize, then exit here
            } catch (Exception e) { // if we were not able to summarize, then output exception and try again, if retryCount < RETRY_COUNT
                System.out.println("Could not summarize: " + e.getMessage());
                // e.printStackTrace();
            }
        }

        out.println(""); // if above failed, then output empty string;
    }


    public String summarize(String url, boolean summarize) throws Exception {
        String article = "";
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
            final String folderAbsolutePath = MEAD_DATA_LOCATION + folderName;

            initFiles(article, folderName, folderAbsolutePath);

            execute("perl " + MEAD_LOCATION + "bin/addons/formatting/text2cluster.pl " + folderAbsolutePath);

            summaries = summarize(folderAbsolutePath);

            execute("rm -r " + MEAD_DATA_LOCATION + folderName);
        } else {
            summaries = null;
        }

        String categoriesJson = getCategory(article);
        return new Gson().toJson(new ResponseJsonObject(categoriesJson, summaries));
    }


    private List<SummaryObject> summarize(String folderAbsolutePath) throws IOException {
        List<SummaryObject> summaries = new ArrayList<>();
        for (int length = 75; length <= 135; length += 15) { // T0D0: remove hardcode
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

        String command = "java -cp " + CATEGORIZER_FILENAME + " " + CATEGORIZER_LAUNCH_STRING + " \"" + article + "\"";

        System.out.println(command);
        Process categorize = Runtime.getRuntime().exec(command, null, new File(CATEGORIZATION_LOCATION));

        BufferedReader in = new BufferedReader(new InputStreamReader(categorize.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("line: " + line);
            if (!line.equals("")) {
                categoriesJson = line;
            }
        }

        System.out.println("categories:" + categoriesJson);
        return categoriesJson;
    }


    private static String execute(String command) {
        StringBuilder sb = new StringBuilder();
        String[] commands = new String[]{"/bin/bash", "-c", command};
        try {
            Process proc = Runtime.getRuntime().exec(commands);
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
