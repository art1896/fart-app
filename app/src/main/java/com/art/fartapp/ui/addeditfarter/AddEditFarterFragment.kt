package com.art.fartapp.ui.addeditfarter

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentAddEditFarterBinding
import com.art.fartapp.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class AddEditFarterFragment : Fragment(R.layout.fragment_add_edit_farter),
    EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentAddEditFarterBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddEditFarterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditFarterBinding.bind(view)
        binding.apply {
            imageButtonQrScanner.setOnClickListener {
                if (editTextName.text!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    this.textInputLayout.error = "Enter name"
                } else {
                    requestPermissions()
                }
            }
            editTextName.setText(viewModel.farterName)
            imageButtonQrScanner.isVisible = viewModel.farter == null
            if (viewModel.farter != null) {
                lottie.setAnimation(R.raw.edit_anim)
            } else {
                lottie.setAnimation(R.raw.qr_scanner_json)
            }
            fabSaveFarter.isVisible = viewModel.farter != null
            editTextName.addTextChangedListener {
                viewModel.farterName = it.toString()
                if (it!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    textInputLayout.error = "Enter name"
                } else textInputLayout.isErrorEnabled = false
            }
            fabSaveFarter.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fartersEvent.collect { event ->
                when (event) {
                    is AddEditFarterViewModel.FartersEvent.NavigateToEditFarterScreen -> {
                        val action =
                            AddEditFarterFragmentDirections.actionAddEditFarterFragmentToQrScannerFragment(
                                event.name
                            )
                        findNavController().navigate(action)
                    }
                    is AddEditFarterViewModel.FartersEvent.NavigateBackWithResult -> {
                        binding.editTextName.clearFocus()
                        setFragmentResult(
                            "edit_request",
                            bundleOf("edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditFarterViewModel.FartersEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
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