package com.javiersantos.whatsappbetaupdater.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;
import com.javiersantos.whatsappbetaupdater.asyncs.GetLatestVersion;
import com.javiersantos.whatsappbetaupdater.callback.UpdaterCallback;
import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;
import com.javiersantos.whatsappbetaupdater.utils.AppPreferences;
import com.javiersantos.whatsappbetaupdater.utils.UtilsApp;
import com.javiersantos.whatsappbetaupdater.utils.UtilsAsync;
import com.javiersantos.whatsappbetaupdater.utils.UtilsDialog;
import com.javiersantos.whatsappbetaupdater.utils.UtilsEnum;
import com.javiersantos.whatsappbetaupdater.utils.UtilsUI;
import com.javiersantos.whatsappbetaupdater.utils.UtilsWhatsApp;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.pnikosis.materialishprogress.ProgressWheel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements UpdaterCallback {
    private AppPreferences appPreferences;
    private Boolean doubleBackToExitPressedOnce = false;
    private Update mUpdate;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.whatsapp_latest_version)
    TextView whatsapp_latest_version;
    @BindView(R.id.whatsapp_installed_version)
    TextView whatsapp_installed_version;
    @BindView(R.id.toolbar_subtitle)
    TextView toolbar_subtitle;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.progress_wheel)
    ProgressWheel progressWheel;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        this.appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();

        // Set drawable to FAB
        fab.setImageDrawable(new IconicsDrawable(this).icon(MaterialDesignIconic.Icon.gmi_download).color(Color.WHITE).sizeDp(24));

        // Check if there is an app update and show dialog
        if (appPreferences.getShowAppUpdates()) {
            new UtilsAsync.LatestAppVersion(this, UtilsApp.getAppVersionName(this), new UpdaterCallback() {
                @Override
                public void onFinished(Update update, boolean isUpdateAvailable) {
                    if (isUpdateAvailable)
                        UtilsDialog.showUpdateAvailableDialog(MainActivity.this, update);
                }

                @Override
                public void onLoading() {}

                @Override
                public void onError(UpdaterError error) {}
            }).execute();
        }

        // PullToRefresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetLatestVersion(MainActivity.this, UtilsWhatsApp.getInstalledWhatsAppVersion(MainActivity.this), MainActivity.this).execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Configure notification if it's not running in background (first time that app is running) and pref is enabled
        if (!UtilsApp.isNotificationRunning(this)) {
            UtilsApp.setNotification(this, appPreferences.getEnableNotifications(), appPreferences.getHoursNotification());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if there is a newest WhatsApp update and show UI changes
        new GetLatestVersion(this, UtilsWhatsApp.getInstalledWhatsAppVersion(this), this).execute();
    }

    private void checkInstalledWhatsAppVersion() {
        if (UtilsWhatsApp.isWhatsAppInstalled(this)) {
            whatsapp_installed_version.setText(UtilsWhatsApp.getInstalledWhatsAppVersion(this));
        } else {
            whatsapp_installed_version.setText(getResources().getString(R.string.whatsapp_not_installed));
        }
    }

    @OnClick(R.id.fab)
    public void toDownload() {
        new UtilsAsync.DownloadFile(MainActivity.this, UtilsEnum.DownloadType.WHATSAPP_APK, mUpdate).execute();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.toast_tap, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_donate).setIcon(new IconicsDrawable(this).icon(MaterialDesignIconic.Icon.gmi_paypal_alt).color(Color.WHITE).actionBar());
        menu.findItem(R.id.action_settings).setIcon(new IconicsDrawable(this).icon(MaterialDesignIconic.Icon.gmi_settings).color(Color.WHITE).actionBar());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_donate:
                UtilsDialog.showDonateDialog(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinished(Update update, boolean isUpdateAvailable) {
        this.mUpdate = update;
        progressWheel.setVisibility(View.GONE);
        whatsapp_latest_version.setVisibility(View.VISIBLE);
        whatsapp_latest_version.setText(update.getLatestVersion());

        boolean isWhatsAppInstalled = UtilsWhatsApp.isWhatsAppInstalled(MainActivity.this);
        if (isWhatsAppInstalled && isUpdateAvailable) {
            UtilsUI.showFAB(fab, true);
            toolbar_subtitle.setText(String.format(getResources().getString(R.string.update_available), update.getLatestVersion()));
            if (appPreferences.getAutoDownload()) {
                new UtilsAsync.DownloadFile(MainActivity.this, UtilsEnum.DownloadType.WHATSAPP_APK, update).execute();
            }
        } else if (!isWhatsAppInstalled) {
            UtilsUI.showFAB(fab, true);
            toolbar_subtitle.setText(String.format(getResources().getString(R.string.update_not_installed), update.getLatestVersion()));
        } else {
            UtilsUI.showFAB(fab, false);
            toolbar_subtitle.setText(getResources().getString(R.string.update_not_available));
        }
    }

    @Override
    public void onLoading() {
        checkInstalledWhatsAppVersion();
        whatsapp_latest_version.setVisibility(View.GONE);
        progressWheel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(UpdaterError error) {
        if (error == UpdaterError.NO_INTERNET_CONNECTION)
            toolbar_subtitle.setText(getResources().getString(R.string.update_not_connection));
        whatsapp_latest_version.setText("NA");
    }

}
