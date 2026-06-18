package nibm.mad.snapshop

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nibm.mad.snapshop.data.NavRoutes
import nibm.mad.snapshop.screens.history.HistoryScreen
import nibm.mad.snapshop.screens.main.MainScreen
import nibm.mad.snapshop.screens.settings.SettingsScreen
import nibm.mad.snapshop.ui.theme.SnapShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        setContent {
            SnapShopTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    // Navigation 3 back stack owned at the activity level
    val backStack = rememberNavBackStack(NavRoutes.Main)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
        },
        entryProvider = entryProvider {
            entry<NavRoutes.Main> {
                MainScreen(onNavigate = { routeStr ->
                    val targetKey = when (routeStr) {
                        NavRoutes.Settings.route -> NavRoutes.Settings
                        NavRoutes.History.route -> NavRoutes.History
                        else -> NavRoutes.Main
                    }

                    backStack.clear()
                    backStack.add(targetKey)
                })
            }

            entry<NavRoutes.Settings> {
                SettingsScreen()
            }

            entry<NavRoutes.History> {
                HistoryScreen()
            }
        }
    )
}

@Preview
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
