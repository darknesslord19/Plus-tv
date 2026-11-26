package com.oliverastudio.sukafilm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/** Saves the stack trace of a crash or a frozen main thread. Nothing is sent anywhere. */
final class CrashRecorder {
    private static final long BLOCK_LIMIT_MS = 3500;
    private static SharedPreferences preferences;

    private CrashRecorder() {}

    static void install(Context context) {
        preferences = context.getSharedPreferences("diagnostics", Context.MODE_PRIVATE);

        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            save("ÇÖKME (" + thread.getName() + ")\n" + Log.getStackTraceString(error));
            if (previous != null) previous.uncaughtException(thread, error);
        });

        Thread watchdog = new Thread(CrashRecorder::watch, "freeze-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private static void watch() {
        Handler main = new Handler(Looper.getMainLooper());
        while (true) {
            final boolean[] answered = {false};
            long start = SystemClock.uptimeMillis();
            main.post(() -> {
                synchronized (answered) { answered[0] = true; }
            });
            SystemClock.sleep(BLOCK_LIMIT_MS);
            boolean done;
            synchronized (answered) { done = answered[0]; }
            if (!done) {
                StringBuilder report = new StringBuilder("DONMA (")
                        .append(SystemClock.uptimeMillis() - start).append(" ms)\n");
                for (StackTraceElement line : Looper.getMainLooper().getThread().getStackTrace()) {
                    report.append("  at ").append(line).append('\n');
                }
                save(report.toString());
                // Wait for recovery so one freeze is recorded only once.
                while (!done) {
                    SystemClock.sleep(500);
                    synchronized (answered) { done = answered[0]; }
                }
            }
        }
    }

    private static void save(String report) {
        Runtime runtime = Runtime.getRuntime();
        String header = "Android " + Build.VERSION.SDK_INT + ", " + Build.MODEL
                + ", bellek " + ((runtime.totalMemory() - runtime.freeMemory()) >> 20)
                + "/" + (runtime.maxMemory() >> 20) + " MB, video "
                + MovieRepository.getInstance().getAllMovies().size() + "\n";
        String text = header + report;
        // commit(): the process may be killed right after this.
        preferences.edit().putString("report", text.substring(0, Math.min(text.length(), 6000))).commit();
    }

    static String pendingReport() {
        return preferences.getString("report", null);
    }

    static void clear() {
        preferences.edit().remove("report").apply();
    }
}
