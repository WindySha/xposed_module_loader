package com.storm.wind.xposed;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.wind.xposed.entry.XposedModuleEntry;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class XposedTestApplication extends Application {

    static {
        List<String> list = new ArrayList<String>() {
            {
                add("/mnt/sdcard/app-debug.apk");
                add("/data/data/com.storm.wind.xposed/files/app-debug.apk");
            }
        };
        XposedModuleEntry.init(null, list);
//        XposedModuleEntry.init(null, "/data/data/com.storm.wind.xposed/");
    }

    @Override
    protected void attachBaseContext(Context base) {
//        List<String> list = new ArrayList<String>() {
//            {
//                add("/mnt/sdcard/app-debug.apk");
//                add("/data/data/com.storm.wind.xposed/files/app-debug.apk");
//            }
//        };
////        XposedModuleEntry.init(base, list);
//        XposedModuleEntry.init(base, null, false);

        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hookOnCreate();
    }

    private void hookOnCreate() {
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.e("xiawanli", " beforeHookedMethod  onCreate");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.e("xiawanli", " beforeHookedMethod  onCreate");
            }
        });
    }

}
