package nibm.mad.snapshop.screens.main

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import nibm.mad.snapshop.R
import nibm.mad.snapshop.controllers.distillProductQuery
import nibm.mad.snapshop.controllers.searchImageWithSerpApi
import nibm.mad.snapshop.controllers.uploadImageToImgBB
import nibm.mad.snapshop.ui.theme.BrandBlue
import nibm.mad.snapshop.ui.theme.SnapShopTheme
import nibm.mad.snapshop.ui.theme.TextDark

// Pipeline step states
enum class SearchStep {
    IDLE, UPLOADING, SEARCHING, DISTILLING, SUCCESS, ERROR
}

// Ordered list of the 3 active pipeline steps
private val PIPELINE_STEPS = listOf(
    SearchStep.UPLOADING,
    SearchStep.SEARCHING,
    SearchStep.DISTILLING
)

// ─────────────────────────────────────────────────────────────
// Step progress tracker composable
// ─────────────────────────────────────────────────────────────
@Composable
private fun SearchStepTracker(currentStep: SearchStep, productCount: Int) {
    val currentIndex = PIPELINE_STEPS.indexOf(currentStep).coerceAtLeast(0)

    val stepLabels = listOf("Upload", "Search", "Analyze")
    val stepDescriptions = listOf(
        "Uploading your image securely...",
        "Scanning the web for matches...",
        if (productCount > 0) "Found $productCount products! Identifying..." else "Analyzing product details..."
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // Gives breathing room on the screen edges
    ) {

        // ── Step circles + connecting lines ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PIPELINE_STEPS.forEachIndexed { index, step ->
                val isCompleted = currentIndex > index
                val isActive   = currentStep == step

                // ENHANCEMENT: Animate layout bounds dynamically via Dp instead of scaling graphics layer.
                // This forces connecting lines to adapt cleanly without overlapping or cropping.
                val circleSize by animateDpAsState(
                    targetValue = if (isActive) 40.dp else 34.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "circle_size_$index"
                )

                val bgColor by animateColorAsState(
                    targetValue = when {
                        isCompleted -> BrandBlue
                        isActive    -> BrandBlue.copy(alpha = 0.12f)
                        else        -> Color(0xFFF0F0F0)
                    },
                    animationSpec = tween(400),
                    label = "circle_bg_$index"
                )

                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .background(color = bgColor, shape = CircleShape)
                        .then(
                            if (isActive)
                                Modifier.border(2.dp, BrandBlue, CircleShape)
                            else
                                Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCompleted -> {
                            // Checkmark ✓ for completed steps
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isActive -> {
                            // Spinner for the current step
                            CircularProgressIndicator(
                                color = BrandBlue,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            // Number for pending steps
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFFBDBDBD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Connecting line between circles
                if (index < PIPELINE_STEPS.size - 1) {
                    val lineColor by animateColorAsState(
                        targetValue = if (currentIndex > index) BrandBlue else Color(0xFFE0E0E0),
                        animationSpec = tween(500),
                        label = "line_color_$index"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        // ── Step labels below circles ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            stepLabels.forEachIndexed { index, label ->
                val labelColor by animateColorAsState(
                    targetValue = if (currentIndex >= index) BrandBlue else Color(0xFFBDBDBD),
                    animationSpec = tween(400),
                    label = "label_color_$index"
                )
                Text(
                    text  = label,
                    fontSize = 11.sp,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    fontWeight = if (currentStep == PIPELINE_STEPS[index])
                        FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Animated description that slides when the step changes ──
        AnimatedContent(
            targetState = stepDescriptions[currentIndex.coerceIn(0, stepDescriptions.lastIndex)],
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 3 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "step_description"
        ) { desc ->
            Text(
                text  = desc,
                color = TextDark.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────
@Composable
fun ObjectResultsScreen(croppedImageUriString: String) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    ObjectResultsContent(croppedImageUriString, isVisible)
}

// ─────────────────────────────────────────────────────────────
// Main content
// ─────────────────────────────────────────────────────────────
@Composable
fun ObjectResultsContent(
    croppedImageUriString: String,
    isVisible: Boolean
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var currentStep       by remember { mutableStateOf(SearchStep.IDLE) }
    var headerText        by remember { mutableStateOf("Snapped") }
    var productTitles     by remember { mutableStateOf<List<String>>(emptyList()) }
    var foundProductCount by remember { mutableIntStateOf(0) }

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
            .padding(24.dp)
            .animateContentSize(animationSpec = spring()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isSuccess) Arrangement.Top else Arrangement.SpaceBetween
    ) {

        // ═══════════════════════════
        // HEADER
        // ═══════════════════════════
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = if (isSuccess) 16.dp else 0.dp)
        ) {
            // Title animates from "Snapped" → product name on success
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
                        .padding(top = 16.dp)
                        .graphicsLayer { alpha = fadeAlpha }
                )
            }

            // Shutter icon disappears on success
            AnimatedVisibility(
                visible = !isSuccess,
                exit    = fadeOut(tween(200)) + shrinkVertically(tween(300))
            ) {
                Icon(
                    painter           = painterResource(id = R.drawable.snap_icon),
                    contentDescription = "SnapIcon",
                    tint              = BrandBlue.copy(alpha = 0.9f),
                    modifier          = Modifier.size(64.dp)
                )
            }
        }

        // ═══════════════════════════
        // IMAGE  (shrinks on success)
        // ═══════════════════════════
        Box(
            modifier = if (isSuccess) {
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
            )
        }

        // ═══════════════════════════
        // BOTTOM SECTION
        // ═══════════════════════════
        if (isSuccess) {

            // ── Results list ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text       = "${productTitles.size} related products found",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = BrandBlue,
                    modifier   = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(productTitles) { title ->
                        Card(
                            colors   = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape    = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text     = title,
                                modifier = Modifier.padding(16.dp),
                                color    = TextDark,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

        } else {

            // ── Loading tracker + button ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(bottom = 24.dp)
            ) {

                // 3-step progress tracker (visible while pipeline is running)
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

                // Error message (visible only on ERROR)
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

                // Search / Retry button
                Button(
                    onClick = {
                        scope.launch {
                            currentStep       = SearchStep.UPLOADING
                            foundProductCount = 0

                            // ── Step 1: Upload image ──
                            val directImageLink = uploadImageToImgBB(
                                context,
                                croppedImageUriString.toUri()
                            )

                            if (directImageLink != null) {
                                currentStep = SearchStep.SEARCHING

                                // ── Step 2: Search with SerpApi ──
                                val productResults = searchImageWithSerpApi(
                                    imageUrl = directImageLink
                                )

                                if (productResults.isNotEmpty()) {
                                    // Reveal count before the next step so the description updates
                                    foundProductCount = productResults.size
                                    currentStep       = SearchStep.DISTILLING

                                    val allTitles  = productResults.map { it.title }
                                    val top5Titles = allTitles.take(5)

                                    // ── Step 3: Distill with Gemini ──
                                    val finalDistilledQuery = distillProductQuery(top5Titles)

                                    if (finalDistilledQuery != null) {
                                        headerText    = finalDistilledQuery
                                        productTitles = allTitles
                                        currentStep   = SearchStep.SUCCESS
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
                    // Button label animates between states
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

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun ObjectResultsScreenPreview() {
    SnapShopTheme {
        ObjectResultsContent(
            croppedImageUriString = "content://media/external/images/media/123",
            isVisible             = true
        )
    }
}