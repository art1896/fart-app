package com.art.fartapp.ui.addeditfarter

import android.animation.Animator
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentAddWithNfcBinding
import com.art.fartapp.db.Farter
import com.art.fartapp.util.NdefMessageParser
import com.art.fartapp.util.findNavControllerById
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "LoyaltyCardReader"

@AndroidEntryPoint
class AddWithNfcFragment : Fragment(R.layout.fragment_add_with_nfc) {

    private var _binding: FragmentAddWithNfcBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel>()
    private val args by navArgs<AddWithNfcFragmentArgs>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddWithNfcBinding.bind(view)

        viewModel.intent.get.observe(viewLifecycleOwner) { intent ->
            intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                val token = parserNDEFMessage(messages)
                viewModel.insertFarter(Farter(token, args.name))
                viewModel.intent.set(null)
                startDoneAnimation()
            }
        }

    }

    private fun startDoneAnimation() {
        binding.apply {
            text1.text = "DONE"
            text2.text = "Successfully added to your list"
            lottie.setAnimation(R.raw.done)
            lottie.repeatCount = 0
            lottie.playAnimation()
            lottie.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    findNavControllerById(R.id.nav_host_fragment).popBackStack()
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {
                }
            })
        }
    }

    private fun parserNDEFMessage(messages: List<NdefMessage>): String {
        val builder = StringBuilder()
        val records = NdefMessageParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str)
        }
        return builder.toString()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}