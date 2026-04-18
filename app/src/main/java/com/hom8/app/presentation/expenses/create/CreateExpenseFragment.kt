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
    private var lastParticipantsRendered: Set<String> = emptySet()

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
        
        // Split mode chips
        view.findViewById<Chip>(R.id.chipEqualSplit).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSplitMode(SplitMode.EQUAL)
        }
        view.findViewById<Chip>(R.id.chipCustomSplit).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setSplitMode(SplitMode.CUSTOM)
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
        val chipGroupParticipants = view.findViewById<ChipGroup>(R.id.chipGroupParticipants)
        val layoutParticipants = view.findViewById<LinearLayout>(R.id.layoutParticipants)
        val layoutCustomAmounts = view.findViewById<LinearLayout>(R.id.layoutCustomAmounts)

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
                    layoutParticipants.visibility = if (state.isSplitMode) View.VISIBLE else View.GONE

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
                        
                        // Rebuild participant chips when members or selection changes
                        if (state.members != lastMembersRendered || state.selectedParticipants != lastParticipantsRendered) {
                            lastParticipantsRendered = state.selectedParticipants
                            buildParticipantChips(chipGroupParticipants, state.members, state.selectedParticipants)
                        }
                        
                        // Show/hide custom amounts based on split mode
                        layoutCustomAmounts.visibility = if (state.splitMode == SplitMode.CUSTOM) View.VISIBLE else View.GONE
                        
                        // Build custom amount inputs when in custom mode
                        if (state.splitMode == SplitMode.CUSTOM) {
                            buildCustomAmountInputs(layoutCustomAmounts, state.members, state.selectedParticipants, state.customAmounts)
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
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(20f * resources.displayMetrics.density)
                    .build()
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.setPayerId(member.id)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun buildParticipantChips(
        chipGroup: ChipGroup,
        members: List<ExpenseMemberOption>,
        selectedParticipants: Set<String>
    ) {
        chipGroup.removeAllViews()
        members.forEach { member ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
                text = member.name
                tag = member.id
                isCheckable = true
                isChecked = selectedParticipants.contains(member.id)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(20f * resources.displayMetrics.density)
                    .build()
                setOnCheckedChangeListener { _, _ ->
                    viewModel.toggleParticipant(member.id)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun buildCustomAmountInputs(
        container: LinearLayout,
        members: List<ExpenseMemberOption>,
        selectedParticipants: Set<String>,
        customAmounts: Map<String, Double>
    ) {
        container.removeAllViews()
        
        members.filter { selectedParticipants.contains(it.id) }.forEach { member ->
            val inputLayout = TextInputLayout(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing2)
                }
                hint = getString(R.string.expenses_custom_amount_hint, member.name)
                prefixText = "COP "
                // Usar setBoxCornerRadii en lugar de boxCornerRadius
                setBoxCornerRadii(8f, 8f, 8f, 8f)
            }
            
            val editText = TextInputEditText(requireContext()).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                val currentAmount = customAmounts[member.id]
                if (currentAmount != null && currentAmount > 0) {
                    setText(currentAmount.toString())
                }
                
                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        val amount = s.toString().toDoubleOrNull() ?: 0.0
                        viewModel.setCustomAmount(member.id, amount)
                    }
                })
            }
            
            inputLayout.addView(editText)
            container.addView(inputLayout)
        }
    }
}
