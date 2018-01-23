package com.javiersantos.whatsappbetaupdater.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import com.afollestad.materialdialogs.MaterialDialog;

public class UtilsHelper {

    public static void dismissDialog(MaterialDialog dialog) {
        if (dialog.isShowing()) { // Check if the dialog is showing
            // Get the Context that was used for the dialog
            Context context = ((ContextWrapper) dialog.getContext()).getBaseContext();

            // If the Context was an Activity AND it hasn't been finished
            if (context instanceof Activity) {
                if (!((Activity)context).isFinishing()) {
                    dialog.dismiss();
                }
            } else {
                dialog.dismiss();
            }
        }
    }

}
