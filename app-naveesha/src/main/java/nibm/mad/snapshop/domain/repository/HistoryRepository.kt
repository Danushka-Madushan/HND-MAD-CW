package nibm.mad.snapshop.domain.repository

import kotlinx.coroutines.flow.Flow
import nibm.mad.snapshop.domain.model.HistoryEntry

interface HistoryRepository {
    fun getAllHistory(): Flow<List<HistoryEntry>>
    suspend fun insertHistory(entry: HistoryEntry)
    suspend fun deleteHistory(entry: HistoryEntry)
    suspend fun clearAllHistory()
    fun searchHistory(query: String): Flow<List<HistoryEntry>>
    suspend fun syncPendingHistory()
    suspend fun restoreHistoryFromFirestore()
}
