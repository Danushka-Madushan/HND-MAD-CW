package nibm.mad.snapshop.composables

import nibm.mad.snapshop.R

sealed class BottomNavScaffold(val route: String, val iconRes: Int) {
    object Settings : BottomNavScaffold("settings", R.drawable.ic_settings)
    object Main : BottomNavScaffold("main", R.drawable.ic_center)
    object History : BottomNavScaffold("history", R.drawable.ic_history)
}