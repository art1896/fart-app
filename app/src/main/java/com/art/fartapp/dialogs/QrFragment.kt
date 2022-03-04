package com.art.fartapp.dialogs

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentQrBinding
import com.art.fartapp.util.exhaustive
import com.art.fartapp.util.getBitmapUri
import com.art.fartapp.util.setQrToImageView
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "QrFragment"

@AndroidEntryPoint
class QrFragment : Fragment(R.layout.fragment_qr) {

    private val args by navArgs<QrFragmentArgs>()
    private val viewModel by viewModels<QrViewModel>()
    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!
    private lateinit var nfcAdapter: NfcAdapter
    private var token: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQrBinding.bind(view)
        setHasOptionsMenu(true)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.qrEvents.collect { event ->
                when (event) {
                    is QrViewModel.QrEvents.ShareQr -> {
                        shareQr(event.qr)
                    }
                    QrViewModel.QrEvents.OpenNfcDialog -> {
                        openBottomSheet()
                    }
                }.exhaustive
            }
        }

        if (args.token == null) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                token = it
                viewModel.updateToken(token!!)
                setQrToImageView(it, binding.imageViewQr, requireContext())

            }
        } else setQrToImageView(args.token!!, binding.imageViewQr, requireContext())

        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            binding.apply {
                nfcText.text = "NFC isn't available on the device"
                nfc.isEnabled = false
                nfc.alpha = 0.6f
            }
        } else {
            nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        }

        binding.nfc.setOnClickListener { viewModel.onNfcClick() }
    }

    private fun shareQr(icon: Bitmap?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(icon!!, requireContext()))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, "Share with"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                viewModel.onShareQrClick(
                    setQrToImageView(
                        if (args.token != null) args.token!! else token!!,
                        null,
                        requireContext()
                    )
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openBottomSheet() {
        val action =
            QrFragmentDirections.actionQrFragmentToNfcSendBottomSheet(if (args.token != null) args.token!! else token!!)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}