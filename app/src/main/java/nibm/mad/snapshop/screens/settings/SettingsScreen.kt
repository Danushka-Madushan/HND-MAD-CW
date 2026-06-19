package nibm.mad.snapshop.screens.settings

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            Text("Settings Screen")
        }
    }
}