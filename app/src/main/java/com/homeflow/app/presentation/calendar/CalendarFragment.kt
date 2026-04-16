package com.homeflow.app.presentation.calendar

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.homeflow.app.R
import com.homeflow.app.presentation.dashboard.adapters.UpcomingTasksAdapter
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private val viewModel: CalendarViewModel by viewModels()

    @Inject
    lateinit var session: SessionManager

    private lateinit var dayTasksAdapter: UpcomingTasksAdapter
    private val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFmt = SimpleDateFormat("MMMM d", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDayTasksRecycler(view)
        setupNavigation(view)
        setupViewModeChips(view)
        observeState(view)
    }

    private fun setupDayTasksRecycler(view: View) {
        dayTasksAdapter = UpcomingTasksAdapter(
            userInitials = session.userInitials.ifEmpty { "U" },
            onItemClick = { task ->
                val action = CalendarFragmentDirections.actionCalendarToTaskDetail(task.id)
                findNavController().navigate(action)
            }
        )
        view.findViewById<RecyclerView>(R.id.rvDayTasks).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dayTasksAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupNavigation(view: View) {
        view.findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener {
            if (viewModel.uiState.value.viewMode == CalendarViewMode.WEEKLY) {
                viewModel.previousWeek()
            } else {
                viewModel.previousMonth()
            }
        }
        view.findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener {
            if (viewModel.uiState.value.viewMode == CalendarViewMode.WEEKLY) {
                viewModel.nextWeek()
            } else {
                viewModel.nextMonth()
            }
        }
    }

    private fun setupViewModeChips(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupView)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val mode = when (group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)?.id) {
                R.id.chipWeekly -> CalendarViewMode.WEEKLY
                else -> CalendarViewMode.MONTHLY
            }
            viewModel.setViewMode(mode)
        }
    }

    private fun observeState(view: View) {
        val tvMonthYear = view.findViewById<TextView>(R.id.tvMonthYear)
        val calendarGrid = view.findViewById<GridLayout>(R.id.calendarGrid)
        val tvSelectedDay = view.findViewById<TextView>(R.id.tvSelectedDay)
        val tvDayTaskCount = view.findViewById<TextView>(R.id.tvDayTaskCount)
        val rvDayTasks = view.findViewById<RecyclerView>(R.id.rvDayTasks)
        val layoutDayEmpty = view.findViewById<LinearLayout>(R.id.layoutDayEmpty)
        val layoutMonthNav = view.findViewById<View>(R.id.layoutMonthNav)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isWeekly = state.viewMode == CalendarViewMode.WEEKLY

                    // Month header
                    val cal = Calendar.getInstance().apply {
                        set(state.displayedYear, state.displayedMonth, 1)
                    }
                    tvMonthYear.text = if (isWeekly) {
                        // Show week range: "Apr 7 – Apr 13"
                        val weekCal = Calendar.getInstance().apply {
                            set(state.displayedYear, state.displayedMonth, state.selectedDay)
                        }
                        val weekStart = weekCal.clone() as Calendar
                        val dow = (weekStart.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
                        weekStart.add(Calendar.DAY_OF_MONTH, -dow)
                        val weekEnd = weekStart.clone() as Calendar
                        weekEnd.add(Calendar.DAY_OF_MONTH, 6)
                        val rangeFmt = SimpleDateFormat("MMM d", Locale.getDefault())
                        "${rangeFmt.format(weekStart.time)} – ${rangeFmt.format(weekEnd.time)}"
                    } else {
                        monthFmt.format(cal.time)
                    }

                    // Show/hide prev/next nav in weekly mode (navigate by week)
                    layoutMonthNav.visibility = View.VISIBLE

                    // Build calendar grid
                    if (isWeekly) {
                        buildWeekGrid(
                            grid = calendarGrid,
                            year = state.displayedYear,
                            month = state.displayedMonth,
                            selectedDay = state.selectedDay,
                            daysWithTasks = state.daysWithTasks
                        )
                    } else {
                        buildCalendarGrid(
                            grid = calendarGrid,
                            year = state.displayedYear,
                            month = state.displayedMonth,
                            selectedDay = state.selectedDay,
                            daysWithTasks = state.daysWithTasks
                        )
                    }

                    // Selected day header
                    val selectedCal = Calendar.getInstance().apply {
                        set(state.displayedYear, state.displayedMonth, state.selectedDay)
                    }
                    tvSelectedDay.text = "Tasks for ${dayFmt.format(selectedCal.time)}"
                    val taskCount = state.tasksForSelectedDay.size
                    tvDayTaskCount.text = if (taskCount == 0) "" else "$taskCount task${if (taskCount != 1) "s" else ""}"

                    // Day tasks list
                    dayTasksAdapter.submitList(state.tasksForSelectedDay)
                    if (state.tasksForSelectedDay.isEmpty()) {
                        layoutDayEmpty.visibility = View.VISIBLE
                        rvDayTasks.visibility = View.GONE
                    } else {
                        layoutDayEmpty.visibility = View.GONE
                        rvDayTasks.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun buildCalendarGrid(
        grid: GridLayout,
        year: Int,
        month: Int,
        selectedDay: Int,
        daysWithTasks: Set<Int>
    ) {
        grid.removeAllViews()

        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        // Monday = 0, Sunday = 6 (shift from Calendar's Sunday=1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
        val todayDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else -1

        val cellSize = (resources.displayMetrics.widthPixels - 2 * resources.getDimensionPixelSize(R.dimen.spacing2)) / 7
        val mono = ResourcesCompat.getFont(requireContext(), R.font.jetbrains_mono)

        // Empty cells for first week
        repeat(firstDayOfWeek) {
            grid.addView(buildEmptyCell(cellSize))
        }

        // Day cells
        for (day in 1..daysInMonth) {
            grid.addView(buildDayCell(
                day = day,
                cellSize = cellSize,
                isSelected = day == selectedDay,
                isToday = day == todayDay,
                hasTask = daysWithTasks.contains(day),
                typeface = mono,
                onClick = { viewModel.selectDay(day) }
            ))
        }
    }

    private fun buildWeekGrid(
        grid: GridLayout,
        year: Int,
        month: Int,
        selectedDay: Int,
        daysWithTasks: Set<Int>
    ) {
        grid.removeAllViews()

        // Find the Monday of the week that contains selectedDay
        val anchorCal = Calendar.getInstance().apply { set(year, month, selectedDay) }
        val dowOffset = (anchorCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        val weekStartCal = anchorCal.clone() as Calendar
        weekStartCal.add(Calendar.DAY_OF_MONTH, -dowOffset)

        val today = Calendar.getInstance()
        val cellSize = (resources.displayMetrics.widthPixels - 2 * resources.getDimensionPixelSize(R.dimen.spacing2)) / 7
        val mono = ResourcesCompat.getFont(requireContext(), R.font.jetbrains_mono)

        for (i in 0..6) {
            val dayCal = weekStartCal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)
            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
            val dayMonth = dayCal.get(Calendar.MONTH)
            val dayYear = dayCal.get(Calendar.YEAR)

            val isSelected = dayNum == selectedDay && dayMonth == month && dayYear == year
            val isToday = dayNum == today.get(Calendar.DAY_OF_MONTH)
                && dayMonth == today.get(Calendar.MONTH)
                && dayYear == today.get(Calendar.YEAR)
            // Dots only make sense for same month tasks; use day-of-month as key
            val hasTask = if (dayMonth == month) daysWithTasks.contains(dayNum) else false

            grid.addView(buildDayCell(
                day = dayNum,
                cellSize = cellSize,
                isSelected = isSelected,
                isToday = isToday,
                hasTask = hasTask,
                typeface = mono,
                onClick = { viewModel.selectDay(dayNum) }
            ))
        }
    }

    private fun buildEmptyCell(size: Int): View {
        return View(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = size
                height = size
            }
        }
    }

    private fun buildDayCell(
        day: Int,
        cellSize: Int,
        isSelected: Boolean,
        isToday: Boolean,
        hasTask: Boolean,
        typeface: Typeface?,
        onClick: () -> Unit
    ): View {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
            }
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(context, android.R.drawable.list_selector_background)
            setOnClickListener { onClick() }
        }

        val tvDay = TextView(context).apply {
            text = day.toString()
            this.typeface = typeface
            textSize = 13f
            gravity = android.view.Gravity.CENTER
            when {
                isSelected -> {
                    setTextColor(ContextCompat.getColor(context, R.color.colorOnPrimary))
                    background = ContextCompat.getDrawable(context, R.drawable.bg_day_selected)
                    setTypeface(this.typeface, Typeface.BOLD)
                }
                isToday -> {
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    setTypeface(this.typeface, Typeface.BOLD)
                }
                else -> {
                    setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))
                }
            }
            val dp4 = resources.getDimensionPixelSize(R.dimen.spacing1)
            setPadding(dp4, dp4, dp4, dp4)
        }
        container.addView(tvDay)

        // Task dot
        if (hasTask) {
            val dot = View(context).apply {
                val dotSize = resources.getDimensionPixelSize(R.dimen.spacing1) + 2
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    topMargin = 2
                }
                background = ContextCompat.getDrawable(context, R.drawable.bg_task_dot)
            }
            container.addView(dot)
        }

        return container
    }
}
