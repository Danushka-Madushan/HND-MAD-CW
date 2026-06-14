package nibm.mad.snapshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nibm.mad.snapshop.screens.permissions.presentation.MediaPermissionScreen
import nibm.mad.snapshop.ui.theme.SnapShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapShopTheme {
                MediaPermissionScreen({})
            }
        }
    }
}
