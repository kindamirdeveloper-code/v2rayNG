
package com.v2ray.ang.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.entities.ServersCache
import kotlinx.coroutines.flow.StateFlow

private val TAB_BAR_PADDING = 4.dp
private val TAB_SPACING = 3.dp
private val TAB_HEIGHT = 36.dp
private val SELECTED_TAB_MARGIN = 12.dp

@Composable
fun GroupTabBar(
    groups: List<GroupMapItem>,
    selectedTabIndex: Int,
    mainViewModel: MainViewModel,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (groups.isEmpty()) return

    val selectedIndex = selectedTabIndex.coerceIn(0, groups.lastIndex)
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = CircleShape,
        color = if (dark) {
            colors.surfaceContainerHigh
        } else {
            colors.surfaceContainerLow
        },
        border = BorderStroke(
            1.dp,
            colors.outlineVariant.copy(alpha = if (dark) 0.55f else 0.4f)
        ),
        shadowElevation = 2.dp
    ) {
        BoxWithConstraints {
            val density = LocalDensity.current
            val barPaddingPx = with(density) { TAB_BAR_PADDING.roundToPx() }
            val availablePx = constraints.maxWidth - 2 * barPaddingPx

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(TAB_BAR_PADDING)
            ) {
                EqualOrNaturalTabsLayout(availablePx = availablePx) {
                    groups.forEachIndexed { index, group ->
                        key(group.id) {
                            val serverFlow = remember(group.id, mainViewModel) {
                                mainViewModel.serversForGroup(group.id)
                            }

                            GroupTabItem(
                                group = group,
                                selected = index == selectedIndex,
                                serverFlow = serverFlow,
                                onClick = { onTabClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EqualOrNaturalTabsLayout(
    availablePx: Int,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, _ ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) {}

        val spacing = TAB_SPACING.roundToPx()
        val tabHeight = TAB_HEIGHT.roundToPx()
        val count = measurables.size
        val gaps = spacing * (count - 1)
        val natural = measurables.map { it.maxIntrinsicWidth(tabHeight) }
        val equalShare = ((availablePx - gaps) / count).coerceAtLeast(1)

        val widths = when {
            availablePx <= 0 -> natural
            natural.all { it <= equalShare } -> List(count) { equalShare }
            natural.sum() + gaps <= availablePx -> {
                val extra = (availablePx - gaps - natural.sum()) / count
                natural.map { it + extra }
            }
            else -> natural
        }

        val placeables = measurables.mapIndexed { i, measurable ->
            measurable.measure(
                Constraints.fixed(
                    widths[i].coerceAtLeast(1),
                    tabHeight
                )
            )
        }

        layout(widths.sum() + gaps, tabHeight) {
            var x = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x, 0)
                x += placeable.width + spacing
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupTabItem(
    group: GroupMapItem,
    selected: Boolean,
    serverFlow: StateFlow<List<ServersCache>>,
    onClick: () -> Unit
) {
    val servers by serverFlow.collectAsStateWithLifecycle()
    val count = if (group.id.isEmpty()) 0 else servers.size
    val colors = MaterialTheme.colorScheme
    val bringIntoView = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val marginPx = with(LocalDensity.current) { SELECTED_TAB_MARGIN.toPx() }

    val tabBackground by animateColorAsState(
        targetValue = if (selected) {
            colors.primary.copy(alpha = 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "groupTabBackground"
    )

    val tabTextColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurfaceVariant,
        animationSpec = tween(200),
        label = "groupTabTextColor"
    )

    val badgeBackground by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.45f),
        animationSpec = tween(200),
        label = "groupTabBadgeBackground"
    )

    val badgeTextColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.surface,
        animationSpec = tween(200),
        label = "groupTabBadgeTextColor"
    )

    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        animationSpec = tween(180),
        label = "groupTabScale"
    )

    LaunchedEffect(selected, size) {
        if (selected && size != IntSize.Zero) {
            bringIntoView.bringIntoView(
                Rect(-marginPx, 0f, size.width + marginPx, size.height.toFloat())
            )
        }
    }

    Row(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoView)
            .onSizeChanged { size = it }
            .clip(CircleShape)
            .background(tabBackground)
            .then(Modifier)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.remarks,
            modifier = Modifier,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = tabTextColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        if (count > 0) {
            Spacer(Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                    .clip(CircleShape)
                    .background(badgeBackground)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    ),
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
