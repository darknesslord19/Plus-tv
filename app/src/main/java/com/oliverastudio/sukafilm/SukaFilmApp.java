package com.oliverastudio.sukafilm;

import android.app.Application;

public class SukaFilmApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PlaylistManager.init(this);
    }
}
