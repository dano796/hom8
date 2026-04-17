package com.hom8.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.data.local.dao.TaskDao
import com.hom8.app.data.local.entity.TaskEntity
import com.hom8.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

enum class CalendarViewMode { MONTHLY, WEEKLY }

data class CalendarUiState(
    val displayedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val displayedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTHLY,
    val tasksForMonth: List<TaskEntity> = emptyList(),
    val tasksForSelectedDay: List<TaskEntity> = emptyList(),
    /** Set of day-of-month numbers that have tasks */
    val daysWithTasks: Set<Int> = emptySet()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val session: SessionManager
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        Triple(
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    )
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTHLY)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = _viewState.flatMapLatest { (year, month, selectedDay) ->
        val hogarId = session.hogarId.ifEmpty { "default" }
        val cal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val startOfMonth = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        val endOfMonth = cal.timeInMillis

        taskDao.getTasksByDateRange(hogarId, startOfMonth, endOfMonth).combine(
            taskDao.getTasksForDay(hogarId, dayStart(year, month, selectedDay), dayEnd(year, month, selectedDay))
        ) { monthTasks, dayTasks ->
            val daysWithTasks = monthTasks
                .mapNotNull { task -> task.fechaLimite?.let { ts ->
                    Calendar.getInstance().apply { timeInMillis = ts }.get(Calendar.DAY_OF_MONTH)
                }}
                .toSet()

            CalendarUiState(
                displayedYear = year,
                displayedMonth = month,
                selectedDay = selectedDay,
                viewMode = _viewMode.value,
                tasksForMonth = monthTasks,
                tasksForSelectedDay = dayTasks,
                daysWithTasks = daysWithTasks
            )
        }
    }.combine(_viewMode) { state, mode ->
        state.copy(viewMode = mode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState()
    )

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.update { mode }
    }

    fun previousMonth() {
        _viewState.update { (y, m, _) ->
            if (m == 0) Triple(y - 1, 11, 1)
            else Triple(y, m - 1, 1)
        }
    }

    fun nextMonth() {
        _viewState.update { (y, m, _) ->
            if (m == 11) Triple(y + 1, 0, 1)
            else Triple(y, m + 1, 1)
        }
    }

    fun previousWeek() {
        _viewState.update { (y, m, d) ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            cal.add(Calendar.WEEK_OF_YEAR, -1)
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    fun nextWeek() {
        _viewState.update { (y, m, d) ->
            val cal = Calendar.getInstance().apply { set(y, m, d) }
            cal.add(Calendar.WEEK_OF_YEAR, 1)
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    fun selectDay(day: Int) {
        _viewState.update { (y, m, _) -> Triple(y, m, day) }
    }

    private fun dayStart(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun dayEnd(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
}
