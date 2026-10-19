package com.ftsdoc.mobile.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class BannerTone {
    Info,
    Success,
    Error,
}

@Composable
fun StatusBanner(
    text: String,
    tone: BannerTone,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val background = when (tone) {
        BannerTone.Error -> colors.errorContainer
        BannerTone.Success -> colors.secondaryContainer
        BannerTone.Info -> colors.primaryContainer
    }
    val foreground = when (tone) {
        BannerTone.Error -> colors.onErrorContainer
        BannerTone.Success -> colors.onSecondaryContainer
        BannerTone.Info -> colors.onPrimaryContainer
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = background,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = foreground,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
