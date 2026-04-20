package com.hom8.app.presentation.expenses.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hom8.app.R
import com.hom8.app.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ExpensesListFragment : Fragment() {

    private val viewModel: ExpensesViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private lateinit var adapter: ExpensesAdapter

    private val currencyFmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_expenses_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter(view)
        setupFilters(view)
        setupNavigation(view)
        observeState(view)
    }

    private fun setupAdapter(view: View) {
        adapter = ExpensesAdapter(
            currentUserId = session.userId,
            onItemClick = { expense ->
                // Navigate to create/edit with expenseId
                val action = ExpensesListFragmentDirections
                    .actionExpensesListToCreateExpense(expense.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { expense ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Eliminar gasto")
                    .setMessage("¿Eliminar \"${expense.descripcion}\"?")
                    .setPositiveButton(R.string.action_delete) { _, _ ->
                        viewModel.deleteExpense(expense)
                        Snackbar.make(requireView(), "Gasto eliminado", Snackbar.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
            }
        )

        view.findViewById<RecyclerView>(R.id.rvExpenses).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExpensesListFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupFilters(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategory)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(R.id.chipFood) -> ExpenseCategoryFilter.COMIDA
                checkedIds.contains(R.id.chipSupermarket) -> ExpenseCategoryFilter.SUPERMERCADO
                checkedIds.contains(R.id.chipServices) -> ExpenseCategoryFilter.SERVICIOS
                checkedIds.contains(R.id.chipTransport) -> ExpenseCategoryFilter.TRANSPORTE
                checkedIds.contains(R.id.chipEntertainment) -> ExpenseCategoryFilter.OCIO
                else -> ExpenseCategoryFilter.ALL
            }
            viewModel.setCategoryFilter(filter)
        }
    }

    private fun setupNavigation(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fabAddExpense).setOnClickListener {
            val action = ExpensesListFragmentDirections
                .actionExpensesListToCreateExpense(null)
            findNavController().navigate(action)
        }

        view.findViewById<TextView>(R.id.tvBalances).setOnClickListener {
            val action = ExpensesListFragmentDirections.actionExpensesListToBalances()
            findNavController().navigate(action)
        }

        view.findViewById<ImageButton>(R.id.btnExpensesSettings).setOnClickListener {
            if (viewModel.uiState.value.isAdmin) showModeDialog()
        }
    }

    private fun showModeDialog() {
        val currentMode = viewModel.uiState.value.isSplitMode
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_expense_mode, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setBackground(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            .create()
        
        // Make dialog background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val cardSplitMode = dialogView.findViewById<View>(R.id.cardSplitMode)
        val cardTrackingMode = dialogView.findViewById<View>(R.id.cardTrackingMode)
        val radioSplitMode = dialogView.findViewById<android.widget.RadioButton>(R.id.radioSplitMode)
        val radioTrackingMode = dialogView.findViewById<android.widget.RadioButton>(R.id.radioTrackingMode)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        
        // Set initial selection
        radioSplitMode.isChecked = currentMode
        radioTrackingMode.isChecked = !currentMode
        
        // Handle card clicks
        cardSplitMode.setOnClickListener {
            radioSplitMode.isChecked = true
            radioTrackingMode.isChecked = false
            viewModel.setExpensesMode(true)
            dialog.dismiss()
        }
        
        cardTrackingMode.setOnClickListener {
            radioSplitMode.isChecked = false
            radioTrackingMode.isChecked = true
            viewModel.setExpensesMode(false)
            dialog.dismiss()
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun observeState(view: View) {
        val tvTheyOwe = view.findViewById<TextView>(R.id.tvTheyOweAmount)
        val tvYouOwe = view.findViewById<TextView>(R.id.tvYouOweAmount)
        val tvExpenseCount = view.findViewById<TextView>(R.id.tvExpenseCount)
        val rvExpenses = view.findViewById<RecyclerView>(R.id.rvExpenses)
        val emptyLayout = view.findViewById<LinearLayout>(R.id.layoutExpensesEmpty)
        val layoutBalanceSummary = view.findViewById<LinearLayout>(R.id.layoutBalanceSummary)
        val tvBalances = view.findViewById<TextView>(R.id.tvBalances)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Mode-dependent visibility
                    val balanceVisibility = if (state.isSplitMode) View.VISIBLE else View.GONE
                    layoutBalanceSummary.visibility = balanceVisibility
                    tvBalances.visibility = balanceVisibility

                    // Settings button only visible to admin
                    view.findViewById<ImageButton>(R.id.btnExpensesSettings).visibility =
                        if (state.isAdmin) View.VISIBLE else View.GONE

                    tvTheyOwe.text = currencyFmt.format(state.theyOweAmount)
                    tvYouOwe.text = currencyFmt.format(state.youOweAmount)

                    val count = state.expenses.size
                    val total = currencyFmt.format(state.totalAmount)
                    tvExpenseCount.text = if (count == 0) {
                        "Sin gastos"
                    } else {
                        "$count gasto${if (count != 1) "s" else ""} · total $total"
                    }

                    adapter.updateMembersMap(state.membersMap)
                    adapter.updateSplitMode(state.isSplitMode)
                    adapter.submitList(state.expenses)

                    if (state.expenses.isEmpty() && !state.isLoading) {
                        emptyLayout.visibility = View.VISIBLE
                        rvExpenses.visibility = View.GONE
                    } else {
                        emptyLayout.visibility = View.GONE
                        rvExpenses.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
