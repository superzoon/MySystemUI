package cn.nubia.systemui

import android.annotation.SuppressLint
import android.app.Service
import android.content.*
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
    private var mSystemUI:SystemUI? = null

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

    inner class NubiaSystemUI:INubiaSystemUI.Stub(), IBinder.DeathRecipient {
        override fun onFocusWindowChange(name: ComponentName) {
            UpdateMonitor.get().callFocusWindowChange(name)
        }

        override fun onStartActivity(name: ComponentName) {
            UpdateMonitor.get().callStartActivity(name)
        }

        override fun onStopActivity(name: ComponentName) {
            UpdateMonitor.get().callStopActivity(name)
        }

        override fun onBiometricChange(type: Int, data: Bundle) {
            UpdateMonitor.get().callBiometricChange(type, data)
        }

        override fun onAodViewChange(show: Boolean) {
            UpdateMonitor.get().callAodViewChange(show)
        }

        override fun onKeyguardChange(show: Boolean, occluded: Boolean) {
            UpdateMonitor.get().callKeyguardChange(show, occluded)
        }

        override fun onStartWakingUp() {
            UpdateMonitor.get().callStartWakingUp()
        }

        override fun onFinishedWakingUp() {
            UpdateMonitor.get().callFinishedWakingUp()
        }

        override fun onStartGoingToSleep(reason: Int) {
            UpdateMonitor.get().callStartGoingToSleep(reason)
        }

        override fun onFinishedGoingToSleep() {
            UpdateMonitor.get().callFinishedGoingToSleep()
        }

        override fun onFingerprintKeycode(keycode: Int) {
            UpdateMonitor.get().callFingerprintKeycode(keycode)
        }

        override fun binderDied() {
            UpdateMonitor.get().callSystemUIDisConnect()
        }

        override fun onConnect(systemui: IBinder) {
            systemui.linkToDeath(this, 0)
            mSystemUI = SystemUI(systemui)
            UpdateMonitor.get().callSystemUIConnect(mSystemUI!!)
        }

        override fun onSystemUIChange(type:Int, data:Bundle) {
            UpdateMonitor.get().callSystemUIChange(type, data)
        }
    }
}