package com.javiersantos.whatsappbetaupdater;

public class Config {

    public static final String GITHUB_URL = "https://github.com/javiersantos/WhatsAppBetaUpdater";
    public static final String GITHUB_TAGS = GITHUB_URL.concat("/tags");
    public static final String GITHUB_APK = GITHUB_URL.concat("/releases/download/");
    public static final String WHATSAPP_URL = "http://www.whatsapp.com/android/";
    public static final String WHATSAPP_APK = WHATSAPP_URL.concat("current/WhatsApp.apk");
    public static final String PAYPAL_DONATION = "donate@javiersantos.me";

    public static final String PATTERN_LATEST_VERSION = "<p class=\"version\" align=\"center\">Version";
    public static final String PATTERN_LATEST_VERSION_CDN = "<a class=\"button\" href=\"";

}
