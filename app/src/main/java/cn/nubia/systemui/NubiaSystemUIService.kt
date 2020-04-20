package cn.nubia.systemui

import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.os.*
import android.util.Log
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.common.SystemUIUpdateMonitor
import java.io.FileDescriptor
import java.io.PrintWriter

class NubiaSystemUIService:Service(){
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Service"
    }

    val mNubiaSystemUI by lazy { NubiaSystemUI() }
    private var mSystemUI:SystemUI? = null
    private val mHandler = Handler(Looper.getMainLooper(),object:Handler.Callback{
        override fun handleMessage(msg: Message): Boolean {
            if(msg.what == 118){
                Log.e(TAG, "onCreate keyboard=${msg.obj}")
            }
            return false
        }
    })

    fun call(type: Int, data: Bundle) {
        Log.e(TAG, "call type=${type} data=${data}")
    }

    override fun onCreate() {
        super.onCreate()
        Controller.forEach { it.callStart(this) }
        val newConfig: Configuration = resources.configuration
        Log.e(TAG, "onCreate keyboard=${newConfig.keyboard} hardKeyboardHidden=${newConfig.keyboardHidden} keyboardHidden=${newConfig.keyboardHidden}")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Controller.forEach { it.callTrimMemory(level) }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        Controller.forEach { it.dump(fd, writer, args) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Controller.forEach { it.callStop(this) }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mNubiaSystemUI
    }

    inner class NubiaSystemUI:INubiaSystemUI.Stub(), IBinder.DeathRecipient {
        override fun onFocusWindowChange(name: ComponentName) {
            SystemUIUpdateMonitor.get().callFocusWindowChange(name)
        }

        override fun onStartActivity(name: ComponentName) {
            SystemUIUpdateMonitor.get().callStartActivity(name)
        }

        override fun onStopActivity(name: ComponentName) {
            SystemUIUpdateMonitor.get().callStopActivity(name)
        }

        override fun onBiometricChange(type: Int, data: Bundle) {
            SystemUIUpdateMonitor.get().callBiometricChange(type, data)
        }

        override fun onAodViewChange(show: Boolean) {
            SystemUIUpdateMonitor.get().callAodViewChange(show)
        }

        override fun onKeyguardChange(show: Boolean, occluded: Boolean) {
            SystemUIUpdateMonitor.get().callKeyguardChange(show, occluded)
        }

        override fun onStartWakingUp() {
            SystemUIUpdateMonitor.get().callStartWakingUp()
        }

        override fun onFinishedWakingUp() {
            SystemUIUpdateMonitor.get().callFinishedWakingUp()
        }

        override fun onStartGoingToSleep(reason: Int) {
            SystemUIUpdateMonitor.get().callStartGoingToSleep(reason)
        }

        override fun onFinishedGoingToSleep() {
            SystemUIUpdateMonitor.get().callFinishedGoingToSleep()
        }

        override fun onFingerprintKeycode(keycode: Int) {
            SystemUIUpdateMonitor.get().callFingerprintKeycode(keycode)
        }

        override fun binderDied() {
            SystemUIUpdateMonitor.get().callSystemUIDisConnect()
        }

        override fun onConnect(systemui: IBinder) {
            systemui.linkToDeath(this, 0)
            mSystemUI = SystemUI(systemui)
            SystemUIUpdateMonitor.get().callSystemUIConnect(mSystemUI!!)
        }

        override fun callNubiaSystemUI(type: Int, data: Bundle) {
            call(type, data)
        }
    }
}