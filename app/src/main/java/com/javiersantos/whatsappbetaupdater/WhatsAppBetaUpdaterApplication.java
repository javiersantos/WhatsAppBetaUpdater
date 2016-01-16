package com.javiersantos.whatsappbetaupdater;

import android.app.Application;

import com.javiersantos.whatsappbetaupdater.util.AppPreferences;

public class WhatsAppBetaUpdaterApplication extends Application {
    private static AppPreferences appPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        appPreferences = new AppPreferences(this);
    }

    public static AppPreferences getAppPreferences() {
        return appPreferences;
    }
}