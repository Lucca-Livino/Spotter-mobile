package dev.fslab.academia.ui.screens.treinador

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.AlunoData
import dev.fslab.academia.model.HistoricoPesoMetricas
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.ExerciciosFrequentesSection
import dev.fslab.academia.ui.components.GruposMusculareSection
import dev.fslab.academia.ui.components.SecaoEvolucaoPeriodo
import dev.fslab.academia.ui.components.StatsSection
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.util.Motion
import dev.fslab.academia.ui.viewmodel.AlunoEstatisticasUiState
import dev.fslab.academia.ui.viewmodel.DesvincularAlunoState
import dev.fslab.academia.ui.viewmodel.PeriodoEstatisticasAluno
import dev.fslab.academia.ui.viewmodel.TreinoDeletarUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel
import dev.fslab.academia.ui.viewmodel.TreinadorAlunoDetalheUiState
import dev.fslab.academia.ui.viewmodel.TreinadorAlunoDetalheViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private val diasSemanaLabels = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

@Composable
fun TreinadorAlunoDetalheScreen(
    alunoId: String,
    onBack: () -> Unit,
    onMontarTreino: (String, String) -> Unit,
    onAbrirTreino: (String) -> Unit,
    onDesvinculado: () -> Unit = {},
    viewModel: TreinadorAlunoDetalheViewModel = viewModel(),
    treinoViewModel: TreinoViewModel = viewModel(),
    autoLoad: Boolean = true
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val uiState by viewModel.uiState.collectAsState()
    val deletarState by treinoViewModel.deletarState.collectAsState()
    val desvincularState by viewModel.desvincularState.collectAsState()
    val estatisticasState by viewModel.estatisticasState.collectAsState()
    val periodoEstatisticas by viewModel.periodoEstatisticas.collectAsState()
    val treinoParaExcluir = remember { mutableStateOf<TreinoData?>(null) }
    val mostrarDialogDesvincular = remember { mutableStateOf(false) }

    LaunchedEffect(alunoId, autoLoad) {
        if (autoLoad) {
            viewModel.carregar(alunoId)
        }
    }

    LaunchedEffect(deletarState) {
        if (deletarState is TreinoDeletarUiState.Success) {
            treinoViewModel.resetDeletar()
            treinoParaExcluir.value = null
            viewModel.carregar(alunoId)
        }
    }

    LaunchedEffect(desvincularState) {
        if (desvincularState is DesvincularAlunoState.Success) {
            viewModel.resetDesvincular()
            onDesvinculado()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            val title = when (val state = uiState) {
                is TreinadorAlunoDetalheUiState.Success -> state.aluno.nome.split(" ").firstOrNull() ?: "Perfil"
                else -> "Perfil"
            }
            AcademiaAppBar(
                title = title,
                subtitle = "Perfil do Cliente",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Crossfade(targetState = uiState, animationSpec = Motion.contentSpec(), label = "alunoDetalhe") { state ->
        when (state) {
            is TreinadorAlunoDetalheUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando perfil...", color = colors.textSecondary)
                }
            }
            is TreinadorAlunoDetalheUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, null, tint = colors.textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = colors.textSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
            is TreinadorAlunoDetalheUiState.Success -> {
                AlunoDetalheContent(
                    aluno = state.aluno,
                    treinos = state.treinos,
                    diasTreino = state.diasTreino,
                    ultimoTreino = state.ultimoTreino,
                    estatisticasState = estatisticasState,
                    periodoEstatisticas = periodoEstatisticas,
                    modifier = Modifier.padding(innerPadding),
                    onMontarTreino = { onMontarTreino(state.aluno.id, state.aluno.nome) },
                    onAbrirTreino = onAbrirTreino,
                    onExcluirTreino = { treino -> treinoParaExcluir.value = treino },
                    onDesvincularAluno = { mostrarDialogDesvincular.value = true },
                    onSelecionarPeriodoEstatisticas = { periodo -> viewModel.selecionarPeriodoEstatisticas(state.aluno.id, periodo) }
                )
            }
            else -> Unit
        }
        }
    }

    if (mostrarDialogDesvincular.value) {
        val carregandoDesvincular = desvincularState is DesvincularAlunoState.Loading
        AlertDialog(
            onDismissRequest = { if (!carregandoDesvincular) mostrarDialogDesvincular.value = false },
            containerColor = colors.surface,
            title = { Text("Desvincular aluno?", color = colors.textPrimary) },
            text = {
                Text(
                    "O aluno perderá acesso aos treinos que você montou para ele.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.desvincularAluno(alunoId) },
                    enabled = !carregandoDesvincular,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.textOnPrimary
                    )
                ) { Text(if (carregandoDesvincular) "Desvinculando..." else "Desvincular") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!carregandoDesvincular) mostrarDialogDesvincular.value = false },
                    enabled = !carregandoDesvincular
                ) { Text("Cancelar", color = colors.textSecondary) }
            }
        )
    }

    val desvincularErro = desvincularState as? DesvincularAlunoState.Error
    if (desvincularErro != null) {
        AlertDialog(
            onDismissRequest = { viewModel.resetDesvincular() },
            containerColor = colors.surface,
            title = { Text("Falha ao desvincular", color = colors.textPrimary) },
            text = { Text(desvincularErro.message, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetDesvincular() }) {
                    Text("OK", color = colors.primary)
                }
            }
        )
    }

    val treinoExcluir = treinoParaExcluir.value
    if (treinoExcluir != null) {
        val carregando = deletarState is TreinoDeletarUiState.Loading
        AlertDialog(
            onDismissRequest = { if (!carregando) treinoParaExcluir.value = null },
            containerColor = colors.surface,
            title = { Text("Excluir treino?", color = colors.textPrimary) },
            text = {
                Text(
                    "Esta ação não pode ser desfeita e removerá o treino deste cliente.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { treinoViewModel.deletar(treinoExcluir.id) },
                    enabled = !carregando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.textOnPrimary
                    )
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!carregando) treinoParaExcluir.value = null },
                    enabled = !carregando
                ) { Text("Cancelar", color = colors.textSecondary) }
            }
        )
    }

    val erroDeletar = deletarState as? TreinoDeletarUiState.Error
    if (erroDeletar != null) {
        AlertDialog(
            onDismissRequest = { treinoViewModel.resetDeletar() },
            containerColor = colors.surface,
            title = { Text("Falha ao excluir", color = colors.textPrimary) },
            text = { Text(erroDeletar.message, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { treinoViewModel.resetDeletar() }) {
                    Text("OK", color = colors.primary)
                }
            }
        )
    }
}

