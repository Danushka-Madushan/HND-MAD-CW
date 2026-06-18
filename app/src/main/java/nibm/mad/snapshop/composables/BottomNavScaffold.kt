package nibm.mad.snapshop.composables

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import nibm.mad.snapshop.R

@Serializable
sealed class BottomNavScaffold(val route: String, val iconRes: Int) : NavKey {
    @Serializable
    data object Settings : BottomNavScaffold("settings", R.drawable.ic_settings)

    @Serializable
    data object Main : BottomNavScaffold("main", R.drawable.ic_center)

    @Serializable
    data object History : BottomNavScaffold("history", R.drawable.ic_history)
}

@Serializable
data class ProductDetailsKey(val productId: String) : NavKey