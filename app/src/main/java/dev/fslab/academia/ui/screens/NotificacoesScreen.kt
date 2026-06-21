package dev.fslab.academia.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.academia.service.NotificacaoLocal
import dev.fslab.academia.service.NotificationInboxManager
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificacoesScreen(
    onBack: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val notificacoes by NotificationInboxManager.notifications.collectAsState()

    LaunchedEffect(Unit) {
        NotificationInboxManager.markAllRead()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Notificações",
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    if (notificacoes.isNotEmpty()) {
                        TextButton(onClick = { NotificationInboxManager.clear() }) {
                            Text("Limpar", color = colors.textSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notificacoes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.textSecondary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(dimens.spaceMd))
                    Text(
                        "Nenhuma notificação",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(dimens.spaceSm))
                    Text(
                        "As notificações recebidas aparecerão aqui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(notificacoes, key = { it.id }) { notif ->
                    NotificacaoItem(notif)
                    HorizontalDivider(
                        color = colors.inputBorder.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = dimens.screenPaddingH)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificacaoItem(notif: NotificacaoLocal) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val (icone, cor) = iconePorTipo(notif.tipo)
    val timestamp = formatTimestamp(notif.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notif.lida) colors.primary.copy(alpha = 0.04f) else colors.background)
            .padding(horizontal = dimens.screenPaddingH, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(cor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notif.titulo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!notif.lida) FontWeight.SemiBold else FontWeight.Normal,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (!notif.lida) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = notif.corpo,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun iconePorTipo(tipo: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    val colors = LocalAcademiaColors.current
    return when (tipo) {
        "NOVA_MENSAGEM", "SESSAO_FINALIZADA_TREINADOR" ->
            Icons.AutoMirrored.Filled.Chat to colors.primary
        "TREINO_ATRIBUIDO" ->
            Icons.Filled.FitnessCenter to colors.primary
        "TREINO_ATUALIZADO" ->
            Icons.Filled.Update to colors.featureOrange
        "TREINO_REMOVIDO" ->
            Icons.Filled.Cancel to colors.error
        "SESSAO_INICIADA" ->
            Icons.Filled.PlayArrow to colors.primary
        "SESSAO_FINALIZADA" ->
            Icons.Filled.CheckCircle to colors.primary
        "SESSAO_CANCELADA" ->
            Icons.Filled.Cancel to colors.error
        else ->
            Icons.Filled.Notifications to colors.textSecondary
    }
}

private fun formatTimestamp(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 60_000 -> "Agora"
        diff < 3_600_000 -> "${diff / 60_000} min atrás"
        diff < 86_400_000 -> "${diff / 3_600_000} h atrás"
        else -> SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date(ts))
    }
}
