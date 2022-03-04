package com.art.fartapp.ui.addeditfarter

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentAddFarterBinding
import com.art.fartapp.util.exhaustive
import com.art.fartapp.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


private const val TAG = "LoyaltyCardReader"

@AndroidEntryPoint
class AddEditFarterFragment : Fragment(R.layout.fragment_add_farter),
    EasyPermissions.PermissionCallbacks {


    private var _binding: FragmentAddFarterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddFarterViewModel>()

    private var nfcAdapter: NfcAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddFarterBinding.bind(view)

        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            binding.apply {
                pairWithNfc.text = "NFC isn't available on the device"
                pairWithNfc.isEnabled = false
            }
        } else {
            nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        }


        binding.apply {
            imageButtonQrScanner.setOnClickListener {
                if (editTextName.text!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    this.textInputLayout.error = "Enter name"
                } else {
                    requestPermissions()
                }
            }
            editTextName.addTextChangedListener {
                if (it!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    textInputLayout.error = "Enter name"
                } else textInputLayout.isErrorEnabled = false
            }
            pairWithNfc.setOnClickListener {
                if (editTextName.text!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    this.textInputLayout.error = "Enter name"
                } else {
                    hideKeyboard()
                    viewModel.onPairWithNfcClick(this.editTextName.text.toString())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fartersEvent.collect { event ->
                when (event) {
                    is AddFarterViewModel.FartersEvent.NavigateToEditFarterScreen -> {
                        val action =
                            AddEditFarterFragmentDirections.actionAddEditFarterFragmentToQrScannerFragment(
                                event.name
                            )
                        hideKeyboard()
                        findNavController().navigate(action)
                    }
                    is AddFarterViewModel.FartersEvent.NavigateToNfcScreen -> {
                        val action =
                            AddEditFarterFragmentDirections.actionAddEditFarterFragmentToAddWithNfcFragment(
                                event.name
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        viewModel.onScannClick(binding.editTextName.text.toString())
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.CAMERA
            )
        ) {
            viewModel.onScannClick(binding.editTextName.text.toString())
            return
        } else EasyPermissions.requestPermissions(
            this,
            "You need to accept camera permissions to use this app.",
            1,
            Manifest.permission.CAMERA
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}