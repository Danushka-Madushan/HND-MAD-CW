package nibm.mad.snapshop.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isProductSearchEnabled: StateFlow<Boolean>
    val isSearchCacheEnabled: StateFlow<Boolean>
    val isSyncEnabled: StateFlow<Boolean>

    fun setProductSearchEnabled(enabled: Boolean)
    fun setSearchCacheEnabled(enabled: Boolean)
    fun setSyncEnabled(enabled: Boolean)
}
