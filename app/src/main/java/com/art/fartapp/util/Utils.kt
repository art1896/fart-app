package com.art.fartapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.experimental.xor


val hexArray = "0123456789ABCDEF".toCharArray()


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

fun bytesToHex(bytes: ByteArray): String? {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v: Int = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v ushr 4]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

fun isEqual(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) {
        return false
    }
    var result = 0
    for (i in a.indices) {
        result = result or (a[i] xor b[i]).toInt()
    }
    return result == 0
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

fun setQrToImageView(text: String, imageView: ImageView?, context: Context): Bitmap? {
    val multiFormatWriter = MultiFormatWriter()
    val size = (context.getDisplayDimensions().first * 0.6).toInt()
    try {
        val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        imageView?.setImageBitmap(bitmap)
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun Fragment.findNavControllerById(@IdRes id: Int): NavController {
    var parent = parentFragment
    while (parent != null) {
        if (parent is NavHostFragment && parent.id == id) {
            return parent.navController
        }
        parent = parent.parentFragment
    }
    throw RuntimeException("NavController with specified id not found")
}

fun getBitmapUri(image: Bitmap, context: Context): Uri? {
    val imagesFolder = File(context.cacheDir, "images")
    var uri: Uri? = null
    try {
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "shared_image.png")
        val stream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        uri = FileProvider.getUriForFile(context, "com.art.fileprovider", file)
    } catch (e: IOException) {
    }
    return uri
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

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}