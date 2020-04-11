package cn.nubia.systemui.fingerprint.ui

import android.graphics.PixelFormat
import android.view.*


class SurfaceViewWindow{

    var isShow = false
    var mSurfaceControl:SurfaceControl?=null
    val mTransaction = SurfaceControl.Transaction()
    val mSurface by lazy {
        Surface::class.java.getConstructor().newInstance() as Surface
    }

    fun copyFrom(surface: Surface, control: SurfaceControl){
        Surface::class.java.getDeclaredMethod("copyFrom", SurfaceControl::class.java).invoke(surface, control)
    }

    fun addView(){
        val icon_size = 200
        val icon_x = (1280-icon_size)/2f
        mSurfaceControl = SurfaceControl.Builder(SurfaceSession())
                .setName("FingerprintView")
                .setOpaque(true)
                .setBufferSize(icon_size, icon_size)
                .setFormat(PixelFormat.TRANSLUCENT)
//                .setFlags(SurfaceControl.HIDDEN)
                .build()
        try {
            SurfaceControl.openTransaction()
            mSurfaceControl?.apply {
                setLayer(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                setPosition(icon_x,1300f)
                setMatrix(1f,0f,0f,1f)
//                setWindowCrop(icon_size, icon_size)
            }
        }finally {
            SurfaceControl.closeTransaction()
        }
        mSurfaceControl?.also { copyFrom(mSurface, it)}
        draw()
    }

    private fun  draw(){
        try {
            var canvas = mSurface.lockCanvas(null)
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
        mSurfaceControl?.apply {
            if(!isShow && isValid()){
                SurfaceControl.openTransaction()
                try {
                    show()
                }finally {
                    SurfaceControl.closeTransaction()
                }
                isShow = true
            }
        }
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