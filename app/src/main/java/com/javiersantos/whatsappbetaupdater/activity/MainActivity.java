package com.javiersantos.whatsappbetaupdater.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;
import com.javiersantos.whatsappbetaupdater.util.AppPreferences;
import com.javiersantos.whatsappbetaupdater.util.UtilsApp;
import com.javiersantos.whatsappbetaupdater.util.UtilsAsync;
import com.javiersantos.whatsappbetaupdater.util.UtilsDialog;
import com.javiersantos.whatsappbetaupdater.util.UtilsEnum;
import com.javiersantos.whatsappbetaupdater.util.UtilsWhatsApp;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.pnikosis.materialishprogress.ProgressWheel;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private AppPreferences appPreferences;
    private Boolean doubleBackToExitPressedOnce = false;

    // Variables
    private TextView whatsapp_latest_version, whatsapp_installed_version, toolbar_subtitle;
    private FloatingActionButton fab;
    private ProgressWheel progressWheel;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        this.appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();
        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.toolbar_subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
        this.whatsapp_latest_version = (TextView) findViewById(R.id.whatsapp_latest_version);
        this.whatsapp_installed_version = (TextView) findViewById(R.id.whatsapp_installed_version);
        this.swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        this.progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set drawable to FAB
        fab.setImageDrawable(new IconicsDrawable(this).icon(MaterialDesignIconic.Icon.gmi_download).color(Color.WHITE).sizeDp(24));

        // Check if there is an app update and show dialog
        if (appPreferences.getShowAppUpdates()) {
            new UtilsAsync.LatestAppVersion(this).execute();
        }

        // PullToRefresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UtilsAsync.LatestWhatsAppVersion(context, whatsapp_latest_version, toolbar_subtitle, fab, progressWheel).execute();
                checkInstalledWhatsAppVersion();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UtilsAsync.DownloadFile(context, UtilsEnum.DownloadType.WHATSAPP_APK, whatsapp_latest_version.getText().toString()).execute();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if there is a newest WhatsApp update and show UI changes
        new UtilsAsync.LatestWhatsAppVersion(this, whatsapp_latest_version, toolbar_subtitle, fab, progressWheel).execute();

        // Get latest WhatsApp installed version
        checkInstalledWhatsAppVersion();

        // Configure notification if enabled
        UtilsApp.setNotification(context, appPreferences.getEnableNotifications(), appPreferences.getHoursNotification());
    }

    private void checkInstalledWhatsAppVersion() {
        if (UtilsWhatsApp.isWhatsAppInstalled(this)) {
            whatsapp_installed_version.setText(UtilsWhatsApp.getInstalledWhatsAppVersion(this));
        } else {
            whatsapp_installed_version.setText(getResources().getString(R.string.whatsapp_not_installed));
        }
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

        menu.findItem(R.id.action_donate).setIcon(new IconicsDrawable(context).icon(MaterialDesignIconic.Icon.gmi_paypal_alt).color(Color.WHITE).actionBar());
        menu.findItem(R.id.action_settings).setIcon(new IconicsDrawable(context).icon(MaterialDesignIconic.Icon.gmi_settings).color(Color.WHITE).actionBar());

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
                UtilsDialog.showDonateDialog(context);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
