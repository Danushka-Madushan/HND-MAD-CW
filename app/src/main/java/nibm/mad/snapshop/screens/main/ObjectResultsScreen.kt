package nibm.mad.snapshop.screens.main

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import nibm.mad.snapshop.ui.theme.BrandBlue
import nibm.mad.snapshop.ui.theme.SnapShopTheme
import nibm.mad.snapshop.ui.theme.TextDark

@Composable
fun ObjectResultsScreen(croppedImageUriString: String) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    ObjectResultsContent(croppedImageUriString, isVisible)
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

    // Shared fade for header + button — graphicsLayer means layout space is always reserved,
    // so SpaceBetween won't jump as these animate in
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fade_alpha"
    )

    // Bouncy scale for the image card
    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header — layout space held; only visually fades
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Snapped",
                    color = TextDark,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .graphicsLayer { alpha = fadeAlpha }
                )
                Icon(
                    painter = painterResource(id = R.drawable.snap_icon),
                    contentDescription = "SnapIcon",
                    tint = BrandBlue.copy(alpha = 0.9f),
                    modifier = Modifier.size(64.dp)
                )
            }

            // 2. Image card — scale + fade via graphicsLayer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = croppedImageUriString.toUri(),
                    contentDescription = "Cropped Object",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Button(
                onClick = {  },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(34),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Search",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
            isVisible = true
        )
    }
}