package com.wind.xposed.entry;

import android.content.Context;
import android.util.Log;
import com.wind.xposed.entry.util.XpatchUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Windysha
 */
public class SandHookInitialization {

    public static void init(Context context) {

        if (context == null) {
            Log.e("SandHookInitialization", "try to init SandHook, but app context is null !!!!");
            return;
        }

        sandHookCompat(context);

//        SandHookConfig.DEBUG = XpatchUtils.isApkDebugable(context);
//        XposedCompat.cacheDir = context.getCacheDir();
//        XposedCompat.context = context;
//        XposedCompat.classLoader = context.getClassLoader();
//        XposedCompat.isFirstApplication = true;

        String SandHookConfigClassName = "com.swift.sandhook.SandHookConfig";
        boolean isDebug = XpatchUtils.isApkDebugable(context);

        try {
            Class SandHookConfigClaszz = Class.forName(SandHookConfigClassName);
            Field DEBUG_field = SandHookConfigClaszz.getDeclaredField("DEBUG");
            DEBUG_field.setAccessible(true);
            DEBUG_field.set(null, isDebug);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        String XposedCompatClassName = "com.swift.sandhook.xposedcompat.XposedCompat";

        try {
            Class XposedCompatgClaszz = Class.forName(XposedCompatClassName);

            Field cacheDir_field = XposedCompatgClaszz.getDeclaredField("cacheDir");
            cacheDir_field.setAccessible(true);
            cacheDir_field.set(null, context.getCacheDir());

            Field context_field = XposedCompatgClaszz.getDeclaredField("context");
            context_field.setAccessible(true);
            context_field.set(null, context);

            Field classLoader_field = XposedCompatgClaszz.getDeclaredField("classLoader");
            classLoader_field.setAccessible(true);
            classLoader_field.set(null, context.getClassLoader());

            Field isFirstApplication_field = XposedCompatgClaszz.getDeclaredField("isFirstApplication");
            isFirstApplication_field.setAccessible(true);
            isFirstApplication_field.set(null, true);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void sandHookCompat(Context context) {
//        SandHook.disableVMInline();
//        SandHook.tryDisableProfile(context.getPackageName());
//        SandHook.disableDex2oatInline(false);

        String className = "com.swift.sandhook.SandHook";
        try {
            Class sandHook_Clazz = Class.forName(className);

            Method disableVMInline_method = sandHook_Clazz.getDeclaredMethod("disableVMInline");
            disableVMInline_method.setAccessible(true);
            disableVMInline_method.invoke(null);

            Method tryDisableProfile_method = sandHook_Clazz.getDeclaredMethod("tryDisableProfile");
            tryDisableProfile_method.setAccessible(true);
            tryDisableProfile_method.invoke(null, context.getPackageName());

            Method disableDex2oatInline_method = sandHook_Clazz.getDeclaredMethod("disableDex2oatInline");
            disableDex2oatInline_method.setAccessible(true);
            disableDex2oatInline_method.invoke(null, false);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
