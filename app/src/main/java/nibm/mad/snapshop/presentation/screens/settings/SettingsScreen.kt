package nibm.mad.snapshop.presentation.screens.settings

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import com.google.firebase.auth.FirebaseUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import nibm.mad.snapshop.presentation.theme.BrandBlue
import nibm.mad.snapshop.presentation.theme.SnapShopTheme
import nibm.mad.snapshop.presentation.theme.TextDark
import nibm.mad.snapshop.presentation.theme.TextSecondary
import nibm.mad.snapshop.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val isProductSearchEnabled by viewModel.isProductSearchEnabled.collectAsState()
    val isSearchCacheEnabled by viewModel.isSearchCacheEnabled.collectAsState()
    val isSyncEnabled by viewModel.isSyncEnabled.collectAsState()
    val syncInProgress by viewModel.syncInProgress.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Account Section
            Text(
                "Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = user?.displayName ?: "Signed In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Text(
                            text = user?.email ?: "",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = Color.Red)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.signIn(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign In with Google", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            // General Settings
            Text(
                "General",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            SettingSwitchItem(
                title = "Switch over to product search",
                checked = isProductSearchEnabled,
                onCheckedChange = { viewModel.toggleProductSearch(it) }
            )

            SettingSwitchItem(
                title = "Enable search cache",
                checked = isSearchCacheEnabled,
                onCheckedChange = { viewModel.toggleSearchCache(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            // Sync Section
            Text(
                "Sync",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            SettingSwitchItem(
                title = "Sync search history",
                checked = isSyncEnabled,
                onCheckedChange = { viewModel.toggleSync(it) },
                enabled = user != null
            )

            if (isSyncEnabled && user != null) {
                Button(
                    onClick = { viewModel.syncHistory() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = !syncInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (syncInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sync Now", color = Color.White)
                    }
                }
            }
            
            if (user == null) {
                Text(
                    "Sign in to enable cloud sync",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = if (enabled) TextDark else TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SnapShopTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text("Settings Screen Preview", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            SettingSwitchItem(
                title = "Switch over to product search",
                checked = true,
                onCheckedChange = {}
            )
            SettingSwitchItem(
                title = "Enable search cache",
                checked = false,
                onCheckedChange = {}
            )
            SettingSwitchItem(
                title = "Sync search history",
                checked = false,
                onCheckedChange = {},
                enabled = false
            )
        }
    }
}
