package com.homeflow.app.presentation.homes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.homeflow.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddHomeFragment : Fragment() {

    private val viewModel: AddHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_add_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = view.findViewById<TabLayout>(R.id.tabsAddHome)
        val panelCreate = view.findViewById<LinearLayout>(R.id.panelCreate)
        val panelJoin = view.findViewById<LinearLayout>(R.id.panelJoin)

        tabs.addTab(tabs.newTab().setText("Crear"))
        tabs.addTab(tabs.newTab().setText("Unirse"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                panelCreate.visibility = if (tab.position == 0) View.VISIBLE else View.GONE
                panelJoin.visibility = if (tab.position == 1) View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnCreateHome).setOnClickListener {
            val name = view.findViewById<TextInputEditText>(R.id.etHomeName).text.toString()
            viewModel.createHome(name)
        }

        view.findViewById<View>(R.id.btnJoinHome).setOnClickListener {
            val code = view.findViewById<TextInputEditText>(R.id.etInviteCode).text.toString()
            viewModel.joinHome(code)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isSaved) {
                        findNavController().popBackStack()
                    }
                    state.error?.let { msg ->
                        Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}
