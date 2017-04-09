package com.sjsu.se195.irom;
import org.json.*;
import com.loopj.android.http.*;

public class NoodlioPayClass {
    private static final String BASE_URL = "https://noodlio-pay.p.mashape.com/charge/token";

    private static AsyncHttpClient client = new AsyncHttpClient();

    /*public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }*/

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}