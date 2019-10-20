package com.javiersantos.whatsappbetaupdater.models;

import android.text.TextUtils;

public class Update {
    private String latestVersion;
    private String downloadUrl;

    public Update() {}

    public Update(String latestVersion, String downloadUrl) {
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Boolean isSuccessfull() {
        return !TextUtils.isEmpty(this.latestVersion) && !TextUtils.isEmpty(this.downloadUrl);
    }

    @Override
    public String toString() {
        return "Update{" +
                "latestVersion='" + latestVersion + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
