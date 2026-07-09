package nibm.mad.snapshop.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nibm.mad.snapshop.domain.repository.AuthRepository
import nibm.mad.snapshop.domain.repository.HistoryRepository
import nibm.mad.snapshop.domain.repository.SettingsRepository

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val isProductSearchEnabled: StateFlow<Boolean> = settingsRepository.isProductSearchEnabled
    val isSearchCacheEnabled: StateFlow<Boolean> = settingsRepository.isSearchCacheEnabled
    val isSyncEnabled: StateFlow<Boolean> = settingsRepository.isSyncEnabled

    private val _syncInProgress = MutableStateFlow(false)
    val syncInProgress: StateFlow<Boolean> = _syncInProgress.asStateFlow()

    fun toggleProductSearch(enabled: Boolean) {
        settingsRepository.setProductSearchEnabled(enabled)
    }

    fun toggleSearchCache(enabled: Boolean) {
        settingsRepository.setSearchCacheEnabled(enabled)
    }

    fun toggleSync(enabled: Boolean) {
        settingsRepository.setSyncEnabled(enabled)
        if (enabled) {
            syncHistory()
        }
    }

    fun syncHistory() {
        viewModelScope.launch {
            _syncInProgress.value = true
            historyRepository.syncPendingHistory()
            _syncInProgress.value = false
        }
    }

    fun signIn(context: Context) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(context)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
