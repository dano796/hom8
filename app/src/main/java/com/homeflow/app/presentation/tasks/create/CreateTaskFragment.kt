package com.homeflow.app.presentation.tasks.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.homeflow.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CreateTaskFragment : Fragment() {

    private val viewModel: CreateTaskViewModel by viewModels()
    private val args: CreateTaskFragmentArgs by navArgs()

    private lateinit var etTitle: TextInputEditText
    private lateinit var tilTitle: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var tvDueDate: TextView
    private lateinit var layoutDueDate: LinearLayout
    private lateinit var chipGroupPriority: ChipGroup
    private lateinit var chipGroupAssignee: ChipGroup
    private lateinit var layoutChecklist: LinearLayout
    private lateinit var btnAddSubtask: TextView
    private lateinit var btnSaveTask: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnCancel: TextView

    private var selectedDueDate: Calendar? = null
    private var lastMembersRendered: List<MemberOption> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_create_task, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupListeners()
        observeState()

        // Load task for editing if taskId provided
        args.taskId?.let { viewModel.loadTask(it) }
    }

    private fun bindViews(view: View) {
        etTitle = view.findViewById(R.id.etTitle)
        tilTitle = view.findViewById(R.id.tilTitle)
        etDescription = view.findViewById(R.id.etDescription)
        tvDueDate = view.findViewById(R.id.tvDueDate)
        layoutDueDate = view.findViewById(R.id.layoutDueDate)
        chipGroupPriority = view.findViewById(R.id.chipGroupPriority)
        chipGroupAssignee = view.findViewById(R.id.chipGroupAssignee)
        layoutChecklist = view.findViewById(R.id.layoutChecklist)
        btnAddSubtask = view.findViewById(R.id.btnAddSubtask)
        btnSaveTask = view.findViewById(R.id.btnSaveTask)
        btnBack = view.findViewById(R.id.btnBack)
        btnCancel = view.findViewById(R.id.btnCancel)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { findNavController().popBackStack() }
        btnCancel.setOnClickListener { findNavController().popBackStack() }

        layoutDueDate.setOnClickListener { showDateTimePicker() }

        btnAddSubtask.setOnClickListener { addChecklistItem("") }

        btnSaveTask.setOnClickListener { saveTask() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Populate assignee chips only when the members list changes
                    if (state.members != lastMembersRendered) {
                        lastMembersRendered = state.members
                        buildAssigneeChips(state.members, state.selectedAssigneeId)
                    }
                    // Sync chip selection if the selected assignee changed (e.g. task loaded)
                    if (state.selectedAssigneeId.isNotEmpty()) {
                        val chip = chipGroupAssignee.findViewWithTag<Chip>(state.selectedAssigneeId)
                        if (chip != null && chipGroupAssignee.checkedChipId != chip.id) {
                            chipGroupAssignee.check(chip.id)
                        }
                    }

                    state.task?.let { task ->
                        if (etTitle.text.isNullOrEmpty()) etTitle.setText(task.titulo)
                        if (etDescription.text.isNullOrEmpty()) etDescription.setText(task.descripcion)

                        val chipId = when (task.prioridad) {
                            "ALTA" -> R.id.chipHigh
                            "BAJA" -> R.id.chipLow
                            else -> R.id.chipMedium
                        }
                        chipGroupPriority.check(chipId)

                        task.fechaLimite?.let { ts ->
                            if (selectedDueDate == null) {
                                selectedDueDate = Calendar.getInstance().apply { timeInMillis = ts }
                                updateDueDateDisplay()
                            }
                        }
                    }

                    if (state.saved) {
                        Snackbar.make(requireView(), R.string.tasks_created_message, Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }

                    state.error?.let {
                        Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun buildAssigneeChips(members: List<MemberOption>, selectedId: String) {
        chipGroupAssignee.removeAllViews()
        members.forEach { member ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
                text = member.name
                tag = member.id
                isCheckable = true
                isChecked = member.id == selectedId
                chipCornerRadius = 20f * resources.displayMetrics.density
                setOnCheckedChangeListener { _, checked ->
                    if (checked) viewModel.setAssignee(member.id)
                }
            }
            chipGroupAssignee.addView(chip)
        }
    }

    private fun showDateTimePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                // Now pick time
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        selectedDueDate = cal
                        updateDueDateDisplay()
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDueDateDisplay() {
        selectedDueDate?.let { cal ->
            val fmt = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale("es", "ES"))
            tvDueDate.text = fmt.format(cal.time)
            tvDueDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
        }
    }

    private fun addChecklistItem(text: String) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_checklist_input, layoutChecklist, false)

        val etItem = itemView.findViewById<TextInputEditText>(R.id.etChecklistItem)
        val btnRemove = itemView.findViewById<ImageButton>(R.id.btnRemoveItem)

        etItem.setText(text)
        etItem.hint = getString(R.string.tasks_subtask_hint)

        btnRemove.setOnClickListener {
            layoutChecklist.removeView(itemView)
        }

        layoutChecklist.addView(itemView)
        etItem.requestFocus()
    }

    private fun saveTask() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            tilTitle.error = getString(R.string.tasks_title_required)
            return
        }
        tilTitle.error = null

        val description = etDescription.text?.toString()?.trim() ?: ""
        val priority = when (chipGroupPriority.checkedChipId) {
            R.id.chipHigh -> "ALTA"
            R.id.chipLow -> "BAJA"
            else -> "MEDIA"
        }
        val dueDate = selectedDueDate?.timeInMillis

        // Collect checklist items
        val checklistItems = mutableListOf<String>()
        for (i in 0 until layoutChecklist.childCount) {
            val child = layoutChecklist.getChildAt(i)
            val et = child.findViewById<TextInputEditText?>(R.id.etChecklistItem)
            val text = et?.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) checklistItems.add(text)
        }

        viewModel.saveTask(
            taskId = args.taskId,
            titulo = title,
            descripcion = description,
            prioridad = priority,
            fechaLimite = dueDate,
            checklistItems = checklistItems
        )
    }
}
