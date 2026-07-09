package nibm.mad.snapshop.presentation.screens.main

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import nibm.mad.snapshop.R
import nibm.mad.snapshop.domain.model.ProductMatch
import nibm.mad.snapshop.presentation.components.*
import nibm.mad.snapshop.presentation.theme.BrandBlue
import nibm.mad.snapshop.presentation.theme.SnapShopTheme
import nibm.mad.snapshop.presentation.theme.TextDark
import nibm.mad.snapshop.presentation.viewmodel.ObjectResultsViewModel

@Composable
fun ObjectResultsScreen(
    viewModel: ObjectResultsViewModel,
    croppedImageUriString: String,
    onBackClick: () -> Unit = {},
    // Optional params for viewing from history
    initialHeaderText: String? = null,
    initialResults: List<ProductMatch>? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        isVisible = true 
        if (initialHeaderText != null && initialResults != null) {
            viewModel.initFromHistory(initialHeaderText, initialResults)
        }
    }
    
    ObjectResultsContent(
        viewModel = viewModel,
        croppedImageUriString = croppedImageUriString,
        isVisible = isVisible,
        onBackClick = onBackClick
    )
}

@Preview(showBackground = true)
@Composable
fun ObjectResultsScreenPreview() {
    SnapShopTheme {
        ObjectResultsContent(
            viewModel = ObjectResultsViewModel(
                productRepository = nibm.mad.snapshop.data.repository.ProductRepositoryImpl(),
                historyRepository = nibm.mad.snapshop.data.repository.HistoryRepositoryImpl(
                    historyDao = object : nibm.mad.snapshop.data.local.HistoryDao {
                        override fun getAllHistory() = kotlinx.coroutines.flow.flowOf(emptyList<nibm.mad.snapshop.domain.model.HistoryEntry>())
                        override suspend fun insertHistory(entry: nibm.mad.snapshop.domain.model.HistoryEntry) = 0L
                        override suspend fun deleteHistory(entry: nibm.mad.snapshop.domain.model.HistoryEntry) {}
                        override suspend fun clearAllHistory() {}
                        override fun searchHistory(query: String) = kotlinx.coroutines.flow.flowOf(emptyList<nibm.mad.snapshop.domain.model.HistoryEntry>())
                    },
                    settingsRepository = object : nibm.mad.snapshop.domain.repository.SettingsRepository {
                        override val isProductSearchEnabled = kotlinx.coroutines.flow.MutableStateFlow(false)
                        override val isSearchCacheEnabled = kotlinx.coroutines.flow.MutableStateFlow(false)
                        override val isSyncEnabled = kotlinx.coroutines.flow.MutableStateFlow(false)
                        override fun setProductSearchEnabled(enabled: Boolean) {}
                        override fun setSearchCacheEnabled(enabled: Boolean) {}
                        override fun setSyncEnabled(enabled: Boolean) {}
                    }
                )
            ).apply {
                initFromHistory(
                    headerText = "Sample Product",
                    results = listOf(
                        ProductMatch("Amazon Offer", "https://amazon.com", "Amazon", ""),
                        ProductMatch("eBay Offer", "https://ebay.com", "eBay", "")
                    )
                )
            },
            croppedImageUriString = "",
            isVisible = true,
            onBackClick = {}
        )
    }
}

@Composable
fun ObjectResultsContent(
    viewModel: ObjectResultsViewModel,
    croppedImageUriString: String,
    isVisible: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    val currentStep by viewModel.currentStep.collectAsState()
    val headerText by viewModel.headerText.collectAsState()
    val productMatches by viewModel.productMatches.collectAsState()
    val foundProductCount by viewModel.foundProductCount.collectAsState()

    LaunchedEffect(Unit) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
    }

    val fadeAlpha by animateFloatAsState(
        targetValue    = if (isVisible) 1f else 0f,
        animationSpec  = spring(stiffness = Spring.StiffnessLow),
        label          = "fade_alpha"
    )
    val cardScale by animateFloatAsState(
        targetValue   = if (isVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    val isSuccess = currentStep == SearchStep.SUCCESS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .animateContentSize(animationSpec = spring()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ═══════════════════════════
        // TOP BAR
        // ═══════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(BrandBlue.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = BrandBlue
                )
            }

            if (isSuccess) {
                IconButton(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out this $headerText I found on SnapShop!")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .background(BrandBlue.copy(alpha = 0.1f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = BrandBlue
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isSuccess) Arrangement.Top else Arrangement.SpaceBetween
        ) {
            // ═══════════════════════════
            // IMAGE SECTION
            // ═══════════════════════════
            Box(
                modifier = if (isSuccess) {
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                } else {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model              = croppedImageUriString.toUri(),
                    contentDescription = "Cropped Object",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            // ═══════════════════════════
            // HEADER TEXT
            // ═══════════════════════════
            AnimatedContent(
                targetState = headerText,
                transitionSpec = {
                    (fadeIn(tween(450)) + slideInVertically { -it / 3 })
                        .togetherWith(fadeOut(tween(300)))
                },
                label = "header_text"
            ) { text ->
                Text(
                    text       = text,
                    color      = TextDark,
                    fontSize   = if (isSuccess) 22.sp else 32.sp,
                    textAlign  = TextAlign.Center,
                    fontWeight = if (isSuccess) FontWeight.SemiBold else FontWeight.Light,
                    modifier   = Modifier
                        .padding(vertical = 16.dp)
                        .graphicsLayer { alpha = fadeAlpha }
                )
            }

            // ═══════════════════════════
            // BOTTOM SECTION
            // ═══════════════════════════
            if (isSuccess) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(productMatches) { product ->
                        ProductOfferItem(
                            product = product,
                            onViewOffersClick = {
                                val intent = Intent(Intent.ACTION_VIEW, product.link.toUri())
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(bottom = 24.dp)
                ) {
                    AnimatedVisibility(
                        visible = currentStep in PIPELINE_STEPS,
                        enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column(modifier = Modifier.padding(bottom = 28.dp)) {
                            SearchStepTracker(
                                currentStep  = currentStep,
                                productCount = foundProductCount
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = currentStep == SearchStep.ERROR,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text      = "Something went wrong. Please try again.",
                            color     = Color(0xFFE53935),
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.startSearch(context, croppedImageUriString)
                        },
                        enabled = currentStep == SearchStep.IDLE || currentStep == SearchStep.ERROR,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor         = BrandBlue,
                            disabledContainerColor = BrandBlue.copy(alpha = 0.55f),
                            disabledContentColor   = Color.White.copy(alpha = 0.8f)
                        ),
                        shape    = RoundedCornerShape(34),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        AnimatedContent(
                            targetState = when (currentStep) {
                                SearchStep.ERROR -> "Retry Search"
                                SearchStep.IDLE  -> "Search"
                                else             -> "Searching..."
                            },
                            transitionSpec = {
                                fadeIn(tween(200)).togetherWith(fadeOut(tween(200)))
                            },
                            label = "button_label"
                        ) { label ->
                            Text(
                                text       = label,
                                color      = Color.White,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
