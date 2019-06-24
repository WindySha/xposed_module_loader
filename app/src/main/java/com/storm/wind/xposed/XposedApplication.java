package com.storm.wind.xposed;

import android.app.Application;

import com.wind.xposed.entry.XposedModuleEntry;

public class XposedApplication extends Application {

    static {
        // 加载系统中所有已安装的Xposed Modules
        XposedModuleEntry.init();
    }
}
