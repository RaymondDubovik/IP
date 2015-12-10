package com.fergus.summarisationserver;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.UUID;

@Path("/esarestsummariser")
public class ESARESTSummariser {
    private static final String MEAD_LOCATION = "~/Desktop/mead/";


    @POST
    @Path("/post")
    @Produces({"text/plain"})
    public Response summarise(String url) throws IOException {

		/*
        String[] cmd = {"/bin/bash","-c","echo password| sudo -S ls"};
		Process pb = Runtime.getRuntime().exec(cmd);
		*/

        // {"/bin/bash","-c","echo \"password\"| sudo -S ls"};

        String summary = "";
        String foldername = UUID.randomUUID().toString();

        Runtime.getRuntime().exec("sudo mkdir " + MEAD_LOCATION + " data/" + foldername);
        Runtime.getRuntime().exec("sudo chown -R tomcat:tomcat " + MEAD_LOCATION + "data/" + foldername);

        String article = "";
        try {
            article = getArticle(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File articleFile = new File("0");

        articleFile.createNewFile();

        FileWriter writer = new FileWriter(articleFile);
        writer.write(article);
        writer.flush();
        writer.close();

        Runtime.getRuntime().exec("sudo mv 0 " + MEAD_LOCATION + "data/" + foldername);

        String clusterXML = "";
        StringBuilder clusterBuilder = new StringBuilder();
        clusterBuilder.append("<?xml version='1.0'?>\n");
        clusterBuilder.append("<CLUSTER LANG='ENG'>\n");
        clusterBuilder.append("\t<D DID='0' />\n");
        clusterBuilder.append("</CLUSTER>\n");

        clusterXML = clusterBuilder.toString();

        File clusterFile = new File(foldername + ".cluster");

        clusterFile.createNewFile();

        FileWriter clusterWriter = new FileWriter(clusterFile);
        clusterWriter.write(clusterXML);
        clusterWriter.flush();
        clusterWriter.close();

        Runtime.getRuntime().exec("sudo mv " + foldername + ".cluster " + MEAD_LOCATION + "data/" + foldername);

        Process p = Runtime.getRuntime().exec("sudo perl " + MEAD_LOCATION + "bin/addons/formatting/text2cluster.pl /home/ec2-user/mead/data/" + foldername);
        try {
            Process meadSummarise = Runtime.getRuntime().exec("sudo perl " + MEAD_LOCATION + "bin/mead.pl -w -a 85 " + foldername);

            BufferedReader in = new BufferedReader(new InputStreamReader(meadSummarise.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                line = line.substring(5);
                if (!line.equals("")) {
                    summary = summary + " " + line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().exec("sudo rm -r " + MEAD_LOCATION + "data/" + foldername);

        return Response.status(201).entity(summary).build();
    }


    public String getArticle(String url) throws Exception {
        int resolveTimeout = 500;
        HtmlFetcher fetcher = new HtmlFetcher();

        JResult res = fetcher.fetchAndExtract(url, resolveTimeout, true);
        String articleText = res.getText();

        return articleText;
    }
}