package com.oliverastudio.sukafilm;

import android.app.Application;

public class SukaFilmApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashRecorder.install(this);
        PlaylistManager.init(this);
        // After a crash or freeze, start with the default list so the app can open again.
        if (CrashRecorder.pendingReport() != null) PlaylistManager.useDefaultUrl();
    }
}
