package com.hom8.app.presentation.expenses.create

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CreateExpenseFragment : Fragment() {

    private val viewModel: CreateExpenseViewModel by viewModels()
    private val args: CreateExpenseFragmentArgs by navArgs()

    private val dateFmt = SimpleDateFormat("MMMM d, yyyy", Locale("es", "ES"))

    // Category -> card view id mapping
    private val categoryCardMap = mapOf(
        "COMIDA" to R.id.catFood,
        "SUPERMERCADO" to R.id.catSupermarket,
        "SERVICIOS" to R.id.catServices,
        "TRANSPORTE" to R.id.catTransport,
        "OCIO" to R.id.catEntertainment,
        "OTROS" to R.id.catOther
    )

    private var lastMembersRendered: List<ExpenseMemberOption> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_create_expense, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadExpense(args.expenseId)

        setupCategoryGrid(view)
        setupDatePicker(view)
        setupButtons(view)
        observeState(view)
    }

    private fun setupCategoryGrid(view: View) {
        categoryCardMap.forEach { (category, cardId) ->
            view.findViewById<MaterialCardView>(cardId).setOnClickListener {
                viewModel.setCategory(category)
            }
        }
    }

    private fun setupDatePicker(view: View) {
        view.findViewById<LinearLayout>(R.id.rowDate).setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    cal.set(year, month, day)
                    viewModel.setDate(cal.timeInMillis)
                    view.findViewById<TextView>(R.id.tvSelectedDate).text =
                        dateFmt.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }
        view.findViewById<TextView>(R.id.tvCancel).setOnClickListener {
            findNavController().popBackStack()
        }
        view.findViewById<MaterialButton>(R.id.btnCancelExpense).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveExpense).setOnClickListener {
            val description = view.findViewById<TextInputEditText>(R.id.etDescription).text.toString().trim()
            val amount = view.findViewById<TextInputEditText>(R.id.etAmount).text.toString().trim()
            val note = view.findViewById<TextInputEditText>(R.id.etNote).text.toString().trim()
            viewModel.saveExpense(description, amount, note)
        }
    }

    private fun observeState(view: View) {
        val tilDescription = view.findViewById<TextInputLayout>(R.id.tilDescription)
        val tilAmount = view.findViewById<TextInputLayout>(R.id.tilAmount)
        val tvTitle = view.findViewById<TextView>(R.id.tvToolbarTitle)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etAmount)
        val etNote = view.findViewById<TextInputEditText>(R.id.etNote)
        val tvDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val chipGroupPaidBy = view.findViewById<ChipGroup>(R.id.chipGroupPaidBy)
        val layoutPaidBy = view.findViewById<LinearLayout>(R.id.layoutPaidBy)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Title changes for edit mode
                    state.existingExpense?.let { expense ->
                        tvTitle.text = "Editar gasto"
                        if (etDescription.text.isNullOrEmpty()) {
                            etDescription.setText(expense.descripcion)
                            etAmount.setText(expense.monto.toString())
                            etNote.setText(expense.nota)
                        }
                    }

                    // Paid by section — visible in SPLIT mode only
                    layoutPaidBy.visibility = if (state.isSplitMode) View.VISIBLE else View.GONE

                    if (state.isSplitMode) {
                        // Rebuild chips only when member list changes
                        if (state.members != lastMembersRendered) {
                            lastMembersRendered = state.members
                            buildPayerChips(chipGroupPaidBy, state.members, state.selectedPayerId)
                        } else {
                            // Sync selection without rebuilding chips
                            for (i in 0 until chipGroupPaidBy.childCount) {
                                val chip = chipGroupPaidBy.getChildAt(i) as? Chip ?: continue
                                chip.isChecked = chip.tag == state.selectedPayerId
                            }
                        }
                    }

                    // Highlight selected category
                    categoryCardMap.forEach { (category, cardId) ->
                        val card = view.findViewById<MaterialCardView>(cardId)
                        if (category == state.selectedCategory) {
                            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                            card.strokeWidth = 3
                        } else {
                            card.strokeColor = ContextCompat.getColor(requireContext(), R.color.colorOutline)
                            card.strokeWidth = 1
                        }
                    }

                    // Date display
                    tvDate.text = dateFmt.format(state.selectedDate)

                    // Errors
                    when {
                        state.error?.contains("Descripción") == true -> {
                            tilDescription.error = state.error
                            tilAmount.error = null
                        }
                        state.error?.contains("monto") == true -> {
                            tilAmount.error = state.error
                            tilDescription.error = null
                        }
                        state.error != null -> {
                            Snackbar.make(requireView(), state.error, Snackbar.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                        else -> {
                            tilDescription.error = null
                            tilAmount.error = null
                        }
                    }

                    // Navigate back on save
                    if (state.isSaved) {
                        Snackbar.make(requireView(), "¡Gasto guardado!", Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun buildPayerChips(
        chipGroup: ChipGroup,
        members: List<ExpenseMemberOption>,
        selectedPayerId: String
    ) {
        chipGroup.removeAllViews()
        members.forEach { member ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
                text = member.name
                tag = member.id
                isCheckable = true
                isChecked = member.id == selectedPayerId
                chipCornerRadius = 20f
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.setPayerId(member.id)
                }
            }
            chipGroup.addView(chip)
        }
    }
}
