package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import dev.fslab.academia.model.SessaoListItemData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.ExerciciosFrequentesSection
import dev.fslab.academia.ui.components.GruposMusculareSection
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.SecaoEvolucaoPeriodo
import dev.fslab.academia.ui.components.StatsSection
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.AcademiaColors
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.HistoricoUiState
import dev.fslab.academia.ui.viewmodel.HistoricoViewModel
import dev.fslab.academia.ui.viewmodel.PeriodoFiltro
import dev.fslab.academia.ui.viewmodel.SessoesCarregarMaisState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PERIODO_LABELS = listOf("7 dias", "30 dias", "3 meses", "Tudo", "Personalizado")
private val STATUS_LABELS = listOf(null to "Todas", "CONCLUIDA" to "Concluídas", "CANCELADA" to "Canceladas")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    onNavigateTab: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onAbrirProgressao: (String, String) -> Unit = { _, _ -> },
    onAbrirSessao: (String) -> Unit = {},
    viewModel: HistoricoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val uiState by viewModel.uiState.collectAsState()
    val carregarMaisState by viewModel.carregarMaisState.collectAsState()
    val periodoFiltro by viewModel.periodoFiltro.collectAsState()
    val filtroStatus by viewModel.filtroStatus.collectAsState()
    val comparativoState by viewModel.comparativoState.collectAsState()

    var mostrarMaisMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val datePickerState = rememberDateRangePickerState()

    LaunchedEffect(Unit) {
        viewModel.carregarHistorico()
    }

    LaunchedEffect(uiState) {
        if (uiState !is HistoricoUiState.Loading) isRefreshing = false
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = datePickerState.selectedStartDateMillis
                    val end = datePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.selecionarPeriodo(PeriodoFiltro.personalizado(start, end))
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { AcademiaAppBar("Histórico") },
        bottomBar = {
            AppNavigationBar(
                items = alunoNavItems,
                selectedIndex = 3,
                onItemSelected = { idx ->
                    val route = alunoNavItems[idx].route
                    if (route == MAIS_ROUTE) mostrarMaisMenu = true else onNavigateTab(route)
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; viewModel.carregarHistorico() },
            state = pullRefreshState,
            modifier = Modifier.padding(padding)
        ) {
        when (val state = uiState) {
            is HistoricoUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is HistoricoUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(state.message, color = colors.errorText, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.carregarHistorico() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) { Text("Tentar novamente", color = colors.textOnPrimary) }
                    }
                }
            }
            is HistoricoUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PERIODO_LABELS) { label ->
                                val isSelected = periodoFiltro.label == label
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        when (label) {
                                            "7 dias" -> viewModel.selecionarPeriodo(PeriodoFiltro.seteDias())
                                            "30 dias" -> viewModel.selecionarPeriodo(PeriodoFiltro.trintaDias())
                                            "3 meses" -> viewModel.selecionarPeriodo(PeriodoFiltro.tresMeses())
                                            "Tudo" -> viewModel.selecionarPeriodo(PeriodoFiltro.TUDO)
                                            "Personalizado" -> showDatePicker = true
                                        }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                                        selectedLabelColor = colors.primary,
                                        containerColor = colors.surface,
                                        labelColor = colors.textSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = colors.primary.copy(alpha = 0.4f),
                                        borderColor = colors.inputBorder
                                    )
                                )
                            }
                        }
                    }

                    item {
                        StatsSection(stats = state.stats, colors = colors)
                    }

                    item {
                        SecaoEvolucaoPeriodo(
                            state = comparativoState,
                            periodoLabel = periodoFiltro.label,
                            colors = colors
                        )
                    }

                    if (state.grupos.isNotEmpty()) {
                        item {
                            GruposMusculareSection(grupos = state.grupos, colors = colors)
                        }
                    }

                    if (state.frequentes.isNotEmpty()) {
                        item {
                            ExerciciosFrequentesSection(
                                frequentes = state.frequentes,
                                colors = colors,
                                onTap = { ex -> onAbrirProgressao(ex.exercicioId, ex.nome) }
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sessões (${state.stats.totalSessoes})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                STATUS_LABELS.forEach { (status, label) ->
                                    val selected = filtroStatus == status
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.selecionarFiltroStatus(status) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                                            selectedLabelColor = colors.primary,
                                            containerColor = colors.surface,
                                            labelColor = colors.textSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            selectedBorderColor = colors.primary.copy(alpha = 0.4f),
                                            borderColor = colors.inputBorder
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (state.sessoes.isEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhuma sessão encontrada neste período",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(state.sessoes) { sessao ->
                            SessaoHistoricoCard(
                                sessao = sessao,
                                colors = colors,
                                onClick = { onAbrirSessao(sessao.id) }
                            )
                        }
                    }

                    item {
                        when (carregarMaisState) {
                            is SessoesCarregarMaisState.Idle -> {
                                Box(Modifier.fillMaxWidth().padding(dimens.cardPadding), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { viewModel.carregarMaisSessoes() }) {
                                        Text("Carregar mais", color = colors.primary)
                                    }
                                }
                            }
                            is SessoesCarregarMaisState.Loading -> {
                                Box(Modifier.fillMaxWidth().padding(dimens.cardPadding), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.height(24.dp).width(24.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            is SessoesCarregarMaisState.AllLoaded -> {
                                Box(Modifier.fillMaxWidth().padding(dimens.cardPadding), contentAlignment = Alignment.Center) {
                                    Text("Fim do histórico", color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            is SessoesCarregarMaisState.Error -> {
                                Box(Modifier.fillMaxWidth().padding(dimens.cardPadding), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { viewModel.carregarMaisSessoes() }) {
                                        Text("Erro — tentar novamente", color = colors.errorText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        } // PullToRefreshBox
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
private fun SessaoHistoricoCard(sessao: SessaoListItemData, colors: AcademiaColors, onClick: () -> Unit = {}) {
    val dimens = LocalDimens.current
    val duracaoMin = calcularDuracaoMin(sessao.inicio, sessao.fim)
    val dataFormatada = formatarDataHoraHistorico(sessao.inicio)
    val (badgeBg, badgeFg, badgeText) = when (sessao.status) {
        "CONCLUIDA" -> Triple(colors.primary.copy(alpha = 0.15f), colors.primary, "CONCLUÍDA")
        "CANCELADA" -> Triple(colors.error.copy(alpha = 0.15f), colors.error, "CANCELADA")
        else -> Triple(colors.featureOrange.copy(alpha = 0.15f), colors.featureOrange, "EM ANDAMENTO")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimens.cardPaddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sessao.treinoNome,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    dataFormatada,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (duracaoMin != null) {
                    Text(
                        "⏱ $duracaoMin min",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badgeText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = badgeFg)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun calcularDuracaoMin(inicio: String?, fim: String?): Long? {
    if (inicio == null || fim == null) return null
    return try {
        val i = Instant.parse(inicio)
        val f = Instant.parse(fim)
        java.time.Duration.between(i, f).toMinutes().takeIf { it > 0 }
    } catch (_: Exception) { null }
}

private fun formatarDataHoraHistorico(inicio: String?): String {
    if (inicio == null) return ""
    return try {
        val instant = Instant.parse(inicio)
        val zdt = instant.atZone(ZoneId.systemDefault())
        val fmt = DateTimeFormatter.ofPattern("EEE, dd 'de' MMMM · HH'h'mm", java.util.Locale.forLanguageTag("pt-BR"))
        zdt.format(fmt)
    } catch (_: Exception) { inicio }
}

