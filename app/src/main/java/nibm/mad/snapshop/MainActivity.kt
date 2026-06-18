package nibm.mad.snapshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import nibm.mad.snapshop.screens.home.HomeScreen
import nibm.mad.snapshop.ui.theme.SnapShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnapShopTheme {
                HomeScreen()
            }
        }
    }
}
