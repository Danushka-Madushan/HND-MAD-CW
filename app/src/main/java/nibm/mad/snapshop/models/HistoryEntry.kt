package nibm.mad.snapshop.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
class HistoryEntry(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var productName: String = "",
    var imageUrl: String = "",
    var timestamp: Long = 0L,
    var resultsJson: String = ""
)
