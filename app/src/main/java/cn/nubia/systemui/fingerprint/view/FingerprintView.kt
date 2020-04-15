package cn.nubia.systemui.fingerprint.view

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.SurfaceControl
import android.view.SurfaceSession
import android.view.SurfaceUtil
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.fingerprint.ThreadHelper
import java.util.concurrent.locks.ReentrantLock

class FingerprintView(val mContext: Context){
    companion object {
        val NAME = "FingerprintView"
        val TAG = "${NubiaSystemUIApplication.TAG}.${NAME}"
        val OPAQUE = 0X00000400
        val ZORDER = 9999999
    }

    var mDrawingAllowed = false
    var mIsCreating = false
    val mSurfaceHolder = object :BaseSurfaceHolder(mContext){

        init {
            requestedType = PixelFormat.RGBA_8888
        }
        override fun onUpdateSurface() {
        }

        override fun onRelayoutContainer() {
        }

        override fun onAllowLockCanvas(): Boolean {
            return mDrawingAllowed
        }

        override fun isCreating(): Boolean {
            return mIsCreating
        }

        override fun setKeepScreenOn(screenOn: Boolean) {
            throw UnsupportedOperationException("not support keep screen on")
        }
    }

    private fun checkThread(){
        if(ThreadHelper.get().getSurfaceHandler().looper.isCurrentThread){
            throw IllegalThreadStateException("not in surface thread")
        }
    }
    fun setFormat(format:Int){
        mRequestedFormat = when (format){
            PixelFormat.OPAQUE -> PixelFormat.RGB_565
            else -> format
        }
        updateSurface()
    }

    fun setVisibility(vis:Boolean){
        mRequestedVisible = vis
        updateSurface()
    }

    var mDeferredDestroySurfaceControl: SurfaceControl?=null
    var mSurfaceControl: SurfaceControl?=null
    val mTransaction = SurfaceControl.Transaction()
    var mHaveFrame = false
    var isCreate = false
    val mSurface = mSurfaceHolder.mSurface
    var mVisible = false
    var mRequestedVisible = false
    var mFormat = PixelFormat.TRANSLUCENT
    var mRequestedFormat = PixelFormat.TRANSLUCENT
    val mFrameRect = Rect()
    var mSurfaceSession :SurfaceSession? = null
    private fun updateSurface(){
        if(!mHaveFrame || mFrameRect.width()<=0 || mFrameRect.height()<=0){
            Log.i(TAG, "${System.identityHashCode(this)} updateSurface: has no frame, ${mFrameRect}")
            return
        }
        if(isCreate&&!mSurfaceHolder.mSurface.isValid){
            Log.i(TAG, "${System.identityHashCode(this)} updateSurface: mSurface no isValid")
            return
        }

        val formatChanged = mFormat!=mRequestedFormat
        val visibleChanged = mVisible != mRequestedVisible
        mVisible = mRequestedVisible
        val visible = mVisible
        val createing = (mSurfaceControl == null || formatChanged || visibleChanged) && mRequestedVisible
        if (createing){
            mSurfaceSession = SurfaceSession()
            mDeferredDestroySurfaceControl = mSurfaceControl
            undateOpaqueFlag()
            mSurfaceControl = SurfaceControl.Builder(SurfaceSession())
                    .setName(NAME)
                    .setOpaque((mSurfaceFlags and OPAQUE)!=0)
                    .setBufferSize(mFrameRect.width(), mFrameRect.height())
                    .setFormat(mFormat)
                    .setFlags(mSurfaceFlags)
                    .build()
        }else{
            Log.e(TAG,"mSurfaceControl is null")
            return
        }
        mSurfaceLock.lock()
        try {
            mDrawingStopped = !visible

            SurfaceControl.openTransaction()
            try {
                mSurfaceControl?.apply {
                    setLayer(ZORDER)
                    if(mVisible){
                        show()
                    }else{
                        hide()
                    }
                    if(mUseAlpha){
                        setAlpha(mAlpha)
                    }
                    if(createing){
                        setPosition(mFrameRect.left.toFloat(), mFrameRect.top.toFloat())
                        setMatrix(1f, 0f, 0f, 1f)
                        setWindowCrop(mFrameRect.width(), mFrameRect.height())
//                        setBufferSize(mFrameRect.width(), mFrameRect.height())
                    }

                }
            } finally {
                SurfaceControl.closeTransaction()
            }
            if (createing){
                SurfaceUtil.copyFrom(mSurface, mSurfaceControl)
            }
            if(mContext.applicationInfo.targetSdkVersion < Build.VERSION_CODES.O){
                SurfaceUtil.createFrom(mSurface, mSurfaceControl)
            }
        }finally {
            mSurfaceLock.unlock()
        }
    }

    var mUseAlpha = false

    fun useAlpha(){
        mUseAlpha = true
    }

    var mAlpha:Float = 1f
    var mDrawingStopped = false
    val mSurfaceLock = ReentrantLock()
    var mSurfaceFlags = SurfaceControl.HIDDEN
    fun undateOpaqueFlag(){
        if (!PixelFormat.formatHasAlpha(mRequestedFormat)){
            mSurfaceFlags = mSurfaceFlags or OPAQUE
        }else{
            mSurfaceFlags = mSurfaceFlags and OPAQUE.inv()
        }
    }
}