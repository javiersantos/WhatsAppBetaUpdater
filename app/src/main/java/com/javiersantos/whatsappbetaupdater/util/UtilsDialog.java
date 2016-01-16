package com.javiersantos.whatsappbetaupdater.util;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;

import java.io.File;

public class UtilsDialog {

    public static MaterialDialog showDownloadingDialog(Context context, UtilsEnum.DownloadType downloadType, String version) {
        Boolean showMinMax = false; // Show a max/min ratio to the left of the seek bar

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .progress(false, 100, showMinMax)
                .cancelable(false);

        switch (downloadType) {
            case WHATSAPP_APK:
                builder.title(String.format(context.getResources().getString(R.string.downloading), context.getResources().getString(R.string.app_whatsapp), version));
                break;
            case UPDATE:
                builder.title(String.format(context.getResources().getString(R.string.downloading), context.getResources().getString(R.string.app_name), version));
                break;
        }

        return builder.show();
    }

    public static MaterialDialog showSaveAPKDialog(Context context, final File file) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.delete))
                .content(context.getResources().getString(R.string.delete_description))
                .cancelable(false)
                .positiveText(context.getResources().getString(R.string.button_save))
                .negativeText(context.getResources().getString(R.string.button_delete))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        file.delete();
                    }
                }).show();

        return dialog;
    }

    public static MaterialDialog showUpdateAvailableDialog(final Context context, final String version) {
        final AppPreferences appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(String.format(context.getResources().getString(R.string.app_update), version))
                .content(context.getResources().getString(R.string.app_update_description))
                .positiveText(context.getResources().getString(R.string.button_update))
                .negativeText(context.getResources().getString(android.R.string.cancel))
                .neutralText(context.getResources().getString(R.string.button_disable_update))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        new UtilsAsync.DownloadFile(context, UtilsEnum.DownloadType.UPDATE, version).execute();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        appPreferences.setShowAppUpdate(false);
                    }
                }).show();

        return dialog;
    }

    public static MaterialDialog showDonateDialog(final Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.action_donate))
                .content(context.getResources().getString(R.string.donate_description))
                .items(context.getResources().getStringArray(R.array.donate_amount))
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        context.startActivity(UtilsApp.getPayPalIntent(text.toString()));
                        return true;
                    }
                })
                .positiveText(context.getResources().getString(R.string.button_paypal))
                .show();

        return dialog;
    }

}
