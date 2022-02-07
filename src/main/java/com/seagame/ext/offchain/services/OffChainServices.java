//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seagame.ext.offchain.services;

import com.creants.creants_2x.core.util.AppConfig;
import com.creants.creants_2x.core.util.QAntTracer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.util.*;

public class OffChainServices {
    private static final String KEY = "DyeqNAAgwiAsHZ3pg8vD";
    private static final String SYSTEM = "battle_city";
    private static final String GAME = "battle_city_mu";
    private static final String GRAPH_URL_INTERNAL = AppConfig.getGraphApi() + "/internal/";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int MAX_CONNECTION_POOL = 100;
    private static OffChainServices instance;
    private CloseableHttpClient client;
    private static final Timer timer = new Timer();

    public static OffChainServices getInstance() {
        if (instance == null) {
            instance = new OffChainServices();
        }

        return instance;
    }

    private OffChainServices() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        this.client = HttpClients.custom().setConnectionManager(cm).build();
        cm.setDefaultMaxPerRoute(100);
    }

    private String doPostOffChain(List<NameValuePair> formReq, String action) {
        return this.doPostRequest(formReq, GRAPH_URL_INTERNAL + action);
    }

    private String doPostRequest(List<NameValuePair> formReq, String url) {
        int statusCode = -1;
        try {
            Request bodyForm = Request.Post(url).addHeader("Content-Type", "application/x-www-form-urlencoded").connectTimeout(5000).socketTimeout(5000).bodyForm(formReq);
            HttpResponse httpResponse = Executor.newInstance(this.client).execute(bodyForm).returnResponse();
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } catch (Exception var6) {
            QAntTracer.error(this.getClass(), new Object[]{"doPostRequest! url: " + url + ", tracer:" + QAntTracer.getTraceMessage(var6)});
        }

        throw new RuntimeException("Failed : HTTP error code " + statusCode + ", url:" + url);
    }


    public static void main(String[] args) {
    }
}
