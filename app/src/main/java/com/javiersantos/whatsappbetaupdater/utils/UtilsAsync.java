package com.javiersantos.whatsappbetaupdater.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javiersantos.whatsappbetaupdater.Config;
import com.javiersantos.whatsappbetaupdater.R;
import com.javiersantos.whatsappbetaupdater.callback.UpdaterCallback;
import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class UtilsAsync {

    public static class LatestAppVersion extends AsyncTask<Void, Void, String> {
        private WeakReference<Context> mContextRef;
        private String mInstalledUpdate;
        private UpdaterCallback mCallback;

        public LatestAppVersion(Context context, String installedUpdate, UpdaterCallback callback) {
            this.mContextRef = new WeakReference<>(context);
            this.mInstalledUpdate = installedUpdate;
            this.mCallback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            Context context = mContextRef.get();

            if (context != null && UtilsNetwork.isNetworkAvailable(context)) {
                return getLatestAppVersion();
            } else {
                mCallback.onError(UpdaterError.NO_INTERNET_CONNECTION);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String version) {
            super.onPostExecute(version);
            mCallback.onFinished(new Update(version, null), UtilsWhatsApp.isUpdateAvailable(mInstalledUpdate, version));
        }
    }

    public static class DownloadFile extends AsyncTask<Void, Integer, Integer> {
        private Context context;
        private MaterialDialog dialog;
        private UtilsEnum.DownloadType downloadType;
        private Update update;
        private String path, filename, downloadUrl;

        public DownloadFile(Context context, UtilsEnum.DownloadType downloadType, Update update) {
            this.context = context;
            this.downloadType = downloadType;
            this.update = update;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";

            // Configure cancel button and show progress dialog
            MaterialDialog.Builder builder = UtilsDialog.showDownloadingDialog(context, downloadType, update.getLatestVersion());
            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    cancel(true);
                }
            });
            dialog = builder.show();

            // Configure type of download: WhatsApp update or Beta Updater update
            switch (downloadType) {
                case WHATSAPP_APK:
                    filename = "WhatsApp_" + update.getLatestVersion() + ".apk";
                    downloadUrl = update.getDownloadUrl();
                    break;
                case UPDATE:
                    filename = context.getPackageName() + "_" + update.getLatestVersion() + ".apk";
                    downloadUrl = Config.GITHUB_APK + "v" + update.getLatestVersion() + "/" + context.getPackageName() + ".apk";
                    break;
            }

            // Create download directory if doesn't exist
            File file = new File(path);
            if (!file.exists()) { file.mkdir(); }

        }

        @Override
        protected Integer doInBackground(Void... voids) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            Integer lengthOfFile = 0;

            try {
                URL url = new URL(downloadUrl);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // Getting file lenght
                lengthOfFile = connection.getContentLength();
                // Read file
                input = connection.getInputStream();
                // Where to write file
                output = new FileOutputStream(new File(path, filename));

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    // Close input if download has been cancelled
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // Updating download progress
                    if (lengthOfFile > 0) {
                        publishProgress((int) ((total * 100) / lengthOfFile));
                    }
                    output.write(data, 0, count);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null) { output.close(); }
                    if (input != null) { input.close(); }
                } catch (IOException ignored) {}

                if (connection != null) {
                    connection.disconnect();
                }
            }

            return lengthOfFile;
        }

        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer file_length) {
            UtilsHelper.dismissDialog(dialog);
            File file = new File(path, filename);
            if (file_length != null && file.length() == file_length) {
                // File download: OK
                context.startActivity(UtilsIntent.getOpenAPKIntent(file));
                switch (downloadType) {
                    case WHATSAPP_APK:
                        UtilsDialog.showSaveAPKDialog(context, file, update.getLatestVersion());
                        break;
                    case UPDATE:
                        break;
                }
            } else {
                // File download: FAILED
                onCancelled();
                UtilsDialog.showSnackbar(context, context.getResources().getString(R.string.snackbar_failed));
            }
        }

        @Override
        protected void onCancelled() {
            // Delete uncompleted file
            File file = new File(path, filename);
            if (file.exists()) { file.delete(); }
        }

    }

    private static String getLatestAppVersion() {
        String source = "";

        try {
            URL url = new URL(Config.GITHUB_TAGS);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
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

        String[] split = source.split(">");
        int i = 0;
        while (i < split.length) {
            if (split[i].startsWith("v")) {
                split = split[i].split("(v)|(<)");
                return split[1].trim();
            }
            i++;
        }

        return null;
    }

}
