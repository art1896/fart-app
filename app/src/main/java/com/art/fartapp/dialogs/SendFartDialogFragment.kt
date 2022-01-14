package com.art.fartapp.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendFartDialogFragment : DialogFragment() {

    private val viewModel by viewModels<SendFartViewModel>()
    private val args by navArgs<SendFartDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Send fart")
            .setMessage("Do you really want to send fart to ${args.farter.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->
//                viewModel.onConfirmClick(args.farter)
            }
            .create()

}