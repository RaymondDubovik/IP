package main.java.uk.ac.gla.student.raymond2039897d;

import com.google.gson.Gson;
import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
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
    private static final String MEAD_LOCATION = "/home/svchost/Desktop/mead/";
    public static final int ARTICLE_RESOLVE_TIMEOUT = 1000;
    public static final int RETRY_COUNT = 3;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter("url");

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                out.println(summarise(url));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String summarise(String url) throws IOException {
        String folderName = UUID.randomUUID().toString();
        final String folderAbsolutePath = MEAD_LOCATION + "data/" + folderName;

        execute("mkdir " + folderAbsolutePath);

        String article = "";
        try {
            article = getArticle(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        execute("perl " + MEAD_LOCATION + "bin/addons/formatting/text2cluster.pl " + folderAbsolutePath);

        List<SummaryObject> summaries = new ArrayList<>();
        for (int length = 75; length <= 135; length += 15) {
            try {
                String summary = "";
                // TODO: execute for different lengths:
                Process meadSummarise = Runtime.getRuntime().exec("perl " + MEAD_LOCATION + "bin/mead.pl -w -a " + length + " " + folderAbsolutePath);

                BufferedReader in = new BufferedReader(new InputStreamReader(meadSummarise.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.substring(5);
                    if (!line.equals("")) {
                        summary = summary + " " + line;
                    }
                }

                System.out.println(summary);

                summaries.add(new SummaryObject().setText(summary).setLength(length));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        execute("rm -r " + MEAD_LOCATION + "data/" + folderName);

        return new Gson().toJson(new ResponseJsonObject("Sport", summaries));
    }


    private static String execute(String command) {
        StringBuilder sb = new StringBuilder();
        String[] commands = new String[]{"/bin/bash", "-c", command};
        try {
            Process proc = Runtime.getRuntime().exec(commands);
            //Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

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

        // System.out.println(sb.toString());

        return sb.toString();
    }


    private String getArticle(String url) throws Exception {
        JResult result = new HtmlFetcher().fetchAndExtract(url, ARTICLE_RESOLVE_TIMEOUT, true);
        return result.getText();
    }


    private class ResponseJsonObject {
        private String category;
        private List<SummaryObject> summaries;


        public ResponseJsonObject(String category, List<SummaryObject> summaries) {
            this.category = category;
            this.summaries = summaries;
        }
    }

    
    private class SummaryObject {
        private int length;
        private String text;


        public SummaryObject() {
        }


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
