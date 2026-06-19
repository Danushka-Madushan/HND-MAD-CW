package nibm.mad.snapshop.screens.main

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.core.net.toUri
import nibm.mad.snapshop.ui.theme.SnapShopTheme
import nibm.mad.snapshop.ui.theme.TextDark

@Composable
fun ObjectResultsScreen(croppedImageUriString: String) {
    // Controls the trigger for the entry animation
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true // Trigger animation immediately upon entering the screen
    }

    ObjectResultsContent(
        croppedImageUriString = croppedImageUriString,
        isVisible = isVisible
    )
}

@Composable
fun ObjectResultsContent(
    croppedImageUriString: String,
    isVisible: Boolean
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                // Bouncy spring effect akin to Google Lens snapping onto an object
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(initialAlpha = 0f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f) // Keep it proportional, or let it wrap content
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                // Coil's AsyncImage handles loading the local file Uri smoothly
                AsyncImage(
                    model = croppedImageUriString.toUri(),
                    contentDescription = "Cropped Object",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }

        // Placeholder for future action buttons
        if (isVisible) {
            Text(
                text = "Ready for processing...",
                color = TextDark.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ObjectResultsScreenPreview() {
    SnapShopTheme {
        ObjectResultsContent(
            croppedImageUriString = "content://media/external/images/media/123",
            isVisible = true
        )
    }
}
