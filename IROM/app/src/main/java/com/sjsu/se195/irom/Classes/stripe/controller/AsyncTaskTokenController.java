package com.sjsu.se195.irom.Classes.stripe.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.sjsu.se195.irom.Classes.NoodlioPayClass;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Logic needed to create tokens using the {@link android.os.AsyncTask} methods included in the
 * sdk: {@link Stripe#createToken(Card, String, TokenCallback)}.
 */
public class AsyncTaskTokenController {

    private CardInputWidget mCardInputWidget;
    private Context mContext;
    private ErrorDialogHandler mErrorDialogHandler;
    private ListViewController mOutputListController;
    private ProgressDialogController mProgressDialogController;
    private String mPublishableKey;

    public AsyncTaskTokenController(
            @NonNull Button button,
            @NonNull CardInputWidget cardInputWidget,
            @NonNull Context context,
            @NonNull ErrorDialogHandler errorDialogHandler,
            @NonNull ListViewController outputListController,
            @NonNull ProgressDialogController progressDialogController,
            @NonNull String publishableKey) {
        mCardInputWidget = cardInputWidget;
        mContext = context;
        mErrorDialogHandler = errorDialogHandler;
        mPublishableKey = publishableKey;
        mProgressDialogController = progressDialogController;
        mOutputListController = outputListController;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCard();
            }
        });
    }

    public void detach() {
        mCardInputWidget = null;
    }

    private void saveCard() {
        Card cardToSave = mCardInputWidget.getCard();
        if (cardToSave == null) {
            mErrorDialogHandler.showError("Invalid Card Data");
            return;
        }
        mProgressDialogController.startProgress();
        new Stripe(mContext).createToken(
                cardToSave,
                mPublishableKey,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        mOutputListController.addToList(token);
                        mProgressDialogController.finishProgress();
                        NoodlioPayClass pay = new NoodlioPayClass();
                        String url = "https://noodlio-pay.p.mashape.com/charge/token";
                        RequestParams params = new RequestParams();
                        params.add("amount","100");
                        params.add("currency","usd");
                        params.add("description","Test");
                        params.add("source",token.getId());
                        params.add("stripe_account","acct_1A6SPtKvjOLIhzMH");
                        params.add("test","true");
                        pay.post(url,params, new JsonHttpResponseHandler(){
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                System.out.println("HTTP POST Call successful");
                                System.out.println(statusCode);
                                System.out.println(response);
                            }
                        });

                    }
                    public void onError(Exception error) {
                        mErrorDialogHandler.showError(error.getLocalizedMessage());
                        mProgressDialogController.finishProgress();
                    }
                }
        );
    }
}

/*
                        NoodlioPayClass pay = new NoodlioPayClass();
                        String url = "https://noodlio-pay.p.mashape.com/charge/token";
                        RequestParams params = new RequestParams();
                        params.put("amount","1");
                        params.put("currency","usd");
                        params.put("description","Test");
                        params.put("source",token);
                        params.put("stripe_account","acct_1A6SPtKvjOLIhzMH");
                        params.put("test","true");
                        pay.post(url, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                mOutputListController.addToList(token);
                                mProgressDialogController.finishProgress();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            }
                        });

 */