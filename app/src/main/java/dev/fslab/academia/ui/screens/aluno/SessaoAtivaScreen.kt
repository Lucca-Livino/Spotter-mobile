package dev.fslab.academia.ui.screens.aluno

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.SessaoData
import dev.fslab.academia.model.SessaoExercicioData
import dev.fslab.academia.model.SessaoResumoData
import dev.fslab.academia.model.SessaoSerieData
import dev.fslab.academia.model.SessaoSerieItemRequest
import dev.fslab.academia.model.SessaoSeriesUpdateRequest
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AnimacaoPlayer
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.SessaoSeriesUiState
import dev.fslab.academia.ui.viewmodel.SessaoUiState
import dev.fslab.academia.ui.viewmodel.SessaoViewModel
import dev.fslab.academia.ui.util.SessionSoundManager
import kotlinx.coroutines.delay

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun SessaoAtivaScreen(
    treinoId: String?,
    onBack: () -> Unit,
    viewModel: SessaoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val prsBatidos by viewModel.prsBatidos.collectAsState()

    LaunchedEffect(treinoId) {
        if (treinoId != null) viewModel.iniciar(treinoId)
        else viewModel.verificarEmAndamento()
    }

    when (val state = uiState) {
        SessaoUiState.Idle, SessaoUiState.Loading -> LoadingPlaceholder()
        SessaoUiState.SemSessaoAtiva -> SemSessaoAtivaConteudo(onBack)
        is SessaoUiState.EmAndamento -> ExecucaoSessao(
            sessao = state.sessao,
            viewModel = viewModel,
            onBack = onBack
        )
        is SessaoUiState.Finalizada -> ResumoSessaoConteudo(
            sessao = state.sessao,
            resumo = state.resumo,
            prsBatidos = prsBatidos,
            onVoltar = onBack
        )
        is SessaoUiState.Error -> ErroConteudo(
            message = state.message,
            onBack = onBack,
            onRetry = { if (treinoId != null) viewModel.iniciar(treinoId) else viewModel.verificarEmAndamento() }
        )
    }
}

// ─── Execução principal ────────────────────────────────────────────────────────

