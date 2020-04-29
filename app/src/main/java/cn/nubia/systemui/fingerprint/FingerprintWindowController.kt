package cn.nubia.systemui.fingerprint

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.R
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.fingerprint.ui.SurfaceViewWindow
import cn.nubia.systemui.fingerprint.view.IconView

class FingerprintWindowController(mContext: Context):Controller(mContext), IconView.Callback {

    val mHandler = NubiaThreadHelper.get().getMainHander()
    val mThreadHelper = NubiaThreadHelper.get()
    val mFingerprintController by lazy { getController(FingerprintController::class.java) }
    val mIconView:IconView by lazy { View.inflate(mContext, R.layout.fingerprint_icon_view, null) as IconView }
    interface Callback{
        fun onShow()
        fun onHide()
        fun onFingerDown()
        fun onFingerUp()
    }
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.WindowControl"
    }

    var mCallback:Callback? = null
        set(value) {
            Log.i(TAG, "set callback")
            field = value
        }

    init {
        mHandler.post{
            mIconView.setCallback(FingerprintWindowController@this)
        }
    }

    override fun onFingerDown() {
        checkThread()
        mCallback?.onFingerDown()
    }

    override fun onFingerUp() {
        checkThread()
        mCallback?.onFingerUp()
    }

    fun showFingerDownImage(){
        checkThread()
        mIconView.showFingerDownImage()
    }

    fun showFingerUpImage(){
        checkThread()
        mIconView.showFingerUpImage()
    }

    override fun getHandler(): Handler {
        return mHandler
    }

    override fun onStart(service: NubiaSystemUIService) {
        checkThread()
        Log.i(TAG, "onStart  service=${service}")
    }

    override fun onStop(service: NubiaSystemUIService) {
        checkThread()
    }

    private fun handleShow(){
        checkThread()
    }

    fun show(){
        NubiaThreadHelper.get().synInvoke(getHandler()){
            handleShow()
        }
    }

    fun downAnimation(){}
    fun upAnimation(){}
    fun hide(){}
}