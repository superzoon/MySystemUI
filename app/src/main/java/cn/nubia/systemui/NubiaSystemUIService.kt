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
import android.view.WindowManager
import cn.nubia.systemui.aidl.ISystemUI;
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.SystemUI
import cn.nubia.systemui.ext.Controller
import cn.nubia.systemui.ext.UpdateMonitor
import cn.nubia.systemui.fingerprint.ThreadHelper
import java.io.FileDescriptor
import java.io.PrintWriter

@SuppressLint("NewApi")
class NubiaSystemUIService:Service(){
    val TAG = "${NubiaSystemUIApplication.TAG}.Service"

    val mNubiaSystemUI by lazy { NubiaSystemUI() }
    val mWindowManager by lazy { getSystemService(WindowManager::class.java) }
    val mDisplayManager by lazy { getSystemService(DisplayManager::class.java) }
    val mTelecomManager by lazy { getSystemService(TelecomManager::class.java) }
    val mTelephonyManager by lazy { getSystemService(TelephonyManager::class.java) }

    private val mInternalObj = object :BroadcastReceiver(), DisplayManager.DisplayListener {
        val mDisplayIds = arrayListOf<Int>()
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                Intent.ACTION_BATTERY_CHANGED -> {}
                else -> print("error receive")
            }
        }
        override fun onDisplayAdded(displayId: Int) {
            mDisplayIds.add(displayId)
        }

        override fun onDisplayRemoved(displayId: Int) {
            mDisplayIds.remove(displayId)
        }

        override fun onDisplayChanged(displayId: Int) {
            if(mDisplayIds.contains(displayId)){
                mDisplayManager.getDisplay(displayId)?.state.also {
                    UpdateMonitor.get().callDisplayChange(displayId, it!!)
                }
            }
        }
    }

    val mPhoneStateListener = object :PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
        }
    }
    private fun register(){
        val filter = IntentFilter()
        registerReceiver(mInternalObj, filter)
        mDisplayManager.registerDisplayListener(mInternalObj, ThreadHelper.get().getMainHander())
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun unRegister(){
        unregisterReceiver(mInternalObj)
        mDisplayManager.unregisterDisplayListener(mInternalObj)
    }

    override fun onCreate() {
        super.onCreate()
        register()
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
        unRegister();
    }

    override fun onBind(intent: Intent?): IBinder? {
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