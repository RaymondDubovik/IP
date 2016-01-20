/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fergus.esa.backend;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class GcmSender {
    public static final String API_KEY = "AIzaSyBte7ymF7rWmmXm5MMCh1XFQOHt9lgb_cs";

    public void sendNotification(String data) {
        try {
            // Create connection to send GCM Message request.
            HttpURLConnection connection = (HttpURLConnection) new URL("https://android.googleapis.com/gcm/send").openConnection();
            connection.setRequestProperty("Authorization", "key=" + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());

            // Read GCM response.
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String str;
            while ((str = input.readLine()) != null) {
                stringBuilder.append(str);
            }
            input.close();

            String response = stringBuilder.toString();
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            e.printStackTrace();
        }
    }
}