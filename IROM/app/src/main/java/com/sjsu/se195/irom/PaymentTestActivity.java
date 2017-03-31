package com.sjsu.se195.irom;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.wallet.WalletConstants;


public class PaymentTestActivity extends NavigationDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_payment_test, null, false);
        this.drawer.addView(contentView, 0);


        // Show spinner or other UI cue to indicate progress
        showProgressDialog();
        Wallet.Payments.isReadyToPay(mGoogleApiClient).setResultCallback(
                new ResultCallback&lt;BooleanResult&gt;() {
            @Override
            public void onResult(@NonNull BooleanResult booleanResult) {
                hideProgressDialog();
                if (booleanResult.getStatus().isSuccess()) {
                    if (booleanResult.getValue()) {
                        // Show Android Pay buttons alongside regular checkout button
                        // ...
                    } else {
                        // Hide Android Pay buttons, show a message that Android Pay
                        // cannot be used yet, and display a traditional checkout button
                        // ...
                    }
                } else {
                    // Error making isReadyToPay call
                    Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                }
            }
        });
    }


    void showProgressDialog()
    {

    }
}
