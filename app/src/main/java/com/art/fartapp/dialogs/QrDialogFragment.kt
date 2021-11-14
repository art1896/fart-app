package com.art.fartapp.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.util.getDisplayDimensions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrDialogFragment : DialogFragment() {

    private val args by navArgs<QrDialogFragmentArgs>()
    private val viewModel by viewModels<QrViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rootView = inflater.inflate(R.layout.dialog_qr, container, false)
        val imageView = rootView.findViewById<ImageView>(R.id.image_view_qr)
        if (args.token == null) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                viewModel.updateToken(it)
                generateQr(it, imageView)
            }
        } else generateQr(args.token!!, imageView)
        return rootView
    }


    private fun generateQr(token: String, imageView: ImageView) {
        val multiFormatWriter = MultiFormatWriter()
        val size = (requireContext().getDisplayDimensions().first * 0.8).toInt()
        try {
            val bitMatrix = multiFormatWriter.encode(token, BarcodeFormat.QR_CODE, size, size)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        val width = RelativeLayout.LayoutParams.WRAP_CONTENT
        val height = RelativeLayout.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.setCancelable(true)
    }
}