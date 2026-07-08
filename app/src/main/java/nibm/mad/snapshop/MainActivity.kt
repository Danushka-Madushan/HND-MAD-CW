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

import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import nibm.mad.snapshop.data.local.AppDatabase
import nibm.mad.snapshop.data.repository.HistoryRepositoryImpl
import nibm.mad.snapshop.data.repository.ProductRepositoryImpl
import nibm.mad.snapshop.domain.model.ProductMatch
import nibm.mad.snapshop.presentation.navigation.NavRoutes
import nibm.mad.snapshop.presentation.screens.auth.AuthSyncScreen
import nibm.mad.snapshop.presentation.screens.history.HistoryScreen
import nibm.mad.snapshop.presentation.screens.main.MainScreen
import nibm.mad.snapshop.presentation.screens.main.ObjectResultsScreen
import nibm.mad.snapshop.presentation.screens.permissions.CameraPermissionScreen
import nibm.mad.snapshop.presentation.screens.permissions.MediaPermissionScreen
import nibm.mad.snapshop.presentation.screens.settings.SettingsScreen
import nibm.mad.snapshop.presentation.theme.SnapShopTheme
import nibm.mad.snapshop.presentation.viewmodel.HistoryViewModel
import nibm.mad.snapshop.presentation.viewmodel.MainViewModel
import nibm.mad.snapshop.presentation.viewmodel.ObjectResultsViewModel

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

    // Manual DI
    val database = remember { AppDatabase.getDatabase(context) }
    val historyRepository = remember { HistoryRepositoryImpl(database.historyDao()) }
    val productRepository = remember { ProductRepositoryImpl() }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
        },
        entryProvider = entryProvider {
            entry<NavRoutes.Main> {
                val mainViewModel: MainViewModel = viewModel()
                MainScreen(
                    viewModel = mainViewModel,
                    onNavigate = { targetKey ->
                        if (targetKey is NavRoutes.Main) {
                            backStack.clear()
                        }
                        backStack.add(targetKey)
                    }
                )
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
                val historyViewModel = remember { HistoryViewModel(historyRepository) }
                HistoryScreen(
                    viewModel = historyViewModel,
                    onHistoryItemClick = { entry, matches ->
                        backStack.add(
                            NavRoutes.ObjectResults(
                                uri = entry.imageUrl,
                                headerText = entry.productName,
                                resultsJson = Json.encodeToString(matches)
                            )
                        )
                    }
                )
            }

            entry<NavRoutes.ObjectResults> { key ->
                val resultsViewModel = remember { 
                    ObjectResultsViewModel(productRepository, historyRepository) 
                }
                ObjectResultsScreen(
                    viewModel = resultsViewModel,
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
