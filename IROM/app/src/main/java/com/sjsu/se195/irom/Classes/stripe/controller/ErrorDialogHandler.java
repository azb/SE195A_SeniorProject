package com.sjsu.se195.irom.Classes.stripe.controller;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.sjsu.se195.irom.R;
import com.sjsu.se195.irom.Classes.stripe.dialog.ErrorDialogFragment;

/**
 * A convenience class to handle displaying error dialogs.
 */
public class ErrorDialogHandler {

    FragmentManager mFragmentManager;

    public ErrorDialogHandler(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void showError(String errorMessage) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(
                R.string.validationErrors, errorMessage);
        fragment.show(mFragmentManager, "error");
    }
}
