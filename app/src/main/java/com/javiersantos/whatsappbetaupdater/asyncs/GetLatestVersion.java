package com.javiersantos.whatsappbetaupdater.asyncs;

import android.content.Context;
import android.os.AsyncTask;

import com.javiersantos.whatsappbetaupdater.Config;
import com.javiersantos.whatsappbetaupdater.callback.UpdaterCallback;
import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;
import com.javiersantos.whatsappbetaupdater.utils.UtilsNetwork;
import com.javiersantos.whatsappbetaupdater.utils.UtilsWhatsApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetLatestVersion extends AsyncTask<Void, Void, Update> {
    private UpdaterCallback mCallback;
    private String mInstalledUpdate;
    private WeakReference<Context> mContextRef;

    public GetLatestVersion(Context context, String installedUpdate, UpdaterCallback callback) {
        this.mContextRef = new WeakReference<>(context);
        this.mInstalledUpdate = installedUpdate;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onLoading();
    }

    @Override
    protected Update doInBackground(Void... voids) {
        Context context = mContextRef.get();

        if (context != null && UtilsNetwork.isNetworkAvailable(context)) {
            Update update = getUpdate();
            if (update != null)
                return update;
            else
                mCallback.onError(UpdaterError.UPDATE_NOT_FOUND);
        } else
            mCallback.onError(UpdaterError.NO_INTERNET_CONNECTION);
        return null;
    }

    public static Update getUpdate() {
        String source = "";

        try {
            URL url = new URL(Config.WHATSAPP_URL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder str = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
                str.append(line);
            }

            in.close();

            source = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String s : extractUrls(source)) {
            if (s.contains(".apk")) {
                Pattern pattern = Pattern.compile("\\d+(\\.\\d+)+");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    return new Update(matcher.group(), s);
                }
            }
        }

        return null;
    }

    private static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    @Override
    protected void onPostExecute(Update update) {
        super.onPostExecute(update);
        if (update != null)
            mCallback.onFinished(update, mInstalledUpdate != null && UtilsWhatsApp.isUpdateAvailable(mInstalledUpdate, update.getLatestVersion()));
    }

}
