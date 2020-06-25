package cn.nubia.systemui.activity

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import cn.nubia.systemui.renderer.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRender(
        val mSurface:GLSurfaceView,
        val mContext:Context = mSurface.context,
        val mTriangle:Triangle = Triangle(mSurface))
    :GLSurfaceView.Renderer, Runnable{

    init {
        mSurface.setEGLContextClientVersion(3)
        mSurface.requestFocus();
        mSurface.setFocusableInTouchMode(true);
        mSurface.setRenderer(this)
        mSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY;
    }

    fun onResume(){
        mSurface.onResume()
    }

    fun onPause(){
        mSurface.onPause()
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glEnable(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glEnable(GLES30.GL_DEPTH_BUFFER_BIT)
        Thread(this).start()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0,0,width,height)
        val ratio = width*1f/height;
        Matrix.frustumM(Triangle.mProjMatrix, 0, -ratio,  ratio, -1f, 1f, 1f, 10f)
        Matrix.setLookAtM(Triangle.mVMatrix, 0, 0f, 0f ,3f, 0f, 0f, 0f, 1f, 0f,  0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        mTriangle.drawSelf()
    }

    val ANGLE_SPAN = 0.375F;

    override fun run() {
        while (true){
            mTriangle.xAngle += ANGLE_SPAN
            Thread.sleep(20)
        }
    }


}