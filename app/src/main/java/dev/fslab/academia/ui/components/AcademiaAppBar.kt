package dev.fslab.academia.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import dev.fslab.academia.ui.theme.LocalAcademiaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademiaAppBar(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = LocalAcademiaColors.current
    TopAppBar(
        title = {
            if (subtitle.isNullOrBlank()) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
            } else {
                androidx.compose.foundation.layout.Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.textPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textSecondary
        )
    )
}
