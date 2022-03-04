package com.art.fartapp.ui.farters

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.art.fartapp.services.KHostApduService
import com.art.fartapp.util.*
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import java.security.AccessController.getContext
import javax.inject.Inject


private const val TAG = "FarterDatabase"

@AndroidEntryPoint
class FartersFragment : Fragment(R.layout.fragment_farters), FartersAdapter.OnItemClickListener {

    private var _binding: FragmentFartersBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FartersViewModel>()
    private lateinit var searchView: SearchView
    private var hideFabJob: Job? = null
    private lateinit var fartersAdapter: FartersAdapter

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFartersBinding.bind(view)
        fartersAdapter = FartersAdapter(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.apply {
            recyclerViewFarters.apply {
                adapter = fartersAdapter
                setHasFixedSize(true)
                addItemDecoration(SimpleDividerItemDecoration(requireContext(), R.drawable.line_divider))
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            RecyclerView.SCROLL_STATE_IDLE -> {
                                hideFab()
                            }
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                if (binding.fabAddFarter.alpha < 1f) {
                                    binding.fabAddFarter.animate().alpha(1f)
                                }
                            }
                        }
                    }
                })
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
                    val farter = fartersAdapter.currentList[viewHolder.absoluteAdapterPosition]
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

        setFragmentResultListener("user_guide_showed") { _, bundle ->
            viewModel.updateIsGuideShowed(bundle.getBoolean("showed"))
        }

        hideFab()

        viewModel.farters.observe(viewLifecycleOwner) {
            fartersAdapter.submitList(it)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (!viewModel.preferencesFlow.first().isGuideShowed) {
                viewModel.onUserGuideClick()
            }
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
                            FartersFragmentDirections.actionGlobalAddBottomSheet()
                        findNavController().navigate(action)
                    }
                    is FartersViewModel.FartersEvent.ShowFarterSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    is FartersViewModel.FartersEvent.NavigatetoDeleteAllFartersScreen -> {
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
                        if(findNavController().currentDestination?.label!! != "QrDialogFragment") {
                            findNavController().navigate(action)
                        } else {}
                    }
                    FartersViewModel.FartersEvent.ShowNoInternetConnectionMessage -> {
                        Snackbar.make(
                            requireView(),
                            "No Internet Connection",
                            Snackbar.LENGTH_LONG
                        ).setBackgroundTint(
                            Color.parseColor("#EE5260")
                        ).show()
                    }
                    FartersViewModel.FartersEvent.NavigateToUserGuideScreen -> {
                        showUserGuideDialog()
                    }
                    is FartersViewModel.FartersEvent.OpenBottomSheet -> {
                        val action = FartersFragmentDirections.actionGlobalSendBottomSheet(event.farter)
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        binding.adView.destroy()
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
        _binding = null
    }

    private fun showUserGuideDialog() {
        val action = FartersFragmentDirections.actionGlobalHowToDialogFragment()
        findNavController().navigate(action)
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        binding.adView.resume()
        super.onResume()
    }

    private fun hideFab() {
        if (this::fartersAdapter.isInitialized && fartersAdapter.itemCount != 0) {
            hideFabJob?.cancel()
            hideFabJob = CoroutineScope(Dispatchers.Main).launch {
                delay(3000)
                try {
                    binding.fabAddFarter.animate().alpha(0f)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
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
            R.id.action_user_guide -> {
                viewModel.onUserGuideClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onItemClick(farter: Farter) {
        viewModel.onSendFartClick(farter)
    }

}