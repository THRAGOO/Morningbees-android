package com.jasen.kimjaeseung.morningbees.app

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.jasen.kimjaeseung.morningbees.utils.BeeInfoManager
import com.jasen.kimjaeseung.morningbees.utils.DeviceInfoManager
import com.jasen.kimjaeseung.morningbees.utils.SharedPreference

class GlobalApp : Application() {
    var DEBUG = true   //for log in debug mode

    companion object {
        lateinit var prefs: SharedPreference
        lateinit var prefsBeeInfo: BeeInfoManager
        lateinit var prefsDeviceInfo: DeviceInfoManager
    }

    override fun onCreate() {
        // SharedPreferences 클래스는 앱에 있는 다른 Activity 보다 먼저 생성되어야함.

        prefs = SharedPreference(applicationContext)
        prefsBeeInfo = BeeInfoManager(applicationContext)
        prefsDeviceInfo = DeviceInfoManager(applicationContext)

        super.onCreate()
        this.DEBUG = isDebuggable(this);
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    private fun isDebuggable(context: Context): Boolean {
        var debuggable = false
        val pm: PackageManager = context.getPackageManager()
        try {
            val appinfo = pm.getApplicationInfo(context.getPackageName(), 0)
            debuggable = 0 != appinfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        } catch (e: PackageManager.NameNotFoundException) { /* debuggable variable will remain false */
        }
        return debuggable
    }
}