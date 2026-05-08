package dev.fslab.academia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.academia.ui.theme.LocalAcademiaColors

data class MaisMenuItem(
    val label: String,
    val descricao: String,
    val icone: ImageVector,
    val route: String
)

val maisMenuItems = listOf(
    MaisMenuItem("Exercícios", "Catálogo e gerenciamento completo", Icons.AutoMirrored.Filled.DirectionsRun, "exercicio_catalogo"),
    MaisMenuItem("Aparelhos", "Equipamentos disponíveis na academia", Icons.Filled.Build, "aparelhos"),
    MaisMenuItem("Perfil", "Suas informações e conta", Icons.Filled.Person, "perfil"),
    MaisMenuItem("Configurações", "Tema, notificações e preferências", Icons.Filled.Settings, "configuracoes"),
    MaisMenuItem("Notificações", "Avisos e atualizações", Icons.Filled.Notifications, "notificacoes"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaisMenuBottomSheet(
    onDismiss: () -> Unit,
    onNavegar: (String) -> Unit
) {
    val colors = LocalAcademiaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Menu",
                color = colors.textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            maisMenuItems.forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavegar(item.route); onDismiss() }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        item.icone,
                        contentDescription = item.label,
                        tint = colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.label,
                            color = colors.textPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            item.descricao,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (idx < maisMenuItems.lastIndex) {
                    HorizontalDivider(
                        color = colors.lightGray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
