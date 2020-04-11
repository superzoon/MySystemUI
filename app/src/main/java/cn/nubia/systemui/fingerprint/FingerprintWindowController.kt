package cn.nubia.systemui.fingerprint

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.view.Surface
import android.view.SurfaceControl
import android.view.SurfaceSession
import cn.nubia.systemui.NubiaSystemUIService
import cn.nubia.systemui.common.Controller
import cn.nubia.systemui.fingerprint.ui.SurfaceViewWindow

class FingerprintWindowController(mContext: Context):Controller(mContext){

    val mHandler = ThreadHelper.get().getFingerHander()
    val mFingerprintController by lazy { getController(FingerprintController::class.java) }
    val mSurfaceView by lazy { SurfaceViewWindow() }
    override fun getHandler(): Handler {
        return mHandler
    }

    override fun onStart(service: NubiaSystemUIService) {
        mSurfaceView?.addView()
        mSurfaceView?.show()
    }

    override fun onStop(service: NubiaSystemUIService) {
        mSurfaceView?.removeView()
    }

}