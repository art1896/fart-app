package com.art.fartapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Size
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.fragment.app.Fragment
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener
import java.util.*
import android.content.ContentResolver
import android.graphics.drawable.Drawable
import android.net.Uri
import java.io.File
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


val <T> T.exhaustive: T
    get() = this

@SuppressLint("MissingPermission")
fun Fragment.vibrate() {
    val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(50)
    }
}

fun Activity.showShowCaseView(
    title: String,
    contentText: String,
    view: View,
    guideListener: GuideListener
) {
    GuideView.Builder(this)
        .setTitle(title)
        .setContentText(contentText)
        .setTargetView(view)
        .setDismissType(DismissType.anywhere)
        .setGuideListener(guideListener)
        .build()
        .show()
}

fun getRandomColor(): Int {
    val rnd = Random()
    return Color.argb(255, rnd.nextInt(250) + 1, rnd.nextInt(250) + 1, rnd.nextInt(250) + 1)
}

fun Context.getResourceName(id: Int): String = resources.getResourceEntryName(id)

fun Context.getResourceId(resourceName: String, type: String) = resources.getIdentifier(resourceName, type, packageName)

fun View.sp() = resources.displayMetrics.scaledDensity
fun View.dp() = resources.displayMetrics.density
fun View.getDrawableX(drawableId: Int): Drawable {
    return ContextCompat.getDrawable(context, drawableId)!!
}

fun View.getColorX(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(context, colorRes)
}

@Throws(Resources.NotFoundException::class)
fun Context.getUriToResource(@AnyRes resId: Int): Uri? {
    val res = resources
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId)
    )
}

fun Context.getDisplayDimensions(): Pair<Int, Int> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = display.width
        val height = display.height
        return Pair(width, height)
    } else {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics: WindowMetrics = wm.currentWindowMetrics
        val windowInsets = metrics.windowInsets
        val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
        )

        val insetsWidth: Int = insets.right + insets.left
        val insetsHeight: Int = insets.top + insets.bottom

        val bounds: Rect = metrics.bounds
        val legacySize = Size(
            bounds.width() - insetsWidth,
            bounds.height() - insetsHeight
        )
        return Pair(legacySize.width, legacySize.height)
    }
}

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()