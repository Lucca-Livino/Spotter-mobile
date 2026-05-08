package dev.fslab.academia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.EscopoExercicio
import dev.fslab.academia.model.ExercicioData
import dev.fslab.academia.model.GrupoMuscular
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.ExercicioFiltros
import dev.fslab.academia.ui.viewmodel.ExercicioListUiState
import dev.fslab.academia.ui.viewmodel.ExercicioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercicioPickerBottomSheet(
    idsExcluidos: Set<String> = emptySet(),
    autoSelecionarId: String? = null,
    onSelecionar: (ExercicioData) -> Unit,
    onCriarExercicio: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ExercicioViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()
    val filtros by viewModel.filtros.collectAsState()

    var busca by remember { mutableStateOf(filtros.busca) }
    var mostrarMapa by remember { mutableStateOf(false) }
    var menuGrupoAberto by remember { mutableStateOf(false) }

    LaunchedEffect(busca) {
        viewModel.atualizarFiltros(filtros.copy(busca = busca))
    }

    LaunchedEffect(uiState, autoSelecionarId) {
        val sucesso = uiState as? ExercicioListUiState.Success ?: return@LaunchedEffect
        if (autoSelecionarId == null) return@LaunchedEffect
        val achado = sucesso.exercicios.firstOrNull { it.id == autoSelecionarId }
        if (achado != null) onSelecionar(achado)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Selecionar exercício",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Escolha um existente ou crie um novo",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Button(
                    onClick = onCriarExercicio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Criar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar por nome") },
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

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Mapa corporal (visual)
                FilterChip(
                    selected = filtros.grupoMuscular != null || mostrarMapa,
                    onClick = { mostrarMapa = !mostrarMapa },
                    label = { Text(if (!mostrarMapa && filtros.grupoMuscular != null) filtros.grupoMuscular!!.display else "Mapa corporal") },
                    leadingIcon = { Icon(Icons.Filled.Map, null, modifier = Modifier.size(16.dp)) },
                    colors = chipColors()
                )
                // 2. Grupo muscular (dropdown texto)
                Box {
                    FilterChip(
                        selected = filtros.grupoMuscular != null,
                        onClick = { menuGrupoAberto = true },
                        label = { Text(filtros.grupoMuscular?.display ?: "Grupo muscular") },
                        leadingIcon = { Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(16.dp)) },
                        colors = chipColors()
                    )
                    DropdownMenu(
                        expanded = menuGrupoAberto,
                        onDismissRequest = { menuGrupoAberto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos os grupos") },
                            onClick = {
                                viewModel.atualizarFiltros(filtros.copy(grupoMuscular = null))
                                menuGrupoAberto = false
                            }
                        )
                        GrupoMuscular.values().forEach { grupo ->
                            DropdownMenuItem(
                                text = { Text(grupo.display) },
                                onClick = {
                                    viewModel.atualizarFiltros(filtros.copy(grupoMuscular = grupo))
                                    menuGrupoAberto = false
                                }
                            )
                        }
                    }
                }
                // 3. Pessoais
                FilterChip(
                    selected = filtros.escopo == EscopoExercicio.PESSOAL,
                    onClick = {
                        val novo = if (filtros.escopo == EscopoExercicio.PESSOAL)
                            EscopoExercicio.TODOS else EscopoExercicio.PESSOAL
                        viewModel.atualizarFiltros(filtros.copy(escopo = novo))
                    },
                    label = { Text("Pessoais") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, modifier = Modifier.size(16.dp)) },
                    colors = chipColors()
                )
                // 4. Globais
                FilterChip(
                    selected = filtros.escopo == EscopoExercicio.GLOBAL,
                    onClick = {
                        val novo = if (filtros.escopo == EscopoExercicio.GLOBAL)
                            EscopoExercicio.TODOS else EscopoExercicio.GLOBAL
                        viewModel.atualizarFiltros(filtros.copy(escopo = novo))
                    },
                    label = { Text("Globais") },
                    leadingIcon = { Icon(Icons.Filled.Public, null, modifier = Modifier.size(16.dp)) },
                    colors = chipColors()
                )
                // 5. Em uso
                FilterChip(
                    selected = filtros.emUso == true,
                    onClick = {
                        val proximo = when (filtros.emUso) {
                            null -> true
                            true -> false
                            false -> null
                        }
                        viewModel.atualizarFiltros(filtros.copy(emUso = proximo))
                    },
                    label = {
                        Text(if (filtros.emUso == false) "Sem treino" else "Em uso")
                    },
                    leadingIcon = { Icon(Icons.Filled.Bookmark, null, modifier = Modifier.size(16.dp)) },
                    colors = chipColors()
                )
            }

            if (mostrarMapa) {
                Spacer(Modifier.height(8.dp))
                MapaCorporal(
                    grupoSelecionado = filtros.grupoMuscular,
                    onGrupoSelecionado = { grupo ->
                        viewModel.atualizarFiltros(filtros.copy(grupoMuscular = grupo))
                        if (grupo != null) mostrarMapa = false
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 460.dp)
            ) {
                when (val state = uiState) {
                    ExercicioListUiState.Idle, ExercicioListUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = colors.primary) }
                    }
                    is ExercicioListUiState.Error -> {
                        Text(state.message, color = colors.error, modifier = Modifier.padding(16.dp))
                    }
                    ExercicioListUiState.Empty -> {
                        EstadoVazio(onCriarExercicio)
                    }
                    is ExercicioListUiState.Success -> {
                        val disponiveis = state.exercicios.filter { it.id !in idsExcluidos }
                        if (disponiveis.isEmpty()) {
                            EstadoTodosNoTreino()
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(disponiveis, key = { it.id }) { ex ->
                                    LinhaExercicio(ex) { onSelecionar(ex) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = LocalAcademiaColors.current.lightGray,
    labelColor = LocalAcademiaColors.current.textPrimary,
    iconColor = LocalAcademiaColors.current.textSecondary,
    selectedContainerColor = LocalAcademiaColors.current.primary,
    selectedLabelColor = LocalAcademiaColors.current.textOnPrimary,
    selectedLeadingIconColor = LocalAcademiaColors.current.textOnPrimary
)

@Composable
private fun EstadoVazio(onCriar: () -> Unit) {
    val colors = LocalAcademiaColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Nenhum exercício encontrado",
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Ajuste os filtros ou crie um novo exercício.",
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onCriar,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.textOnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.size(6.dp))
            Text("Criar exercício", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EstadoTodosNoTreino() {
    val colors = LocalAcademiaColors.current
    Text(
        "Todos os exercícios encontrados já estão no treino",
        color = colors.textSecondary,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun LinhaExercicio(exercicio: ExercicioData, onClick: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val resumo = exercicio.musculos
        .filter { it.tipoAtivacao == "PRIMARIO" }
        .joinToString(", ") { it.nome }
        .ifBlank { exercicio.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lightGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Bolt, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                exercicio.nome,
                color = colors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                resumo,
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Filled.ChevronRight, null, tint = colors.textSecondary)
    }
}
