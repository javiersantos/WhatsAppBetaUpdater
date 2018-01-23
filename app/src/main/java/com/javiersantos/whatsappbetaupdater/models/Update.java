package com.javiersantos.whatsappbetaupdater.models;

public class Update {
    private String latestVersion;
    private String downloadUrl;

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
}
