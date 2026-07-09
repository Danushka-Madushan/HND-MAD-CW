package nibm.mad.snapshop.presentation.screens.history

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json
import nibm.mad.snapshop.R
import nibm.mad.snapshop.domain.model.HistoryEntry
import nibm.mad.snapshop.domain.model.ProductMatch
import nibm.mad.snapshop.presentation.components.HistoryItem
import nibm.mad.snapshop.presentation.theme.BrandBlue
import androidx.compose.ui.tooling.preview.Preview
import nibm.mad.snapshop.presentation.theme.SnapShopTheme
import nibm.mad.snapshop.presentation.theme.TextDark
import nibm.mad.snapshop.presentation.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onHistoryItemClick: (HistoryEntry, List<ProductMatch>) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Search Bar & Calendar Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = { 
                    Icon(
                        painter = painterResource(id = R.drawable.search), 
                        contentDescription = null, 
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F3F4),
                    unfocusedContainerColor = Color(0xFFF1F3F4),
                    disabledContainerColor = Color(0xFFF1F3F4),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            IconButton(
                onClick = { /* Calendar Filter logic if any */ },
                modifier = Modifier
                    .size(56.dp)
                    .background(BrandBlue, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = "Filter",
                    tint = Color.White
                )
            }
        }

        Text(
            text = "Recent",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "No search history yet" else "No results found",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(historyList) { entry ->
                    val productMatches = remember(entry.resultsJson) {
                        try {
                            Json.decodeFromString<List<ProductMatch>>(entry.resultsJson)
                        } catch (e: Exception) {
                            emptyList<ProductMatch>()
                        }
                    }

                    HistoryItem(
                        entry = entry,
                        onItemClick = { 
                            onHistoryItemClick(entry, productMatches) 
                        },
                        onShareClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this ${entry.productName} I found on SnapShop!")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        onViewOffersClick = {
                            onHistoryItemClick(entry, productMatches)
                        },
                        onDeleteClick = {
                            viewModel.deleteEntry(entry)
                        }
                    )
                }
            }
        }
    }
}
