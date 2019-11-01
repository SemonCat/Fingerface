package com.edison.fingerface

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XModuleResources
import android.hardware.fingerprint.MyFingerprintManager
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedHandler : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private lateinit var modulePath: String
    private lateinit var modRes: XModuleResources
    private lateinit var prefs: XSharedPreferences

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        modRes = XModuleResources.createInstance(modulePath, null)
        prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.processName != "android" && lpparam.processName != "com.android.systemui") {
            hookFingerprintService(lpparam.classLoader)
            hookPackageManager(lpparam.classLoader)
        }
    }

    private fun hookFingerprintService(cl: ClassLoader) {
        val getSystemServiceHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.args[0].toString() == Context.FINGERPRINT_SERVICE) {
                    param.result = MyFingerprintManager(param.thisObject as Context)
                }
            }
        }

        XposedHelpers.findAndHookMethod(
            "android.app.ContextImpl", cl,
            "getSystemService", String::class.java,
            getSystemServiceHook
        )
    }

    private fun hookPackageManager(cl: ClassLoader) {
        val hasSystemFeatureHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.args[0].toString() == PackageManager.FEATURE_FINGERPRINT) {
                    param.result = true
                }
            }
        }

        XposedHelpers.findAndHookMethod(
            "android.app.ApplicationPackageManager", cl,
            "hasSystemFeature", String::class.java,
            hasSystemFeatureHook
        )
    }

}
