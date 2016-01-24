package com.javiersantos.whatsappbetaupdater.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

public class AppPreferences {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final String KeyAutoDownload = "prefAutoDownload";
    private final String KeyShowAppUpdates = "prefShowAppUpdates";
    private final String KeyEnableNotifications = "prefEnableNotifications";
    private final String KeySoundNotification = "prefSoundNotification";
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

    public void setShowAppUpdate(Boolean res) {
        editor.putBoolean(KeyShowAppUpdates, res);
        editor.commit();
    }

    public Boolean getEnableNotifications() {
        return sharedPreferences.getBoolean(KeyEnableNotifications, true);
    }

    public Uri getSoundNotification() {
        String ringtone = sharedPreferences.getString(KeySoundNotification, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
        return Uri.parse(ringtone);
    }

    public void setSoundNotification(Uri uri) {
        editor.putString(KeySoundNotification, uri.toString());
        editor.commit();
    }

    public Integer getHoursNotification() {
        String hours = sharedPreferences.getString(KeyHoursNotification, "12");
        return Integer.parseInt(hours);
    }

}
