package cn.nubia.systemui.common

import android.content.*
import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import android.view.WindowManager
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.NubiaBiometricMonitor
import cn.nubia.systemui.fingerprint.SystemBiometricMonitor
import cn.nubia.systemui.fingerprint.ThreadHelper
import java.lang.NumberFormatException
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class UpdateMonitor private constructor(){
    private val mHandler = Handler(Looper.getMainLooper());
    private val mList = mutableListOf<Reference<UpdateMonitorCallback>>()
    private val mDisplayStateMap = mutableMapOf<Int, Int>()
    private var mSystemUI:SystemUI? = null
    private val mContext by lazy {
        NubiaSystemUIApplication.getContext()
    }
    val mWindowManager by lazy { mContext.getSystemService(WindowManager::class.java) }
    val mDisplayManager by lazy { mContext.getSystemService(DisplayManager::class.java) }
    val mTelephonyManager by lazy { mContext.getSystemService(TelephonyManager::class.java) }

    interface  UpdateMonitorCallback{
        fun onSystemUIConnect(systemui: SystemUI){}
        fun onSystemUIDisConnect(){}
        fun onDisplayChange(displayId: Int, state:Int, stateStr:String){}
        fun onFocusWindowChange(name:ComponentName){}
        fun onStartActivity(name:ComponentName){}
        fun onStopActivity(name:ComponentName){}
        fun onAodViewChange(show:Boolean){}
        fun onKeyguardChange(show:Boolean, occluded:Boolean){}
        fun onStartWakingUp(){}
        fun onFinishedWakingUp(){}
        fun onStartGoingToSleep(reason:Int){}
        fun onFinishedGoingToSleep(){}
        fun onFingerprintKeycode(keycode:Int){}
    }

    val mPhoneStateListener = object :PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
        }
    }

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
            if(displayId==Display.DEFAULT_DISPLAY || mDisplayIds.contains(displayId)){
                mDisplayManager.getDisplay(displayId)?.state.also {
                    UpdateMonitor.get().callDisplayChange(displayId, it!!)
                }
            }
        }
    }

    init {
        val filter = IntentFilter()
        mContext.registerReceiver(mInternalObj, filter)
        mDisplayManager.registerDisplayListener(mInternalObj, ThreadHelper.get().getMainHander())
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        mHandler.post{UpdateMonitor.get().callDisplayChange(Display.DEFAULT_DISPLAY, mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY).state)}
    }

    fun callSystemUIDisConnect(){
        mHandler.post{
            mSystemUI = null
            mList.forEach{
                it.get()?.onSystemUIDisConnect()
            }
        }
    }

    fun callBiometricChange(action:Int, data:Bundle){
        val debug = false
        val log = fun (any:String){ if(debug) Log.i(TAG, any)}
        when(action){
            BiometricConstant.TYPE_SHOW -> {
                Log.w(TAG, "callBiometricChange TYPE_SHOW")
                val bundle = data.getBundle("bundle")
                val receiver = IBiometricServiceReceiverInternal.Stub.asInterface(data.getBinder("receiver"))
                val type = data.getInt("type")
                val requireConfirmation = data.getBoolean("requireConfirmation")
                val userId = data.getBoolean("userId")
                log("callBiometricChange TYPE_SHOW bundle=${bundle} receiver=${receiver} " +
                        "type=${type} requireConfirmation=${requireConfirmation} userId=${userId}")
                SystemBiometricMonitor.get().callShowBiometricView(bundle, receiver, type, requireConfirmation, userId)
            }
            BiometricConstant.TYPE_HIDE -> {
                log("callBiometricChange TYPE_HIDE")
                SystemBiometricMonitor.get().callHideBiometricView()
            }
            BiometricConstant.TYPE_AUTHENTICATED ->{
                val authenticated = data.getBoolean("authenticated")
                val failureReason = data.getString("failureReason")
                SystemBiometricMonitor.get().callBiometricAuthenticated(authenticated, failureReason)
                log("callBiometricChange TYPE_AUTHENTICATED authenticated=${authenticated} failureReason=${failureReason}")

            }
            BiometricConstant.TYPE_HELP ->{
                val message = data.getString("message")
                log("callBiometricChange TYPE_HELP message=${message}")
                SystemBiometricMonitor.get().callBiometricHelp(message)
            }
            BiometricConstant.TYPE_ERROR ->{
                val error = data.getString("error")
                log("callBiometricChange TYPE_ERROR error=${error}")
                SystemBiometricMonitor.get().callBiometricError(error)
            }
            BiometricConstant.TYPE_ATTR_FLAGES->{
                val flags = data.getInt("flags")
                callBiometricAttrFlagesChange(flags)
            }
            else -> {
                if(data.containsKey("info")){
                    data.getString("info")?.split("_")?.apply {
                        when(get(0)){
                            "startAuth" -> {
                                val owner = if(size>1){get(1)}else{null}
                                log("startAuth owner = ${owner}")
                                NubiaBiometricMonitor.get().callStartAuth(owner)
                            }
                            "doneAuth" -> {
                                log("doneAuth")
                                NubiaBiometricMonitor.get().callDoneAuth()
                            }
                            "stopAuth" -> {
                                log("stopAuth")
                                NubiaBiometricMonitor.get().callStopAuth()
                            }
                            "autherror" -> {
                                log("autherror")
                                NubiaBiometricMonitor.get().callAuthError()
                            }
                            "failAuth" -> {
                                log("failAuth")
                                NubiaBiometricMonitor.get().callFailAuth()
                            }
                            else -> {
                                val info = try {
                                    get(0).toInt()
                                }catch (e:NumberFormatException){
                                    0
                                }
                                log("acquired info = ${info}")
                                NubiaBiometricMonitor.get().callAcquired(info)
                            }
                        }
                    }
                }
            }

        }
    }

    var mOldFingerprintRelationState = 0
    private fun callBiometricAttrFlagesChange(state:Int){
        if(BiometricDiplayConstant.isValidState(state)){
            if(mOldFingerprintRelationState != state){
                mOldFingerprintRelationState = state
                Log.i(TAG, "fingerprint relation state change = ${BiometricDiplayConstant.toString(state)}")
            }
        }else{
            throw IllegalAccessError("error fingerprint relation state = ${state}")
        }
    }

    fun callSystemUIConnect(systemui: SystemUI){
        mHandler.post{
            mSystemUI = systemui
            mList.forEach{
                it.get()?.onSystemUIConnect(systemui)
            }
        }
    }

    fun getSystemUI():SystemUI? = mSystemUI

    fun getDisplayState():Int = getDisplayState(Display.DEFAULT_DISPLAY)

    fun getDisplayState(displayId:Int):Int = mDisplayStateMap.getOrDefault(displayId, Display.STATE_UNKNOWN)

    fun callDisplayChange(displayId:Int, state:Int){
        if(!(mDisplayStateMap.containsKey(displayId) && mDisplayStateMap.get(displayId)==state)){
            mDisplayStateMap.put(displayId, state)
            mHandler.post{
                mList.forEach{
                    it.get()?.onDisplayChange(displayId, state, when(state){
                        Display.STATE_OFF -> "STATE_OFF"
                        Display.STATE_ON -> "STATE_ON"
                        Display.STATE_DOZE -> "STATE_DOZE"
                        Display.STATE_DOZE_SUSPEND -> "STATE_DOZE_SUSPEND"
                        Display.STATE_VR -> "STATE_VR"
                        else  -> "STATE_UNKNOWN"
                    })
                }
            }
        }
    }

    fun addCallback(callback: UpdateMonitorCallback?){
        callback.apply {
            mHandler.post{
                if(mList.find { it.get()== this } == null){
                    mList.add(WeakReference(this))
                }
                removeCallback(null)
                Log.i(TAG, "addCallback size=${mList.size}")
            }
        }
    }

    fun removeCallback(callback: UpdateMonitorCallback?){
        mHandler.post{
            mList.removeAll { it.get() == callback }
        }
    }

    fun callFocusWindowChange(name: ComponentName) {

        mHandler.post{
            mList.forEach{
                it.get()?.onFocusWindowChange(name)
            }
        }
    }

    fun callKeyboardChange(new:Int){
        Log.e(TAG, "callKeyboardChange ${new}")
    }

    fun callStartActivity(name: ComponentName) {
        mHandler.post{
            mList.forEach{
                it.get()?.onStartActivity(name)
            }
        }
    }

    fun callStopActivity(name: ComponentName) {
        mHandler.post{
            mList.forEach{
                it.get()?.onStopActivity(name)
            }
        }
    }

    fun callAodViewChange(show: Boolean) {
        mHandler.post{
            mList.forEach{
                it.get()?.onAodViewChange(show)
            }
        }
    }

    fun callKeyguardChange(show: Boolean, occluded: Boolean) {
        mHandler.post{
            mList.forEach{
                it.get()?.onKeyguardChange(show, occluded)
            }
        }
    }

    fun callStartWakingUp() {
        mHandler.post{
            mList.forEach{
                it.get()?.onStartWakingUp()
            }
        }
    }

    fun callFinishedWakingUp() {
        mHandler.post{
            mList.forEach{
                it.get()?.onFinishedWakingUp()
            }
        }
    }

    fun callStartGoingToSleep(reason: Int) {
        mHandler.post{
            mList.forEach{
                it.get()?.onStartGoingToSleep(reason)
            }
        }
    }

    fun callFinishedGoingToSleep() {
        mHandler.post{
            mList.forEach{
                it.get()?.onFinishedGoingToSleep()
            }
        }
    }

    fun callFingerprintKeycode(keycode: Int) {
        mHandler.post{
            mList.forEach{
                it.get()?.onFingerprintKeycode(keycode)
            }
        }
    }

    companion object {
        private val TAG = "${NubiaSystemUIApplication.TAG}.Monitor"
        private  var mUpdateMonitor:UpdateMonitor? = null
            get(){
                if (field == null){
                    field = UpdateMonitor()
                }
                return field
            }

        public fun get():UpdateMonitor{
            return mUpdateMonitor!!
        }
    }
}