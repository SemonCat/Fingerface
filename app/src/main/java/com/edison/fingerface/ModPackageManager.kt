package com.edison.fingerface

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XModuleResources
import android.hardware.fingerprint.FingerprintManager
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import android.hardware.fingerprint.MyFingerprintManager
import de.robv.android.xposed.XposedHelpers
import android.R.attr.classLoader


class ModPackageManagerService {
    companion object {
        @JvmStatic
        fun initAndroid(prefs: XSharedPreferences, classLoader: ClassLoader, modRes: XModuleResources) {

            val hasSystemFeatureHook = object : XC_MethodHook() {

                override fun afterHookedMethod(param: MethodHookParam?) {
                    if (param!!.args[0].toString() == PackageManager.FEATURE_FINGERPRINT) {
                        param!!.result = true
                    }
                }
            }

            XposedHelpers.findAndHookMethod("android.app.ContextImpl", classLoader,
                "getPackageManager", object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        if (param!!.result is PackageManager) {
                            val clazz = param.result.javaClass
                            val packageMethod = clazz.getDeclaredMethod("hasSystemFeature", String::class.java)

                            XposedBridge.hookMethod(packageMethod, hasSystemFeatureHook)

                        }
                    }
                })

        }
    }
}