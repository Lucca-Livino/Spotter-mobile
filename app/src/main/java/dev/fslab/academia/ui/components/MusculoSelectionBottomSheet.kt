package dev.fslab.academia.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.GrupoMuscular
import dev.fslab.academia.model.MusculoData
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.MusculoUiState
import dev.fslab.academia.ui.viewmodel.MusculoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusculoSelectionBottomSheet(
    selecionados: Set<String>,
    grupoMuscularInicial: GrupoMuscular? = null,
    onConfirmar: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onConfirmarComDados: ((List<MusculoData>) -> Unit)? = null,
    viewModel: MusculoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()

    var busca by remember { mutableStateOf("") }
    var grupoFiltro by remember { mutableStateOf(grupoMuscularInicial) }
    var selecaoLocal by remember { mutableStateOf(selecionados) }
    var dadosCache by remember { mutableStateOf<Map<String, MusculoData>>(emptyMap()) }

    LaunchedEffect(grupoFiltro, busca) {
        viewModel.carregar(
            grupoMuscular = grupoFiltro?.apiValue,
            nome = busca.takeIf(String::isNotBlank)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Selecionar músculos",
                color = colors.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar músculo") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textInput,
                    unfocusedTextColor = colors.textInput,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedContainerColor = colors.lightGray,
                    unfocusedContainerColor = colors.lightGray,
                    cursorColor = colors.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrupoMuscular.values().forEach { grupo ->
                    FilterChip(
                        selected = grupoFiltro == grupo,
                        onClick = {
                            grupoFiltro = if (grupoFiltro == grupo) null else grupo
                        },
                        label = { Text(grupo.display) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.lightGray,
                            labelColor = colors.textPrimary,
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.textOnPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 420.dp)
            ) {
                when (val state = uiState) {
                    MusculoUiState.Loading, MusculoUiState.Idle -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    is MusculoUiState.Error -> {
                        Text(state.message, color = colors.errorText, modifier = Modifier.padding(16.dp))
                    }
                    is MusculoUiState.Success -> {
                        LaunchedEffect(state.musculos) {
                            dadosCache = dadosCache + state.musculos.associateBy { it.id }
                        }
                        if (state.musculos.isEmpty()) {
                            Text(
                                "Nenhum músculo encontrado",
                                color = colors.textSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn {
                                items(state.musculos, key = { it.id }) { musculo ->
                                    MusculoListItem(
                                        musculo = musculo,
                                        selecionado = musculo.id in selecaoLocal,
                                        onToggle = {
                                            selecaoLocal = if (musculo.id in selecaoLocal) {
                                                selecaoLocal - musculo.id
                                            } else {
                                                selecaoLocal + musculo.id
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val dados = selecaoLocal.mapNotNull { dadosCache[it] }
                    onConfirmarComDados?.invoke(dados)
                    onConfirmar(selecaoLocal)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary
                )
            ) {
                Text("Confirmar (${selecaoLocal.size})", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MusculoListItem(
    musculo: MusculoData,
    selecionado: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                musculo.nome,
                color = colors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                musculo.grupoMuscular,
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingContent = {
            Checkbox(
                checked = selecionado,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    uncheckedColor = colors.textSecondary,
                    checkmarkColor = colors.textOnPrimary
                )
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selecionado) colors.primary.copy(alpha = 0.10f) else colors.surface
        )
    )
}
