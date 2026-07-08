package nibm.mad.snapshop.data.repository

import kotlinx.coroutines.flow.Flow
import nibm.mad.snapshop.data.local.HistoryDao
import nibm.mad.snapshop.domain.model.HistoryEntry
import nibm.mad.snapshop.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<HistoryEntry>> {
        return historyDao.getAllHistory()
    }

    override suspend fun insertHistory(entry: HistoryEntry) {
        historyDao.insertHistory(entry)
    }

    override suspend fun deleteHistory(entry: HistoryEntry) {
        historyDao.deleteHistory(entry)
    }

    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }

    override fun searchHistory(query: String): Flow<List<HistoryEntry>> {
        return historyDao.searchHistory(query)
    }
}
