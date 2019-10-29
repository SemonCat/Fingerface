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


class ModFingerface {
    companion object {
        @JvmStatic
        fun initAndroid(prefs: XSharedPreferences, classLoader: ClassLoader, modRes: XModuleResources) {
            val getSystemServiceHook = object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    if (param!!.args[0].toString() == Context.FINGERPRINT_SERVICE) {
                        Log.d("XposedHandler", "Get Fingerpring service, call from:${param!!.method.name}, thisObj:${param!!.result}")
                        val mgr = MyFingerprintManager()
                        mgr.SetContext(param!!.thisObject as Context)
                        param!!.result = mgr
                    }
                }
            }

            XposedBridge.hookAllMethods(
                Class.forName("android.app.ContextImpl"),
                "getSystemService",
                getSystemServiceHook
            )
        }
    }
}