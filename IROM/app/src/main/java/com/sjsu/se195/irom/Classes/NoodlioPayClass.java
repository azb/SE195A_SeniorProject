package com.sjsu.se195.irom.Classes;
import org.json.*;
import com.loopj.android.http.*;

public class NoodlioPayClass {
    private static final String BASE_URL = "https://noodlio-pay.p.mashape.com/charge/token";

    private static AsyncHttpClient client = new AsyncHttpClient();

    /*public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }*/

    public static void post(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.addHeader("X-Mashape-Key","7CDnDK7O6FmshdvAFz1fTE2Qg0T4p1vwAaAjsnBFO2drjrYQom");
        client.addHeader("Content-Type","application/x-www-form-urlencoded");
        client.addHeader("Accept","application/json");
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}