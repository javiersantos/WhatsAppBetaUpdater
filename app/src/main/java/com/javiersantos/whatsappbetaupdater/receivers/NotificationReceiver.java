package com.javiersantos.whatsappbetaupdater.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.WhatsAppBetaUpdaterApplication;
import com.javiersantos.whatsappbetaupdater.activities.MainActivity;
import com.javiersantos.whatsappbetaupdater.asyncs.NotifyVersion;
import com.javiersantos.whatsappbetaupdater.callback.UpdaterCallback;
import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;
import com.javiersantos.whatsappbetaupdater.utils.AppPreferences;
import com.javiersantos.whatsappbetaupdater.utils.UtilsWhatsApp;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new NotifyVersion(context, UtilsWhatsApp.getInstalledWhatsAppVersion(context), new UpdaterCallback() {
            @Override
            public void onFinished(Update update, boolean isUpdateAvailable) {
                if (UtilsWhatsApp.isWhatsAppInstalled(context) && isUpdateAvailable) {
                    AppPreferences appPreferences = WhatsAppBetaUpdaterApplication.getAppPreferences();

                    String title = String.format(context.getResources().getString(R.string.notification), update.getLatestVersion());
                    String message = String.format(context.getResources().getString(R.string.notification_description), context.getResources().getString(R.string.app_name));
                    intent.putExtra("title", title);
                    intent.putExtra("message", message);

                    Intent notIntent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationManagerCompat manager = NotificationManagerCompat.from(context);

                    NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle().bigText(message);
                    Integer resId = R.mipmap.ic_launcher;

                    NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(context.getResources(), resId));

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setContentIntent(contentIntent)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setStyle(style)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(true)
                            .extend(wearableExtender);

                    // Check if "Silent Notification Tone" is selected
                    if (!appPreferences.getSoundNotification().toString().equals("null")) {
                        builder.setSound(appPreferences.getSoundNotification());
                    }

                    Notification notification = builder.build();
                    manager.notify(0, notification);
                }
            }

            @Override
            public void onLoading() {

            }

            @Override
            public void onError(UpdaterError error) {

            }
        }).execute();
    }

}
