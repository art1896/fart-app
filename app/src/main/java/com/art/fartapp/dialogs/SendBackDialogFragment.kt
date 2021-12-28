package com.art.fartapp.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendBackDialogFragment : DialogFragment() {

    private val viewModel by viewModels<SendBackViewModel>()
    private val args by navArgs<SendBackDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Send fart back")
            .setMessage("Do you really want to send back FART?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.sendBack(args.token)
            }
            .create()

}