package nibm.mad.snapshop.screens.main

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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nibm.mad.snapshop.R
import nibm.mad.snapshop.controllers.distillProductQuery
import nibm.mad.snapshop.controllers.searchImageWithSerpApi
import nibm.mad.snapshop.controllers.uploadImageToImgBB
import nibm.mad.snapshop.data.AppDatabase
import nibm.mad.snapshop.models.HistoryEntry
import nibm.mad.snapshop.models.ProductMatch
import nibm.mad.snapshop.screens.main.components.*
import nibm.mad.snapshop.ui.theme.BrandBlue
import nibm.mad.snapshop.ui.theme.SnapShopTheme
import nibm.mad.snapshop.ui.theme.TextDark

@Composable
fun ObjectResultsScreen(
    croppedImageUriString: String,
    onBackClick: () -> Unit = {},
    // Optional params for viewing from history
    initialHeaderText: String? = null,
    initialResults: List<ProductMatch>? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    ObjectResultsContent(
        croppedImageUriString = croppedImageUriString,
        isVisible = isVisible,
        onBackClick = onBackClick,
        initialHeaderText = initialHeaderText,
        initialResults = initialResults
    )
}

@Composable
fun ObjectResultsContent(
    croppedImageUriString: String,
    isVisible: Boolean,
    onBackClick: () -> Unit,
    initialHeaderText: String? = null,
    initialResults: List<ProductMatch>? = null
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    var currentStep       by remember { 
        mutableStateOf(if (initialResults != null) SearchStep.SUCCESS else SearchStep.IDLE) 
    }
    var headerText        by remember { mutableStateOf(initialHeaderText ?: "Snapped") }
    var productMatches     by remember { mutableStateOf<List<ProductMatch>>(initialResults ?: emptyList()) }
    var foundProductCount by remember { mutableIntStateOf(initialResults?.size ?: 0) }

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
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.link))
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
                            scope.launch {
                                currentStep       = SearchStep.UPLOADING
                                foundProductCount = 0

                                val directImageLink = uploadImageToImgBB(
                                    context,
                                    croppedImageUriString.toUri()
                                )

                                if (directImageLink != null) {
                                    currentStep = SearchStep.SEARCHING

                                    val productResults = searchImageWithSerpApi(
                                        imageUrl = directImageLink
                                    )

                                    if (productResults.isNotEmpty()) {
                                        foundProductCount = productResults.size
                                        currentStep       = SearchStep.DISTILLING

                                        val allTitles  = productResults.map { it.title }
                                        val top5Titles = allTitles.take(5)

                                        val finalDistilledQuery = distillProductQuery(top5Titles)

                                        if (finalDistilledQuery != null) {
                                            headerText     = finalDistilledQuery
                                            productMatches = productResults
                                            currentStep    = SearchStep.SUCCESS
                                            
                                            // Save to History
                                            val historyEntry = HistoryEntry(
                                                productName = finalDistilledQuery,
                                                imageUrl = croppedImageUriString,
                                                timestamp = System.currentTimeMillis(),
                                                resultsJson = Json.encodeToString(productResults)
                                            )
                                            database.historyDao().insertHistory(historyEntry)
                                            
                                        } else {
                                            currentStep = SearchStep.ERROR
                                        }
                                    } else {
                                        currentStep = SearchStep.ERROR
                                    }
                                } else {
                                    currentStep = SearchStep.ERROR
                                }
                            }
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

@Preview(showBackground = true)
@Composable
fun ObjectResultsScreenPreview() {
    SnapShopTheme {
        ObjectResultsContent(
            croppedImageUriString = "content://media/external/images/media/123",
            isVisible             = true,
            onBackClick = {}
        )
    }
}
