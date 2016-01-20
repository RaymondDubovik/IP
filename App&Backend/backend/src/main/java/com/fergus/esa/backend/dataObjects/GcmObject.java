package com.fergus.esa.backend.dataObjects;

import twitter4j.JSONException;
import twitter4j.JSONObject;

/**
 * Author: Raymond Dubovik (https://github.com/RaymondDubovik)
 * Date: 20.01.2016
 */
public class GcmObject {
    /**
     * Holds parameter key name
     */
    private static final String PARAM_DATA = "data";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_RECIPIENT = "to";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_TEST = "dry_run";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_NOTIFICATION = "notification";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_MESSAGE = "body";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_SOUND = "sound";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_TITLE = "push_title";
    /**
     * Holds parameter key name
     */
    private static final String PARAM_CONTENT_AVAILABLE = "content_available";

    /**
     * Holds recipient push notification token ID
     */
    private String recipient = "";
    /**
     * Holds message to be sent with the push notification
     */
    private String message = "";
    /**
     * Holds the name of the sound that will be played once notification is received
     */
    private String sound = "default";
    /**
     * True if no notification will be delivered, false otherwise
     */
    private boolean isTest = false;
    /**
     * Title of the push notification
     */
    private String title = "";
    /**
     * Additional data for the push notification
     */
    private String data = "";


    /**
     * Constructs the object
     *
     * @param recipient Push notification token ID of the recipient
     * @param title     Title of the push notification
     * @param message   Message to be sent to the recipient
     */
    public GcmObject(String recipient, String title, String message) {
        this.recipient = recipient;
        this.title = title;
        this.message = message;
    }


    public GcmObject setIsTest(boolean isTest) {
        this.isTest = isTest;
        return this;
    }


    public GcmObject setData(String data) {
        this.data = data;
        return this;
    }


    public GcmObject disableSound() {
        sound = "";
        return this;
    }


    @Override
    public String toString() {
        return "{" +
                ",\"" + PARAM_NOTIFICATION + "\":\"{" + "\"" +
                "\"" + PARAM_SOUND + "\":\"" + sound + "\"" +
                ",\"" + PARAM_TITLE + "\":\"" + title + "\"" +
                ",\"" + PARAM_MESSAGE + "\":\"" + message + "\"" +
                ",\"" + PARAM_DATA + "\":\"" + data + "\"" +
                "}";
    }


    public String toJson() {
        try {
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject json = new JSONObject();
            json.put("message", message.trim());
            json.put(PARAM_CONTENT_AVAILABLE, true);
            json.put(PARAM_RECIPIENT, recipient);

            if (isTest) {
                json.put(PARAM_TEST, isTest);
            }

            JSONObject data = new JSONObject();
            data.put(PARAM_TITLE, title);
            data.put(PARAM_MESSAGE, message);

            if (sound != null && !sound.equals("")) {
                data.put(PARAM_SOUND, sound);
            }

            if (this.data != null && !this.data.equals("")) {
                data.put(PARAM_DATA, this.data);
            }

            json.put(PARAM_NOTIFICATION, data);

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}