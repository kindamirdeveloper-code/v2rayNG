
package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.verticalScrollbar

enum class MainDestination(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    Subscriptions(
        R.drawable.ic_subscriptions_24dp,
        R.string.title_sub_setting
    ),
    PerAppProxy(
        R.drawable.ic_per_apps_24dp,
        R.string.per_app_proxy_settings
    ),
    Routing(
        R.drawable.ic_routing_24dp,
        R.string.routing_settings_title
    ),
    Settings(
        R.drawable.ic_settings_24dp,
        R.string.title_settings
    )
}

private val drawerItems = listOf(
    MainDestination.Subscriptions,
    MainDestination.PerAppProxy,
    MainDestination.Routing,
    MainDestination.Settings
)

@Composable
fun MainDrawerContent(
    drawerState: DrawerState,
    onNavigate: (MainDestination) -> Unit
) {
    val drawerScrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = LocalDarkTheme.current

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier.fillMaxWidth(0.78f),
        drawerContainerColor = colors.surface,
        drawerShape = RoundedCornerShape(
            topEnd = 28.dp,
            bottomEnd = 28.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(drawerScrollState)
                .verticalScrollbar(drawerScrollState)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            colors.primary.copy(
                                alpha = if (isDarkTheme) 0.10f else 0.055f
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.10f),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(
                                        R.mipmap.ic_launcher_foreground
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(88.dp),
                                    colorFilter = if (isDarkTheme) {
                                        ColorFilter.tint(
                                            Color.White,
                                            BlendMode.SrcIn
                                        )
                                    } else {
                                        null
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.title_settings),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    start = 16.dp,
                    bottom = 8.dp
                )
            )

            drawerItems.forEach { item ->
                val interactionSource = remember {
                    MutableInteractionSource()
                }

                val isPressed by
                    interactionSource.collectIsPressedAsState()

                val itemScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.975f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "drawerItemScale"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isPressed) {
                        colors.primary
                    } else {
                        colors.onSurfaceVariant
                    },
                    animationSpec = tween(160),
                    label = "drawerIconColor"
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(item.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = {
                        onNavigate(item)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurface
                    ),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .graphicsLayer {
                            scaleX = itemScale
                            scaleY = itemScale
                        }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .clip(CircleShape)
                    .background(colors.outlineVariant.copy(alpha = 0.5f))
            ) {}

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
