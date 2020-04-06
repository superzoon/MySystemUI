package cn.nubia.systemui.fingerprint

import android.content.Context
import cn.nubia.systemui.ext.Controller

class FingerprintWindowController(mContext: Context):Controller(mContext){

    val mHandler = ThreadHelper.get().getFingerHander()
    val mFingerprintController by lazy { getController(FingerprintController::class.java) }
}