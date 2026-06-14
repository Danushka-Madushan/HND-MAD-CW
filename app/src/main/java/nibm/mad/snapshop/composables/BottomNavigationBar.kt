package nibm.mad.snapshop.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nibm.mad.snapshop.ui.theme.BrandBlue
import nibm.mad.snapshop.ui.theme.TextSecondary
import nibm.mad.snapshop.ui.theme.White

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavScaffold.Settings,
        BottomNavScaffold.Main,
        BottomNavScaffold.History
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), // Elevated top curves from Figma
        color = White,
        shadowElevation = 16.dp // Generates the subtle top shadow gradient
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // Standard comfortable navigation height
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route

                // Smooth color transitions for active selections
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) BrandBlue else Color.Transparent,
                    label = "ColorAnimation"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) White else TextSecondary,
                    label = "IconColorAnimation"
                )

                // Clickable structural item container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Removes default rectangular splash overlay
                        ) {
                            if (!isSelected) onNavigate(screen.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // The dynamic selection circle background
                    Box(
                        modifier = Modifier
                            .size(56.dp) // Perfect proportions for the centered vector icon
                            .background(color = backgroundColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = screen.iconRes),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp) // Scales vector drawables evenly
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(currentRoute = "main", onNavigate = {})
}