@Composable
private fun AlunoDetalheContent(
    aluno: AlunoData,
    treinos: List<TreinoData>,
    diasTreino: Set<Int>,
    ultimoTreino: LocalDate?,
    estatisticasState: AlunoEstatisticasUiState,
    periodoEstatisticas: PeriodoEstatisticasAluno,
    modifier: Modifier = Modifier,
    onMontarTreino: () -> Unit,
    onAbrirTreino: (String) -> Unit,
    onExcluirTreino: (TreinoData) -> Unit,
    onDesvincularAluno: () -> Unit = {},
    onSelecionarPeriodoEstatisticas: (PeriodoEstatisticasAluno) -> Unit
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val hoje = LocalDate.now()
    val diaHojeIdx = hoje.dayOfWeek.value % 7

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.inputBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AvatarCliente(nome = aluno.nome, size = 60.dp)
                    Column {
                        Text(
                            text = aluno.nome,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        // Objetivo placeholder (aluno data does not have it yet, but could be inferred or mock)
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ALUNO VINCULADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.inputBorder))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(value = treinos.size.toString(), label = "Treinos")
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(colors.inputBorder))
                    StatItem(value = "${diasTreino.size}x", label = "por semana")
                    
                    ultimoTreino?.let {
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(colors.inputBorder))
                        StatItem(value = formatarRelativo(it), label = "Último treino")
                    }
                }
            }
        }

        // Dias de Treino
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "DIAS DE TREINO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                        .padding(dimens.cardPaddingSmall),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    diasSemanaLabels.forEachIndexed { index, dia ->
                        val ativo = diasTreino.contains(index)
                        val isHoje = index == diaHojeIdx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        ativo && isHoje -> colors.primary
                                        ativo -> colors.primary.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = dia,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    ativo && isHoje -> colors.textOnPrimary
                                    ativo -> colors.primary
                                    else -> colors.textSecondary.copy(alpha = 0.5f)
                                }
                            )
                            if (ativo) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isHoje) colors.textOnPrimary.copy(alpha = 0.5f) else colors.primary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Últimos Treinos
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.History, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = "ÚLTIMOS TREINOS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary
                )
            }
        }

        if (treinos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FitnessCenter, null, tint = colors.inputBorder, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum treino registrado", color = colors.textSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(treinos) { treino ->
                TreinoCard(
                    treino = treino,
                    onExcluir = { onExcluirTreino(treino) }
                )
            }
        }

        // CTA
        item {
            Button(
                onClick = onMontarTreino,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Icon(Icons.Default.AddCircle, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Montar treino para ${aluno.nome.split(" ").firstOrNull()}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = onDesvincularAluno,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.PersonOff, null, tint = colors.error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Desvincular aluno",
                    color = colors.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            EstatisticasAlunoSection(
                state = estatisticasState,
                periodoSelecionado = periodoEstatisticas,
                onSelecionarPeriodo = onSelecionarPeriodoEstatisticas
            )
        }
    }
}

