package nibm.mad.snapshop.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nibm.mad.snapshop.presentation.theme.BrandBlue
import nibm.mad.snapshop.presentation.theme.TextDark

enum class SearchStep {
    IDLE, UPLOADING, SEARCHING, DISTILLING, SUCCESS, ERROR
}

val PIPELINE_STEPS = listOf(
    SearchStep.UPLOADING,
    SearchStep.SEARCHING,
    SearchStep.DISTILLING
)

@Composable
fun SearchStepTracker(currentStep: SearchStep, productCount: Int) {
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
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PIPELINE_STEPS.forEachIndexed { index, step ->
                val isCompleted = currentIndex > index
                val isActive   = currentStep == step

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
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isActive -> {
                            CircularProgressIndicator(
                                color = BrandBlue,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFFBDBDBD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

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
