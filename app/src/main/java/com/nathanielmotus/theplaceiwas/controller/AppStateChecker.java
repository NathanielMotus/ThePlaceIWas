package com.nathanielmotus.theplaceiwas.controller;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class AppStateChecker {

    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager activityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos=activityManager.getRunningAppProcesses();
        if (runningAppProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo info : runningAppProcessInfos) {
                if (info.processName.equals(packageName)) {
                    Log.i("TEST","Importance : "+info.importance);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAppInForeground(Context context, String packageName) {
        ActivityManager activityManager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos=activityManager.getRunningAppProcesses();
        if (runningAppProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo info : runningAppProcessInfos) {
                if (info.processName.equals(packageName) && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }
}
