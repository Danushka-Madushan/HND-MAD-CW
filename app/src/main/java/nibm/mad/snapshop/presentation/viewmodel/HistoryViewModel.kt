package nibm.mad.snapshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nibm.mad.snapshop.domain.model.HistoryEntry
import nibm.mad.snapshop.domain.repository.HistoryRepository

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyList: StateFlow<List<HistoryEntry>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                historyRepository.getAllHistory()
            } else {
                historyRepository.searchHistory(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshHistory()
    }

    fun refreshHistory() {
        viewModelScope.launch {
            historyRepository.syncPendingHistory()
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            historyRepository.deleteHistory(entry)
        }
    }
}
