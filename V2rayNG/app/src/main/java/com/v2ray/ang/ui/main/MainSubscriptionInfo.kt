package com.v2ray.ang.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionUsageInfo
import com.v2ray.ang.extension.toTrafficString
import com.v2ray.ang.handler.SubscriptionInfoParser
import com.v2ray.ang.util.Utils

private const val MILLIS_PER_SECOND = 1000L
private const val EXPIRY_DATE_PATTERN = "yyyy-MM-dd"
private const val CARD_ALPHA = 0.97f

/** Locale-neutral values the subscription information box renders. */
internal data class SubscriptionInfoUiModel(
    val title: String?,
    val usedBytes: Long,
    /** Traffic quota in bytes; 0 means unlimited. */
    val totalBytes: Long,
    /** Used share of the quota in 0..1; null when the quota is unlimited. */
    val trafficFraction: Float?,
    /** Expiry in epoch milliseconds; null when the subscription never expires. */
    val expireMillis: Long?,
    val expired: Boolean,
    val supportUrl: String?,
    val webPageUrl: String?,
) {
    val trafficLow: Boolean get() = trafficFraction != null && trafficFraction >= TRAFFIC_WARNING_FRACTION

    companion object {
        const val TRAFFIC_WARNING_FRACTION = 0.9f
    }
}

internal fun buildSubscriptionInfoUiModel(
    info: SubscriptionUsageInfo,
    nowMillis: Long,
): SubscriptionInfoUiModel {
    val unlimited = info.total <= 0L
    val expireMillis = info.expire.takeIf { it > 0L }?.let { it * MILLIS_PER_SECOND }
    return SubscriptionInfoUiModel(
        title = info.title,
        usedBytes = info.used,
        totalBytes = if (unlimited) 0L else info.total,
        trafficFraction = if (unlimited) null else (info.used.toDouble() / info.total).toFloat().coerceIn(0f, 1f),
        expireMillis = expireMillis,
        expired = expireMillis != null && expireMillis <= nowMillis,
        supportUrl = info.supportUrl,
        webPageUrl = info.webPageUrl,
    )
}

/**
 * Fixed box on top of a subscription group showing the account information published by the
 * subscription panel: traffic, expiry date and shortcuts (support link, panel page, update).
 */

@Composable
internal fun SubscriptionInfoCard(
    info: SubscriptionUsageInfo,
    fallbackTitle: String,
    onOpenLink: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model = remember(info) {
        buildSubscriptionInfoUiModel(info, System.currentTimeMillis())
    }

    val cardShape = RoundedCornerShape(22.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val accentColor = when {
        model.expired -> MaterialTheme.colorScheme.error
        model.trafficLow -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = cardShape,
        color = cardColor.copy(alpha = CARD_ALPHA),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 10.dp,
                top = 10.dp,
                bottom = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "SUBSCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = model.title ?: fallbackTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                model.supportUrl?.let { url ->
                    CardIconButton(
                        iconRes = if (SubscriptionInfoParser.isTelegramLink(url)) {
                            R.drawable.ic_telegram_24dp
                        } else {
                            R.drawable.ic_feedback_24dp
                        },
                        contentDescription = stringResource(
                            R.string.sub_info_open_support
                        ),
                        onClick = { onOpenLink(url) }
                    )
                }

                model.webPageUrl?.let { url ->
                    CardIconButton(
                        iconRes = R.drawable.ic_about_24dp,
                        contentDescription = stringResource(
                            R.string.sub_info_open_page
                        ),
                        onClick = { onOpenLink(url) }
                    )
                }

                CardIconButton(
                    iconRes = R.drawable.ic_refresh_24dp,
                    contentDescription = stringResource(
                        R.string.title_sub_update
                    ),
                    onClick = onRefresh
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsRow(model)

                model.trafficFraction?.let { fraction ->
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}


@Composable
private fun CardIconButton(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(painterResource(iconRes), contentDescription, Modifier.size(20.dp))
    }
}

@Composable
private fun StatsRow(model: SubscriptionInfoUiModel) {
    val totalText = if (model.totalBytes > 0L) {
        model.totalBytes.toTrafficString()
    } else {
        stringResource(R.string.sub_info_unlimited)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.sub_info_traffic, model.usedBytes.toTrafficString(), totalText),
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.bodySmall,
            color = if (model.trafficLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        model.expireMillis?.let { expireMillis ->
            Spacer(Modifier.size(8.dp))
            Text(
                text = Utils.formatTimestamp(expireMillis, EXPIRY_DATE_PATTERN),
                style = MaterialTheme.typography.bodySmall,
                color = if (model.expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
