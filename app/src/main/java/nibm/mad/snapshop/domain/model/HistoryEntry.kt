package nibm.mad.snapshop.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var productName: String = "",
    var imageUrl: String = "",
    var timestamp: Long = 0L,
    var resultsJson: String = "",
    var isSynced: Boolean = false
)