@Composable
private fun EstatisticasAlunoSection(
    state: AlunoEstatisticasUiState,
    periodoSelecionado: PeriodoEstatisticasAluno,
    onSelecionarPeriodo: (PeriodoEstatisticasAluno) -> Unit
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Insights, null, tint = colors.primary, modifier = Modifier.size(16.dp))
            Text(
                text = "ESTATÍSTICAS DO ALUNO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodoEstatisticasAluno.entries.forEach { periodo ->
                val selected = periodo == periodoSelecionado
                FilterChip(
                    selected = selected,
                    onClick = { onSelecionarPeriodo(periodo) },
                    label = { Text(periodo.label, style = MaterialTheme.typography.labelMedium) },
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
        Spacer(modifier = Modifier.height(12.dp))

        when (state) {
            is AlunoEstatisticasUiState.Idle, is AlunoEstatisticasUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is AlunoEstatisticasUiState.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(state.message, color = colors.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
            is AlunoEstatisticasUiState.Success -> {
                StatsSection(stats = state.stats, colors = colors)
                SecaoEvolucaoPeriodo(state = state.comparativo, periodoLabel = periodoSelecionado.label, colors = colors)
                if (state.grupos.isNotEmpty()) {
                    GruposMusculareSection(grupos = state.grupos, colors = colors)
                }
                if (state.frequentes.isNotEmpty()) {
                    ExerciciosFrequentesSection(frequentes = state.frequentes, colors = colors, onTap = {})
                }
                state.historicoPeso?.let { peso ->
                    if (peso.totalRegistros > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SecaoPesoAluno(historicoPeso = peso, colors = colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.primary)
        Text(text = label, fontSize = 10.sp, color = colors.textSecondary)
    }
}

@Composable
private fun TreinoCard(
    treino: TreinoData,
    onExcluir: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = treino.nome, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(
                text = "${treino.diasSemana?.size ?: 0} dias · ${treino.dataCriacao?.let { formatarRelativo(it) } ?: ""}",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        IconButton(onClick = onExcluir) {
            Icon(Icons.Default.Delete, contentDescription = "Excluir treino", tint = colors.error)
        }
    }
}

@Composable
private fun AvatarCliente(nome: String, size: androidx.compose.ui.unit.Dp) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val iniciais = nome.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.15f))
            .border(1.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciais,
            fontSize = (size.value / 2.5f).sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
    }
}

@Composable
private fun SecaoPesoAluno(historicoPeso: HistoricoPesoMetricas, colors: dev.fslab.academia.ui.theme.AcademiaColors) {
    val tendenciaIcon = when (historicoPeso.tendencia) {
        "SUBINDO" -> Icons.AutoMirrored.Filled.TrendingUp
        "DESCENDO" -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    val tendenciaColor = when (historicoPeso.tendencia) {
        "SUBINDO" -> colors.featureOrange
        "DESCENDO" -> colors.featureGreen
        else -> colors.textSecondary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.MonitorWeight, null, tint = colors.primary, modifier = Modifier.size(16.dp))
            Text(
                text = "EVOLUÇÃO DE PESO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = historicoPeso.pesoAtualKg?.let { "%.1f kg".format(it) } ?: "—",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "peso atual",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    if (historicoPeso.tendencia != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(tendenciaIcon, null, tint = tendenciaColor, modifier = Modifier.size(28.dp))
                            Text(
                                text = when (historicoPeso.tendencia) {
                                    "SUBINDO" -> "subindo"
                                    "DESCENDO" -> "descendo"
                                    else -> "estável"
                                },
                                fontSize = 11.sp,
                                color = tendenciaColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.inputBorder)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PesoStatItem(
                        value = historicoPeso.pesoMinimoKg?.let { "%.1f kg".format(it) } ?: "—",
                        label = "mínimo"
                    )
                    Box(
                        modifier = Modifier.width(1.dp).height(32.dp).background(colors.inputBorder)
                    )
                    PesoStatItem(
                        value = historicoPeso.pesoMaximoKg?.let { "%.1f kg".format(it) } ?: "—",
                        label = "máximo"
                    )
                    Box(
                        modifier = Modifier.width(1.dp).height(32.dp).background(colors.inputBorder)
                    )
                    PesoStatItem(
                        value = historicoPeso.variacaoTotalKg?.let {
                            val prefix = if (it > 0) "+" else ""
                            "$prefix%.1f kg".format(it)
                        } ?: "—",
                        label = "variação total"
                    )
                }

                historicoPeso.variacaoUltimaSemanKg?.let { varSemana ->
                    val prefix = if (varSemana > 0) "+" else ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Variação última semana", fontSize = 12.sp, color = colors.textSecondary)
                        Text(
                            "$prefix%.1f kg".format(varSemana),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (varSemana > 0) colors.featureOrange else if (varSemana < 0) colors.featureGreen else colors.textSecondary
                        )
                    }
                }

                Text(
                    text = "${historicoPeso.totalRegistros} registro${if (historicoPeso.totalRegistros != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PesoStatItem(value: String, label: String) {
    val colors = LocalAcademiaColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        Text(label, fontSize = 10.sp, color = colors.textSecondary)
    }
}

private fun formatarRelativo(dataStr: String): String {
    val data = runCatching { OffsetDateTime.parse(dataStr).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(dataStr).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(dataStr) }.getOrNull()
        ?: return ""
        
    return formatarRelativo(data)
}

private fun formatarRelativo(data: LocalDate): String {
    val hoje = LocalDate.now()
    val dias = ChronoUnit.DAYS.between(data, hoje)
    return when (dias) {
        0L -> "hoje"
        1L -> "ontem"
        in 2..6 -> "há $dias dias"
        in 7..13 -> "há 1 semana"
        else -> "há ${dias / 7} semanas"
    }
}
