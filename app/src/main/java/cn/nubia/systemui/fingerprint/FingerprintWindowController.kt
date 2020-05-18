package cn.nubia.systemui.fingerprint

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.R
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.fingerprint.view.FingerView

class FingerprintWindowController:Controller, FingerView.Callback {
    interface Callback{
        fun onShow()
        fun onHide()
        fun onFingerDown()
        fun onFingerUp()
    }

    val mHandler = NubiaThreadHelper.get().getMainHander()
    private var mShow = false
    private var mFingerView:FingerView? = null
    val mFingerprintController by lazy { getController(FingerprintController::class.java) }
   val mWindowManager = mContext.getSystemService(WindowManager::class.java)

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.WindowControl"
//        val TYPE_FP_VIEW = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;//2300;
        val TYPE_FP_VIEW = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//2300;
        val ICON_SIZE = 190
        val ICON_Y = 2156-ICON_SIZE/2
        val mToken = Binder()
    }

    var mCallback:Callback? = null
        set(value) {
            Log.i(TAG, "set callback")
            field = value
        }

    private val mLayoutParams :WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            type = TYPE_FP_VIEW
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    .or(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    .or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    .or(WindowManager.LayoutParams.FLAG_SPLIT_TOUCH)
                    .or(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP.or(Gravity.CENTER_HORIZONTAL)
            width = ICON_SIZE
            height = ICON_SIZE
            x = 0
            y = ICON_Y
            token = mToken
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            windowAnimations = 0
            packageName = mContext.packageName
            title = "FingerprintUIView"
        }
    }

    constructor(mContext: Context):super(mContext) {
        handlerInvoke{

        }
    }

    override fun onFingerDown() {
        Log.i(TAG, "onFingerDown 2")
        mCallback?.onFingerDown()
    }

    override fun onFingerUp() {
        Log.i(TAG, "onFingerUp 2")
        mCallback?.onFingerUp()
    }

    fun showFingerDownImage(){
        checkThread()
        mFingerView?.showFingerDownImage()
    }

    fun showFingerUpImage(){
        checkThread()
        mFingerView?.showFingerUpImage()
    }

    override fun getHandler(): Handler {
        return mHandler
    }

    private fun handleStart(){
        checkThread()
    }

    override fun onStart(service: NubiaSystemUIService) {
        Log.i(TAG, "onStart  service=${service}")
        handlerInvoke(::handleStart)
    }

    private fun handleStop(){
        checkThread()

    }

    override fun onStop(service: NubiaSystemUIService) {
        handlerInvoke(::handleStop)
    }

    private fun handleShow(){
        checkThread()
        if(!mShow){
            mFingerView = View.inflate(mContext, R.layout.finger_icon_view, null) as FingerView
            mFingerView!!.setCallback(this)
            mWindowManager.addView(mFingerView, mLayoutParams)
            mShow = true
        }
    }

    fun show(){
        synInvoke(::handleShow)
    }

    private fun handleHide(){
        checkThread()
        if(mShow){
            mFingerView!!.setCallback(null)
            mWindowManager.removeView(mFingerView)
            mFingerView = null
            mShow = false
        }
    }

    fun hide(){
        synInvoke(::handleHide)
    }

    private fun handleDownAnimation(){
        checkThread()

    }

    fun downAnimation(){
        handlerInvoke(::handleDownAnimation)
    }

    private fun handleUpAnimation(){
        checkThread()

    }

    fun upAnimation(){
        handlerInvoke(::handleUpAnimation)
    }

    fun syn(action: FingerprintWindowController.()->Unit){
        mThreadHelper.synInvoke(getHandler()) {
            action()
        }
    }

    fun  <T> syn(action: FingerprintWindowController.()->T):T?{
        val funcation:()->T={
            action()
        }
        return  mThreadHelper.synInvoke(getHandler(), funcation)
    }

    fun post(action: FingerprintWindowController.()->Unit){
        mThreadHelper.handlerInvoke(getHandler()) {
            action()
        }
    }
}