package com.javiersantos.whatsappbetaupdater.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import com.javiersantos.whatsappbetaupdater.Config;
import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.receiver.NotificationReceiver;

import java.io.File;
import java.util.Currency;
import java.util.Locale;

public class UtilsApp {

    public static void setNotification(Context context, Boolean enable, Integer hours) {
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (enable) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), getHoursToSeconds(hours), pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static Intent getAPKIntent(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apk.getAbsolutePath()), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent getPayPalIntent(String amount) {
        String amountRes = amount.replaceAll("\\D+","").trim(); // Remove symbol ($, €, etc)

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(generatePayPalDonationLink(amountRes, getPayPalCurrency())));

        return intent;
    }

    public static String[] getDonationArray(Context context) {
        String[] donationArray = context.getResources().getStringArray(R.array.donate_amount);
        for (int i = 0; i < donationArray.length; i++) {
            donationArray[i] = String.format(donationArray[i], getPayPalSymbol());
        }

        return donationArray;
    }

    private static String generatePayPalDonationLink(String amount, String currency) {
        return "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=" + Config.PAYPAL_DONATION + "&currency_code=" + currency + "&amount=" + amount + "&item_name=Donation%20for%20%22Beta%20Updater%20for%20WhatsApp%22";
    }

    private static String getPayPalCurrency() {
        Currency currency = Currency.getInstance(Locale.getDefault());
        return currency.getCurrencyCode().equals("EUR") ? "EUR" : "USD";
    }

    private static String getPayPalSymbol() {
        Currency currency = Currency.getInstance(Locale.getDefault());
        return currency.getSymbol().equals("€") ? "€" : "$";
    }

    /**
     * Retrieve your own app version
     * @param context Context
     * @return String with the app version
     */
    public static String getAppVersionName(Context context) {
        String res = "0.0.0.0";
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Retrieve your own app version code
     * @param context Context
     * @return int with the app version code
     */
    public static int getAppVersionCode(Context context) {
        int res = 0;
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    private static Integer getHoursToSeconds(Integer hours) {
        return hours * hours * 60 * 60 * 1000;
    }

}
