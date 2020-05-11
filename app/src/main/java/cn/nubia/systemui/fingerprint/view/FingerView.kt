package cn.nubia.systemui.fingerprint.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import cn.nubia.systemui.NubiaSystemUIApplication
import cn.nubia.systemui.R
import cn.nubia.systemui.common.setVisible

/**
 * TODO: document your custom view class.
 */
class FingerView : FrameLayout, View.OnTouchListener {
    companion object {
        val TAG = "${NubiaSystemUIApplication.TAG}.FingerView"
    }
    /**
     * In the example view, this drawable is drawn above the text.
     */
    private var mCallback:Callback?=null
    private var mNormalDrawable: Drawable? = null
    private var mPressDrawable: Drawable? = null
    private val mFingerprintView:ImageView by lazy {
        (findViewById(R.id.fingerprint_icon_view) as ImageView).apply {
            setImageDrawable(mNormalDrawable)
            setOnTouchListener(this@FingerView)
        }
    }

    interface Callback{
        fun onFingerDown()
        fun onFingerUp()
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.FingerView, defStyle, 0)

        if (a.hasValue(R.styleable.FingerView_normalDrawable)) {
            mNormalDrawable = a.getDrawable(R.styleable.FingerView_normalDrawable)
        } else {
            mNormalDrawable = resources.getDrawable(R.drawable.fingerprint_icon_normal, context.theme)
        }

        if (a.hasValue(R.styleable.FingerView_pressDrawable)) {
            mPressDrawable = a.getDrawable(R.styleable.FingerView_pressDrawable)
        } else {
            mNormalDrawable = resources.getDrawable(R.drawable.fingerprint_icon_press, context.theme)
        }
        a.recycle()

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showFingerUpImage()
    }

    fun showFingerDownImage(){
        mFingerprintView.setImageDrawable(mPressDrawable)
    }

    fun showFingerUpImage(){
        mFingerprintView.setImageDrawable(mNormalDrawable)
    }

    fun setCallback(callback:Callback?){
        mCallback = callback
    }

    private fun fingerprintTouch(event: MotionEvent?):Boolean{
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "onFingerDown 1")
                mCallback?.onFingerDown()
            }
            MotionEvent.ACTION_UP->{
                Log.i(TAG, "onFingerUp 1")
                mCallback?.onFingerUp()
            }
        }
        return true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP ->{
                Log.i(TAG, "dispatchTouchEvent ${event}")
            }
        }
        return when(v){
            mFingerprintView -> fingerprintTouch(event)
            else -> { false }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
