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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.OutlinedButton
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
import dev.fslab.academia.model.EscopoExercicio
import dev.fslab.academia.model.ExercicioData
import dev.fslab.academia.model.ExercicioMusculoData
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AparelhoSelectionBottomSheet
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.MapaCorporal
import dev.fslab.academia.ui.components.MusculoSelectionBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.util.pressScale
import dev.fslab.academia.ui.util.rememberInteractionSource
import dev.fslab.academia.ui.viewmodel.ExercicioFiltros
import dev.fslab.academia.ui.viewmodel.ExercicioListUiState
import dev.fslab.academia.ui.viewmodel.ExercicioViewModel

@Composable
fun ExercicioCatalogoScreen(
    onBack: () -> Unit,
    onNavigateTab: (String) -> Unit,
    onLogout: () -> Unit = {},
    onAbrirDetalhe: (String) -> Unit = {},
    onCriar: () -> Unit = {},
    viewModel: ExercicioViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val uiState by viewModel.uiState.collectAsState()
    val filtros by viewModel.filtros.collectAsState()

    var mostrarMapa by remember { mutableStateOf(false) }
    var mostrarMusculos by remember { mutableStateOf(false) }
    var mostrarAparelhos by remember { mutableStateOf(false) }
    var mostrarMaisMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    val subtitulo = when (val s = uiState) {
        is ExercicioListUiState.Success -> "${s.total} exercícios"
        ExercicioListUiState.Empty -> "0 exercícios"
        ExercicioListUiState.Loading -> "Carregando…"
        else -> null
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Exercícios",
                subtitle = subtitulo,
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.carregar() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        bottomBar = {
            AppNavigationBar(
                items = alunoNavItems,
                selectedIndex = 4,
                onItemSelected = { idx ->
                    val route = alunoNavItems[idx].route
                    if (route == MAIS_ROUTE) mostrarMaisMenu = true
                    else onNavigateTab(route)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCriar,
                containerColor = colors.primary,
                contentColor = colors.textOnPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo", fontWeight = FontWeight.Bold) }
            )
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
                item {
                    OutlinedTextField(
                        value = filtros.busca,
                        onValueChange = { viewModel.atualizarFiltros(filtros.copy(busca = it)) },
                        placeholder = { Text("Buscar exercício") },
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

                item {
                    BarraFiltros(
                        filtros = filtros,
                        onAbrirMapa = { mostrarMapa = !mostrarMapa },
                        onAbrirMusculos = { mostrarMusculos = true },
                        onAbrirAparelhos = { mostrarAparelhos = true },
                        onAlternarEscopo = { novoEscopo ->
                            viewModel.atualizarFiltros(filtros.copy(escopo = novoEscopo))
                        },
                        onAlternarEmUso = {
                            val proximo = when (filtros.emUso) {
                                null -> true
                                true -> false
                                false -> null
                            }
                            viewModel.atualizarFiltros(filtros.copy(emUso = proximo))
                        },
                        onAlternarComMidia = {
                            val proximo = when (filtros.comMidia) {
                                null -> true
                                true -> false
                                false -> null
                            }
                            viewModel.atualizarFiltros(filtros.copy(comMidia = proximo))
                        },
                        onAlternarTipo = { tipo ->
                            val proximo = if (filtros.tipoExercicio == tipo) null else tipo
                            viewModel.atualizarFiltros(filtros.copy(tipoExercicio = proximo))
                        },
                        onLimpar = { viewModel.atualizarFiltros(ExercicioFiltros()) }
                    )
                }

                if (mostrarMapa) {
                    item {
                        MapaCorporal(
                            grupoSelecionado = filtros.grupoMuscular,
                            onGrupoSelecionado = { grupo ->
                                viewModel.atualizarFiltros(filtros.copy(grupoMuscular = grupo))
                            }
                        )
                    }
                }

                when (val state = uiState) {
                    ExercicioListUiState.Idle, ExercicioListUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colors.primary)
                            }
                        }
                    }
                    is ExercicioListUiState.Error -> {
                        item { CardErro(state.message) { viewModel.carregar() } }
                    }
                    ExercicioListUiState.Empty -> {
                        item { CardVazio() }
                    }
                    is ExercicioListUiState.Success -> {
                        items(state.exercicios, key = { it.id }) { exercicio ->
                            Box(Modifier.animateItem()) {
                                ExercicioCard(
                                    exercicio = exercicio,
                                    onClick = { onAbrirDetalhe(exercicio.id) }
                                )
                            }
                        }
                        if (state.page < state.totalPages) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(onClick = { viewModel.carregarMais() }) {
                                        Text(
                                            "Carregar mais (${state.exercicios.size}/${state.total})",
                                            color = colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (mostrarMusculos) {
        MusculoSelectionBottomSheet(
            selecionados = filtros.musculoIds,
            grupoMuscularInicial = filtros.grupoMuscular,
            onConfirmar = { ids ->
                viewModel.atualizarFiltros(filtros.copy(musculoIds = ids))
                mostrarMusculos = false
            },
            onDismiss = { mostrarMusculos = false }
        )
    }

    if (mostrarAparelhos) {
        AparelhoSelectionBottomSheet(
            selecionados = filtros.aparelhoIds,
            onConfirmar = { ids ->
                viewModel.atualizarFiltros(filtros.copy(aparelhoIds = ids))
                mostrarAparelhos = false
            },
            onDismiss = { mostrarAparelhos = false }
        )
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) },
            onLogout = onLogout
        )
    }
}

@Composable
private fun BarraFiltros(
    filtros: ExercicioFiltros,
    onAbrirMapa: () -> Unit,
    onAbrirMusculos: () -> Unit,
    onAbrirAparelhos: () -> Unit,
    onAlternarEscopo: (EscopoExercicio) -> Unit,
    onAlternarEmUso: () -> Unit,
    onAlternarComMidia: () -> Unit,
    onAlternarTipo: (TipoExercicio) -> Unit,
    onLimpar: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val scroll = rememberScrollState()
    val temFiltros = filtros.grupoMuscular != null ||
        filtros.musculoIds.isNotEmpty() ||
        filtros.aparelhoIds.isNotEmpty() ||
        filtros.busca.isNotBlank() ||
        filtros.escopo != EscopoExercicio.TODOS ||
        filtros.emUso != null ||
        filtros.comMidia != null ||
        filtros.tipoExercicio != null

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filtros.grupoMuscular != null,
            onClick = onAbrirMapa,
            label = { Text(filtros.grupoMuscular?.display ?: "Mapa corporal") },
            leadingIcon = { Icon(Icons.Filled.Map, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.musculoIds.isNotEmpty(),
            onClick = onAbrirMusculos,
            label = {
                Text(
                    if (filtros.musculoIds.isEmpty()) "Músculos"
                    else "${filtros.musculoIds.size} músculos"
                )
            },
            leadingIcon = { Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.aparelhoIds.isNotEmpty(),
            onClick = onAbrirAparelhos,
            label = {
                Text(
                    if (filtros.aparelhoIds.isEmpty()) "Aparelhos"
                    else "${filtros.aparelhoIds.size} aparelhos"
                )
            },
            leadingIcon = { Icon(Icons.Filled.Build, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.escopo == EscopoExercicio.PESSOAL,
            onClick = {
                onAlternarEscopo(
                    if (filtros.escopo == EscopoExercicio.PESSOAL) EscopoExercicio.TODOS
                    else EscopoExercicio.PESSOAL
                )
            },
            label = { Text("Pessoais") },
            leadingIcon = { Icon(Icons.Filled.Person, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.escopo == EscopoExercicio.GLOBAL,
            onClick = {
                onAlternarEscopo(
                    if (filtros.escopo == EscopoExercicio.GLOBAL) EscopoExercicio.TODOS
                    else EscopoExercicio.GLOBAL
                )
            },
            label = { Text("Globais") },
            leadingIcon = { Icon(Icons.Filled.Public, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.emUso == true,
            onClick = onAlternarEmUso,
            label = { Text(if (filtros.emUso == false) "Sem treino" else "Em uso") },
            leadingIcon = { Icon(Icons.Filled.Bookmark, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.REPETICAO,
            onClick = { onAlternarTipo(TipoExercicio.REPETICAO) },
            label = { Text("Repetição") },
            leadingIcon = { Icon(Icons.Filled.Repeat, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.TEMPO,
            onClick = { onAlternarTipo(TipoExercicio.TEMPO) },
            label = { Text("Tempo") },
            leadingIcon = { Icon(Icons.Filled.Timer, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.DISTANCIA,
            onClick = { onAlternarTipo(TipoExercicio.DISTANCIA) },
            label = { Text("Distância") },
            leadingIcon = { Icon(Icons.Filled.Straighten, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.comMidia == true,
            onClick = onAlternarComMidia,
            label = { Text(if (filtros.comMidia == false) "Sem animação" else "Com animação") },
            leadingIcon = { Icon(Icons.Filled.PlayCircle, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.REPETICAO,
            onClick = { onAlternarTipo(TipoExercicio.REPETICAO) },
            label = { Text("Repetição") },
            leadingIcon = { Icon(Icons.Filled.Repeat, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.TEMPO,
            onClick = { onAlternarTipo(TipoExercicio.TEMPO) },
            label = { Text("Tempo") },
            leadingIcon = { Icon(Icons.Filled.Timer, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
        )
        FilterChip(
            selected = filtros.tipoExercicio == TipoExercicio.DISTANCIA,
            onClick = { onAlternarTipo(TipoExercicio.DISTANCIA) },
            label = { Text("Distância") },
            leadingIcon = { Icon(Icons.Filled.Straighten, null, modifier = Modifier.size(18.dp)) },
            colors = academiaFilterChipColors()
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
private fun academiaFilterChipColors(): androidx.compose.material3.SelectableChipColors {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    return FilterChipDefaults.filterChipColors(
        containerColor = colors.surface,
        labelColor = colors.textPrimary,
        iconColor = colors.textSecondary,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.textOnPrimary,
        selectedLeadingIconColor = colors.textOnPrimary
    )
}

@Composable
private fun ExercicioCard(
    exercicio: ExercicioData,
    onClick: () -> Unit = {}
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val descricao = exercicio.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição cadastrada"
    val primarios = exercicio.musculos.count { it.tipoAtivacao == "PRIMARIO" }
    val interacao = rememberInteractionSource()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interacao)
            .clickable(interactionSource = interacao, indication = null, onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercicio.nome,
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
                    Icon(Icons.Filled.Bolt, null, tint = colors.primary)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${exercicio.musculos.size} músculos • $primarios primários • ${exercicio.aparelhos.size} aparelhos",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                val (tipoLabel, tipoIcone, tipoCor) = when (exercicio.tipo) {
                    TipoExercicio.TEMPO -> Triple("Tempo", Icons.Filled.Timer, colors.featureOrange)
                    TipoExercicio.DISTANCIA -> Triple("Distância", Icons.AutoMirrored.Filled.DirectionsRun, colors.featureGreen)
                    else -> Triple("Repetição", Icons.Filled.Refresh, colors.featureBlue)
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(tipoLabel, style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(tipoIcone, null, modifier = Modifier.size(14.dp), tint = tipoCor) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = tipoCor.copy(alpha = 0.12f),
                        labelColor = tipoCor
                    )
                )
            }

            if (exercicio.musculos.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exercicio.musculos.take(4).forEach { MusculoChip(it) }
                    if (exercicio.musculos.size > 4) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("+${exercicio.musculos.size - 4}") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = colors.lightGray,
                                labelColor = colors.textSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MusculoChip(musculo: ExercicioMusculoData) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val ehPrimario = musculo.tipoAtivacao == "PRIMARIO"
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                musculo.nome,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (ehPrimario) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = if (ehPrimario) colors.primary.copy(alpha = 0.18f) else colors.lightGray,
            labelColor = if (ehPrimario) colors.primary else colors.textSecondary
        )
    )
}

@Composable
private fun CardErro(mensagem: String, onTentarNovamente: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
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
private fun CardVazio() {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Nenhum exercício encontrado",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tente ajustar os filtros ou ampliar a busca.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
