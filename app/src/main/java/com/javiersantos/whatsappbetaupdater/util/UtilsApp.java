package com.javiersantos.whatsappbetaupdater.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import com.javiersantos.whatsappbetaupdater.Config;
import com.javiersantos.whatsappbetaupdater.receiver.NotificationReceiver;

import java.io.File;
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
        String amountRes = amount.replaceAll("\\D+","").trim();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(generatePayPalDonationLink(amountRes, getPayPalCurrency())));

        return intent;
    }

    private static String generatePayPalDonationLink(String amount, UtilsEnum.PayPalCurrency currency) {
        return "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=" + Config.PAYPAL_DONATION + "&currency_code=" + currency + "&amount=" + amount + "&item_name=Donation%20for%20%22Beta%20Updater%20for%20WhatsApp%22";
    }

    private static UtilsEnum.PayPalCurrency getPayPalCurrency() {
        if (Locale.getDefault().getLanguage().equals("es")) {
            return UtilsEnum.PayPalCurrency.EUR;
        } else {
            return UtilsEnum.PayPalCurrency.USD;
        }
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
