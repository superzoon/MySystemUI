package cn.nubia.systemui.fingerprint.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.nubia.systemui.R

import java.util.ArrayList

class FingerShortcutView : LinearLayout {

    companion object {

        val NORMAL_INTERP = PathInterpolator(0.3f, 0.1f, 0.3f, 1.0f)
        val SLIDE_FAST_INTERP = PathInterpolator(0f, 0f, 0.05f, 0.1f)
        val SLIDE_NORMAL_INTERP = PathInterpolator(0.42f, 0f, 1f, 1f)
    }

    var mBackgroundDrawable: Drawable? = null
    var mEntitys: ArrayList<ShortcutEntity<Intent>>? = null
    var initCenterIndex = 0
    var mFullScreenView: View? = null
    var mFingerShortcutSlide: FingerShortcutSlide? = null
    var mNavigation: Navigation? = null
    var mCallback: Callback? = null
    var mBgHandler: Handler? = null

    interface Callback {
        fun onSlideTo(mView: FingerShortcutView, index: Int, entity: ShortcutEntity<*>?)
        fun onSelect(mView: FingerShortcutView, index: Int, entity: ShortcutEntity<*>?)
        fun onCancel()
    }

    constructor(context: Context) : super(context) {
        initView(View.inflate(getContext(), R.layout.finger_shortcut_view, this))
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mFullScreenView == null) {
            initView(View.inflate(context, R.layout.finger_shortcut_view, this))
        }
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                startShowAnimation()
                viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    fun startShowAnimation() {
        val startAlpha = 0
        val endAlpha = 255
        val listener = object : AnimatorListener() {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val process = animation.animatedValue as Int
                mBackgroundDrawable!!.alpha = process
            }

            override fun onAnimationEnd(animation: Animator) {
                mBackgroundDrawable!!.alpha = endAlpha
            }
        }
        val animator = ValueAnimator.ofInt(startAlpha, endAlpha)
        animator.interpolator = LinearInterpolator()
        animator.duration = 150
        animator.addUpdateListener(listener)
        animator.addListener(listener)
        animator.start()
        postDelayed({ startAnimation(-100, 0, 0.95f, 1f, 0f, 1f, null) }, 50)
    }

    fun startDismissAnimation() {
        startAnimation(0, -100, 1f, 0.95f, 1f, 0f, null)
    }

    fun startAnimation(startY: Int, endY: Int, startScale: Float, endScale: Float, startAlpha: Float, endAlpha: Float, run: Runnable?) {
        val listener = object : AnimatorListener() {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val process = animation.animatedValue as Float
                val newInter = NORMAL_INTERP.getInterpolation(process)
                mFullScreenView!!.scrollY = (startY + (endY - startY) * newInter).toInt()
                mFullScreenView!!.scaleX = startScale + (endScale - startScale) * newInter
                mFullScreenView!!.scaleY = startScale + (endScale - startScale) * newInter
                mFullScreenView!!.alpha = startAlpha + (endAlpha - startAlpha) * process
            }

            override fun onAnimationEnd(animation: Animator) {
                mFullScreenView!!.scrollY = 0
                mFullScreenView!!.scaleX = endScale
                mFullScreenView!!.scaleY = endScale
                mFullScreenView!!.alpha = endAlpha
                run?.run()
            }
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.interpolator = LinearInterpolator()
        animator.duration = 150
        animator.addUpdateListener(listener)
        animator.addListener(listener)
        animator.start()
    }

    fun initView(fullView: View) {
        mFullScreenView = fullView
        mBackgroundDrawable = resources.getDrawable(R.drawable.finger_shortcut_view_bg)
        mFullScreenView!!.background = mBackgroundDrawable
        val slideView = fullView.findViewById(R.id.finger_shortcut_slide_view) as SlideView
        val textView = fullView.findViewById(R.id.finger_shortcut_tips_view) as TextView
        mFingerShortcutSlide = FingerShortcutSlide(slideView, textView)
        mNavigation = Navigation(fullView.findViewById(R.id.finger_shortcut_navigation_image))
    }

    fun setCallback(callback: Callback): FingerShortcutView {
        mCallback = callback
        return this
    }

    fun setEntity(centerIndex: Int, entitys: ArrayList<ShortcutEntity<Intent>>, background: Handler): FingerShortcutView {
        if (background.looper.thread === Looper.getMainLooper().thread) {
            throw IllegalStateException("background handler in main thread.")
        }
        mBgHandler = background
        mEntitys = entitys
        initCenterIndex = centerIndex
        if (mFingerShortcutSlide != null) {
            mFingerShortcutSlide!!.initEntityView(centerIndex, mEntitys)
        }
        return this
    }

    class ShortcutEntity<T : Intent>(val iconId: Int, val textId: Int, val tag: T) {
        var mIconView: ImageView? = null
        var mTextView: TextView? = null
        var centerX: Int = 0
    }

    class SlideView : FrameLayout {

        val mEntitys = ArrayList<ShortcutEntity<*>>()
        var mCurrentState = SROLLED_STATE_NORMAL
        var mCenterX = 0
        var mCenterY = -1
        var mTextYMin = 0
        var mTextYMax = 0
        var mTextSize = 0f
        var mTextScale = 0f
        var mIconSize = 0
        var mIconScale = 0f
        var mIconMargin = 0
        var mAlphaMin = 0f
        var mAlphaMax = 0f
        var triggerSlideSpace = 0
        var triggerCancelSpace = 0
        val mScreenScrollStyle = 0
        var mAnimationProgress = 0f
        var mMinScroll = 0
        var mMaxScroll = 0
        var mIconViewWidth = 0
        var mCurrentIndex = 0
        var mScrollAnimation: ValueAnimator? = null
        var mScaleAnimator: ValueAnimator? = null
        var mCenterEntity: ShortcutEntity<*>? = null
        var mSlideCallback: SlideCallback? = null

        interface SlideCallback {
            fun onSlide(index: Int, centerEntity: ShortcutEntity<*>?)
            fun onSelect(index: Int, centerEntity: ShortcutEntity<*>?)
            fun onMoveIn(centerEntity: ShortcutEntity<*>?)
            fun onMoveOut(centerEntity: ShortcutEntity<*>?)
            fun onCancel()
        }

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            saveAttributeDataForStyleable(context, attrs)
        }

        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr, 0) {
            saveAttributeDataForStyleable(context, attrs)
        }

        fun setSlideCallback(callback: SlideCallback) {
            this.mSlideCallback = callback
        }

        fun saveAttributeDataForStyleable(context: Context, attrs: AttributeSet) {
            /***
             * app:center_y="38dip" y方向中心轴
             * app:title_y_min="40dip" 文字距离中心轴距离
             * app:title_y_max="56dip"  文字距离中心轴最大距离
             * app:title_size="11dp" 文字大小
             * app:title_size_scale="1.36" 文字最大缩放
             * app:icon_size="52dp" icon view大小
             * app:icon_size_scale="1.46"  最大缩放
             * app:icon_margin="18dp" 边距
             * app:alpha_min="0.4" 不透明最小
             * app:alpha_max="0.9" 不透明最大
             * app:trigger_slide_space="35dp" 触发滑动到ICON距离
             * app:trigger_cancel_space="45dp" 触发取消的距离
             */
            if (mCenterY == -1) {
                val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView)
                mCenterY = typedArray.getDimensionPixelSize(R.styleable.SlideView_center_y, dip2px(38f))
                mTextYMin = typedArray.getDimensionPixelSize(R.styleable.SlideView_title_y_min, dip2px(40f))
                mTextYMax = typedArray.getDimensionPixelSize(R.styleable.SlideView_title_y_max, dip2px(56f))
                mTextSize = typedArray.getDimension(R.styleable.SlideView_title_size, dip2px(11f).toFloat())
                mTextScale = typedArray.getFloat(R.styleable.SlideView_title_size_scale, 1.36f)
                mIconSize = typedArray.getDimensionPixelSize(R.styleable.SlideView_icon_size, dip2px(52f))
                mIconScale = typedArray.getFloat(R.styleable.SlideView_icon_size_scale, 1.46f)
                mIconMargin = typedArray.getDimensionPixelSize(R.styleable.SlideView_icon_margin, dip2px(18f))
                mAlphaMin = typedArray.getFloat(R.styleable.SlideView_alpha_min, 0.6f)
                mAlphaMax = typedArray.getFloat(R.styleable.SlideView_alpha_max, 0.9f)
                triggerSlideSpace = typedArray.getDimensionPixelSize(R.styleable.SlideView_trigger_slide_space, dip2px(35f))
                triggerCancelSpace = typedArray.getDimensionPixelSize(R.styleable.SlideView_trigger_cancel_space, dip2px(45f))
                mIconViewWidth = mIconMargin * 2 + mIconSize
            }
        }

        fun dip2px(dpValue: Float): Int {
            val scale = resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        fun px2dip(pxValue: Float): Int {
            val scale = resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        fun updateShortcurtEntity(centerIndex: Int, entitys: ArrayList<ShortcutEntity<Intent>>?) {
            if (Thread.currentThread() !== Looper.getMainLooper().thread) {
                post { updateShortcurtEntity(centerIndex, entitys) }
                return
            }
            removeAllViews()
            mEntitys.clear()
            mEntitys.addAll(entitys!!)
            for (mEntity in entitys) {
                addIconView(mEntity)
                addTextView(mEntity)
            }
            mCurrentIndex = centerIndex
            requestLayout()
        }

        fun addIconView(mEntity: ShortcutEntity<*>) {
            val mIconView = ImageView(context)
            mIconView.setImageResource(mEntity.iconId)
            mIconView.tag = mEntity
            mIconView.alpha = mAlphaMin
            mEntity.mIconView = mIconView
            val lp = FrameLayout.LayoutParams(mIconSize, mIconSize)
            addView(mEntity.mIconView, lp)
        }

        fun addTextView(mEntity: ShortcutEntity<*>) {
            var mTextView = TextView(context)
            mTextView.setTextColor(Color.WHITE)
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            mTextView.alpha = mAlphaMin
            mTextView.setText(mEntity.textId)
            mTextView.tag = mEntity
            mEntity.mTextView = mTextView
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            addView(mEntity.mTextView, lp)
        }

        protected fun screenScrolled(screenCenter: Int) {
            when (mScreenScrollStyle) {
                0 -> normalScreenScrolled(screenCenter)
                else -> {
                }
            }
        }

        fun moveIn() {
            val callback = Runnable {
                if (mSlideCallback != null) {
                    mSlideCallback!!.onMoveIn(mCenterEntity)
                }
            }
            startScaleIconAnimation(0f, 1f, SROLLED_STATE_TOUCH_IN_ANIMATION, SROLLED_STATE_TOUCH_IN, callback)
        }

        fun moveOut() {
            val callback = Runnable {
                if (mSlideCallback != null) {
                    mSlideCallback!!.onMoveOut(mCenterEntity)
                }
            }
            startScaleIconAnimation(1f, 0f, SROLLED_STATE_TOUCH_OUT_ANIMATION, SROLLED_STATE_TOUCH_OUT, callback)
        }

        fun touchUp() {
            if (mSlideCallback != null) {
                if ((mCurrentState == SROLLED_STATE_TOUCH_IN) or (mCurrentState == SROLLED_STATE_TOUCH_IN_ANIMATION)) {
                    mSlideCallback!!.onSelect(mEntitys.indexOf(mCenterEntity), mCenterEntity)
                    startScaleIconAnimation(1f, 0f, SROLLED_STATE_TOUCH_OUT_ANIMATION, SROLLED_STATE_TOUCH_OUT, null)
                } else {
                    mSlideCallback!!.onCancel()
                }
            }
        }

        fun startScaleIconAnimation(start: Float, end: Float, startState: Int, endState: Int, callback: Runnable?) {
            setCurrentState(startState)
            val startValue: Float
            if (mScaleAnimator != null && mScaleAnimator!!.isRunning) {
                startValue = mScaleAnimator!!.animatedValue as Float
                mScaleAnimator!!.cancel()
            } else {
                startValue = start
            }
            val duration = (Math.abs(end - startValue) * 300).toLong()

            val listener = object : AnimatorListener() {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    mAnimationProgress = animation.animatedValue as Float
                    postInvalidate()
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!this.isCancel) {
                        setCurrentState(endState)
                        mAnimationProgress = end
                        mScaleAnimator = null
                        postInvalidate()
                    }
                    callback?.run()
                }
            }
            mScaleAnimator = ValueAnimator.ofFloat(startValue, end)
            mScaleAnimator!!.interpolator = NORMAL_INTERP
            mScaleAnimator!!.duration = duration
            mScaleAnimator!!.addUpdateListener(listener)
            mScaleAnimator!!.addListener(listener)
            mScaleAnimator!!.start()
        }

        fun movePrevious(isFastVelocity: Boolean): Boolean {
            if (mCurrentIndex > 0) {
                mCurrentIndex--
                startScrollAnimation(isFastVelocity)
                return true
            }
            return false
        }

        fun moveNext(isFastVelocity: Boolean): Boolean {
            if (mCurrentIndex < mEntitys.size - 1) {
                mCurrentIndex++
                startScrollAnimation(isFastVelocity)
                return true
            }
            return false
        }

        fun startScrollAnimation(isFastVelocity: Boolean) {
            var isFastVelocity = isFastVelocity
            val startScroolX = scrollX
            mCenterEntity = mEntitys[mCurrentIndex]
            val endScroolX = mCenterEntity!!.centerX - mCenterX
            if (mScrollAnimation != null && mScrollAnimation!!.isRunning) {
                mScrollAnimation!!.cancel()
                isFastVelocity = true
            }
            val listener = object : AnimatorListener() {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    scrollTo(animation.animatedValue as Int, 0)
                    postInvalidate()
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!this.isCancel) {
                        scrollTo(endScroolX, 0)
                        mScrollAnimation = null
                        postInvalidate()
                        if (mSlideCallback != null) {
                            mSlideCallback!!.onSlide(mEntitys.indexOf(mCenterEntity!!), mCenterEntity)
                        }
                    }
                }
            }
            mScrollAnimation = ValueAnimator.ofInt(startScroolX, endScroolX)
            mScrollAnimation!!.interpolator = if (isFastVelocity) SLIDE_FAST_INTERP else NORMAL_INTERP//SLIDE_NORMAL_INTERP);
            mScrollAnimation!!.duration = (if (isFastVelocity) 100 else 200).toLong()
            mScrollAnimation!!.addUpdateListener(listener)
            mScrollAnimation!!.addListener(listener)
            mScrollAnimation!!.start()
        }

        fun setCurrentState(state: Int) {
            mCurrentState = state
        }

        protected fun normalScreenScrolled(screenCenter: Int) {
            val halfIconSize = mIconSize / 2 + mIconMargin
            for (i in mEntitys.indices) {
                val entity = mEntitys[i]
                val entityCenterX = entity.centerX
                val icon = entity.mIconView
                val title = entity.mTextView
                //The distance between the center of the current entity and the center
                val distance = Math.abs(screenCenter - entityCenterX)
                if (distance < halfIconSize) {
                    //The percentage of distance occupied
                    val scrollProgress = (halfIconSize - distance) * 1f / halfIconSize
                    //Interpolation is calculated according to the state
                    var interpolatedProgress = 0f
                    when (mCurrentState) {
                        SROLLED_STATE_NORMAL, SROLLED_STATE_TOUCH_OUT -> interpolatedProgress = 0f
                        SROLLED_STATE_TOUCH_IN_ANIMATION, SROLLED_STATE_TOUCH_OUT_ANIMATION -> interpolatedProgress = if (mAnimationProgress < scrollProgress) mAnimationProgress else scrollProgress
                        SROLLED_STATE_TOUCH_IN -> interpolatedProgress = scrollProgress
                    }
                    Log.e("xlan", "normalScreenScrolled scrollProgress=$scrollProgress interpolatedProgress=$interpolatedProgress mSrolledstate=$mCurrentState")
                    //Fading is not the last mCenterEntity
                    if (entity != mCenterEntity) {
                        interpolatedProgress = interpolatedProgress * 0.8f
                    }

                    //set alpha
                    var alpha = mAlphaMin + (1 - mAlphaMin) * interpolatedProgress
                    icon!!.alpha = alpha
                    //set scale center
                    icon.pivotX = halfIconSize.toFloat()
                    icon.pivotX = (icon.width / 2).toFloat()
                    icon.pivotY = (icon.width / 2).toFloat()
                    //set scale
                    var scale = 1 + (mIconScale - 1) * interpolatedProgress
                    icon.scaleX = scale
                    icon.scaleY = scale
                    if (mScrollAnimation == null && mScaleAnimator == null && SROLLED_STATE_TOUCH_IN == mCurrentState) {
                        icon.alpha = 1f
                        icon.scaleX = mIconScale
                        icon.scaleY = mIconScale
                    }

                    //set alpha
                    alpha = mAlphaMin + (mAlphaMax - mAlphaMin) * interpolatedProgress
                    title!!.alpha = alpha
                    //set scale center
                    title.pivotX = (title.width / 2).toFloat()
                    title.pivotY = 0f
                    //set scale
                    scale = 1 + (mTextScale - 1) * interpolatedProgress
                    title.scaleX = scale
                    title.scaleY = scale
                    val translationY = (mTextYMax - mTextYMin) * interpolatedProgress
                    title.translationY = translationY
                    if (mScrollAnimation == null && mScaleAnimator == null && SROLLED_STATE_TOUCH_IN == mCurrentState) {
                        title.alpha = 1f
                        title.translationY = (mTextYMax - mTextYMin).toFloat()
                        title.scaleX = mTextScale
                        title.scaleY = mTextScale
                    }
                } else {
                    //set normal
                    icon!!.alpha = mAlphaMin
                    icon.pivotX = (icon.width / 2).toFloat()
                    icon.pivotY = (icon.width / 2).toFloat()
                    icon.scaleX = 1f
                    icon.scaleY = 1f

                    title!!.alpha = mAlphaMin
                    title.pivotX = (title.width / 2).toFloat()
                    title.pivotY = (title.width / 2).toFloat()
                    title.translationY = 0f
                    title.scaleX = 1f
                    title.scaleY = 1f
                }
            }
        }

        override fun dispatchDraw(canvas: Canvas) {
            screenScrolled(scrollX + mCenterX)
            val drawingTime = drawingTime
            for (i in mEntitys.indices) {
                val entity = mEntitys[i]
                val icon = entity.mIconView
                drawChild(canvas, icon, drawingTime)
                val title = entity.mTextView
                canvas.save()
                drawChild(canvas, title, drawingTime)
                canvas.restore()
            }
        }

        fun move(moveX: Int) {
            scrollTo(mCenterEntity!!.centerX - mCenterX + moveX, 0)
        }

        override fun scrollTo(x: Int, y: Int) {
            var x = x
            if (x > mMaxScroll) {
                x = mMaxScroll
            } else if (x < mMinScroll) {
                x = mMinScroll
            }
            super.scrollTo(x, y)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            mCenterX = ((right - left) / 2f + left).toInt()
            val paddingTop = paddingTop
            val textTop = mCenterY + mTextYMin + paddingTop
            val iconTop = mCenterY - mIconSize / 2 + paddingTop
            for (i in mEntitys.indices) {
                val entity = mEntitys[i]
                val cX = mCenterX - i * mIconViewWidth
                entity.centerX = cX
                var width = entity.mIconView!!.width
                var height = entity.mIconView!!.height
                var l = (cX - width / 2f).toInt()
                var t = iconTop
                var r = (cX + width / 2f).toInt()
                var b = t + height
                entity.mIconView!!.layout(l, t, r, b)

                width = entity.mTextView!!.width
                height = entity.mTextView!!.height
                l = (cX - width / 2f).toInt()
                t = textTop
                r = (cX + width / 2f).toInt()
                b = t + height
                entity.mTextView!!.layout(l, t, r, b)
                if (i == 0) {
                    mMaxScroll = cX - mCenterX
                } else if (i == mEntitys.size - 1) {
                    mMinScroll = cX - mCenterX
                }
            }
            mCenterEntity = mEntitys[mCurrentIndex]
            scrollTo(mCenterEntity!!.centerX - mCenterX, 0)
        }

        companion object {
            val SROLLED_STATE_NORMAL = 0
            val SROLLED_STATE_TOUCH_IN = 1
            val SROLLED_STATE_TOUCH_IN_ANIMATION = 2
            val SROLLED_STATE_TOUCH_OUT = 3
            val SROLLED_STATE_TOUCH_OUT_ANIMATION = 4
        }
    }

    inner class FingerShortcutSlide(private val mSlideView: SlideView?, private val mShowView: TextView) : SlideView.SlideCallback {
        private val mVibrator: Vibrator?
        private var mVibrationEffect: VibrationEffect? = null

        val triggerSlideSpace: Int
            get() = mSlideView!!.triggerSlideSpace

        val triggerCancelSpace: Int
            get() = mSlideView!!.triggerCancelSpace

        init {
            mVibrator = context.getSystemService(Vibrator::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                mVibrationEffect = VibrationEffect.createOneShot(10, 254)
            }
            mSlideView!!.setSlideCallback(this)
            if (mEntitys != null) {
                initEntityView(initCenterIndex, mEntitys)
            }
        }

        private fun vibrate() {
            mBgHandler!!.post {
                if (Build.VERSION.SDK_INT >= 26) {
                    mVibrator!!.vibrate(mVibrationEffect)
                }
            }
        }

        fun initEntityView(centerIndex: Int, mEntitys: ArrayList<ShortcutEntity<Intent>>?) {
            Log.e("xlan", " initEntityView" + mEntitys!!)
            mSlideView!!.updateShortcurtEntity(centerIndex, mEntitys)
        }

        fun touchUp() {
            vibrate()
            mSlideView!!.touchUp()
            mNavigation!!.startAnimation(1f, 0f)
        }

        fun movePrevious(isFastVelocity: Boolean) {
            if (mSlideView!!.movePrevious(isFastVelocity)) {
                vibrate()
            }
        }

        fun moveNext(isFastVelocity: Boolean) {
            if (mSlideView!!.moveNext(isFastVelocity)) {
                vibrate()
            }
        }

        fun moveIn() {
            mNavigation!!.startAnimation(0f, 1f)
            mShowView.alpha = 0.4f
            mShowView.setText(R.string.finger_shortcut_tips_slide_cancel)
            vibrate()
            mSlideView!!.moveIn()
        }

        fun moveOut() {
            mNavigation!!.startAnimation(1f, 0f)
            mShowView.alpha = 0.9f
            mShowView.setText(R.string.finger_shortcut_tips_slide_go_out)
            vibrate()
            mSlideView!!.moveOut()
        }

        fun move(moveX: Int) {
            mSlideView!!.move(moveX)
        }

        override fun onSlide(index: Int, centerEntity: ShortcutEntity<*>?) {
            if (mCallback != null) {
                mCallback!!.onSlideTo(this@FingerShortcutView, index, centerEntity)
            }
            mNavigation!!.resetLastX()
        }

        override fun onMoveIn(centerEntity: ShortcutEntity<*>?) {}

        override fun onMoveOut(centerEntity: ShortcutEntity<*>?) {}

        override fun onSelect(index: Int, centerEntity: ShortcutEntity<*>?) {
            if (mCallback != null) {
                mCallback!!.onSelect(this@FingerShortcutView, index, centerEntity)
            }
            startDismissAnimation()
        }

        override fun onCancel() {
            if (mCallback != null) {
                mCallback!!.onCancel()
            }
            startDismissAnimation()
        }
    }

    inner class Navigation(private val mView: View?) : Drawable(), View.OnTouchListener {
        private val mPaint = Paint()
        private var mVelocityTracker: VelocityTracker? = null
        private var mScrollProgress = 0f
        private var mCurrentX: Int = 0
        private var mLastX = 0
        var mDownY = 0
        var isInNavigationH = false
        var isInNavigationV = false
        var mOutNavigationTime: Long = 0
        val mTriggerMoveTime: Long = 300
        val mPadding: Int

        val isFastVelocity: Boolean
            get() {
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.computeCurrentVelocity(1000)
                    val xVelocity = Math.abs(mVelocityTracker!!.xVelocity)
                    if (xVelocity > 1000) {
                        return true
                    }
                }
                return false
            }

        val drawRect: RectF?
            get() {
                val rect = bounds
                val space = Math.abs(mScrollProgress * rect.width() / 2f)
                return if (space * 2 > rect.height()) {
                    RectF(rect.centerX() - space, rect.top.toFloat(), rect.centerX() + space, rect.bottom.toFloat())
                } else {
                    null
                }
            }

        init {
            mPadding = dip2px(5f)
            mPaint.color = Color.WHITE
            mPaint.alpha = (255 * 0.4f).toInt()
            mPaint.style = Paint.Style.FILL
            mView!!.background = this
            (mView.parent as View).setOnTouchListener(this)
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return touch(event)
        }

        fun addMovementToVelocity(event: MotionEvent) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain()
            }
            mVelocityTracker!!.addMovement(event)
        }

        fun clearVelocity() {
            mVelocityTracker!!.clear()
        }

        fun recycleVelocity() {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }

        fun resetLastX() {
            mLastX = mCurrentX
        }

        fun dip2px(dpValue: Float): Int {
            val scale = resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        fun touch(event: MotionEvent): Boolean {
            mCurrentX = event.x.toInt()
            addMovementToVelocity(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.i("xlan", " mFingerShortcutSlide.moveIn()")
                    mFingerShortcutSlide!!.moveIn()
                    isInNavigationH = true
                    isInNavigationV = true
                    mLastX = mCurrentX
                    mDownY = event.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val triggerSlideSpace = mFingerShortcutSlide!!.triggerSlideSpace
                    val triggerCancelSpace = mFingerShortcutSlide!!.triggerCancelSpace
                    val distanceY = (mDownY - event.y).toInt()
                    if (distanceY > triggerCancelSpace && isInNavigationH) {
                        Log.i("xlan", " mFingerShortcutSlide.moveOut()")
                        mFingerShortcutSlide!!.moveOut()
                        mLastX = mCurrentX
                        isInNavigationH = false
                    } else if (!isInNavigationH && distanceY <= triggerCancelSpace) {
                        Log.i("xlan", " mFingerShortcutSlide.moveIn()")
                        mFingerShortcutSlide!!.moveIn()
                        mLastX = mCurrentX
                        isInNavigationH = true
                    } else if (isInNavigationH) {
                        val moveDistance = (mCurrentX - mLastX).toFloat()
                        val isFastVelocity = isFastVelocity
                        val multiplying = if (isFastVelocity) 1.57f else 1f
                        if (Math.abs(moveDistance) * multiplying > triggerSlideSpace) {
                            if (moveDistance > 0) {
                                Log.i("xlan", " mFingerShortcutSlide.moveNext()")
                                mFingerShortcutSlide!!.moveNext(isFastVelocity)
                            } else {
                                Log.i("xlan", " mFingerShortcutSlide.movePrevious()")
                                mFingerShortcutSlide!!.movePrevious(isFastVelocity)
                            }
                            mLastX = mCurrentX
                            clearVelocity()
                        } else {
                            val rawX = event.rawX
                            val left = (mView!!.left + mPadding).toFloat()
                            val right = (mView.right - mPadding).toFloat()
                            if (rawX < left || rawX > right) {
                                if (isInNavigationV) {
                                    isInNavigationV = false
                                    mOutNavigationTime = System.currentTimeMillis()
                                } else if (System.currentTimeMillis() - mOutNavigationTime > mTriggerMoveTime) {
                                    mOutNavigationTime = System.currentTimeMillis()
                                    if (rawX < left) {
                                        Log.i("xlan", " mFingerShortcutSlide.movePrevious()")
                                        mFingerShortcutSlide!!.movePrevious(false)
                                    } else {
                                        Log.i("xlan", " mFingerShortcutSlide.moveNext()")
                                        mFingerShortcutSlide!!.moveNext(false)
                                    }
                                }
                            } else {
                                isInNavigationV = true
                                mFingerShortcutSlide!!.move((mLastX - mCurrentX) / 6)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.i("xlan", " mFingerShortcutSlide.touchUp()")
                    mFingerShortcutSlide!!.touchUp()
                    recycleVelocity()
                }
            }
            return true
        }

        fun startAnimation(start: Float, end: Float) {
            val obj = mView!!.tag
            if (obj != null && obj is ValueAnimator) {
                obj.cancel()
            }

            val listener = object : AnimatorListener() {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    mScrollProgress = animation.animatedValue as Float
                    mView.postInvalidate()
                }

                override fun onAnimationEnd(animation: Animator) {
                    mView.tag = null
                    mView.postInvalidate()
                }

                override fun onAnimationCancel(animation: Animator) {
                    mView.tag = null
                    mView.postInvalidate()
                }
            }
            val animator = ValueAnimator.ofFloat(start, end)
            animator.interpolator = NORMAL_INTERP
            animator.duration = 150
            animator.addUpdateListener(listener)
            animator.addListener(listener)
            mView.tag = animator
            animator.start()
        }

        override fun draw(canvas: Canvas) {
            val rect = drawRect
            if (rect != null) {
                canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, mPaint)
            }
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }
    }

    private open class AnimatorListener : ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        protected var isCancel = false
        override open fun onAnimationStart(animation: Animator) {}

        override open fun onAnimationEnd(animation: Animator) {}

        override open fun onAnimationCancel(animation: Animator) {
            isCancel = true
        }

        override open fun onAnimationRepeat(animation: Animator) {}

        override open fun onAnimationUpdate(animation: ValueAnimator) {}
    }


}
