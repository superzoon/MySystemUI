package cn.nubia.systemui.fingerprint

import android.content.Context
import android.os.Handler
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.NubiaThreadHelper
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.fingerprint.ui.SurfaceViewWindow

class FingerprintWindowController(mContext: Context):Controller(mContext){

    val mHandler = NubiaThreadHelper.get().getFingerHander()
    val mFingerprintController by lazy { getController(FingerprintController::class.java) }
    val mSurfaceView by lazy { SurfaceViewWindow(mContext) }

    interface Callback{
        fun onShow()
        fun onHide()
        fun onDown()
        fun onUp()
    }
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.WindowControl"
    }

    var mCallback:Callback? = null
        set(value) {
            Log.i(TAG, "set callback")
            field = value
        }


    override fun getHandler(): Handler {
        return mHandler
    }

    override fun onStart(service: NubiaSystemUIService) {
        Log.i(TAG, "onStart  service=${service}")
        mSurfaceView.addView()
        mSurfaceView.show()
    }

    override fun onStop(service: NubiaSystemUIService) {
        mSurfaceView.removeView()
    }

}