package com.javiersantos.whatsappbetaupdater.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.javiersantos.whatsappbetaupdater.models.Version;

public class UtilsWhatsApp {

    public static String getInstalledWhatsAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo("com.whatsapp", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Boolean isWhatsAppInstalled(Context context) {
        Boolean res;

        try {
            context.getPackageManager().getPackageInfo("com.whatsapp", 0);
            res = true;
        } catch (PackageManager.NameNotFoundException e) {
            res = false;
        }

        return res;
    }

    public static Boolean isUpdateAvailable(String installedVersion, String latestVersion) {
        Version installed = new Version(installedVersion);
        Version latest = new Version(latestVersion);

        return installed.compareTo(latest) < 0;
    }

}
