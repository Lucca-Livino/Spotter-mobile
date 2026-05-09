package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinoFiltros
import dev.fslab.academia.ui.viewmodel.TreinoListUiState
import dev.fslab.academia.ui.viewmodel.TreinoReorderUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel

@Composable
fun TreinosScreen(
    onBack: () -> Unit,
    onNavigateTab: (String) -> Unit,
    onAbrirDetalhe: (String) -> Unit = {},
    onCriar: () -> Unit = {},
    viewModel: TreinoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val uiState by viewModel.uiState.collectAsState()
    val filtros by viewModel.filtros.collectAsState()
    val reorderState by viewModel.reorderState.collectAsState()

    var modoReorder by remember { mutableStateOf(false) }
    var ordemLocal by remember { mutableStateOf<List<TreinoData>>(emptyList()) }
    var mostrarMaisMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    LaunchedEffect(uiState) {
        if (uiState is TreinoListUiState.Success && modoReorder) {
            ordemLocal = (uiState as TreinoListUiState.Success).treinos
        }
    }

    LaunchedEffect(reorderState) {
        if (reorderState is TreinoReorderUiState.Success) {
            modoReorder = false
            ordemLocal = emptyList()
            viewModel.resetReorder()
            viewModel.carregar()
        }
    }

    val subtitulo = when {
        modoReorder -> "Reordenando treinos"
        uiState is TreinoListUiState.Success -> "${(uiState as TreinoListUiState.Success).total} treinos"
        uiState is TreinoListUiState.Empty -> "0 treinos"
        uiState is TreinoListUiState.Loading -> "Carregando…"
        else -> null
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Treinos",
                subtitle = subtitulo,
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    val sucesso = uiState as? TreinoListUiState.Success
                    val temItens = (sucesso?.treinos?.size ?: 0) >= 2
                    if (modoReorder) {
                        val carregandoReorder = reorderState is TreinoReorderUiState.Loading
                        IconButton(
                            onClick = {
                                modoReorder = false
                                ordemLocal = emptyList()
                                viewModel.resetReorder()
                            },
                            enabled = !carregandoReorder
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                        }
                        IconButton(
                            onClick = {
                                val original = sucesso?.treinos.orEmpty().associate { it.id to it.ordem }
                                val mudancas = ordemLocal.mapIndexedNotNull { idx, t ->
                                    val novaOrdem = idx + 1
                                    if (original[t.id] != novaOrdem) t.id to novaOrdem else null
                                }.toMap()
                                viewModel.reordenar(mudancas)
                            },
                            enabled = !carregandoReorder
                        ) {
                            if (carregandoReorder) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.primary
                                )
                            } else {
                                Icon(Icons.Filled.Check, contentDescription = "Salvar ordem", tint = colors.primary)
                            }
                        }
                    } else {
                        if (temItens) {
                            IconButton(onClick = {
                                modoReorder = true
                                ordemLocal = sucesso?.treinos.orEmpty()
                            }) {
                                Icon(Icons.Filled.Reorder, contentDescription = "Reordenar")
                            }
                        }
                        IconButton(onClick = { onNavigateTab("exercicio_catalogo") }) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = "Exercícios")
                        }
                        IconButton(onClick = { viewModel.carregar() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                        }
                    }
                }
            )
        },
        bottomBar = {
            AppNavigationBar(
                items = alunoNavItems,
                selectedIndex = 1,
                onItemSelected = { idx ->
                    val route = alunoNavItems[idx].route
                    if (route == MAIS_ROUTE) mostrarMaisMenu = true
                    else onNavigateTab(route)
                }
            )
        },
        floatingActionButton = {
            if (!modoReorder) {
                ExtendedFloatingActionButton(
                    onClick = onCriar,
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Novo treino", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.backgroundGradientStart, colors.backgroundGradientEnd)
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!modoReorder) item {
                    OutlinedTextField(
                        value = filtros.busca,
                        onValueChange = { viewModel.atualizarFiltros(filtros.copy(busca = it)) },
                        placeholder = { Text("Buscar treino") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.inputBorder,
                            focusedTextColor = colors.textInput,
                            unfocusedTextColor = colors.textInput,
                            cursorColor = colors.primary
                        )
                    )
                }

                if (!modoReorder) item {
                    BarraFiltrosTreino(
                        filtros = filtros,
                        onAlternarDia = { dia ->
                            val novo = if (dia in filtros.diasSemana) {
                                filtros.diasSemana - dia
                            } else {
                                filtros.diasSemana + dia
                            }
                            viewModel.atualizarFiltros(filtros.copy(diasSemana = novo))
                        },
                        onAlternarComExercicios = {
                            viewModel.atualizarFiltros(
                                filtros.copy(somenteComExercicios = !filtros.somenteComExercicios)
                            )
                        },
                        onLimpar = { viewModel.atualizarFiltros(TreinoFiltros()) }
                    )
                }

                when (val state = uiState) {
                    TreinoListUiState.Idle, TreinoListUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colors.primary)
                            }
                        }
                    }
                    is TreinoListUiState.Error -> {
                        item { CardErroTreino(state.message) { viewModel.carregar() } }
                    }
                    TreinoListUiState.Empty -> {
                        item { CardVazioTreino() }
                    }
                    is TreinoListUiState.Success -> {
                        val lista = if (modoReorder) ordemLocal else state.treinos
                        if (modoReorder) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            "Reordenar treinos",
                                            color = colors.textPrimary,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Use as setas para mudar a posição. Toque no botão de check para salvar.",
                                            color = colors.textSecondary,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                        if (!modoReorder && state.totalPages > 1) {
                            item {
                                Text(
                                    "Página ${state.page} de ${state.totalPages}",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        items(lista, key = { it.id }) { treino ->
                            if (modoReorder) {
                                val idx = lista.indexOf(treino)
                                TreinoReorderCard(
                                    treino = treino,
                                    posicao = idx + 1,
                                    podeSubir = idx > 0,
                                    podeDescer = idx < lista.size - 1,
                                    onSubir = {
                                        if (idx > 0) {
                                            val nova = lista.toMutableList()
                                            val tmp = nova[idx - 1]
                                            nova[idx - 1] = nova[idx]
                                            nova[idx] = tmp
                                            ordemLocal = nova
                                        }
                                    },
                                    onDescer = {
                                        if (idx < lista.size - 1) {
                                            val nova = lista.toMutableList()
                                            val tmp = nova[idx + 1]
                                            nova[idx + 1] = nova[idx]
                                            nova[idx] = tmp
                                            ordemLocal = nova
                                        }
                                    }
                                )
                            } else {
                                TreinoCard(
                                    treino = treino,
                                    onClick = { onAbrirDetalhe(treino.id) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) }
        )
    }
}

@Composable
private fun BarraFiltrosTreino(
    filtros: TreinoFiltros,
    onAlternarDia: (DiaSemana) -> Unit,
    onAlternarComExercicios: () -> Unit,
    onLimpar: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val scroll = rememberScrollState()
    val temFiltros = filtros.diasSemana.isNotEmpty() ||
        filtros.somenteComExercicios ||
        filtros.busca.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DiaSemana.values().forEach { dia ->
            FilterChip(
                selected = dia in filtros.diasSemana,
                onClick = { onAlternarDia(dia) },
                label = { Text(dia.curto) },
                leadingIcon = {
                    Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.textPrimary,
                    iconColor = colors.textSecondary,
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = colors.textOnPrimary,
                    selectedLeadingIconColor = colors.textOnPrimary
                )
            )
        }
        FilterChip(
            selected = filtros.somenteComExercicios,
            onClick = onAlternarComExercicios,
            label = { Text("Com exercícios") },
            leadingIcon = { Icon(Icons.Filled.Bookmark, null, modifier = Modifier.size(16.dp)) },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = colors.surface,
                labelColor = colors.textPrimary,
                iconColor = colors.textSecondary,
                selectedContainerColor = colors.primary,
                selectedLabelColor = colors.textOnPrimary,
                selectedLeadingIconColor = colors.textOnPrimary
            )
        )
        if (temFiltros) {
            AssistChip(
                onClick = onLimpar,
                label = { Text("Limpar") },
                leadingIcon = { Icon(Icons.Filled.Close, null, modifier = Modifier.size(18.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.error,
                    leadingIconContentColor = colors.error
                )
            )
        }
    }
}

@Composable
private fun TreinoCard(treino: TreinoData, onClick: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val descricao = treino.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição cadastrada"
    val dias = treino.diasSemana.orEmpty().mapNotNull(DiaSemana::fromApi)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        treino.nome,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        descricao,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary)
                }
            }

            if (dias.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dias.forEach { dia ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(dia.curto) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = colors.primary.copy(alpha = 0.18f),
                                labelColor = colors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreinoReorderCard(
    treino: TreinoData,
    posicao: Int,
    podeSubir: Boolean,
    podeDescer: Boolean,
    onSubir: () -> Unit,
    onDescer: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    posicao.toString(),
                    color = colors.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    treino.nome,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val diasResumo = treino.diasSemana.orEmpty()
                    .mapNotNull(DiaSemana::fromApi)
                    .joinToString(" • ") { it.curto }
                    .ifBlank { "Sem dias definidos" }
                Text(
                    diasResumo,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = onSubir, enabled = podeSubir) {
                Icon(
                    Icons.Filled.ArrowUpward, "Subir",
                    tint = if (podeSubir) colors.textPrimary else colors.textSecondary
                )
            }
            IconButton(onClick = onDescer, enabled = podeDescer) {
                Icon(
                    Icons.Filled.ArrowDownward, "Descer",
                    tint = if (podeDescer) colors.textPrimary else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CardErroTreino(mensagem: String, onTentarNovamente: () -> Unit) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Não foi possível carregar a lista",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(mensagem, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onTentarNovamente,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary
                )
            ) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun CardVazioTreino() {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Nenhum treino cadastrado",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Crie seu primeiro treino tocando no botão Novo treino.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
