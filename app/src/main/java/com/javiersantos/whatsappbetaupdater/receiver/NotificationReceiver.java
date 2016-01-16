package com.javiersantos.whatsappbetaupdater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.javiersantos.whatsappbetaupdater.util.UtilsAsync;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new UtilsAsync.NotifyWhatsAppVersion(context, intent).execute();
    }

}
