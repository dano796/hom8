package com.hom8.app.presentation.tasks.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TasksListFragment : Fragment() {

    private val viewModel: TasksViewModel by viewModels()
    
    @Inject
    lateinit var session: com.hom8.app.util.SessionManager

    private lateinit var rvTasks: RecyclerView
    private lateinit var tvTaskCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var chipGroupWho: ChipGroup
    private lateinit var chipGroupPriority: ChipGroup
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var btnSortTasks: ImageButton
    private lateinit var tasksAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tasks_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupAdapter()
        setupListeners()
        observeState()
    }

    private fun bindViews(view: View) {
        rvTasks = view.findViewById(R.id.rvTasks)
        tvTaskCount = view.findViewById(R.id.tvTaskCount)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        chipGroupWho = view.findViewById(R.id.chipGroupWho)
        chipGroupPriority = view.findViewById(R.id.chipGroupPriority)
        etSearch = view.findViewById(R.id.etSearch)
        fabAddTask = view.findViewById(R.id.fabAddTask)
        btnSortTasks = view.findViewById(R.id.btnSortTasks)
    }

    private fun setupAdapter() {
        tasksAdapter = TasksAdapter(
            currentUserId = session.userId,
            onToggleDone = { task -> 
                val success = viewModel.toggleTaskDone(task)
                if (!success) {
                    Snackbar.make(
                        requireView(), 
                        "Solo el responsable de la tarea puede marcarla como completada", 
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            },
            onItemClick = { task ->
                val action = TasksListFragmentDirections
                    .actionTasksListToTaskDetail(task.id)
                findNavController().navigate(action)
            },
            onMenuClick = { task, anchor ->
                showTaskMenu(task, anchor)
            },
            onUnauthorizedToggle = { task ->
                Snackbar.make(
                    requireView(), 
                    "Solo el responsable de la tarea puede marcarla como completada", 
                    Snackbar.LENGTH_LONG
                ).show()
            }
        )
        rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }

    private fun setupListeners() {
        fabAddTask.setOnClickListener {
            val action = TasksListFragmentDirections
                .actionTasksListToCreateTask(null)
            findNavController().navigate(action)
        }

        btnSortTasks.setOnClickListener {
            viewModel.toggleSort()
        }

        etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        chipGroupWho.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipMyTasks -> WhoFilter.MINE
                else -> WhoFilter.ALL
            }
            viewModel.setWhoFilter(filter)
        }

        chipGroupPriority.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipPriorityHigh -> PriorityFilter.HIGH
                R.id.chipPriorityMedium -> PriorityFilter.MEDIUM
                R.id.chipPriorityLow -> PriorityFilter.LOW
                else -> PriorityFilter.ALL
            }
            viewModel.setPriorityFilter(filter)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val filtered = state.filteredTasks
                    tasksAdapter.submitList(filtered)
                    tvTaskCount.text = resources.getQuantityString(
                        R.plurals.tasks_found_plural, filtered.size, filtered.size
                    )
                    layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                    rvTasks.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

                    val sortActive = state.sortOrder != SortOrder.DEFAULT
                    btnSortTasks.imageTintList = if (sortActive)
                        ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
                    else
                        ContextCompat.getColorStateList(requireContext(), R.color.colorTextPrimary)
                }
            }
        }
    }

    private fun showTaskMenu(task: com.hom8.app.data.local.entity.TaskEntity, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.task_context_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEditTask -> {
                    val action = TasksListFragmentDirections
                        .actionTasksListToCreateTask(task.id)
                    findNavController().navigate(action)
                    true
                }
                R.id.menuDeleteTask -> {
                    viewModel.deleteTask(task)
                    Snackbar.make(requireView(), R.string.tasks_deleted_message, Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
