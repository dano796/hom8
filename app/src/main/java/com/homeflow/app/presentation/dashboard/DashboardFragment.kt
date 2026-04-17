package com.homeflow.app.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.homeflow.app.R
import com.homeflow.app.presentation.dashboard.adapters.ActivityFeedAdapter
import com.homeflow.app.presentation.dashboard.adapters.TodayTasksAdapter
import com.homeflow.app.presentation.dashboard.adapters.UpcomingTasksAdapter
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var todayAdapter: TodayTasksAdapter
    private lateinit var upcomingAdapter: UpcomingTasksAdapter
    private lateinit var activityAdapter: ActivityFeedAdapter

    // Views
    private lateinit var tvGreeting: TextView
    private lateinit var tvUserName: TextView
    private lateinit var btnNotifications: ImageButton
    private lateinit var rvTodayTasks: RecyclerView
    private lateinit var rvUpcomingTasks: RecyclerView
    private lateinit var rvActivity: RecyclerView
    private lateinit var tvMonthTotal: TextView
    private lateinit var tvOweLabel: TextView
    private lateinit var tvOweAmount: TextView
    private lateinit var cardYouOwe: MaterialCardView
    private lateinit var tvSeeAllTasks: TextView
    private lateinit var tvSeeAllUpcoming: TextView
    private lateinit var fabDashboard: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupAdapters()
        setupClickListeners()
        observeState()
    }

    private fun bindViews(view: View) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvUserName = view.findViewById(R.id.tvUserName)
        btnNotifications = view.findViewById(R.id.btnNotifications)
        rvTodayTasks = view.findViewById(R.id.rvTodayTasks)
        rvUpcomingTasks = view.findViewById(R.id.rvUpcomingTasks)
        rvActivity = view.findViewById(R.id.rvActivity)
        tvMonthTotal = view.findViewById(R.id.tvMonthTotal)
        tvOweLabel = view.findViewById(R.id.tvOweLabel)
        tvOweAmount = view.findViewById(R.id.tvOweAmount)
        cardYouOwe = view.findViewById(R.id.cardYouOwe)
        tvSeeAllTasks = view.findViewById(R.id.tvSeeAllTasks)
        tvSeeAllUpcoming = view.findViewById(R.id.tvSeeAllUpcoming)
        fabDashboard = view.findViewById(R.id.fabDashboard)
    }

    private fun setupAdapters() {
        todayAdapter = TodayTasksAdapter(
            onToggleDone = { viewModel.toggleTaskDone(it) },
            onItemClick = { task ->
                val action = DashboardFragmentDirections
                    .actionDashboardToTaskDetail(task.id)
                findNavController().navigate(action)
            }
        )
        upcomingAdapter = UpcomingTasksAdapter(
            userInitials = "U",
            onItemClick = { task ->
                val action = DashboardFragmentDirections
                    .actionDashboardToTaskDetail(task.id)
                findNavController().navigate(action)
            }
        )
        activityAdapter = ActivityFeedAdapter()

        rvTodayTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todayAdapter
            isNestedScrollingEnabled = false
        }
        rvUpcomingTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingAdapter
            isNestedScrollingEnabled = false
        }
        rvActivity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activityAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        fabDashboard.setOnClickListener {
            val action = DashboardFragmentDirections
                .actionDashboardToCreateTask(null)
            findNavController().navigate(action)
        }

        btnNotifications.setOnClickListener {
            // Notifications screen to be added in future phase
        }

        tvSeeAllTasks.setOnClickListener {
            // Navigate to tasks tab
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_nav
            )?.selectedItemId = R.id.tasksListFragment
        }

        tvSeeAllUpcoming.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_nav
            )?.selectedItemId = R.id.calendarFragment
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    applyState(state)
                }
            }
        }
    }

    private fun applyState(state: DashboardUiState) {
        // Header
        tvGreeting.text = state.greeting
        tvUserName.text = "${state.userName} \uD83D\uDC4B"

        // Today tasks
        todayAdapter.submitList(state.todayTasks)

        // Upcoming tasks (update adapter with fresh initials if changed)
        if (upcomingAdapter.userInitials != state.userInitials) {
            upcomingAdapter = UpcomingTasksAdapter(
                userInitials = state.userInitials,
                onItemClick = { task ->
                    val action = DashboardFragmentDirections
                        .actionDashboardToTaskDetail(task.id)
                    findNavController().navigate(action)
                }
            )
            rvUpcomingTasks.adapter = upcomingAdapter
        }
        upcomingAdapter.submitList(state.upcomingTasks)

        // Expenses
        val fmt = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        tvMonthTotal.text = fmt.format(state.monthTotal)
        tvOweAmount.text = fmt.format(state.youOweAmount)
        tvOweLabel.text = state.oweLabel
        cardYouOwe.visibility = if (state.isSplitMode) View.VISIBLE else View.GONE

        // Activity feed
        activityAdapter.submitList(state.activityFeed)

        // Notification badge (simple approach: update content description)
        if (state.unreadNotifications > 0) {
            btnNotifications.contentDescription = "${state.unreadNotifications} notificaciones sin leer"
        }
    }
}
