package cn.nubia.systemui.fingerprint.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import cn.nubia.systemui.R

/**
 * TODO: document your custom view class.
 */
class IconView : FrameLayout {

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var mFingerprintDrawable: Drawable? = null
    var mFingerprintView:ImageView? = null
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
                attrs, R.styleable.IconView, defStyle, 0)


        if (a.hasValue(R.styleable.IconView_iconDrawable)) {
            mFingerprintDrawable = a.getDrawable(
                    R.styleable.IconView_iconDrawable)
        }

        a.recycle()

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mFingerprintView = findViewById(R.id.fingerprint_icon_view)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

    }
}
