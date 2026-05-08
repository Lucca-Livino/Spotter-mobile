package dev.fslab.academia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.AparelhoData
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.AparelhoUiState
import dev.fslab.academia.ui.viewmodel.AparelhoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AparelhoSelectionBottomSheet(
    selecionados: Set<String>,
    onConfirmar: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onConfirmarComDados: ((List<AparelhoData>) -> Unit)? = null,
    viewModel: AparelhoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()

    var busca by remember { mutableStateOf("") }
    var selecaoLocal by remember { mutableStateOf(selecionados) }
    var dadosCache by remember { mutableStateOf<Map<String, AparelhoData>>(emptyMap()) }

    LaunchedEffect(busca) {
        viewModel.carregar(nome = busca.takeIf(String::isNotBlank))
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
                text = "Selecionar aparelhos",
                color = colors.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar aparelho") },
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 420.dp)
            ) {
                when (val state = uiState) {
                    AparelhoUiState.Loading, AparelhoUiState.Idle -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    is AparelhoUiState.Error -> {
                        Text(state.message, color = colors.errorText, modifier = Modifier.padding(16.dp))
                    }
                    is AparelhoUiState.Success -> {
                        LaunchedEffect(state.aparelhos) {
                            dadosCache = dadosCache + state.aparelhos.associateBy { it.id }
                        }
                        if (state.aparelhos.isEmpty()) {
                            Text(
                                "Nenhum aparelho encontrado",
                                color = colors.textSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn {
                                items(state.aparelhos, key = { it.id }) { aparelho ->
                                    AparelhoListItem(
                                        aparelho = aparelho,
                                        selecionado = aparelho.id in selecaoLocal,
                                        onToggle = {
                                            selecaoLocal = if (aparelho.id in selecaoLocal) {
                                                selecaoLocal - aparelho.id
                                            } else {
                                                selecaoLocal + aparelho.id
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
private fun AparelhoListItem(
    aparelho: AparelhoData,
    selecionado: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        headlineContent = {
            Text(
                aparelho.nome,
                color = colors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = aparelho.descricao
            ?.takeIf { it.isNotBlank() }
            ?.let { desc ->
                @Composable {
                    Text(
                        desc,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
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
