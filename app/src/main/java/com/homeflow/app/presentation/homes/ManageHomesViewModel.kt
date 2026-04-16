package com.homeflow.app.presentation.homes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeflow.app.data.local.dao.HomeDao
import com.homeflow.app.data.local.entity.HomeEntity
import com.homeflow.app.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ManageHomesUiState(
    val homes: List<HomeEntity> = emptyList(),
    val activeHomeId: String = "",
    val navigateToMain: Boolean = false
)

@HiltViewModel
class ManageHomesViewModel @Inject constructor(
    private val homeDao: HomeDao,
    private val session: SessionManager
) : ViewModel() {

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain.asStateFlow()

    val uiState: StateFlow<ManageHomesUiState> =
        homeDao.getHomesForUser("%${session.userId}%").map { homes ->
            ManageHomesUiState(
                homes = homes,
                activeHomeId = session.hogarId
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ManageHomesUiState(activeHomeId = session.hogarId)
        )

    fun switchHome(homeId: String) {
        session.hogarId = homeId
        _navigateToMain.value = true
    }

    fun onNavigatedToMain() {
        _navigateToMain.value = false
    }
}
