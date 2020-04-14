package cn.nubia.systemui.fingerprint.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.nubia.systemui.NubiaSystemUIApplication

import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock

@Suppress("DEPRECATION", "OverridingDeprecatedMember")
abstract class BaseSurfaceHolder(val mContext:Context) : SurfaceHolder {

    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.SurfaceHolder"
        internal val DEBUG = false
    }

    val mCallbacks = ArrayList<SurfaceHolder.Callback>()
    internal var mGottenCallbacks: Array<SurfaceHolder.Callback?>? = null
    internal var mHaveGottenCallbacks: Boolean = false

    val mSurfaceLock = ReentrantLock()
    val mSurfaceView = SurfaceView(mContext)
    var mSurface= mSurfaceView.holder.surface

    var requestedWidth = -1
        internal set
    var requestedHeight = -1
        internal set

    var requestedFormat = PixelFormat.OPAQUE
        protected set

    var requestedType = -1
        internal set

    internal var mLastLockTime: Long = 0

    internal var mType = -1
    internal val mSurfaceFrame = Rect()
    internal var mTmpDirty: Rect? = null

    init {
        Log.i(TAG, "SURFACE = ${mSurface}")
    }

    val callbacks: Array<SurfaceHolder.Callback?>?
        get() {
            if (mHaveGottenCallbacks) {
                return mGottenCallbacks
            }

            synchronized(mCallbacks) {
                val N = mCallbacks.size
                if (N > 0) {
                    if (mGottenCallbacks == null || mGottenCallbacks!!.size != N) {
                        mGottenCallbacks = arrayOfNulls<SurfaceHolder.Callback>(N)
                    }
                    mCallbacks.toArray<SurfaceHolder.Callback>(mGottenCallbacks)
                } else {
                    mGottenCallbacks = null
                }
                mHaveGottenCallbacks = true
            }

            return mGottenCallbacks
        }

    abstract fun onUpdateSurface()
    abstract fun onRelayoutContainer()
    abstract fun onAllowLockCanvas(): Boolean

    override fun addCallback(callback: SurfaceHolder.Callback) {
        synchronized(mCallbacks) {
            // This is a linear search, but in practice we'll
            // have only a couple callbacks, so it doesn't matter.
            if (mCallbacks.contains(callback) == false) {
                mCallbacks.add(callback)
            }
        }
    }

    override fun removeCallback(callback: SurfaceHolder.Callback) {
        synchronized(mCallbacks) {
            mCallbacks.remove(callback)
        }
    }

    fun ungetCallbacks() {
        mHaveGottenCallbacks = false
    }

    override fun setFixedSize(width: Int, height: Int) {
        if (requestedWidth != width || requestedHeight != height) {
            requestedWidth = width
            requestedHeight = height
            onRelayoutContainer()
        }
    }

    override fun setSizeFromLayout() {
        if (requestedWidth != -1 || requestedHeight != -1) {
            requestedHeight = -1
            requestedWidth = requestedHeight
            onRelayoutContainer()
        }
    }

    override fun setFormat(format: Int) {
        if (requestedFormat != format) {
            requestedFormat = format
            onUpdateSurface()
        }
    }

    override fun setType(t: Int) {
        var type = t
        when (type) {
            SurfaceHolder.SURFACE_TYPE_HARDWARE, SurfaceHolder.SURFACE_TYPE_GPU ->
                type = SurfaceHolder.SURFACE_TYPE_NORMAL
        }
        when (type) {
            SurfaceHolder.SURFACE_TYPE_NORMAL, SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS ->
                if (requestedType != type) {
                    requestedType = type
                    onUpdateSurface()
                }
        }
    }

    override fun lockCanvas(): Canvas? {
        return internalLockCanvas(null, false)
    }

    override fun lockCanvas(dirty: Rect): Canvas? {
        return internalLockCanvas(dirty, false)
    }

    override fun lockHardwareCanvas(): Canvas? {
        return internalLockCanvas(null, true)
    }

    private fun internalLockCanvas(dir: Rect?, hardware: Boolean): Canvas? {
        var dirty = dir
        if (mType == SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS) {
            throw SurfaceHolder.BadSurfaceTypeException(
                    "Surface type is SURFACE_TYPE_PUSH_BUFFERS")
        }
        mSurfaceLock.lock()

        if (DEBUG) Log.i(TAG, "Locking canvas..,")

        var c: Canvas? = null
        if (onAllowLockCanvas()) {
            if (dirty == null) {
                if (mTmpDirty == null) {
                    mTmpDirty = Rect()
                }
                mTmpDirty!!.set(mSurfaceFrame)
                dirty = mTmpDirty
            }

            try {
                if (hardware) {
                    c = mSurface.lockHardwareCanvas()
                } else {
                    c = mSurface.lockCanvas(dirty)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception locking surface", e)
            }

        }

        if (DEBUG) Log.i(TAG, "Returned canvas: " + c!!)
        if (c != null) {
            mLastLockTime = SystemClock.uptimeMillis()
            return c
        }

        // If the Surface is not ready to be drawn, then return null,
        // but throttle calls to this function so it isn't called more
        // than every 100ms.
        var now = SystemClock.uptimeMillis()
        val nextTime = mLastLockTime + 100
        if (nextTime > now) {
            try {
                Thread.sleep(nextTime - now)
            } catch (e: InterruptedException) {
            }

            now = SystemClock.uptimeMillis()
        }
        mLastLockTime = now
        mSurfaceLock.unlock()

        return null
    }

    override fun unlockCanvasAndPost(canvas: Canvas) {
        mSurface.unlockCanvasAndPost(canvas)
        mSurfaceLock.unlock()
    }

    override fun getSurface(): Surface {
        return mSurface
    }

    override fun getSurfaceFrame(): Rect {
        return mSurfaceFrame
    }

    fun setSurfaceFrameSize(width: Int, height: Int) {
        mSurfaceFrame.top = 0
        mSurfaceFrame.left = 0
        mSurfaceFrame.right = width
        mSurfaceFrame.bottom = height
    }

};

