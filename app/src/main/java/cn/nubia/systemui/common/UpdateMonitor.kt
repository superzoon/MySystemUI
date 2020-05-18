package cn.nubia.systemui.common

import android.content.*
import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.WindowManager
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.NubiaBiometricMonitor
import cn.nubia.systemui.fingerprint.SystemBiometricMonitor
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.R
import java.io.File
import java.io.FileDescriptor
import java.io.PrintWriter
import java.lang.NumberFormatException
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class UpdateMonitor : DumpHelper.Dump {
    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        //打印最近40个info
        writer?.apply {
            val start = mIndexForInfo+1
            val end = start+mInfoList.size
            (start..end).forEach{
                mInfoList[it%mInfoList.size]?.also { writer.println("${it}") }
            }
        }
    }

    private val mHandler = Handler(Looper.getMainLooper());
    private val mList = mutableListOf<Reference<UpdateMonitorCallback>>()
    private val mDisplayStateMap = mutableMapOf<Int, Int>()
    private val mInfoList = arrayOfNulls<InfoStr>(40)
    private var mIndexForInfo = 0
    private var mSystemUI:SystemUI? = null
    var mBiometricAttrFlags = 0
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
        fun onPhoneStateChange(state:Int, phoneNumber: String?){}
        fun onFingerUp(){}
        fun onFingerDown(){}
        fun callPackageChange(add: Boolean, uri: Uri?){}
    }

    val mPhoneStateListener = object :PhoneStateListener(){
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            UpdateMonitor.get().callPhoneStateChange(state, phoneNumber)
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

    private constructor() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)
        mContext.registerReceiver(mInternalObj, filter)
        mDisplayManager.registerDisplayListener(mInternalObj, NubiaThreadHelper.get().getMainHander())
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        handlerPost{UpdateMonitor.get().callDisplayChange(Display.DEFAULT_DISPLAY, mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY).state)}
        File(mContext.getString(R.string.tp_action_node)).monitor {
            when(it){
                "finger_down" -> {
                    callFingerDown()
                }
                "finger_up" -> {
                    callFingerUp()
                }
                else -> {
                    Log.w(TAG,"ERR TP ACTION ${it}, ${mContext.getString(R.string.tp_action_node)}")
                }
            }
        }
    }

    private fun handlerPost(action:()->Unit){
        mHandler.post(action)
    }

    fun getSystemUI():SystemUI? = mSystemUI

    fun getDisplayState():Int = getDisplayState(Display.DEFAULT_DISPLAY)

    fun getDisplayState(displayId:Int):Int = mDisplayStateMap.getOrDefault(displayId, Display.STATE_UNKNOWN)

    var isAodViewShow = false
        private set(value) {field = value}

    var mKeyguardShow = false
        private set(value) {field = value}

    var isOccluded = false
        private set(value) {field = value}

    var isWakeUp = false
        private set(value) {field = value}

    var mCurrentActivity: ComponentName? = null
        private set(value) {field = value}

    var mCurrentWindow: ComponentName? = null
        private set(value) {field = value}

    var mPhoneState = TelephonyManager.CALL_STATE_IDLE
        private set(value) {field = value}


    fun callSystemUIDisConnect(){
        handlerPost{
            mSystemUI = null
            mList.forEach{
                it.get()?.onSystemUIDisConnect()
            }
        }
    }

    fun callBiometricChange(action:Int, data:Bundle){
        val debug = true
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
                callBiometricAttrFlagsChange(data.getInt("flags", 0))
            }
            BiometricConstant.TYPE_INFO-> {
                if(data.containsKey("info")){
                    data.getString("info")?.also {
                        mInfoList[mIndexForInfo++]= InfoStr(it)
                        it.split("_")?.apply {
                            val indexStr = get(0)
                            when(indexStr){
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
                                        if(indexStr.startsWith("eenroll")){
                                            FingerprintInfo.MSG_EENROLL_BASE_CODE+indexStr.replace("eenroll","").toInt()
                                        }else{
                                            indexStr.toInt()
                                        }
                                    }catch (e:NumberFormatException){
                                        log("error info = ${get(0)}")
                                        0
                                    }
                                    log("acquired info = ${info}")
                                    NubiaBiometricMonitor.get().callAcquired(info)
                                }
                            }
                        }
                    }
                }else{
                    log("err info action=${action} data=${data}")
                }
            }
            else ->{
                log("other action=${action} data=${data}")
            }

        }
    }

    private fun callBiometricAttrFlagsChange(flags:Int){
        if(BiometricShowFlagsConstant.isValidState(flags)){
            if(mBiometricAttrFlags != flags) {
                mBiometricAttrFlags = flags
                NubiaBiometricMonitor.get().callAttrFlagsChange(BiometricShowFlagsConstant.canShowFingerprint(flags), mBiometricAttrFlags)
            }
        }else{
            throw IllegalAccessError("error biometric attr flages change, flages = ${flags}")
        }
    }

    fun callSystemUIConnect(systemui: SystemUI){
        handlerPost{
            mSystemUI = systemui
            mList.forEach{
                it.get()?.onSystemUIConnect(systemui)
            }
        }
    }

    private fun callPhoneStateChange(state: Int, phoneNumber: String?) {
        handlerPost{
            mPhoneState = state
            mList.forEach{
                it.get()?.onPhoneStateChange(state, phoneNumber)
            }
        }
    }

    fun callDisplayChange(displayId:Int, state:Int){
        if(!(mDisplayStateMap.containsKey(displayId) && mDisplayStateMap.get(displayId)==state)){
            mDisplayStateMap.put(displayId, state)
            handlerPost{
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
            handlerPost{
                if(mList.find { it.get()== this } == null){
                    mList.add(WeakReference(this))
                }
                removeCallback(null)
                Log.i(TAG, "addCallback size=${mList.size}")
            }
        }
    }

    fun removeCallback(callback: UpdateMonitorCallback?){
        handlerPost{
            mList.removeAll { it.get() == callback }
        }
    }

    fun callFocusWindowChange(name: ComponentName) {

        handlerPost{
            mCurrentWindow = name
            mList.forEach{
                it.get()?.onFocusWindowChange(name)
            }
        }
    }

    fun callStartActivity(name: ComponentName) {
        handlerPost{
            mCurrentActivity = name
            mList.forEach{
                it.get()?.onStartActivity(name)
            }
        }
    }

    fun callStopActivity(name: ComponentName) {
        handlerPost{
            mList.forEach{
                it.get()?.onStopActivity(name)
            }
        }
    }

    fun callAodViewChange(show: Boolean) {
        handlerPost{
            isAodViewShow = show
            mList.forEach{
                it.get()?.onAodViewChange(show)
            }
        }
    }

    fun callKeyguardChange(show: Boolean, occluded: Boolean) {
        handlerPost{
            mKeyguardShow = show
            isOccluded = occluded
            mList.forEach{
                it.get()?.onKeyguardChange(show, occluded)
            }
        }
    }

    fun callStartWakingUp() {
        handlerPost{
            isWakeUp = true
            mList.forEach{
                it.get()?.onStartWakingUp()
            }
        }
    }

    fun callFinishedWakingUp() {
        handlerPost{
            mList.forEach{
                it.get()?.onFinishedWakingUp()
            }
        }
    }

    fun callStartGoingToSleep(reason: Int) {
        handlerPost{
            isWakeUp = false
            mList.forEach{
                it.get()?.onStartGoingToSleep(reason)
            }
        }
    }

    fun callFinishedGoingToSleep() {
        handlerPost{
            mList.forEach{
                it.get()?.onFinishedGoingToSleep()
            }
        }
    }

    fun callFingerUp() {
        handlerPost{
            mList.forEach{
                it.get()?.onFingerUp()
            }
        }
    }

    fun callFingerDown() {
        handlerPost{
            mList.forEach{
                it.get()?.onFingerDown()
            }
        }
    }

    fun callFingerprintKeycode(keycode: Int) {
        handlerPost{
            when(keycode){
                KeyEvent.KEYCODE_F11 -> {
                    mList.forEach {
                        it.get()?.onFingerDown()
                    }
                }
                KeyEvent.KEYCODE_F12 -> {
                    mList.forEach{
                        it.get()?.onFingerUp()
                    }
                }
                else -> {
                    Log.w(TAG, "ERROR fingerprint keycode ${keycode}")
                }
            }
        }
    }

    fun callPackageChange(add: Boolean, uri: Uri?) {
        Log.w(TAG, "callPackageChange add = ${add} uri=${uri}")
        handlerPost{
            mList.forEach{
                it.get()?.callPackageChange(add, uri)
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

        fun get():UpdateMonitor{
            return mUpdateMonitor!!
        }
    }
}