@Composable
private fun ExecucaoSessao(
    sessao: SessaoData,
    viewModel: SessaoViewModel,
    onBack: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val seriesState by viewModel.seriesState.collectAsState()
    val maxCargaPorExercicio by viewModel.maxCargaPorExercicio.collectAsState()

    val exerciciosOrdenados = remember(sessao.exercicios) {
        sessao.exercicios.sortedBy { it.ordem }
    }
    val primeiroNaoConcluido = exerciciosOrdenados.indexOfFirst { !it.concluido }.let {
        if (it == -1) exerciciosOrdenados.lastIndex.coerceAtLeast(0) else it
    }
    var exercicioIndex by rememberSaveable { mutableIntStateOf(primeiroNaoConcluido) }

    // Atualiza índice quando sessão muda (ex: após concluir exercício)
    LaunchedEffect(sessao.exercicios) {
        val novo = exerciciosOrdenados.indexOfFirst { !it.concluido }
        if (novo != -1 && exercicioIndex >= exerciciosOrdenados.size) {
            exercicioIndex = novo
        }
    }

    val exercicioAtual = exerciciosOrdenados.getOrNull(exercicioIndex) ?: return

    // Timer geral
    var segundosDecorridos by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(sessao.id) {
        while (true) { delay(1000L); segundosDecorridos++ }
    }

    // Estado de descanso
    var descansoSegundos by remember { mutableIntStateOf(0) }
    var descansoAtivo by remember { mutableStateOf(false) }
    var descansoSerieInfo by remember { mutableStateOf("") }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(descansoAtivo, descansoSegundos) {
        if (descansoAtivo && descansoSegundos > 0) {
            delay(1000L)
            descansoSegundos--
            if (descansoSegundos <= 0) {
                descansoAtivo = false
                SessionSoundManager.playRestEnd()
            }
        }
    }

    val eTempo = exercicioAtual.exercicio.tipo == TipoExercicio.TEMPO
    val maxCargaHistorica: Double? = maxCargaPorExercicio[exercicioAtual.exercicio.id]

    val defaultReps = exercicioAtual.template.repeticoes?.split("-")?.firstOrNull()?.trim()
        ?.filter { it.isDigit() }?.toIntOrNull() ?: 0
    val defaultCarga = exercicioAtual.template.cargaSugerida.orEmpty()
    val defaultMeta = exercicioAtual.template.duracaoSugeridaSegundos ?: 0

    // Mapas mutáveis — permitem entradas extras quando o usuário adiciona séries
    val repsLocais: MutableMap<String, androidx.compose.runtime.MutableIntState> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableIntStateOf(it.repeticoesRealizadas ?: defaultReps) }.toMutableMap()
    }
    val cargaLocais: MutableMap<String, androidx.compose.runtime.MutableState<String>> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableStateOf(it.cargaUtilizada ?: defaultCarga) }.toMutableMap()
    }
    val statusLocais: MutableMap<String, androidx.compose.runtime.MutableState<String>> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableStateOf(it.status) }.toMutableMap()
    }
    val timerSegundosLocais: MutableMap<String, androidx.compose.runtime.MutableIntState> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableIntStateOf(it.tempoRealizadoSegundos ?: 0) }.toMutableMap()
    }
    val timerAtivoLocais: MutableMap<String, androidx.compose.runtime.MutableState<Boolean>> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableStateOf(false) }.toMutableMap()
    }
    val metaLocais: MutableMap<String, androidx.compose.runtime.MutableIntState> = remember(exercicioAtual.id) {
        exercicioAtual.series.associate { it.id to mutableIntStateOf(defaultMeta) }.toMutableMap()
    }

    // Contagem local de séries — pode diferir do template (usuário adiciona/remove)
    var seriesContagemLocal by remember(exercicioAtual.id) {
        mutableIntStateOf(exercicioAtual.series.size)
    }

    fun serieIdPorIndice(i: Int): String =
        if (i < exercicioAtual.series.size) exercicioAtual.series[i].id else "extra_$i"

    fun inicializarSerieExtra(id: String) {
        if (!statusLocais.containsKey(id)) {
            statusLocais[id] = mutableStateOf("PENDENTE")
            repsLocais[id] = mutableIntStateOf(defaultReps)
            cargaLocais[id] = mutableStateOf(defaultCarga)
            timerSegundosLocais[id] = mutableIntStateOf(0)
            timerAtivoLocais[id] = mutableStateOf(false)
            metaLocais[id] = mutableIntStateOf(defaultMeta)
        }
    }

    var mostrarDialogCancelar by remember { mutableStateOf(false) }
    var mostrarDialogFinalizar by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        mostrarDialogCancelar = true
    }

    val concluidos = exerciciosOrdenados.count { it.concluido }
    val total = exerciciosOrdenados.size
    val totalSeries = sessao.exercicios.sumOf { ex ->
        if (ex.id == exercicioAtual.id) seriesContagemLocal else ex.series.size.takeIf { it > 0 } ?: ex.template.series
    }
    val seriesConcluidas = sessao.exercicios.sumOf { ex ->
        if (ex.id == exercicioAtual.id) {
            (0 until seriesContagemLocal).count { i ->
                statusLocais[serieIdPorIndice(i)]?.value == "CONCLUIDA"
            }
        } else {
            ex.series.count { it.status == "CONCLUIDA" }
        }
    }

    val exerciciosComPr by remember(sessao, maxCargaPorExercicio) {
        derivedStateOf {
            sessao.exercicios.count { exData ->
                val max = maxCargaPorExercicio[exData.exercicio.id] ?: return@count false
                exData.series.any { serie ->
                    serie.status == "CONCLUIDA" &&
                        (serie.cargaUtilizada?.trim()?.toDoubleOrNull() ?: 0.0) > max
                }
            }
        }
    }

    LaunchedEffect(seriesState) {
        if (seriesState is SessaoSeriesUiState.Success) {
            viewModel.resetSeries()
        }
    }

    fun salvarSeriesExercicio(concluirExercicio: Boolean) {
        val seriesParaSalvar = (0 until seriesContagemLocal).map { i ->
            val serieId = serieIdPorIndice(i)
            val status = statusLocais[serieId]?.value ?: "PENDENTE"
            val reps = repsLocais[serieId]?.intValue
            val carga = cargaLocais[serieId]?.value?.trim()?.takeIf { it.isNotBlank() }
            val tempo = timerSegundosLocais[serieId]?.intValue?.takeIf { it > 0 }
            SessaoSerieItemRequest(
                numeroSerie = i + 1,
                repeticoesRealizadas = if (!eTempo && status == "CONCLUIDA") reps else null,
                cargaUtilizada = if (status == "CONCLUIDA") carga else null,
                tempoRealizadoSegundos = if (eTempo && status == "CONCLUIDA") tempo else null,
                status = status
            )
        }
        viewModel.registrarSeries(sessao.id, exercicioAtual.id, seriesParaSalvar)
        if (concluirExercicio) {
            viewModel.concluirExercicio(sessao.id, exercicioAtual.id)
        }
    }

    fun avancarExercicio() {
        // Séries pendentes → PULADA antes de avançar
        val temPendente = statusLocais.values.any { it.value == "PENDENTE" }
        if (temPendente) {
            statusLocais.values.forEach { s -> if (s.value == "PENDENTE") s.value = "PULADA" }
        }
        salvarSeriesExercicio(concluirExercicio = true)
        if (exercicioIndex < exerciciosOrdenados.lastIndex) exercicioIndex++
    }

    fun voltarExercicio() {
        if (exercicioIndex > 0) exercicioIndex--
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = sessao.treinoNome,
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    Text(
                        formatarTempo(segundosDecorridos),
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { mostrarDialogCancelar = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancelar sessão", tint = colors.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.backgroundGradientStart, colors.backgroundGradientEnd)))
                .padding(innerPadding)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Exercício ${exercicioIndex + 1} de $total",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "$seriesConcluidas/$totalSeries séries",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { if (total > 0) concluidos.toFloat() / total else 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = colors.primary,
                    trackColor = colors.lightGray,
                    strokeCap = StrokeCap.Round
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // GIF do exercício
                item {
                    ExercicioGifSection(
                        animacaoUrl = exercicioAtual.exercicio.animacaoUrl,
                        nomExercicio = exercicioAtual.exercicio.nome
                    )
                }

                // Header do exercício + navegação
                item {
                    ExercicioHeaderSection(
                        exercicio = exercicioAtual,
                        index = exercicioIndex,
                        total = total,
                        concluidos = concluidos,
                        onVoltar = { voltarExercicio() },
                        onAvancar = { avancarExercicio() }
                    )
                }

                // Séries
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until seriesContagemLocal) {
                            val serieId = serieIdPorIndice(i)
                            inicializarSerieExtra(serieId)
                            val numeroSerie = i + 1

                            val status = statusLocais[serieId] ?: continue
                            val carga = cargaLocais[serieId] ?: continue

                            if (eTempo) {
                                val timerSeg = timerSegundosLocais[serieId] ?: continue
                                val timerAtivo = timerAtivoLocais[serieId] ?: continue
                                val metaLocal = metaLocais[serieId] ?: continue

                                LaunchedEffect(timerAtivo.value, serieId) {
                                    while (timerAtivo.value) {
                                        delay(1000L)
                                        timerSeg.intValue++
                                        val meta = metaLocal.intValue
                                        if (meta > 0 && timerSeg.intValue >= meta) {
                                            timerAtivo.value = false
                                            status.value = "CONCLUIDA"
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            SessionSoundManager.playSerieComplete()
                                            val tempoDescanso = exercicioAtual.template.tempoDescansoSegundos.coerceAtLeast(30)
                                            descansoSerieInfo = "Série $numeroSerie/$seriesContagemLocal — ${exercicioAtual.exercicio.nome}"
                                            descansoSegundos = tempoDescanso
                                            descansoAtivo = true
                                            SessionSoundManager.playRestStart()
                                            break
                                        }
                                    }
                                }

                                SerieTempoRow(
                                    numeroSerie = numeroSerie,
                                    totalSeries = seriesContagemLocal,
                                    segundos = timerSeg.intValue,
                                    metaSegundos = metaLocal.intValue.takeIf { it > 0 },
                                    carga = carga.value,
                                    status = status.value,
                                    timerAtivo = timerAtivo.value,
                                    isPr = run {
                                        if (status.value != "CONCLUIDA") return@run false
                                        val cargaDouble = carga.value.trim().toDoubleOrNull() ?: return@run false
                                        val max = maxCargaHistorica ?: return@run false
                                        cargaDouble > max
                                    },
                                    onAlterarMeta = { metaLocal.intValue = it },
                                    onIniciar = {
                                        timerAtivo.value = true
                                        status.value = "PENDENTE"
                                    },
                                    onParar = {
                                        timerAtivo.value = false
                                        status.value = "CONCLUIDA"
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        SessionSoundManager.playSerieComplete()
                                        val tempoDescanso = exercicioAtual.template.tempoDescansoSegundos.coerceAtLeast(30)
                                        descansoSerieInfo = "Série $numeroSerie/$seriesContagemLocal — ${exercicioAtual.exercicio.nome}"
                                        descansoSegundos = tempoDescanso
                                        descansoAtivo = true
                                        SessionSoundManager.playRestStart()
                                    },
                                    onEditarTempo = { novoTempo ->
                                        timerSeg.intValue = novoTempo.coerceAtLeast(0)
                                    },
                                    onCargaChange = { carga.value = it },
                                    onPular = {
                                        timerAtivo.value = false
                                        status.value = if (status.value == "PULADA") "PENDENTE" else "PULADA"
                                    }
                                )
                            } else {
                                val reps = repsLocais[serieId] ?: continue

                                SerieStepperRow(
                                    numeroSerie = numeroSerie,
                                    totalSeries = seriesContagemLocal,
                                    reps = reps.intValue,
                                    carga = carga.value,
                                    status = status.value,
                                    isPr = run {
                                        if (status.value != "CONCLUIDA") return@run false
                                        val cargaDouble = carga.value.trim().toDoubleOrNull() ?: return@run false
                                        val max = maxCargaHistorica ?: return@run false
                                        cargaDouble > max
                                    },
                                    onRepsChange = { reps.intValue = it.coerceAtLeast(0) },
                                    onCargaChange = { carga.value = it },
                                    onMarcarConcluida = {
                                        val wasConcluida = status.value == "CONCLUIDA"
                                        status.value = if (wasConcluida) "PENDENTE" else "CONCLUIDA"
                                        if (!wasConcluida) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            SessionSoundManager.playSerieComplete()
                                            val tempoDescanso = exercicioAtual.template.tempoDescansoSegundos.coerceAtLeast(30)
                                            descansoSerieInfo = "Série $numeroSerie/$seriesContagemLocal — ${exercicioAtual.exercicio.nome}"
                                            descansoSegundos = tempoDescanso
                                            descansoAtivo = true
                                            SessionSoundManager.playRestStart()
                                        }
                                    },
                                    onPular = {
                                        status.value = if (status.value == "PULADA") "PENDENTE" else "PULADA"
                                    }
                                )
                            }
                        }
                    }
                }

                // Controles adicionar/remover série
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleIconButton(
                            onClick = { if (seriesContagemLocal > 1) seriesContagemLocal-- },
                            backgroundColor = if (seriesContagemLocal > 1) colors.featureRed.copy(alpha = 0.18f) else colors.lightGray.copy(alpha = 0.5f),
                            icon = Icons.Filled.Remove,
                            contentDescription = "Remover série",
                            tint = if (seriesContagemLocal > 1) colors.featureRed else colors.textSecondary
                        )
                        Text(
                            "$seriesContagemLocal séries",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        CircleIconButton(
                            onClick = { seriesContagemLocal++ },
                            backgroundColor = colors.primary.copy(alpha = 0.14f),
                            icon = Icons.Filled.Add,
                            contentDescription = "Adicionar série",
                            tint = colors.primary
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                // Botão concluir exercício
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isUltimo = exercicioIndex == exerciciosOrdenados.lastIndex
                        Button(
                            onClick = {
                                if (isUltimo) {
                                    mostrarDialogFinalizar = true
                                } else {
                                    statusLocais.values.forEach { s ->
                                        if (s.value == "PENDENTE") s.value = "PULADA"
                                    }
                                    salvarSeriesExercicio(concluirExercicio = true)
                                    exercicioIndex++
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.textOnPrimary
                            ),
                            enabled = seriesState !is SessaoSeriesUiState.Loading
                        ) {
                            if (seriesState is SessaoSeriesUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.textOnPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    if (isUltimo) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                    null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isUltimo) "FINALIZAR TREINO" else "PRÓXIMO EXERCÍCIO",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Overlay descanso
    if (descansoAtivo && descansoSegundos > 0) {
        DescansoOverlay(
            segundosRestantes = descansoSegundos,
            tempoTotal = exercicioAtual.template.tempoDescansoSegundos.coerceAtLeast(30),
            serieInfo = descansoSerieInfo,
            onAdicionarDez = { descansoSegundos += 10 },
            onPular = { descansoAtivo = false; descansoSegundos = 0 }
        )
    }

    if (mostrarDialogCancelar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogCancelar = false },
            containerColor = colors.surface,
            title = { Text("Cancelar sessão?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("O progresso será perdido.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogCancelar = false; viewModel.cancelar(sessao.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.textOnPrimary)
                ) { Text("Cancelar sessão") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogCancelar = false }) { Text("Continuar", color = colors.primary) }
            }
        )
    }

    if (mostrarDialogFinalizar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogFinalizar = false },
            containerColor = colors.surface,
            title = { Text("Finalizar treino?", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Séries pendentes serão marcadas como puladas.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogFinalizar = false
                        statusLocais.values.forEach { s -> if (s.value == "PENDENTE") s.value = "PULADA" }
                        salvarSeriesExercicio(concluirExercicio = true)
                        viewModel.registrarPrsBatidos(exerciciosComPr)
                        viewModel.finalizar(sessao.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)
                ) { Text("Finalizar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogFinalizar = false }) { Text("Voltar", color = colors.textSecondary) }
            }
        )
    }
}

