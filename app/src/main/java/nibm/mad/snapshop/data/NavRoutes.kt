package nibm.mad.snapshop.data

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import nibm.mad.snapshop.R

@Serializable
sealed class NavRoutes(val route: String, val iconRes: Int) : NavKey {
    @Serializable
    data object Settings : NavRoutes("settings", R.drawable.ic_settings)

    @Serializable
    data object Main : NavRoutes("main", R.drawable.ic_launcher_foreground)

    @Serializable
    data object History : NavRoutes("history", R.drawable.ic_history)
}