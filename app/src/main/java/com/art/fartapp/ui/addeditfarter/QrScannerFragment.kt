package com.art.fartapp.ui.addeditfarter

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentQrScannerBinding
import com.art.fartapp.db.Farter
import com.art.fartapp.util.exhaustive
import com.art.fartapp.util.findNavControllerById
import com.art.fartapp.util.vibrate
import com.google.zxing.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import me.dm7.barcodescanner.zxing.ZXingScannerView

private const val TAG = "LoyaltyCardReader"

@AndroidEntryPoint
class QrScannerFragment : Fragment(R.layout.fragment_qr_scanner), ZXingScannerView.ResultHandler {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<QrScannerViewModel>()
    private val args by navArgs<QrScannerFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQrScannerBinding.bind(view)
        binding.scanner.setResultHandler(this)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fartersEvent.collect { event ->
                when (event) {
                    is QrScannerViewModel.FartersEvent.NavigateToFartersFragment -> {
                        viewModel.insertFarter(event.farter)
                        findNavControllerById(R.id.nav_host_fragment).popBackStack()
                    }
                }.exhaustive
            }
        }


    }

    override fun onResume() {
        super.onResume()
        binding.scanner.startCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.scanner.stopCamera()
        _binding = null
    }

    override fun handleResult(p0: Result?) {
        vibrate()
        viewModel.onHandleResult(Farter(p0!!.text, args.name))
    }
}