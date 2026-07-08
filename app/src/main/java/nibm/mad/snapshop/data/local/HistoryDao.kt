package nibm.mad.snapshop.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import nibm.mad.snapshop.domain.model.HistoryEntry

@Dao
interface HistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Delete
    suspend fun deleteHistory(entry: HistoryEntry)

    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()
    
    @Query("SELECT * FROM search_history WHERE productName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntry>>
}
