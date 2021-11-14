package com.art.fartapp.ui.farters

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.art.fartapp.R
import com.art.fartapp.data.SortOrder
import com.art.fartapp.databinding.FragmentFartersBinding
import com.art.fartapp.db.Farter
import com.art.fartapp.util.exhaustive
import com.art.fartapp.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class FartersFragment : Fragment(R.layout.fragment_farters), FartersAdapter.OnItemClickListener {

    private var _binding: FragmentFartersBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FartersViewModel>()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFartersBinding.bind(view)
        val fartersAdapter = FartersAdapter(this)
        binding.apply {
            recyclerViewFarters.apply {
                adapter = fartersAdapter
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val farter = fartersAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onFarterSwiped(farter)
                }
            }).attachToRecyclerView(recyclerViewFarters)

            fabAddFarter.setOnClickListener {
                viewModel.onAddNewFarterClick()
            }
        }

        setFragmentResultListener("edit_request") { _, bundle ->
            val result = bundle.getInt("edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.farters.observe(viewLifecycleOwner) {
            binding.imageViewFart.alpha = if (it.isEmpty()) 1f else 0.3f
            fartersAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fartersEvent.collect { event ->
                when (event) {
                    is FartersViewModel.FartersEvent.ShowUndoDeleteFarterMessage -> {
                        Snackbar.make(requireView(), "Farter deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeletedClick(event.farter)
                            }.show()
                    }
                    is FartersViewModel.FartersEvent.NavigateToAddFarterScreen -> {
                        val action =
                            FartersFragmentDirections.actionFartersFragmentToAddEditFarterFragment(
                                title = "New Farter"
                            )
                        findNavController().navigate(action)
                    }
                    is FartersViewModel.FartersEvent.NavigateToEditFarterScreen -> {
                        val action =
                            FartersFragmentDirections.actionFartersFragmentToAddEditFarterFragment(
                                event.farter,
                                "Edit Farter"
                            )
                        findNavController().navigate(action)
                    }
                    is FartersViewModel.FartersEvent.ShowFarterSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    FartersViewModel.FartersEvent.NavigatetoDeleteAllFartersScreen -> {
                        val action =
                            FartersFragmentDirections.actionGlobalDeleteAllFartersDialogFragment()
                        findNavController().navigate(action)
                    }
                    is FartersViewModel.FartersEvent.NavigateToSendFartScreen -> {
                        val action =
                            FartersFragmentDirections.actionGlobalSendFartDialogFragment(event.farter)
                        findNavController().navigate(action)
                    }
                    is FartersViewModel.FartersEvent.NavigateToQrScreen -> {
                        val action =
                            FartersFragmentDirections.actionGlobalQrDialogFragment(viewModel.preferencesFlow.first().token)
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_farters, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_clear -> {
                viewModel.onDeleteAllFartersClick()
                true
            }
            R.id.action_qr -> {
                viewModel.onQrClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onItemClick(farter: Farter) {
        viewModel.onEditFarterClick(farter)
    }

    override fun onSendFartClick(farter: Farter) {
        viewModel.onSendFartClick(farter)
    }
}