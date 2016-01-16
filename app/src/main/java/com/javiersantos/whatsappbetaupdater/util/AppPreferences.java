package com.javiersantos.whatsappbetaupdater.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final String KeyAutoDownload = "prefAutoDownload";
    private final String KeyShowAppUpdates = "prefShowAppUpdates";
    private final String KeyEnableNotifications = "prefEnableNotifications";
    private final String KeyHoursNotification = "prefHoursNotification";

    public AppPreferences(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
    }

    public void resetAppPreferences() {
        editor.clear();
        editor.commit();
    }

    public Boolean getAutoDownload() {
        return sharedPreferences.getBoolean(KeyAutoDownload, false);
    }

    public Boolean getShowAppUpdates() {
        return sharedPreferences.getBoolean(KeyShowAppUpdates, true);
    }

    public Boolean getEnableNotifications() {
        return sharedPreferences.getBoolean(KeyEnableNotifications, true);
    }

    public Integer getHoursNotification() {
        String hours = sharedPreferences.getString(KeyHoursNotification, "12");
        return Integer.parseInt(hours);
    }

}
