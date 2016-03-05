package com.fergus.esa;

import com.fergus.esa.backend.esaEventEndpoint.EsaEventEndpoint;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

/**
 * Author: svchosta (https://github.com/svchosta)
 * Date: 10.12.2015
 */
public class ServerUrls {
    public static final String ROOT_URL = "http://192.168.0.102:8080/_ah/api/";
	private static final int CONNECT_TIMEOUT = 8 * 1000; // 10s
	private static final int READ_TIMEOUT = 5 * 1000; // 5s

	public static final EsaEventEndpoint endpoint = new EsaEventEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), new HttpRequestInitializer() {
		public void initialize(HttpRequest httpRequest) {
			httpRequest.setConnectTimeout(CONNECT_TIMEOUT);
			httpRequest.setReadTimeout(READ_TIMEOUT);
		}}).setRootUrl(ServerUrls.ROOT_URL).build();
}
