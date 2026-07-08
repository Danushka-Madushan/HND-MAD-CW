package nibm.mad.snapshop

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.serialization.json.Json
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import nibm.mad.snapshop.data.NavRoutes
import nibm.mad.snapshop.models.ProductMatch
import nibm.mad.snapshop.screens.auth.AuthSyncScreen
import nibm.mad.snapshop.screens.history.HistoryScreen
import nibm.mad.snapshop.screens.main.MainScreen
import nibm.mad.snapshop.screens.main.ObjectResultsScreen
import nibm.mad.snapshop.screens.permissions.CameraPermissionScreen
import nibm.mad.snapshop.screens.permissions.MediaPermissionScreen
import nibm.mad.snapshop.screens.settings.SettingsScreen
import nibm.mad.snapshop.ui.theme.SnapShopTheme

import android.os.Build
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
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
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("snapshop_prefs", Context.MODE_PRIVATE) }
    var isFirstRun by remember { mutableStateOf(sharedPrefs.getBoolean("is_first_run", true)) }

    val initialRoute = if (isFirstRun) NavRoutes.CameraPermission else NavRoutes.Main
    val backStack = rememberNavBackStack(initialRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
        },
        entryProvider = entryProvider {
            entry<NavRoutes.Main> {
                MainScreen(onNavigate = { targetKey ->
                    if (targetKey is NavRoutes.Main) {
                        backStack.clear()
                    }
                    backStack.add(targetKey)
                })
            }

            entry<NavRoutes.CameraPermission> {
                CameraPermissionScreen(onAllowClicked = {
                    backStack.add(NavRoutes.MediaPermission)
                })
            }

            entry<NavRoutes.MediaPermission> {
                MediaPermissionScreen(onAllowClicked = {
                    backStack.add(NavRoutes.GoogleAuth)
                })
            }

            entry<NavRoutes.GoogleAuth> {
                AuthSyncScreen(onAllowClicked = {
                    sharedPrefs.edit { putBoolean("is_first_run", false) }
                    isFirstRun = false
                    backStack.clear()
                    backStack.add(NavRoutes.Main)
                })
            }

            entry<NavRoutes.Settings> {
                SettingsScreen()
            }

            entry<NavRoutes.History> {
                HistoryScreen(onHistoryItemClick = { entry, matches ->
                    backStack.add(
                        NavRoutes.ObjectResults(
                            uri = entry.imageUrl,
                            headerText = entry.productName,
                            resultsJson = Json.encodeToString(matches)
                        )
                    )
                })
            }

            entry<NavRoutes.ObjectResults> { key ->
                ObjectResultsScreen(
                    croppedImageUriString = key.uri,
                    onBackClick = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    initialHeaderText = key.headerText,
                    initialResults = key.resultsJson?.let {
                        try {
                            Json.decodeFromString<List<ProductMatch>>(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
