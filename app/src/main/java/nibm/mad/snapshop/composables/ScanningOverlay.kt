package nibm.mad.snapshop.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import nibm.mad.snapshop.ui.theme.LightBlueBg

/**
 * Full-screen overlay shown while MLKit processes the captured image.
 * Mirrors the scanner frame layout so the UI doesn't shift, and drives
 * a cyan beam + pulse that makes the wait feel active rather than frozen.
 * All pointer events are consumed so the disabled buttons beneath can't
 * be accidentally triggered.
 */
@Composable
fun ScanningOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")

    // Scan beam travels top → bottom → top over 1.6 s
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    // Frame and label gently pulse to signal active processing
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            // Consume all touch events so nothing underneath fires
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Scanner frame + animated beam, sized to match the idle layout
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            ) {


                // Animated cyan scan beam drawn over the frame
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    val lineY = size.height * scanProgress

                    // Main beam — fades in from the edges for a softer look
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                LightBlueBg.copy(alpha = 0.7f),
                                LightBlueBg,
                                LightBlueBg.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Soft trailing glow that follows the beam downward
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LightBlueBg.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            startY = lineY,
                            endY = lineY + 52.dp.toPx()
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Identifying",
                color = Color.White.copy(alpha = pulseAlpha),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}