package com.javiersantos.whatsappbetaupdater.asyncs;

import android.content.Context;
import android.os.AsyncTask;

import com.javiersantos.whatsappbetaupdater.callback.UpdaterCallback;
import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;
import com.javiersantos.whatsappbetaupdater.utils.UtilsNetwork;
import com.javiersantos.whatsappbetaupdater.utils.UtilsWhatsApp;

import java.lang.ref.WeakReference;

public class NotifyVersion extends AsyncTask<Void, Void, Update> {
    private WeakReference<Context> mContextRef;
    private String mInstalledUpdate;
    private UpdaterCallback mCallback;

    public NotifyVersion(Context context, String installedUpdate,UpdaterCallback callback) {
        this.mContextRef = new WeakReference<>(context);
        this.mInstalledUpdate = installedUpdate;
        this.mCallback = callback;
    }

    @Override
    protected Update doInBackground(Void... voids) {
        Context context = mContextRef.get();

        if (context != null && UtilsNetwork.isNetworkAvailable(context)) {
            return GetLatestVersion.getUpdate();
        } else {
            mCallback.onError(UpdaterError.NO_INTERNET_CONNECTION);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Update update) {
        super.onPostExecute(update);
        mCallback.onFinished(update, mInstalledUpdate != null && UtilsWhatsApp.isUpdateAvailable(mInstalledUpdate, update.getLatestVersion()));
    }
}
