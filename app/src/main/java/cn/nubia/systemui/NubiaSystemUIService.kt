package cn.nubia.systemui

import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.os.*
import android.util.Log
import cn.nubia.systemui.aidl.INubiaSystemUI;
import cn.nubia.systemui.common.*
import cn.nubia.systemui.fingerprint.FingerprintController
import cn.nubia.systemui.fingerprint.SystemBiometricMonitor
import java.io.FileDescriptor
import java.io.PrintWriter

class NubiaSystemUIService:Service(){
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Service"
    }

    private val mNubiaSystemUI by lazy { NubiaSystemUI() }
    private var mSystemUI:SystemUI? = null
    private val mHandler = Handler(Looper.getMainLooper(),object:Handler.Callback{
        override fun handleMessage(msg: Message): Boolean {
            if(msg.what == 118){
                Log.e(TAG, "onCreate keyboard=${msg.obj}")
            }
            return false
        }
    })

    operator fun contains(value:Int):Boolean{
        return mNubiaSystemUI!=null
    }

    fun onCall(type: Int, data: Bundle) {
        Log.e(TAG, "call type=${type} data=${data}")
        when(type){
            SystemUIStateConstant.TYPE_TEST ->{}
            in this ->{}
            else -> {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        Controller.forEach { it.callStart(this) }
        SystemBiometricMonitor.get().addCallback(mSystemBiometricMonitor)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Controller.forEach { it.callTrimMemory(level) }
    }

    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        DumpHelper.dipatchDump(fd, writer, args)
    }

    override fun onDestroy() {
        super.onDestroy()
        Controller.forEach { it.callStop(this) }
        SystemBiometricMonitor.get().removeCallback(mSystemBiometricMonitor)
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

        override fun callNubiaSystemUI(type: Int, data: Bundle) {
            onCall(type, data)
        }
    }


    private val mSystemBiometricMonitor = object : SystemBiometricMonitor.UpdateMonitorCallback{
        val DEBUG = false

        fun log(msg:Any){
            if(DEBUG) Log.i(FingerprintController.TAG,"${msg}")
        }

        override fun showBiometricView(bundle: Bundle?, receiver: IBiometricServiceReceiverInternal?, type: Int, requireConfirmation: Boolean, userId: Boolean){
            log( "showBiometricView bundle=${bundle} receiver=${receiver} type=${type} requireConfirmation=${requireConfirmation} userId=${userId }")
        }

        override fun hideBiometricView(){
            log( "hideBiometricView")
        }

        override fun onBiometricAuthenticated(authenticated: Boolean, failureReason: String?){
            log( "onBiometricAuthenticated authenticated=${authenticated} failureReason=${failureReason}")
        }

        override fun onBiometricHelp(message: String?){
            log( "onBiometricHelp message=${message}")
        }

        override fun onBiometricError(error: String?){
            log( "onBiometricError error=${error}")
        }
    }
}