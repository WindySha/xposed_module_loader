package com.wind.xposed.entry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.wind.xposed.entry.util.FileUtils;
import com.wind.xposed.entry.util.NativeLibraryHelperCompat;
import com.wind.xposed.entry.util.PackageNameCache;
import com.wind.xposed.entry.util.PluginNativeLibExtractor;
import com.wind.xposed.entry.util.SharedPrefUtils;
import com.wind.xposed.entry.util.VMRuntime;
import com.wind.xposed.entry.util.XLog;
import com.wind.xposed.entry.util.XpatchUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XposedHelper;

/**
 * Created by Wind
 */
public class XposedModuleEntry {

    private static final String TAG = "XposedModuleEntry";

    private static AtomicBoolean hasInited = new AtomicBoolean(false);
    private static Context appContext;

    public static void init() {
        init(null, true);
    }

    public static void init(List<String> moduleFilePathList) {
        init(moduleFilePathList, false);
    }

    public static void init(String fileDir) {
        File fileParent = new File(fileDir);
        if (!fileParent.exists()) {
            return;
        }

        List<String> modulePathList = new ArrayList<>();
        File[] childFileList = fileParent.listFiles();
        if (childFileList != null && childFileList.length > 0) {
            for (File file : childFileList) {
                if (file.isFile()) {
                    modulePathList.add(file.getAbsolutePath());
                }
            }
        }
        init(modulePathList, false);
    }

    public static void init(List<String> modulePathList, boolean enableLoadInstalledModules) {
        if (!hasInited.compareAndSet(false, true)) {
            Log.e(TAG, " Xposed module has been loaded !!!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            VMRuntime.setHiddenApiExemptions(new String[]{"L"});
        }
        Context context = XpatchUtils.createAppContext();
        SandHookInitialization.init(context);

        if ((modulePathList == null || modulePathList.size() == 0) && !enableLoadInstalledModules) {
            Log.e(TAG, " modulePathList is null and installed module is disabled, so no xposed modules will be loaded.");
            return;
        }
        loadModulesInternal(context, modulePathList, enableLoadInstalledModules);
    }

    private static void loadModulesInternal(Context context, List<String> userModulePathList, boolean enableLoadInstalledModules) {

        if (context == null) {
            android.util.Log.e(TAG, "try to init XposedModuleEntry, but create app context failed !!!!");
            return;
        }

        appContext = context;

        SharedPrefUtils.init(context);
        ClassLoader originClassLoader = context.getClassLoader();
        List<String> modulePathList = new ArrayList<>();
        List<String> installedModulePathList = null;
        if (enableLoadInstalledModules) {
            installedModulePathList = loadAllInstalledModule(context);
        }

        if (userModulePathList != null && userModulePathList.size() > 0) {
            for (String path : userModulePathList) {
                if (path != null && path.length() > 0 && new File(path).exists()) {
                    String packageName = getPackageNameByPath(context, path);
                    if (packageName != null && packageName.length() > 0) {
                        modulePathList.add(path);
                    } else {
                        Log.e(TAG, " Xposed module file path is invalid.  The file is not an apk file!!!, path -> " + path);
                    }
                } else {
                    Log.e(TAG, " Xposed module file path is invalid!!!  path -> " + path);
                }
            }
        }

        // 过滤掉已经打包在apk中的module，避免同一个module被加载了两次
        if (installedModulePathList != null && installedModulePathList.size() > 0) {
            List<String> packedModulePakcageNameList = null;

            for (String apkPath : modulePathList) {
                if (packedModulePakcageNameList == null) {
                    packedModulePakcageNameList = new ArrayList<>();
                }
                String packageName = getPackageNameByPath(context, apkPath);
                XLog.d(TAG, "Current packed module path ----> " + apkPath + " packageName = " + packageName);
                packedModulePakcageNameList.add(packageName);
            }

            if (packedModulePakcageNameList == null || packedModulePakcageNameList.size() == 0) {
                modulePathList.addAll(installedModulePathList);
            } else {
                for (String apkPath : installedModulePathList) {
                    String packageName = getPackageNameByPath(context, apkPath);
                    XLog.d(TAG, "Current installed module path ----> " + apkPath + " packageName = " + packageName);
                    if (!packedModulePakcageNameList.contains(packageName)) {
                        modulePathList.add(apkPath);
                    }
                }
            }
        }

        String appPrivateDir = context.getFilesDir().getParentFile().getAbsolutePath();
        for (String modulePath : modulePathList) {
            String dexPath = context.getDir("xposed_plugin_dex", Context.MODE_PRIVATE).getAbsolutePath();
            if (!TextUtils.isEmpty(modulePath)) {
                String packageName = getPackageNameByPath(context, modulePath);
                Log.d(TAG, "Current truely loaded module path ----> " + modulePath + " packageName: " + packageName);
                String pathNameSuffix = packageName;
                if (pathNameSuffix == null || pathNameSuffix.isEmpty()) {
                    pathNameSuffix = XpatchUtils.strMd5(modulePath);
                }

                String xposedPluginFilePath = appPrivateDir + "/xpatch_plugin_native_lib/plugin_" + pathNameSuffix;

                String soFilePath;
                if (NativeLibraryHelperCompat.is64bit()) {
                    soFilePath = xposedPluginFilePath + "/lib/arm64";
                } else {
                    soFilePath = xposedPluginFilePath + "/lib/arm";
                }
                // 将插件apk中的so文件释放到soFilePath目录下
                PluginNativeLibExtractor.copySoFileIfNeeded(context, soFilePath, modulePath);

                XposedModuleLoader.loadModule(modulePath, dexPath, soFilePath, context.getApplicationInfo(), originClassLoader);
            }
        }
    }

    private static String getPackageNameByPath(Context context, String apkPath) {
        return PackageNameCache.getInstance(context).getPackageNameByPath(apkPath);
    }

    private static List<String> loadAllInstalledModule(Context context) {
        PackageManager pm = context.getPackageManager();
        List<String> modulePathList = new ArrayList<>();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_META_DATA);
        for (PackageInfo pkg : packageInfoList) {
            ApplicationInfo app = pkg.applicationInfo;
            if (!app.enabled)
                continue;
            if (app.metaData != null && app.metaData.containsKey("xposedmodule")) {
                String apkPath = pkg.applicationInfo.publicSourceDir;
                if (TextUtils.isEmpty(apkPath)) {
                    apkPath = pkg.applicationInfo.sourceDir;
                }
                if (!TextUtils.isEmpty(apkPath)) {
                    XLog.d(TAG, " query installed module path -> " + apkPath);
                    modulePathList.add(apkPath);
                }
            }
        }
        return modulePathList;
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isEmpty(Collection collection) {
        if (collection == null || collection.size() == 0) {
            return true;
        }
        return false;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
