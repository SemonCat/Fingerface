package com.edison.fingerface;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.util.Log;
import android.util.Pair;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.ArrayList;
import java.util.List;

public class XposedHandler implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    public static final String PACKAGE_NAME = XposedHandler.class.getPackage().getName();
    private static final String TAG = "Fingerface";

    private static XSharedPreferences prefs;
    public static XModuleResources modRes;
    private static String MODULE_PATH = null;

    public static List<Pair<XResources, String>> packageResources = new ArrayList<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        modRes = XModuleResources.createInstance(MODULE_PATH, null);
        prefs = new XSharedPreferences(PACKAGE_NAME);

    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

        if (resparam.packageName.equals("android")) {
            return;
        }

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        if (!lpparam.processName.equals("android") && !lpparam.processName.equals("com.android.systemui")) {
            ModFingerface.initAndroid(prefs, lpparam.classLoader, modRes);
            ModPackageManagerService.initAndroid(prefs, lpparam.classLoader, modRes);
        }
    }

}
