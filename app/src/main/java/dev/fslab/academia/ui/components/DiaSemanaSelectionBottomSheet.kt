package dev.fslab.academia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.ui.theme.LocalAcademiaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaSemanaSelectionBottomSheet(
    selecionados: Set<DiaSemana>,
    onConfirmar: (Set<DiaSemana>) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selecaoLocal by remember { mutableStateOf(selecionados) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Dias da semana",
                color = colors.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecione um ou mais dias",
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiaSemana.values().forEach { dia ->
                    val sel = dia in selecaoLocal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) colors.primary.copy(alpha = 0.10f) else colors.lightGray)
                            .clickable {
                                selecaoLocal = if (sel) selecaoLocal - dia else selecaoLocal + dia
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                dia.display,
                                color = colors.textPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                dia.curto,
                                color = colors.textSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Checkbox(
                            checked = sel,
                            onCheckedChange = {
                                selecaoLocal = if (sel) selecaoLocal - dia else selecaoLocal + dia
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primary,
                                uncheckedColor = colors.textSecondary,
                                checkmarkColor = colors.textOnPrimary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { selecaoLocal = emptySet() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Limpar", color = colors.textSecondary) }
                Button(
                    onClick = { onConfirmar(selecaoLocal) },
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    )
                ) {
                    Text(
                        if (selecaoLocal.isEmpty()) "Confirmar" else "Confirmar (${selecaoLocal.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
