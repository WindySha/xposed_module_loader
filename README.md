![](https://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)
![](https://img.shields.io/badge/release-1.0.4-red.svg?style=flat)
![](https://img.shields.io/badge/Android-5%20--%2012-blue.svg?style=flat)
![](https://img.shields.io/badge/arch-armeabi--v7a%20%7C%20arm64--v8a-blue.svg?style=flat)

# Introduction
This is a library used to load xposed module files.   

# Features

* Support loading xposed modules by the apk file path;  
* Support loading all the xposed modules installed in the device;
* Support loading all the native libraries in the xposed modules;
* Support importing xposed styled java hooking framework to android projects;


# Usage
## 1. Add dependency to build.gradle file

This tool is published on [Maven Central](https://search.maven.org/).

```Gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

```Gradle
android {
    defaultConfig {
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}

dependencies {
    implementation 'io.github.windysha:xposed_module_loader:1.0.4'
}
```

## 2. Add init code to the Application file.
* Load xposed modules by file paths:
```
    @Override
    protected void attachBaseContext(Context base) {
        List<String> list = new ArrayList<String>() {
            {
                add("/mnt/sdcard/xposed_module.apk");  // app need to hava permission read files in the sdcard.
                add("/data/data/com.storm.wind.xposed/files/xposed_module.apk");
            }
        };
        XposedModuleEntry.init(base, list);
        super.attachBaseContext(base);
    }
```
* Load xposed modules by file directory:
```
    @Override
    protected void attachBaseContext(Context base) {
        // all xposed module files in the dir /data/data/package_name/ will be loaded.
        XposedModuleEntry.init(base, "/data/data/package_name/");
        super.attachBaseContext(base);
    }
```
* Load all xposed modules installed in the devices:
```
    @Override
    protected void attachBaseContext(Context base) {
        XposedModuleEntry.init(base);
        super.attachBaseContext(base);
    }
```
* Only init java hook framework, do not load any xposed modules:
```
    @Override
    protected void attachBaseContext(Context base) {
        XposedModuleEntry.init(base, null, false);
        super.attachBaseContext(base);
    }
```

# Applied
Early version of [Xpatch](https://github.com/WindySha/Xpatch) use this library to load xposed modules。

# Reference
[SandHook](https://github.com/asLody/SandHook)

# License
```
Copyright 2021 WindySha

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
