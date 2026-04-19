package com.hom8.app.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hom8.app.data.local.dao.NotificationDao
import com.hom8.app.data.local.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao
        .getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }
}