// ─── GIF / WebM do exercício ──────────────────────────────────────────────────

@Composable
private fun ExercicioGifSection(
    animacaoUrl: String?,
    nomExercicio: String
) {
    val colors = LocalAcademiaColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(colors.surface),
        contentAlignment = Alignment.Center
    ) {
        if (!animacaoUrl.isNullOrBlank()) {
            AnimacaoPlayer(
                url = animacaoUrl,
                contentDescription = nomExercicio,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.FitnessCenter, null, tint = colors.textSecondary, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text("Sem animação", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ─── Header do exercício ──────────────────────────────────────────────────────

@Composable
private fun ExercicioHeaderSection(
    exercicio: SessaoExercicioData,
    index: Int,
    total: Int,
    concluidos: Int,
    onVoltar: () -> Unit,
    onAvancar: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Navegação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar, enabled = index > 0) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exercício anterior",
                    tint = if (index > 0) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Exercício ${index + 1} de $total",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "$concluidos concluídos",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onAvancar, enabled = index < total - 1) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Próximo exercício",
                    tint = if (index < total - 1) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Nome + badge concluído
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    exercicio.exercicio.nome,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val eTe = exercicio.template.tipo == TipoExercicio.TEMPO
                    val metricaText = if (eTe) {
                        val dur = exercicio.template.duracaoSugeridaSegundos
                        if (dur != null) "${exercicio.template.series} séries × ${formatarTempo(dur)}" else "${exercicio.template.series} séries"
                    } else {
                        "${exercicio.template.series} séries × ${exercicio.template.repeticoes.orEmpty()} reps"
                    }
                    Text(
                        metricaText,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!eTe) {
                        exercicio.template.cargaSugerida?.let { carga ->
                            Text("· $carga kg sugerido", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (exercicio.concluido) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, "Concluído", tint = colors.primary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = colors.lightGray.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))
    }
}

// ─── Série com steppers ───────────────────────────────────────────────────────

@Composable
private fun SerieStepperRow(
    numeroSerie: Int,
    totalSeries: Int,
    reps: Int,
    carga: String,
    status: String,
    isPr: Boolean = false,
    onRepsChange: (Int) -> Unit,
    onCargaChange: (String) -> Unit,
    onMarcarConcluida: () -> Unit,
    onPular: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    var expandido by remember { mutableStateOf(true) }
    LaunchedEffect(status) { if (status == "CONCLUIDA" || status == "PULADA") expandido = false }

    val chevronAngulo by animateFloatAsState(
        targetValue = if (expandido) 0f else -90f,
        animationSpec = tween(200), label = "chevron"
    )
    val bgColor by animateColorAsState(
        targetValue = when (status) {
            "CONCLUIDA" -> colors.primary.copy(alpha = 0.12f)
            "PULADA" -> colors.error.copy(alpha = 0.08f)
            else -> colors.surface
        },
        animationSpec = tween(250), label = "serieBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            "CONCLUIDA" -> colors.primary.copy(alpha = 0.5f)
            "PULADA" -> colors.error.copy(alpha = 0.3f)
            else -> colors.lightGray.copy(alpha = 0.5f)
        },
        animationSpec = tween(250), label = "serieBorder"
    )

    val cargaDouble = carga.trim().toDoubleOrNull()
    val cargaFmt = cargaDouble?.let { if (it % 1.0 == 0.0) "${it.toInt()}" else "$it" } ?: carga.ifBlank { null }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // Header — clicável para colapsar/expandir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge + info compacta quando colapsado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (status) {
                                    "CONCLUIDA" -> colors.primary.copy(alpha = 0.2f)
                                    "PULADA" -> colors.error.copy(alpha = 0.15f)
                                    else -> colors.lightGray
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Série $numeroSerie/$totalSeries",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                "CONCLUIDA" -> colors.primary
                                "PULADA" -> colors.error
                                else -> colors.textSecondary
                            }
                        )
                    }
                    if (status == "CONCLUIDA" && isPr) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.featureOrange.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "PR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.featureOrange
                            )
                        }
                    }
                    if (!expandido && status != "PENDENTE") {
                        val info = when (status) {
                            "CONCLUIDA" -> buildString {
                                append("${reps}×")
                                if (cargaFmt != null) append(" · ${cargaFmt}kg")
                            }
                            "PULADA" -> "pulada"
                            else -> ""
                        }
                        if (info.isNotBlank()) {
                            Text(
                                info,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (status == "CONCLUIDA") colors.primary else colors.textSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
                // Chevron + ações
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = chevronAngulo }
                    )
                    IconButton(onClick = onPular, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.SkipNext, "Pular", tint = if (status == "PULADA") colors.error else colors.textSecondary, modifier = Modifier.size(22.dp))
                    }
                    CircleIconButton(
                        onClick = onMarcarConcluida,
                        backgroundColor = if (status == "CONCLUIDA") colors.primary else colors.lightGray,
                        icon = Icons.Filled.Check,
                        contentDescription = "Concluir",
                        tint = if (status == "CONCLUIDA") colors.textOnPrimary else colors.textSecondary
                    )
                }
            }

            // Corpo expansível
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200))
            ) {
                if (status != "PULADA") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StepperInput(
                            label = "Reps",
                            valor = "$reps",
                            modifier = Modifier.weight(1f),
                            onMenos = { onRepsChange(reps - 1) },
                            onMais = { onRepsChange(reps + 1) }
                        )
                        StepperInput(
                            label = "Kg",
                            valor = cargaFmt ?: "—",
                            modifier = Modifier.weight(1f),
                            onMenos = {
                                val atual = carga.trim().toDoubleOrNull() ?: 0.0
                                val novo = (atual - 0.5).coerceAtLeast(0.0)
                                onCargaChange(if (novo % 1.0 == 0.0) "${novo.toInt()}" else String.format(java.util.Locale.US, "%.1f", novo))
                            },
                            onMais = {
                                val atual = carga.trim().toDoubleOrNull() ?: 0.0
                                val novo = atual + 0.5
                                onCargaChange(if (novo % 1.0 == 0.0) "${novo.toInt()}" else String.format(java.util.Locale.US, "%.1f", novo))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Stepper genérico ─────────────────────────────────────────────────────────

@Composable
private fun StepperInput(
    label: String,
    valor: String,
    modifier: Modifier = Modifier,
    onMenos: () -> Unit,
    onMais: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = colors.textSecondary, style = MaterialTheme.typography.labelSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircleIconButton(
                onClick = onMenos,
                backgroundColor = colors.featureRed.copy(alpha = 0.18f),
                icon = Icons.Filled.Remove,
                contentDescription = "Diminuir",
                tint = colors.featureRed
            )
            Text(
                valor,
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )
            CircleIconButton(
                onClick = onMais,
                backgroundColor = colors.primary,
                icon = Icons.Filled.Add,
                contentDescription = "Aumentar",
                tint = colors.textOnPrimary
            )
        }
    }
}

// ─── Botão circular reutilizável ──────────────────────────────────────────────

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

// ─── Overlay de descanso ──────────────────────────────────────────────────────

@Composable
private fun DescansoOverlay(
    segundosRestantes: Int,
    tempoTotal: Int,
    serieInfo: String = "",
    onAdicionarDez: () -> Unit,
    onPular: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val progresso = if (tempoTotal > 0) (segundosRestantes.toFloat() / tempoTotal).coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.95f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "DESCANSE",
                    color = colors.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                if (serieInfo.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        serieInfo,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progresso },
                    modifier = Modifier.size(200.dp),
                    color = colors.primary,
                    trackColor = colors.lightGray,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatarTempo(segundosRestantes),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("restante", color = colors.textSecondary, style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAdicionarDez,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.textPrimary)
                ) {
                    Icon(Icons.Filled.Timer, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("+10 seg", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onPular,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.lightGray, contentColor = colors.textPrimary)
                ) {
                    Icon(Icons.Filled.SkipNext, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Pular", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    val colors = LocalAcademiaColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = colors.primary)
            Spacer(Modifier.height(16.dp))
            Text("Carregando sessão...", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─── Sem sessão ───────────────────────────────────────────────────────────────

@Composable
private fun SemSessaoAtivaConteudo(onBack: () -> Unit) {
    val colors = LocalAcademiaColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Filled.FitnessCenter, null, tint = colors.textSecondary, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Nenhuma sessão ativa", color = colors.textPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Inicie um treino para começar.", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)) {
                Text("Voltar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Erro ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErroConteudo(message: String, onBack: () -> Unit, onRetry: () -> Unit) {
    val colors = LocalAcademiaColors.current
    Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Falha ao carregar sessão", color = colors.textPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(message, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)) {
                Text("Tentar novamente", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onBack) { Text("Voltar", color = colors.textSecondary) }
        }
    }
}

// ─── Resumo da sessão ─────────────────────────────────────────────────────────

@Composable
private fun ResumoSessaoConteudo(
    sessao: SessaoData,
    resumo: SessaoResumoData,
    prsBatidos: Int = 0,
    onVoltar: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    Scaffold(
        containerColor = colors.background,
        topBar = { AcademiaAppBar(title = "Sessão Concluída") }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.backgroundGradientStart, colors.backgroundGradientEnd)))
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f))
                            .border(3.dp, colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Parabéns!", color = colors.primary, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text(sessao.treinoNome, color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                    Text("Treino finalizado com sucesso", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    if (prsBatidos > 0) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.featureOrange.copy(alpha = 0.15f))
                                .border(1.dp, colors.featureOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (prsBatidos == 1) "Novo recorde pessoal!" else "$prsBatidos novos recordes pessoais!",
                                color = colors.featureOrange,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Resumo do treino", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = colors.lightGray.copy(alpha = 0.5f))

                        resumo.duracaoMinutos?.let { dur ->
                            ResumoItem(rotulo = "Duração", valor = "$dur min", icone = Icons.Filled.Timer)
                        }
                        ResumoItem(rotulo = "Exercícios", valor = "${resumo.exerciciosConcluidos}/${resumo.exerciciosTotal}", icone = Icons.Filled.FitnessCenter)
                        ResumoItem(rotulo = "Séries", valor = "${resumo.seriesConcluidas}/${resumo.seriesTotal}", icone = Icons.Filled.CheckCircle)
                        if (resumo.volumeTotalKg > 0) {
                            ResumoItem(rotulo = "Volume total", valor = "${String.format("%.1f", resumo.volumeTotalKg)} kg", icone = Icons.Filled.FitnessCenter)
                        }
                        if (resumo.tempoTotalIsometriaSegundos > 0) {
                            ResumoItem(
                                rotulo = "Tempo isometria",
                                valor = formatarTempoAmigavel(resumo.tempoTotalIsometriaSegundos),
                                icone = Icons.Filled.Timer
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxa de conclusão", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${resumo.taxaConclusao.toInt()}%",
                                    color = colors.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (resumo.taxaConclusao / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                                color = colors.primary,
                                trackColor = colors.lightGray,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onVoltar,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("VOLTAR AO INÍCIO", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ResumoItem(
    rotulo: String,
    valor: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector
) {
    val colors = LocalAcademiaColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(rotulo, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(valor, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Série com cronômetro (exercícios por TEMPO) ─────────────────────────────

@Composable
private fun SerieTempoRow(
    numeroSerie: Int,
    totalSeries: Int,
    segundos: Int,
    metaSegundos: Int?,
    carga: String,
    status: String,
    timerAtivo: Boolean,
    isPr: Boolean = false,
    onAlterarMeta: (Int) -> Unit,
    onIniciar: () -> Unit,
    onParar: () -> Unit,
    onEditarTempo: (Int) -> Unit,
    onCargaChange: (String) -> Unit,
    onPular: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val progresso = if (metaSegundos != null && metaSegundos > 0)
        (segundos.toFloat() / metaSegundos).coerceIn(0f, 1f) else 0f
    val metaBatida = metaSegundos != null && segundos >= metaSegundos
    val isIdle = status == "PENDENTE" && !timerAtivo

    var expandido by remember { mutableStateOf(true) }
    // Timer ativo: sempre expandido. Concluída/Pulada: auto-colapsa.
    LaunchedEffect(status, timerAtivo) {
        if (timerAtivo) expandido = true
        else if (status == "CONCLUIDA" || status == "PULADA") expandido = false
    }

    val podeColapsar = !timerAtivo
    val chevronAngulo by animateFloatAsState(
        targetValue = if (expandido) 0f else -90f,
        animationSpec = tween(200), label = "chevronTempo"
    )

    var editandoTempoRealizado by remember { mutableStateOf(false) }
    var tempoEditText by remember(segundos) { mutableStateOf(segundos.toString()) }

    val cargaFmt = carga.trim().toDoubleOrNull()?.let {
        if (it % 1.0 == 0.0) "${it.toInt()}" else "$it"
    } ?: carga.ifBlank { null }

    val bgColor by animateColorAsState(
        targetValue = when (status) {
            "CONCLUIDA" -> if (metaBatida) colors.primary.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.08f)
            "PULADA" -> colors.error.copy(alpha = 0.08f)
            else -> colors.surface
        },
        animationSpec = tween(250), label = "serieTempoBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            "CONCLUIDA" -> if (metaBatida) colors.primary.copy(alpha = 0.8f) else colors.primary.copy(alpha = 0.4f)
            "PULADA" -> colors.error.copy(alpha = 0.3f)
            else -> if (timerAtivo) colors.primary.copy(alpha = 0.7f) else colors.lightGray.copy(alpha = 0.5f)
        },
        animationSpec = tween(250), label = "serieTempoBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (timerAtivo || status == "CONCLUIDA") 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {

            // Header — clicável para colapsar/expandir (bloqueado enquanto timer ativo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (podeColapsar) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expandido = !expandido }
                        else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (status) {
                                    "CONCLUIDA" -> colors.primary.copy(alpha = 0.2f)
                                    "PULADA" -> colors.error.copy(alpha = 0.15f)
                                    else -> if (timerAtivo) colors.primary.copy(alpha = 0.15f) else colors.lightGray
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Série $numeroSerie/$totalSeries",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                "CONCLUIDA" -> colors.primary
                                "PULADA" -> colors.error
                                else -> if (timerAtivo) colors.primary else colors.textSecondary
                            }
                        )
                    }
                    if (status == "CONCLUIDA" && isPr) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.featureOrange.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "PR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.featureOrange
                            )
                        }
                    }
                    // Info compacta quando colapsado
                    if (!expandido && status != "PENDENTE") {
                        val info = when (status) {
                            "CONCLUIDA" -> buildString {
                                append(formatarTempo(segundos))
                                if (cargaFmt != null) append(" · ${cargaFmt}kg")
                            }
                            "PULADA" -> "pulada"
                            else -> ""
                        }
                        if (info.isNotBlank()) {
                            Text(
                                info,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (status == "CONCLUIDA") colors.primary else colors.textSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (podeColapsar) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = chevronAngulo }
                        )
                    }
                    if (status == "CONCLUIDA") {
                        IconButton(onClick = {
                            editandoTempoRealizado = !editandoTempoRealizado
                            if (!expandido) expandido = true
                        }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Edit, "Corrigir tempo", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onPular, modifier = Modifier.size(40.dp), enabled = !timerAtivo) {
                        Icon(Icons.Filled.SkipNext, "Pular", tint = if (status == "PULADA") colors.error else colors.textSecondary, modifier = Modifier.size(22.dp))
                    }
                }
            }

            // Corpo expansível
            AnimatedVisibility(
                visible = expandido && status != "PULADA",
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        isIdle -> {
                            MetaTempoStepper(metaSegundos = metaSegundos ?: 0, onAlterarMeta = onAlterarMeta)
                            if (carga.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StepperInput(
                                        label = "Kg",
                                        valor = cargaFmt ?: "—",
                                        modifier = Modifier.weight(1f),
                                        onMenos = {
                                            val atual = carga.trim().toDoubleOrNull() ?: 0.0
                                            val novo = (atual - 0.5).coerceAtLeast(0.0)
                                            onCargaChange(if (novo % 1.0 == 0.0) "${novo.toInt()}" else String.format(java.util.Locale.US, "%.1f", novo))
                                        },
                                        onMais = {
                                            val atual = carga.trim().toDoubleOrNull() ?: 0.0
                                            val novo = atual + 0.5
                                            onCargaChange(if (novo % 1.0 == 0.0) "${novo.toInt()}" else String.format(java.util.Locale.US, "%.1f", novo))
                                        }
                                    )
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Button(
                                onClick = onIniciar,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Iniciar", fontWeight = FontWeight.Bold)
                            }
                        }
                        timerAtivo -> {
                            TimerCirculo(segundos = segundos, metaSegundos = metaSegundos, progresso = progresso, metaBatida = metaBatida, corPrincipal = colors.primary, corTexto = colors.textPrimary)
                            Button(
                                onClick = onParar,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.textOnPrimary),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.Stop, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Parar", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            TimerCirculo(segundos = segundos, metaSegundos = metaSegundos, progresso = progresso, metaBatida = metaBatida, corPrincipal = if (metaBatida) colors.primary else colors.primary.copy(alpha = 0.5f), corTexto = colors.primary, tamanho = 120)
                            if (editandoTempoRealizado) {
                                OutlinedTextField(
                                    value = tempoEditText,
                                    onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) tempoEditText = it },
                                    label = { Text("Tempo realizado (s)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.lightGray,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        cursorColor = colors.primary,
                                        focusedLabelColor = colors.primary
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            tempoEditText.toIntOrNull()?.let { onEditarTempo(it) }
                                            editandoTempoRealizado = false
                                        }) {
                                            Icon(Icons.Filled.Check, "Confirmar", tint = colors.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaTempoStepper(metaSegundos: Int, onAlterarMeta: (Int) -> Unit) {
    val colors = LocalAcademiaColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FilledTonalIconButton(
            onClick = { onAlterarMeta((metaSegundos - 5).coerceAtLeast(5)) },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.featureRed.copy(alpha = 0.18f),
                contentColor = colors.featureRed
            )
        ) {
            Text("−5s", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatarTempo(metaSegundos),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )
            Text(
                "duração desejada",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
        FilledTonalIconButton(
            onClick = { onAlterarMeta(metaSegundos + 5) },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.primary.copy(alpha = 0.14f),
                contentColor = colors.primary
            )
        ) {
            Text("+5s", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TimerCirculo(
    segundos: Int,
    metaSegundos: Int?,
    progresso: Float,
    metaBatida: Boolean,
    corPrincipal: androidx.compose.ui.graphics.Color,
    corTexto: androidx.compose.ui.graphics.Color,
    tamanho: Int = 140
) {
    val colors = LocalAcademiaColors.current
    Box(contentAlignment = Alignment.Center) {
        if (metaSegundos != null) {
            CircularProgressIndicator(
                progress = { progresso },
                modifier = Modifier.size(tamanho.dp),
                color = corPrincipal,
                trackColor = colors.lightGray,
                strokeWidth = if (tamanho >= 140) 8.dp else 6.dp,
                strokeCap = StrokeCap.Round
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatarTempo(segundos),
                color = corTexto,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            if (metaSegundos != null) {
                Text(
                    "meta: ${formatarTempo(metaSegundos)}",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (metaBatida) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Meta atingida!",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Util ─────────────────────────────────────────────────────────────────────

private fun formatarTempoAmigavel(segundos: Int): String {
    if (segundos <= 0) return "0s"
    val m = segundos / 60
    val s = segundos % 60
    return when {
        m == 0 -> "${s}s"
        s == 0 -> "${m}min"
        else -> "${m}min ${s}s"
    }
}

private fun formatarTempo(segundos: Int): String {
    val h = segundos / 3600
    val m = (segundos % 3600) / 60
    val s = segundos % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
