package com.art.fartapp.dialogs.nfcbottomsheet

import android.app.Dialog
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.BottomSheetNfcSendBinding
import com.art.fartapp.services.KHostApduService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


private const val TAG = "LoyaltyCardReader"

class NfcSendBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetNfcSendBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<NfcSendBottomSheetArgs>()
    private var mNfcAdapter: NfcAdapter? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            d.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            d.behavior.skipCollapsed = true
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView = inflater.inflate(R.layout.bottom_sheet_nfc_send, container, false)
        _binding = BottomSheetNfcSendBinding.bind(rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener { dismiss() }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())

        initService()

    }

    override fun onResume() {
        super.onResume()
        initService()
    }

    private fun initService() {
        val intent = Intent(requireContext(), KHostApduService::class.java)
        intent.putExtra("ndefMessage", args.token)
        requireActivity().startService(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}