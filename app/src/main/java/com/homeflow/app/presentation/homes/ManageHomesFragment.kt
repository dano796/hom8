package com.homeflow.app.presentation.homes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.homeflow.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageHomesFragment : Fragment() {

    private val viewModel: ManageHomesViewModel by viewModels()
    private lateinit var adapter: HomeCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_manage_homes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = HomeCardAdapter(onSwitchClick = { home ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Cambiar a \"${home.nombre}\"?")
                .setMessage("La app se recargará con los datos de este hogar.")
                .setPositiveButton("Cambiar") { _, _ -> viewModel.switchHome(home.id) }
                .setNegativeButton("Cancelar", null)
                .show()
        })

        view.findViewById<RecyclerView>(R.id.rvHomes).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ManageHomesFragment.adapter
            isNestedScrollingEnabled = false
        }

        view.findViewById<FloatingActionButton>(R.id.fabAddHome).setOnClickListener {
            findNavController().navigate(R.id.action_manageHomes_to_addHome)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        adapter.setActiveHomeId(state.activeHomeId)
                        adapter.submitList(state.homes)
                    }
                }
                launch {
                    viewModel.navigateToMain.collect { navigate ->
                        if (navigate) {
                            viewModel.onNavigatedToMain()
                            navigateToMain()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val rootNavController = androidx.navigation.Navigation.findNavController(
            requireActivity(), R.id.nav_host_fragment
        )
        rootNavController.navigate(
            R.id.mainFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.mainFragment, inclusive = true)
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .build()
        )
    }
}
