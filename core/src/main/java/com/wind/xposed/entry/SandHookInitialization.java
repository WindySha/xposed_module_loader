package com.wind.xposed.entry;

import android.content.Context;
import android.util.Log;

import com.swift.sandhook.SandHook;
import com.swift.sandhook.SandHookConfig;
import com.swift.sandhook.xposedcompat.XposedCompat;
import com.wind.xposed.entry.util.XpatchUtils;

/**
 * @author Windysha
 */
public class SandHookInitialization {

    public static void init(Context context) {
        Log.d("SandHookInitialization", "start init");
        if (context == null) {
            Log.e("SandHookInitialization", "try to init SandHook, but app context is null !!!!");
            return;
        }

        sandHookCompat(context);

        SandHookConfig.DEBUG = XpatchUtils.isApkDebugable(context);
        XposedCompat.cacheDir = context.getCacheDir();
        XposedCompat.context = context;
        XposedCompat.classLoader = context.getClassLoader();
        XposedCompat.isFirstApplication = true;
    }

    private static void sandHookCompat(Context context) {
        SandHook.disableVMInline();
        SandHook.tryDisableProfile(context.getPackageName());
        SandHook.disableDex2oatInline(false);
    }
}
