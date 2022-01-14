package com.art.fartapp.util

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.withScale
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.art.fartapp.R
import com.google.android.material.math.MathUtils.lerp

class MorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    sealed class UIState {
        object Button : UIState()
        object Loading : UIState()
        object Animating : UIState()
    }

    var textSize: Float = 18 * sp()
    var text: String = ""
        set(value) {
            field = value
            requestLayout()
        }

    var fromBgColor: Int = getColorX(R.color.teal_200)
    var toBgColor: Int = getColorX(R.color.teal_200)
    var fromTextColor: Int = Color.WHITE
    var toTextColor: Int = getColorX(R.color.white)
    var btnRadius = 8 * dp()

    var iconDrawable = getDrawableX(R.drawable.ic__325077_check_circle_icon)
        set(value) {
            field = value
            setSizeIcon()
        }

    var iconPadding = 16 * dp()
        set(value) {
            field = value
            setSizeIcon()
        }

    private fun setSizeIcon() {
        sizeIcon = iconDrawable.intrinsicWidth + iconPadding
    }

    /////////////////////////// Internal State ///////////////////////////
    private var uiState: UIState = UIState.Button
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val argbEvaluator = ArgbEvaluator()
    private fun getTextPaint() = paint.apply {
        color = argbEvaluator.evaluate(colorTextFraction, fromTextColor, toTextColor) as Int
        paint.textSize = this@MorphButton.textSize
    }

    private fun getBtnBgPaint() = paint.apply {
        color = argbEvaluator.evaluate(colorBgFraction, fromBgColor, toBgColor) as Int
    }


    private val textBound = Rect()
    private var sizeIcon: Float = 0f

    // fraction
    private var iconDegree: Float = 0f
    private var scaleIconFraction: Float = 0f
    private var scaleTextFraction: Float = 0f
    private var colorBgFraction: Float = 0f
    private var colorTextFraction: Float = 0f
    private var morphFraction: Float = 0f

    init {
        setPadding(
            (24 * dp()).toInt(),
            (12 * dp()).toInt(),
            (24 * dp()).toInt(),
            (12 * dp()).toInt()
        )
        text = "Send fart"
        setSizeIcon()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 100
        val desiredHeight = 100

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                desiredWidth.coerceAtMost(widthSize)
            }
            else -> {
                desiredWidth
            }
        }
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                desiredHeight.coerceAtMost(heightSize)
            }
            else -> {
                desiredHeight
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // calculate the bound of the button
        iconDrawable.let { drawable ->
            val left = w / 2 - drawable.intrinsicWidth / 2
            val top = h / 2 - drawable.intrinsicHeight / 2
            val right = left + drawable.intrinsicWidth
            val bottom = top + drawable.intrinsicHeight
            drawable.setBounds(left, top, right, bottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // draw the btn and morph it to circle
        getBtnBgPaint().let { paint ->
            val left = lerp(0f, width / 2f - sizeIcon / 2f, morphFraction)
            val top = lerp(0f, height / 2f - sizeIcon / 2f, morphFraction)
            val right = lerp(width.toFloat(), width / 2f + sizeIcon / 2f, morphFraction)
            val bottom =
                lerp(height.toFloat(), height.toFloat() / 2f + sizeIcon / 2f, morphFraction)
            val radius = lerp(this.btnRadius, sizeIcon / 2f, morphFraction)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }
        // scale down the text
        getTextPaint().let { paint ->
            val scaleX = lerp(1f, 0f, scaleTextFraction)
            val scaleY = scaleX
            val pivotX = width / 2f
            val pivotY = height / 2f
            canvas.withScale(scaleX, scaleY, pivotX, pivotY) {
                val xPos = width / 2f
                val yPos =
                    height / 2 - (paint.descent() + paint.ascent()) / 2f
                canvas.drawText(text, xPos, yPos, paint)
            }
        }
        // scale up the icon
        iconDrawable.let { drawable ->
            val scaleX = lerp(0f, 1f, scaleIconFraction)
            val scaleY = scaleX
            val pivotX = width / 2f
            val pivotY = height / 2f
            canvas.withScale(scaleX, scaleY, pivotX, pivotY) {
                // rotate the icon if it appears
                if (scaleIconFraction > 0) {
                    invalidate()
                }
                rotate(iconDegree, pivotX, pivotY)
                // Anticlockwise direction
                val iconSpeed = 6
                iconDegree = (iconDegree - iconSpeed) % 360
                drawable.draw(canvas)
            }
        }
    }

    fun setUIState(uiState: UIState): Boolean {
        if (this.uiState == UIState.Animating || this.uiState == uiState) {
            return false
        }
        val isReverse = uiState != UIState.Loading
        runAnimation(isReverse).apply {
            doOnEnd {
                this@MorphButton.uiState = uiState
            }
        }
        return true
    }

    private fun runAnimation(isReverse: Boolean): AnimatorSet {
        val values = if (isReverse) {
            floatArrayOf(1f, 0f)
        } else {
            floatArrayOf(0f, 1f)
        }
        val animatorList = listOf(
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    colorBgFraction = it.animatedValue as Float
                    colorTextFraction = it.animatedValue as Float
                    invalidate()
                }
            },
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    scaleTextFraction = it.animatedValue as Float
                    morphFraction = it.animatedValue as Float
                    invalidate()
                }
            },
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    scaleIconFraction = it.animatedValue as Float
                    invalidate()
                }
            }
        ).let {
            if (isReverse) {
                it.reversed()
            } else {
                it
            }
        }
        return AnimatorSet().apply {
            playSequentially(
                animatorList
            )
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                this@MorphButton.uiState = UIState.Animating
            }
            duration = 250L
            start()
        }
    }
}