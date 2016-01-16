package com.javiersantos.whatsappbetaupdater.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;
import com.javiersantos.whatsappbetaupdater.util.AppPreferences;
import com.javiersantos.whatsappbetaupdater.util.UtilsApp;
import com.lb.material_preferences_library.PreferenceActivity;
import com.lb.material_preferences_library.custom_preferences.ListPreference;
import com.lb.material_preferences_library.custom_preferences.Preference;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private Integer hearthCount = 0;
    private SharedPreferences sharedPreferences;
    private AppPreferences appPreferences;
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
        prefHoursNotification = (ListPreference) findPreference("prefHoursNotification");
        initPrefHoursNotification(prefHoursNotification);
        Preference prefLicense = (Preference) findPreference("prefLicense");
        initPrefLicense(prefLicense);
        Preference prefVersion = (Preference) findPreference("prefVersion");
        initPrefVersion(prefVersion);
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

        if (preference == prefHoursNotification) {
            preference.setSummary(String.format(getResources().getString(R.string.settings_interval_description), appPreferences.getHoursNotification()));
        }
    }
}
