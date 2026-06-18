package nibm.mad.snapshop.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nibm.mad.snapshop.composables.BottomNavigationBar
import nibm.mad.snapshop.composables.BottomNavScaffold
import nibm.mad.snapshop.composables.ProductDetailsKey
import nibm.mad.snapshop.screens.auth.AuthSyncScreen
import nibm.mad.snapshop.screens.history.HistoryScreen

import nibm.mad.snapshop.screens.settings.SettingsScreen
import nibm.mad.snapshop.screens.main.MainScreen
import nibm.mad.snapshop.screens.permissions.CameraPermissionScreen
import nibm.mad.snapshop.screens.permissions.MediaPermissionScreen

@Composable
fun HomeScreen() {
    // Navigation 3 gives you direct ownership of the back stack via SnapshotStateList
    val backStack = rememberNavBackStack(BottomNavScaffold.Main)

    // Derived state to pass down to your custom bottom bar to keep track of the active tab
    val currentRoute = when (val currentKey = backStack.lastOrNull()) {
        is BottomNavScaffold -> currentKey.route
        is ProductDetailsKey -> BottomNavScaffold.Main.route // Sub-pages map visually to the Main tab
        else -> BottomNavScaffold.Main.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { routeStr ->
                    val targetKey = when (routeStr) {
                        BottomNavScaffold.Settings.route -> BottomNavScaffold.Settings
                        BottomNavScaffold.History.route -> BottomNavScaffold.History
                        else -> BottomNavScaffold.Main
                    }

                    // To change tabs cleanly without accumulating unnecessary historical trails:
                    backStack.clear()
                    backStack.add(targetKey)
                }
            )
        }
    ) { paddingValues ->
        // NavDisplay handles displaying the active state inside the scaffold area
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            onBack = {
                // System back press gesture interceptor
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            },
            entryProvider = entryProvider {
                // Main Core Screens
                entry<BottomNavScaffold.Main> {
                    MainScreen()
                }

                entry<BottomNavScaffold.Settings> {
                    SettingsScreen()
                }

                entry<BottomNavScaffold.History> {
                    HistoryScreen()
                }
            }
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}