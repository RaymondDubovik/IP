package com.fergus.esa;

import com.fergus.esa.backend.esaEventEndpoint.EsaEventEndpoint;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 10.12.2015
 */
public class ServerUrls {
    public static final String ROOT_URL = "http://192.168.0.102:8080/_ah/api/";

    public static final EsaEventEndpoint endpoint = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl(ServerUrls.ROOT_URL).build();
}
