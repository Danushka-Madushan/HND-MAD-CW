package nibm.mad.snapshop.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nibm.mad.snapshop.domain.repository.SettingsRepository

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("snapshop_settings", Context.MODE_PRIVATE)

    private val _isProductSearchEnabled = MutableStateFlow(sharedPrefs.getBoolean("product_search", true))
    override val isProductSearchEnabled: StateFlow<Boolean> = _isProductSearchEnabled

    private val _isSearchCacheEnabled = MutableStateFlow(sharedPrefs.getBoolean("search_cache", true))
    override val isSearchCacheEnabled: StateFlow<Boolean> = _isSearchCacheEnabled

    private val _isSyncEnabled = MutableStateFlow(sharedPrefs.getBoolean("sync_history", true))
    override val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled

    override fun setProductSearchEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("product_search", enabled).apply()
        _isProductSearchEnabled.value = enabled
    }

    override fun setSearchCacheEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("search_cache", enabled).apply()
        _isSearchCacheEnabled.value = enabled
    }

    override fun setSyncEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("sync_history", enabled).apply()
        _isSyncEnabled.value = enabled
    }
}
