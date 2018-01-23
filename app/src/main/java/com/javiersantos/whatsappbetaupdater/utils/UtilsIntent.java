package com.javiersantos.whatsappbetaupdater.utils;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class UtilsIntent {

    public static Intent getOpenAPKIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent getShareAPKIntent(File file, String shareText) {
        Intent intent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, shareText)
                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                .setType("application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent getPayPalIntent(String amount) {
        String amountRes = amount.replaceAll("\\D+","").trim(); // Remove symbol ($, €, etc)

        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(UtilsApp.generatePayPalDonationLink(amountRes, UtilsApp.getPayPalCurrency())));

        return intent;
    }

}
