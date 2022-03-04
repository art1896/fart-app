package com.art.fartapp.ui.addeditfarter

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.art.fartapp.R
import com.art.fartapp.databinding.FragmentEditBinding
import com.art.fartapp.util.exhaustive
import com.art.fartapp.util.findNavControllerById
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class EditFragment : Fragment(R.layout.fragment_edit) {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<EditViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditBinding.bind(view)

        binding.apply {
            save.setOnClickListener {
                viewModel.onSaveClick()
            }

            editTextName.addTextChangedListener {
                viewModel.farterName = it.toString()
                if (it!!.isEmpty()) {
                    textInputLayout.isErrorEnabled = true
                    textInputLayout.error = "Enter name"
                } else textInputLayout.isErrorEnabled = false
            }

            editTextName.setText(viewModel.farterName)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editEvent.collect { event ->
                when (event) {
                    is EditViewModel.EditEvent.NavigateToFartersFragment -> {
                        binding.editTextName.clearFocus()
                        requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.childFragmentManager.fragments[0]
                            .setFragmentResult(
                                "edit_request",
                                bundleOf("edit_result" to event.result)
                            )
                        findNavControllerById(R.id.nav_host_fragment).popBackStack()
                    }
                    is EditViewModel.EditEvent.ShowInvalidInputMessage -> {
                        binding.apply {
                            textInputLayout.isErrorEnabled = true
                            textInputLayout.error = "Enter name"
                        }
                    }
                }.exhaustive
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}