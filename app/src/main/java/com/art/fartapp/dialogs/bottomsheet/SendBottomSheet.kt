package com.art.fartapp.dialogs.bottomsheet

import android.animation.LayoutTransition
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.BottomSheetSendBinding
import com.art.fartapp.db.Sound
import com.art.fartapp.util.MorphButton
import com.art.fartapp.util.vibrate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect



@AndroidEntryPoint
class SendBottomSheet : BottomSheetDialogFragment(), AudioItemAdapter.OnItemClickListener {

    private var _binding: BottomSheetSendBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<SendBottomSheetArgs>()
    private val viewModel by viewModels<BottomSheetViewModel>()
    private lateinit var soundsAdapter: AudioItemAdapter
    var mMediaPlayer: MediaPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView = inflater.inflate(R.layout.bottom_sheet_send, container, false)
        _binding = BottomSheetSendBinding.bind(rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundsAdapter = AudioItemAdapter(this, requireContext())
        validateSendButton()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.bottomSheetFartersEvent.collect { event ->
                when (event) {
                    is BottomSheetViewModel.BottomSheetFartersEvent.NavigateToEditFarterScreen -> {
                        val action =
                            SendBottomSheetDirections.actionSendBottomSheetToAddEditFarterFragment(
                                event.farter,
                                "Edit Farter"
                            )
                        findNavController().navigate(action)
                    }
                    BottomSheetViewModel.BottomSheetFartersEvent.NavigateToHomeScreen -> {
                        playSound()
                        vibrate()
                        this@SendBottomSheet.dismiss()
                    }
                }
            }
        }
        binding.apply {
            val transition = LayoutTransition()
            transition.setAnimateParentHierarchy(false)
            this.root.layoutTransition = transition
            editFarter.setOnClickListener { viewModel.onEditFarterClick(args.farter) }
            anonymousSwitch.setOnCheckedChangeListener { _, isChecked ->
                textInputLayout.isVisible = !isChecked
                validateSendButton()
            }
            recyclerViewSounds.apply {
                setHasFixedSize(true)
                adapter = soundsAdapter
            }

            sendButton.setOnClickListener {
                sendFart()
            }

            editTextName.addTextChangedListener { validateSendButton() }
        }


        viewModel.sounds.observe(viewLifecycleOwner) {
            soundsAdapter.submitList(it)
        }

    }

    private fun sendFart() {
        binding.sendButton.isEnabled = false
        binding.sendButton.setUIState(MorphButton.UIState.Loading)
        val senderName = binding.editTextName.text.toString()
        val rawRes = viewModel.selectedSound.value!!.rawRes
        val canSendBack = binding.sendBackSwitch.isChecked
        viewModel.onSendFartClick(args.farter, canSendBack, rawRes, senderName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        soundsAdapter.stopPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onItemClick(sound: Sound) {
        viewModel.selectedSound.value = sound
        validateSendButton()
    }

    private fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(requireContext(), R.raw.au_task_1_2_done)
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    private fun validateSendButton() {
        if ((viewModel.selectedSound.value != null && binding.anonymousSwitch.isChecked) ||
            viewModel.selectedSound.value != null && !binding.anonymousSwitch.isChecked && binding.editTextName.text!!.isNotEmpty()
        ) {
            (binding.sendButton as View).isEnabled = true
            binding.sendButton.fromBgColor = Color.parseColor("#5EA3DC")
        } else {
            (binding.sendButton as View).isEnabled = false
            binding.sendButton.fromBgColor = Color.parseColor("#6C7883")
        }

    }

}