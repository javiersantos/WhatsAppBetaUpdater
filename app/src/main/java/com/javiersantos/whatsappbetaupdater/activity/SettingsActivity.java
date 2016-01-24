package com.javiersantos.whatsappbetaupdater.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;
import com.javiersantos.whatsappbetaupdater.util.AppPreferences;
import com.javiersantos.whatsappbetaupdater.util.UtilsApp;
import com.lb.material_preferences_library.PreferenceActivity;
import com.lb.material_preferences_library.custom_preferences.CheckBoxPreference;
import com.lb.material_preferences_library.custom_preferences.ListPreference;
import com.lb.material_preferences_library.custom_preferences.Preference;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private Integer hearthCount = 0;
    private SharedPreferences sharedPreferences;
    private AppPreferences appPreferences;
    private CheckBoxPreference prefEnableNotifications;
    private Preference prefSoundNotification;
    private ListPreference prefHoursNotification;

    @Override
    protected int getPreferencesXmlId() {
        return R.xml.settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        this.appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setPreferenceView();
    }

    private void setPreferenceView() {
        prefEnableNotifications = (CheckBoxPreference) findPreference("prefEnableNotifications");
        prefSoundNotification = (Preference) findPreference("prefSoundNotification");
        initPrefSoundNotification(prefSoundNotification);
        prefHoursNotification = (ListPreference) findPreference("prefHoursNotification");
        initPrefHoursNotification(prefHoursNotification);
        Preference prefLicense = (Preference) findPreference("prefLicense");
        initPrefLicense(prefLicense);
        Preference prefVersion = (Preference) findPreference("prefVersion");
        initPrefVersion(prefVersion);
    }

    private void initPrefSoundNotification(Preference preference) {
        preference.setSummary(RingtoneManager.getRingtone(context, appPreferences.getSoundNotification()).getTitle(context));
        preference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, appPreferences.getSoundNotification())
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getResources().getString(R.string.settings_notifications_sound));
                startActivityForResult(intent, 5);
                return true;
            }
        });
    }

    private void initPrefHoursNotification(ListPreference listPreference) {
        listPreference.setEntries(getResources().getStringArray(R.array.notification_hours));
        listPreference.setEntryValues(getResources().getStringArray(R.array.notification_hours_values));

        listPreference.setSummary(String.format(getResources().getString(R.string.settings_interval_description), appPreferences.getHoursNotification()));
    }

    private void initPrefLicense(Preference preference) {
        preference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                startActivity(new Intent(context, LicenseActivity.class));
                return true;
            }
        });
    }

    private void initPrefVersion(Preference preference) {
        final ArrayList<String> hearts = new ArrayList<>(Arrays.asList("\u2665", "\uD83D\uDC99", "\uD83D\uDC9A", "\uD83D\uDC9C", "\uD83D\uDC9B", "\uD83D\uDC98"));

        String versionName = UtilsApp.getAppVersionName(context);
        int versionCode = UtilsApp.getAppVersionCode(context);
        preference.setTitle(getResources().getString(R.string.app_name) + " v" + versionName + " (" + versionCode + ")");
        preference.setSummary(String.format(getResources().getString(R.string.settings_about), hearts.get(hearthCount)));

        preference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (hearthCount == hearts.size() - 1) { hearthCount = 0; } else { hearthCount++; }
                preference.setSummary(String.format(getResources().getString(R.string.settings_about), hearts.get(hearthCount)));
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = (Preference) findPreference(key);

        if (preference == prefEnableNotifications) {
            if (prefEnableNotifications.isChecked()) {
                prefSoundNotification.setEnabled(true);
                prefHoursNotification.setEnabled(true);
            } else {
                prefSoundNotification.setEnabled(false);
                prefHoursNotification.setEnabled(false);
            }
        } else if (preference == prefSoundNotification) {
            preference.setSummary(RingtoneManager.getRingtone(context, appPreferences.getSoundNotification()).getTitle(context));
        } else if (preference == prefHoursNotification) {
            preference.setSummary(String.format(getResources().getString(R.string.settings_interval_description), appPreferences.getHoursNotification()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                appPreferences.setSoundNotification(uri);
            } else {
                appPreferences.setSoundNotification(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        }
    }
}
