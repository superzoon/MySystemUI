package cn.nubia.systemui.fingerprint.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Log
import android.view.*
import cn.nubia.systemui.NubiaSystemUIApplication


class SurfaceViewWindow(val mContext:Context){
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.Window"
    }
    var isShow = false
    var mSurfaceControl:SurfaceControl?=null
    val mTransaction = SurfaceControl.Transaction()
    val mSurfaceView = SurfaceView(mContext)
    val mSurface= SurfaceUtil.createSurface()!!
    val icon_size = 600
    val icon_x = (1280-icon_size)/2f
    fun addView(){
        mSurfaceControl = SurfaceControl.Builder(SurfaceSession())
                .setName("FingerprintView_xlan")
                .setOpaque(true)
                .setBufferSize(icon_size, icon_size)
                .setFormat(PixelFormat.TRANSLUCENT)
                .setFlags(SurfaceControl.HIDDEN)
                .build()
        Log.i(TAG, "mSurfaceControl =${mSurfaceControl?.javaClass} -> ${mSurfaceControl}")
        try {
            SurfaceControl.openTransaction()
            mSurfaceControl?.apply {
                setLayer(9999999)
                show()
                setAlpha(1f)
                setPosition(icon_x,1300f)
                setMatrix(1f,0f,0f,1f)
                setWindowCrop(Rect(0,0,icon_size, icon_size))
            }
        }finally {
            SurfaceControl.closeTransaction()
        }
        mSurfaceControl?.also { SurfaceUtil.copyFrom(mSurface, it)}
    }

    private fun  draw(){
        Log.i(TAG, "mSurfaceControl = draw ${mSurface}")
        try {
            var canvas = mSurface.lockCanvas(Rect(0,0,icon_size,icon_size))
            Log.i(TAG, "canvas = ${canvas}")
            canvas.drawColor(0xff0000)
            mSurface.unlockCanvasAndPost(canvas)
        }catch (e:Exception){ }
    }

    fun removeView() {
        mSurface.release()
        mSurfaceControl?.also {
            mTransaction.remove(it)
            mTransaction.applay()
            mSurfaceControl = null
        }
    }

    @Synchronized fun show(){
        Log.i(TAG, "mSurfaceControl isValid= ${mSurfaceControl}")
        mSurfaceControl?.apply {
            Log.i(TAG, "mSurfaceControl isValid= ${isValid()}")
            if(!isShow && isValid()){
                SurfaceControl.openTransaction()
                try {
                    show()
                }finally {
                    SurfaceControl.closeTransaction()
                }
                isShow = true
                Log.i(TAG, "mSurfaceControl isShow= ${isShow}")
            }
        }
        draw()
    }

    @Synchronized fun hide(){
        mSurfaceControl?.apply {
            if(isShow && isValid()){
                SurfaceControl.openTransaction()
                try {
                    hide()
                }finally {
                    SurfaceControl.closeTransaction()
                }
                isShow = false
            }
        }
    }
}