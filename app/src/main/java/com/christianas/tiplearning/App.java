package com.christianas.tiplearning;

import android.app.Application;

public class App extends Application {

    private static boolean isSubscribed;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static boolean isIsSubscribed() {
        return isSubscribed;
    }

    public static void setIsSubscribed(boolean isSubscribed) {
        App.isSubscribed = isSubscribed;
    }
}
