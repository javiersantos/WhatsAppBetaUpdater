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
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private AppPreferences appPreferences;
    private Boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        this.appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Views
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final TextView toolbar_subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
        final TextView whatsapp_latest_version = (TextView) findViewById(R.id.whatsapp_latest_version);
        TextView whatsapp_installed_version = (TextView) findViewById(R.id.whatsapp_installed_version);

        // Set drawable to FAB
        fab.setImageDrawable(new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_download).color(Color.WHITE).sizeDp(24));

        // Check if there is an app update and show dialog
        if (appPreferences.getShowAppUpdates()) {
            new UtilsAsync.LatestAppVersion(this).execute();
        }

        // Check if there is a WhatsApp update and show UI changes
        new UtilsAsync.LatestWhatsAppVersion(this, whatsapp_latest_version, toolbar_subtitle, fab).execute();

        if (UtilsWhatsApp.isWhatsAppInstalled(this)) {
            whatsapp_installed_version.setText(UtilsWhatsApp.getInstalledWhatsAppVersion(this));
        } else {
            whatsapp_installed_version.setText(getResources().getString(R.string.whatsapp_not_installed));
        }

        // PullToRefresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UtilsAsync.LatestWhatsAppVersion(context, whatsapp_latest_version, toolbar_subtitle, fab).execute();
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
        UtilsApp.setNotification(context, appPreferences.getEnableNotifications(), appPreferences.getHoursNotification());
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

        menu.findItem(R.id.action_donate).setIcon(new IconicsDrawable(context).icon(FontAwesome.Icon.faw_paypal).color(Color.WHITE).actionBar());
        menu.findItem(R.id.action_settings).setIcon(new IconicsDrawable(context).icon(CommunityMaterial.Icon.cmd_settings).color(Color.WHITE).actionBar());

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
