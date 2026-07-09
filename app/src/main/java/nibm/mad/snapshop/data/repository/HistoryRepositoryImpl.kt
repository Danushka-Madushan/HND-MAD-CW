package nibm.mad.snapshop.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import nibm.mad.snapshop.data.local.HistoryDao
import nibm.mad.snapshop.domain.model.HistoryEntry
import nibm.mad.snapshop.domain.repository.HistoryRepository
import nibm.mad.snapshop.domain.repository.SettingsRepository

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao,
    private val settingsRepository: SettingsRepository
) : HistoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "HistoryRepository"

    override fun getAllHistory(): Flow<List<HistoryEntry>> {
        return historyDao.getAllHistory()
    }

    override suspend fun insertHistory(entry: HistoryEntry) {
        val newId = historyDao.insertHistory(entry).toInt()
        val entryWithId = if (entry.id == 0) entry.copy(id = newId) else entry
        
        if (settingsRepository.isSyncEnabled.first()) {
            syncEntryToFirestore(entryWithId)
        }
    }

    override suspend fun deleteHistory(entry: HistoryEntry) {
        historyDao.deleteHistory(entry)
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users")
                .document(user.uid)
                .collection("history")
                .document(entry.id.toString())
                .delete()
        }
    }

    override suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
        val user = auth.currentUser
        if (user != null) {
            val collection = firestore.collection("users")
                .document(user.uid)
                .collection("history")
            
            val snapshot = collection.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete()
            }
        }
    }

    override fun searchHistory(query: String): Flow<List<HistoryEntry>> {
        return historyDao.searchHistory(query)
    }

    override suspend fun syncPendingHistory() {
        if (!settingsRepository.isSyncEnabled.first()) return
        
        val unsynced = historyDao.getAllHistory().first().filter { !it.isSynced }
        unsynced.forEach { syncEntryToFirestore(it) }
    }

    private suspend fun syncEntryToFirestore(entry: HistoryEntry) {
        val user = auth.currentUser ?: run {
            Log.w(TAG, "Sync skipped: No authenticated user")
            return
        }
        
        try {
            Log.d(TAG, "Syncing entry ${entry.id} to Firestore")
            firestore.collection("users")
                .document(user.uid)
                .collection("history")
                .document(entry.id.toString())
                .set(entry)
                .await()
            
            historyDao.insertHistory(entry.copy(isSynced = true))
            Log.d(TAG, "Successfully synced entry ${entry.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync entry ${entry.id}", e)
        }
    }
}
