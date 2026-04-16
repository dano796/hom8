package com.homeflow.app.presentation.dashboard

import com.homeflow.app.data.local.entity.TaskEntity

data class DashboardUiState(
    val greeting: String = "",
    val userName: String = "",
    val userInitials: String = "",
    val todayTasks: List<TaskEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val activityFeed: List<ActivityItem> = emptyList(),
    val monthTotal: Double = 0.0,
    val youOweAmount: Double = 0.0,
    val oweLabel: String = "",
    val unreadNotifications: Int = 0,
    val isSplitMode: Boolean = true,
    val isLoading: Boolean = false
)

data class ActivityItem(
    val actorInitials: String,
    val actorColorIndex: Int,
    val text: String,
    val timeAgo: String,
    val dotColor: Int // color resource id
)
