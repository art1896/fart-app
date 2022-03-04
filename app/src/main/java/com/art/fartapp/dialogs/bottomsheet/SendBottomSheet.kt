package com.art.fartapp.dialogs.bottomsheet

import android.animation.LayoutTransition
import android.app.Dialog
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.BottomSheetSendBinding
import com.art.fartapp.db.Sound
import com.art.fartapp.util.MorphButton
import com.art.fartapp.util.vibrate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "LoyaltyCardReader"

@AndroidEntryPoint
class SendBottomSheet : BottomSheetDialogFragment() {


    private lateinit var navController: NavController
    private val args by navArgs<SendBottomSheetArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            d.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView = inflater.inflate(R.layout.bottom_sheet_send, container, false)

        val bundle = Bundle()
        bundle.putParcelable("farter", args.farter)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.bottom_sheet_send_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.send_bottom_navigation)

        navController.setGraph(graph, bundle)



        return rootView
    }

}