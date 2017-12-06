package jp.co.nohana.featurediscovery

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView

open class FeatureDiscoveryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private val task = object : Runnable {
        override fun run() {
            createIdleAnimation().start()
            postDelayed(this, EFFECT_DELAY.toLong())
        }
    }

    private val clipRadius: Int
    private val textVerticalPadding: Int

    private var icon: Drawable? = null
    private val outerCircleDrawable: Drawable
    private val innerCircleDrawable: Drawable
    private val effectCircleDrawable: Drawable

    internal var innerScale: Float = 0.toFloat()
    internal var effectScale: Float = 0.toFloat()
    internal var mOuterScale: Float = 0.toFloat()

    private var title: TextView? = null
    private var message: TextView? = null

    private val gestureDetector: GestureDetector
    private var listener: TapListener? = null

    private val colorPrimary: Int
        @ColorInt
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            return typedValue.data
        }

    init {
        clipRadius = resources.getDimensionPixelSize(R.dimen.feature_discovery_inner_radius)
        textVerticalPadding = resources.getDimensionPixelSize(R.dimen.feature_discovery_text_padding_vertical)

        outerCircleDrawable = ShapeDrawable(OvalShape())
        outerCircleDrawable.setColorFilter(colorPrimary, PorterDuff.Mode.SRC_IN)
        outerCircleDrawable.alpha = OUTER_ALPHA
        innerCircleDrawable = ShapeDrawable(OvalShape())
        innerCircleDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        effectCircleDrawable = ShapeDrawable(OvalShape())
        effectCircleDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        effectCircleDrawable.alpha = EFFECT_ALPHA

        gestureDetector = GestureDetector(getContext(), SingleTapGestureListener())
        setWillNotDraw(false)

        LayoutInflater.from(context).inflate(R.layout.merge_feature_dicovery, this)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child.id == R.id.title) {
            title = child as TextView
        } else if (child.id == R.id.message) {
            message = child as TextView
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.MarginLayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is ViewGroup.MarginLayoutParams
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (lp is ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams(lp)
        } else ViewGroup.MarginLayoutParams(lp)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            measureChildWithMargins(v, widthMeasureSpec, 0,
                    heightMeasureSpec, textVerticalPadding * 2)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!changed) {
            return
        }
        val pivotY = pivotY.toInt()
        if (pivotY < (b - t) / 2) {
            //layout below pivot
            val top = pivotY + clipRadius + textVerticalPadding
            var right = Math.min(r, l + title!!.measuredWidth)
            title!!.layout(l, top, right, top + +title!!.measuredHeight)
            right = Math.min(r, l + message!!.measuredWidth)
            message!!.layout(l, title!!.bottom, right, title!!.bottom + message!!.measuredHeight)
        } else {
            //layout above pivot
            val bottom = pivotY - clipRadius - textVerticalPadding
            var right = Math.min(r, l + message!!.measuredWidth)
            message!!.layout(l, bottom - message!!.measuredHeight, right, bottom)
            right = Math.min(r, l + title!!.measuredWidth)
            title!!.layout(l, message!!.top - title!!.measuredHeight, right, message!!.top)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDrawablePosition()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(task)
    }

    override fun setPivotX(pivotX: Float) {
        super.setPivotX(pivotX)
        updateDrawablePosition()
    }

    override fun setPivotY(pivotY: Float) {
        super.setPivotY(pivotY)
        updateDrawablePosition()
    }

    override fun draw(canvas: Canvas) {
        val pivotX = pivotX.toInt()
        val pivotY = pivotY.toInt()

        var saveCount = canvas.save()
        canvas.scale(mOuterScale, mOuterScale, pivotX.toFloat(), pivotY.toFloat())
        outerCircleDrawable.draw(canvas)
        canvas.restoreToCount(saveCount)

        saveCount = canvas.save()
        canvas.scale(innerScale, innerScale, pivotX.toFloat(), pivotY.toFloat())
        innerCircleDrawable.draw(canvas)
        canvas.restoreToCount(saveCount)

        saveCount = canvas.save()
        canvas.scale(effectScale, effectScale, pivotX.toFloat(), pivotY.toFloat())
        effectCircleDrawable.draw(canvas)
        canvas.restoreToCount(saveCount)

        icon!!.draw(canvas)

        super.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun setTapListener(listener: TapListener) {
        this.listener = listener
    }

    open fun setIcon(@DrawableRes iconRes: Int) {
        val icon = DrawableCompat.wrap(ContextCompat.getDrawable(context, iconRes)).mutate()
        DrawableCompat.setTint(icon, colorPrimary)
        this.icon = icon
    }

    fun setTitle(@StringRes titleRes: Int) {
        title!!.setText(titleRes)
    }

    fun setMessage(@StringRes messageRes: Int) {
        message!!.setText(messageRes)
    }

    fun startShowAnimation() {
        title!!.visibility = View.INVISIBLE
        message!!.visibility = View.INVISIBLE
        outerCircleDrawable.alpha = OUTER_ALPHA

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        animator.addUpdateListener { animation ->
            mOuterScale = animation.animatedValue as Float
            innerScale = animation.animatedValue as Float
            postInvalidateOnAnimation()
        }
        animator.addListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                title!!.visibility = View.VISIBLE
                message!!.visibility = View.VISIBLE
            }
        })

        val set = AnimatorSet()
        set.play(animator).before(createIdleAnimation())
        set.start()
        postDelayed(task, EFFECT_DELAY.toLong())
    }

    fun startInteractionAnimation(listener: Animator.AnimatorListener?) {
        title!!.visibility = View.INVISIBLE
        message!!.visibility = View.INVISIBLE

        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.interpolator = AccelerateInterpolator()
        animator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animator.addUpdateListener { animation ->
            innerScale = animation.animatedValue as Float
            outerCircleDrawable.alpha = (OUTER_ALPHA * animation.animatedValue as Float).toInt()
            mOuterScale = 1.1f - animation.animatedValue as Float / 10
            postInvalidateOnAnimation()
        }
        if (listener != null) {
            animator.addListener(listener)
        }
        animator.start()
    }

    fun startDismissAnimation(listener: Animator.AnimatorListener?) {
        title!!.visibility = View.INVISIBLE
        message!!.visibility = View.INVISIBLE

        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.interpolator = AccelerateInterpolator()
        animator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animator.addUpdateListener { animation ->
            innerScale = animation.animatedValue as Float
            mOuterScale = animation.animatedValue as Float
            outerCircleDrawable.alpha = (OUTER_ALPHA * animation.animatedValue as Float).toInt()
            postInvalidateOnAnimation()
        }
        if (listener != null) {
            animator.addListener(listener)
        }
        animator.start()
    }

    private fun createIdleAnimation(): Animator {
        //idle animation runs twice
        val idleAnimator = createSingleIdleAnimation()
        idleAnimator.startDelay = EFFECT_FIRST_DELAY.toLong()
        val idleAnimator2 = createSingleIdleAnimation()

        val set = AnimatorSet()
        set.play(idleAnimator).before(idleAnimator2)
        return set
    }

    private fun createSingleIdleAnimation(): Animator {
        val innerCircleExpandAnimator = ValueAnimator.ofFloat(1f, INNER_MAX_SCALE)
        innerCircleExpandAnimator.interpolator = AccelerateDecelerateInterpolator()
        innerCircleExpandAnimator.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        innerCircleExpandAnimator.addUpdateListener { animation ->
            innerScale = animation.animatedValue as Float
            postInvalidateOnAnimation()
        }

        val innerCircleShrinkAnimator = ValueAnimator.ofFloat(INNER_MAX_SCALE, 1f)
        innerCircleShrinkAnimator.interpolator = AccelerateDecelerateInterpolator()
        innerCircleShrinkAnimator.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        innerCircleShrinkAnimator.addUpdateListener { animation ->
            innerScale = animation.animatedValue as Float
            postInvalidateOnAnimation()
        }

        val innerCircleEffectAnimator = ValueAnimator.ofFloat(0f, 1f)
        innerCircleEffectAnimator.interpolator = LinearInterpolator()
        innerCircleEffectAnimator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        innerCircleEffectAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            //animate scale INNER_MAX_SCALE -> EFFECT_MAX_SCALE
            effectScale = INNER_MAX_SCALE + (EFFECT_MAX_SCALE - INNER_MAX_SCALE) * value
            //animate alpha EFFECT_ALPHA(128) -> 0
            effectCircleDrawable.alpha = (EFFECT_ALPHA * (1 - value)).toInt()
            postInvalidateOnAnimation()
        }
        innerCircleEffectAnimator.addListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                effectScale = 0f
                postInvalidate()
            }
        })
        val set = AnimatorSet()
        set.play(innerCircleExpandAnimator).before(innerCircleShrinkAnimator)
        set.play(innerCircleShrinkAnimator).with(innerCircleEffectAnimator)
        return set
    }

    private fun updateDrawablePosition() {
        val pivotX = pivotX.toInt()
        val pivotY = pivotY.toInt()

        //1. calc radius
        val width: Int
        val displayWidth = resources.displayMetrics.widthPixels
        if (pivotX < displayWidth / 2) {
            width = displayWidth - pivotX
        } else {
            width = pivotX
        }
        if (title!!.measuredHeight == 0) {
            title!!.measure(View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
            message!!.measure(View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        }
        val textHeight = title!!.measuredHeight + message!!.measuredHeight
        val height = clipRadius + textVerticalPadding + textHeight
        val radius = Math.sqrt((width * width + height * height).toDouble()).toInt()

        //2.layout drawables
        outerCircleDrawable.setBounds(pivotX - radius, pivotY - radius,
                pivotX + radius, pivotY + radius)
        innerCircleDrawable.setBounds(pivotX - clipRadius, pivotY - clipRadius,
                pivotX + clipRadius, pivotY + clipRadius)
        effectCircleDrawable.setBounds(pivotX - clipRadius, pivotY - clipRadius,
                pivotX + clipRadius, pivotY + clipRadius)
        icon!!.setBounds(pivotX - icon!!.intrinsicWidth / 2, getPivotY().toInt() - icon!!.intrinsicHeight / 2,
                pivotX + icon!!.intrinsicWidth / 2, getPivotY().toInt() + icon!!.intrinsicHeight / 2)
    }

    interface TapListener {

        fun onTapTarget()

        fun onTapOutSide()
    }

    private inner class SingleTapGestureListener : GestureDetector.OnGestureListener {

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (listener != null) {
                val distance = Math.hypot((e.x - pivotX).toDouble(), (e.y - pivotY).toDouble())
                if (distance < clipRadius) {
                    listener!!.onTapTarget()
                } else {
                    listener!!.onTapOutSide()
                }
                return true
            }
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return false
        }
    }

    companion object {
        private val EFFECT_FIRST_DELAY = 1000
        private val EFFECT_DELAY = 4000
        private val OUTER_ALPHA = 0xF5
        private val EFFECT_ALPHA = 0x80
        private val INNER_MAX_SCALE = 1.1f
        private val EFFECT_MAX_SCALE = 2.0f
    }
}
