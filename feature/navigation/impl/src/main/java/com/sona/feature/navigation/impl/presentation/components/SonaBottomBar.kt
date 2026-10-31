package com.sona.feature.navigation.impl.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sona.feature.favorites.api.FavoritesDestination
import com.sona.feature.home.api.HomeDestination
import com.sona.feature.profile.api.ProfileDestination
import com.sona.feature.searchmusic.api.SearchDestination

private data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val items = listOf(
    BottomBarItem(HomeDestination.ROUTE, "Home", Icons.Rounded.Home),
    BottomBarItem(SearchDestination.ROUTE, "Search", Icons.Rounded.Search),
    BottomBarItem(FavoritesDestination.ROUTE, "Liked", Icons.Rounded.Favorite),
    BottomBarItem(ProfileDestination.ROUTE, "Profile", Icons.Rounded.Person),
)

@Composable
fun SonaBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 14.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tint",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Transparent,
                        )
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                    )
                }
            }
        }
    }
}
