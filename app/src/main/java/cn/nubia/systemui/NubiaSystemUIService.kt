package cn.nubia.systemui

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import android.view.WindowManager
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.common.UpdateMonitor
import cn.nubia.systemui.fingerprint.ThreadHelper
import java.io.FileDescriptor
import java.io.PrintWriter

class NubiaSystemUIService:Service(){
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Service"
    }

    val mNubiaSystemUI by lazy { NubiaSystemUI() }

    override fun onCreate() {
        super.onCreate()
        Controller.forEach { it.onStart(this) }
    }


    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Controller.forEach { it.onTrimMemory(level) }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        Controller.forEach { it.dump(fd, writer, args) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Controller.forEach { it.onStop(this) }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mNubiaSystemUI
    }

    class NubiaSystemUI:INubiaSystemUI.Stub(), IBinder.DeathRecipient {
        override fun binderDied() {
            UpdateMonitor.get().callSystemUIDisConnect()
        }

        override fun onConnect(systemui: IBinder) {
            systemui.linkToDeath(this, 0)
            UpdateMonitor.get().callSystemUIConnect(SystemUI(systemui))
        }

        override fun onSystemUIChange(type:Int, data:Bundle) {
            UpdateMonitor.get().callSystemUIChange(type, data)
        }
    }
}