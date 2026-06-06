package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import dev.fslab.academia.model.ProgressaoItemData
import dev.fslab.academia.model.RecordeExercicioData
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.AcademiaColors
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.HistoricoViewModel
import dev.fslab.academia.ui.viewmodel.PeriodoFiltro
import dev.fslab.academia.ui.viewmodel.ProgressaoUiState
import dev.fslab.academia.ui.viewmodel.RecordeUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PERIODO_LABELS_PROG = listOf("7 dias", "30 dias", "3 meses", "Tudo", "Personalizado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoProgressaoScreen(
    exercicioId: String,
    exercicioNome: String,
    onBack: () -> Unit = {},
    viewModel: HistoricoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val progressaoState by viewModel.progressaoState.collectAsState()
    val periodoFiltro by viewModel.periodoFiltro.collectAsState()
    val recordeState by viewModel.recordeState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()

    LaunchedEffect(exercicioId) {
        viewModel.carregarProgressao(exercicioId, exercicioNome)
        viewModel.carregarRecorde(exercicioId)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = datePickerState.selectedStartDateMillis
                    val end = datePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.carregarProgressao(exercicioId, exercicioNome, PeriodoFiltro.personalizado(start, end))
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
        topBar = {
            AcademiaAppBar(
                title = exercicioNome.ifBlank { "Progressão" },
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = colors.background
    ) { padding ->
        when (val state = progressaoState) {
            is ProgressaoUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is ProgressaoUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(state.message, color = colors.errorText, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.carregarProgressao(exercicioId, exercicioNome) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) { Text("Tentar novamente", color = colors.textOnPrimary) }
                    }
                }
            }
            is ProgressaoUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        CardRecordePr(recordeState = recordeState, colors = colors)
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PERIODO_LABELS_PROG) { label ->
                                val isSelected = periodoFiltro.label == label
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val filtro = when (label) {
                                            "7 dias" -> PeriodoFiltro.seteDias()
                                            "30 dias" -> PeriodoFiltro.trintaDias()
                                            "3 meses" -> PeriodoFiltro.tresMeses()
                                            "Tudo" -> PeriodoFiltro.TUDO
                                            else -> { showDatePicker = true; return@FilterChip }
                                        }
                                        viewModel.carregarProgressao(exercicioId, exercicioNome, filtro)
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

                    if (state.progressao.isEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhum dado de progressão disponível para este período",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        val progressaoCronologica = state.progressao.sortedBy { it.data }
                        val tipoExercicio = progressaoCronologica.firstOrNull()?.tipo ?: TipoExercicio.REPETICAO
                        val eTempo = tipoExercicio == TipoExercicio.TEMPO

                        item {
                            MetricasProgressao(progressao = progressaoCronologica, eTempo = eTempo, colors = colors)
                        }

                        if (eTempo) {
                            val temMelhorTempo = progressaoCronologica.any { it.melhorTempoSegundos != null }
                            if (temMelhorTempo) {
                                item {
                                    GraficoProgressao(
                                        titulo = "Melhor tempo por sessão",
                                        corLinha = colors.featureOrange,
                                        pontos = progressaoCronologica.map { (it.melhorTempoSegundos ?: 0).toFloat() },
                                        datas = progressaoCronologica.map { it.data },
                                        colors = colors,
                                        formatarValor = { formatarSegundosCurto(it.toInt()) }
                                    )
                                }
                            }
                            val temMediaTempo = progressaoCronologica.any { it.mediaTempoSegundos != null }
                            if (temMediaTempo) {
                                item {
                                    GraficoProgressao(
                                        titulo = "Tempo médio por sessão",
                                        corLinha = colors.featureCyan,
                                        pontos = progressaoCronologica.map { (it.mediaTempoSegundos ?: 0).toFloat() },
                                        datas = progressaoCronologica.map { it.data },
                                        colors = colors,
                                        formatarValor = { formatarSegundosCurto(it.toInt()) }
                                    )
                                }
                            }
                        } else {
                            item {
                                GraficoProgressao(
                                    titulo = "Volume total por sessão (kg)",
                                    corLinha = colors.primary,
                                    pontos = progressaoCronologica.map { it.volumeTotal.toFloat() },
                                    datas = progressaoCronologica.map { it.data },
                                    colors = colors,
                                    formatarValor = { "%.0f kg".format(it) }
                                )
                            }

                            val temCarga = progressaoCronologica.any { it.maiorCarga != null }
                            if (temCarga) {
                                item {
                                    GraficoProgressao(
                                        titulo = "Carga máxima por sessão (kg)",
                                        corLinha = Color(0xFFF59E0B),
                                        pontos = progressaoCronologica.map { (it.maiorCarga ?: 0.0).toFloat() },
                                        datas = progressaoCronologica.map { it.data },
                                        colors = colors,
                                        formatarValor = { "%.1f kg".format(it) }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                "Sessões recentes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        if (eTempo) {
                            val maxTempo = state.progressao.mapNotNull { it.melhorTempoSegundos }.maxOrNull() ?: 1
                            items(state.progressao.sortedByDescending { it.data }) { item ->
                                ProgressaoItemCardTempo(item = item, maxTempo = maxTempo, colors = colors)
                            }
                        } else {
                            val maxVolume = state.progressao.maxOfOrNull { it.volumeTotal } ?: 1.0
                            items(state.progressao.sortedByDescending { it.data }) { item ->
                                ProgressaoItemCard(item = item, maxVolume = maxVolume, colors = colors)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricasProgressao(progressao: List<ProgressaoItemData>, eTempo: Boolean, colors: AcademiaColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (eTempo) {
            val melhorTempo = progressao.mapNotNull { it.melhorTempoSegundos }.maxOrNull() ?: 0
            val mediaTempo = progressao.mapNotNull { it.mediaTempoSegundos }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
            val totalTempo = progressao.sumOf { it.tempoTotalSegundos }
            MetricaCard(
                modifier = Modifier.weight(1f),
                valor = if (melhorTempo > 0) formatarSegundosCurto(melhorTempo) else "—",
                label = "melhor tempo",
                colors = colors,
                corValor = colors.featureOrange
            )
            MetricaCard(
                modifier = Modifier.weight(1f),
                valor = if (mediaTempo > 0) formatarSegundosCurto(mediaTempo) else "—",
                label = "tempo médio",
                colors = colors,
                corValor = colors.featureCyan
            )
            MetricaCard(
                modifier = Modifier.weight(1f),
                valor = if (totalTempo > 0) formatarSegundosCurto(totalTempo) else "—",
                label = "tempo total",
                colors = colors
            )
        } else {
            val maxVolume = progressao.maxOfOrNull { it.volumeTotal } ?: 0.0
            val maxCarga = progressao.mapNotNull { it.maiorCarga }.maxOrNull() ?: 0.0
            val mediaRep = progressao.mapNotNull { it.mediaRepeticoes }.average().takeIf { !it.isNaN() } ?: 0.0
            MetricaCard(modifier = Modifier.weight(1f), valor = "%.0f kg".format(maxVolume), label = "vol. máx.", colors = colors)
            MetricaCard(
                modifier = Modifier.weight(1f),
                valor = if (maxCarga > 0) "%.1f kg".format(maxCarga) else "—",
                label = "carga máx.",
                colors = colors,
                corValor = Color(0xFFF59E0B)
            )
            MetricaCard(
                modifier = Modifier.weight(1f),
                valor = if (mediaRep > 0) "%.1f".format(mediaRep) else "—",
                label = "méd. rep.",
                colors = colors,
                corValor = Color(0xFF60A5FA)
            )
        }
    }
}

@Composable
private fun MetricaCard(
    modifier: Modifier = Modifier,
    valor: String,
    label: String,
    colors: AcademiaColors,
    corValor: Color = colors.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = corValor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun GraficoProgressao(
    titulo: String,
    corLinha: Color,
    pontos: List<Float>,
    datas: List<String>,
    colors: AcademiaColors,
    formatarValor: (Float) -> String = { "%.1f".format(it) }
) {
    if (pontos.size < 2) return

    val maxVal = pontos.max()
    val minVal = pontos.min()
    val range = if (maxVal == minVal) 1f else maxVal - minVal

    var selectedIndex by remember { mutableIntStateOf(pontos.lastIndex) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(titulo, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(corLinha)
                )
            }
            Spacer(Modifier.height(8.dp))

            fun indiceMaisProximo(offsetX: Float, canvasWidth: Float): Int {
                if (pontos.size <= 1) return 0
                return pontos.indices.minByOrNull { i ->
                    val px = i * canvasWidth / (pontos.size - 1)
                    kotlin.math.abs(offsetX - px)
                } ?: 0
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .pointerInput(pontos) {
                        detectTapGestures { offset ->
                            selectedIndex = indiceMaisProximo(offset.x, size.width.toFloat())
                        }
                    }
                    .pointerInput(pontos) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            val w = size.width.toFloat()
                            if (w > 0 && pontos.size > 1) {
                                val step = w / (pontos.size - 1)
                                val newX = (selectedIndex * step + dragAmount).coerceIn(0f, w)
                                selectedIndex = indiceMaisProximo(newX, w)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val padTop = 20f
                val padBottom = 20f
                val chartH = h - padTop - padBottom

                fun xAt(i: Int): Float = if (pontos.size > 1) i * w / (pontos.size - 1) else w / 2f
                fun yAt(v: Float): Float = padTop + chartH * (1f - (v - minVal) / range)

                val linePath = Path()
                val fillPath = Path()

                pontos.forEachIndexed { i, v ->
                    val x = xAt(i)
                    val y = yAt(v)
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h - padBottom)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(xAt(pontos.lastIndex), h - padBottom)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(corLinha.copy(alpha = 0.25f), Color.Transparent),
                        startY = padTop,
                        endY = h - padBottom
                    )
                )
                drawPath(
                    path = linePath,
                    color = corLinha,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                pontos.forEachIndexed { i, v ->
                    val x = xAt(i)
                    val y = yAt(v)
                    if (i == selectedIndex) {
                        drawCircle(color = corLinha, radius = 6.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, y))
                        drawLine(
                            color = corLinha.copy(alpha = 0.3f),
                            start = Offset(x, padTop),
                            end = Offset(x, h - padBottom),
                            strokeWidth = 1.dp.toPx()
                        )
                    } else {
                        drawCircle(color = corLinha.copy(alpha = 0.4f), radius = 3.dp.toPx(), center = Offset(x, y))
                    }
                }
            }

            val si = selectedIndex.coerceIn(0, pontos.lastIndex)
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatarDataCurtaProgressao(datas.getOrNull(si)),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    fontSize = 9.sp
                )
                Text(
                    formatarValor(pontos[si]),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = corLinha
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    formatarDataCurtaProgressao(datas.firstOrNull()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 9.sp
                )
                Text(
                    formatarDataCurtaProgressao(datas.lastOrNull()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun ProgressaoItemCard(item: ProgressaoItemData, maxVolume: Double, colors: AcademiaColors) {
    val progress = if (maxVolume > 0) (item.volumeTotal / maxVolume).toFloat().coerceIn(0f, 1f) else 0f
    val dataFormatada = formatarDataProgressaoItem(item.data)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(dataFormatada, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    val detalhes = buildString {
                        item.maiorCarga?.let { append("%.1f kg".format(it)) }
                        item.mediaRepeticoes?.let {
                            if (isNotEmpty()) append(" · ")
                            append("%.0f rep".format(it))
                        }
                    }
                    if (detalhes.isNotBlank()) {
                        Text(detalhes, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("%.0f kg".format(item.volumeTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("vol. total", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = colors.primary,
                trackColor = colors.inputBorder
            )
        }
    }
}

@Composable
private fun ProgressaoItemCardTempo(item: ProgressaoItemData, maxTempo: Int, colors: AcademiaColors) {
    val melhor = item.melhorTempoSegundos ?: 0
    val progress = if (maxTempo > 0) (melhor.toFloat() / maxTempo).coerceIn(0f, 1f) else 0f
    val dataFormatada = formatarDataProgressaoItem(item.data)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(dataFormatada, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    val detalhes = buildString {
                        if (melhor > 0) append("melhor: ${formatarSegundosCurto(melhor)}")
                        item.mediaTempoSegundos?.let {
                            if (isNotEmpty()) append(" · ")
                            append("média: ${formatarSegundosCurto(it)}")
                        }
                    }
                    if (detalhes.isNotBlank()) {
                        Text(detalhes, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (item.tempoTotalSegundos > 0) formatarSegundosCurto(item.tempoTotalSegundos) else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.featureOrange
                    )
                    Text("tempo total", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = colors.featureOrange,
                trackColor = colors.inputBorder
            )
        }
    }
}

private fun formatarSegundosCurto(segundos: Int): String {
    val min = segundos / 60
    val seg = segundos % 60
    return if (min > 0) "${min}m${seg}s" else "${seg}s"
}

private fun formatarDataCurtaProgressao(iso: String?): String {
    if (iso == null) return ""
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("dd/MM", java.util.Locale.forLanguageTag("pt-BR")).format(zdt)
    } catch (_: Exception) { "" }
}

@Composable
private fun CardRecordePr(recordeState: RecordeUiState, colors: AcademiaColors) {
    val recorde = (recordeState as? RecordeUiState.Success)?.data ?: return
    val prOuro = colors.featureOrange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, prOuro.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = prOuro.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = prOuro, modifier = Modifier.size(20.dp))
                Text(
                    "Recorde Pessoal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = prOuro
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                when (recorde.tipo) {
                    TipoExercicio.REPETICAO -> {
                        if (recorde.maiorCargaKg != null) {
                            ColunaRecorde(
                                label = "Carga máxima",
                                valor = "${recorde.maiorCargaKg.let { if (it % 1 == 0.0) it.toInt().toString() else "%.1f".format(it) }} kg",
                                detalhe = recorde.repeticoesNoPr?.let { "${it} reps" },
                                data = formatarDataRecorde(recorde.dataPrCarga),
                                colors = colors
                            )
                        }
                    }
                    TipoExercicio.TEMPO -> {
                        if (recorde.melhorTempoSegundos != null) {
                            ColunaRecorde(
                                label = "Melhor tempo",
                                valor = formatarSegundosCurto(recorde.melhorTempoSegundos),
                                detalhe = null,
                                data = formatarDataRecorde(recorde.dataPrTempo),
                                colors = colors
                            )
                        }
                    }
                    TipoExercicio.DISTANCIA -> {
                        if (recorde.maiorDistanciaMetros != null) {
                            ColunaRecorde(
                                label = "Maior distância",
                                valor = if (recorde.maiorDistanciaMetros >= 1000)
                                    "${"%.2f".format(recorde.maiorDistanciaMetros / 1000)} km"
                                else
                                    "${recorde.maiorDistanciaMetros.toInt()} m",
                                detalhe = null,
                                data = formatarDataRecorde(recorde.dataPrDistancia),
                                colors = colors
                            )
                        }
                    }
                }
                ColunaRecorde(
                    label = "Sessões",
                    valor = recorde.totalSessoes.toString(),
                    detalhe = "total",
                    data = null,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun ColunaRecorde(label: String, valor: String, detalhe: String?, data: String?, colors: AcademiaColors) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        if (detalhe != null) Text(detalhe, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        if (data != null) Text(data, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
    }
}

private fun formatarDataRecorde(iso: String?): String {
    if (iso == null) return ""
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("dd/MM/yy", Locale.forLanguageTag("pt-BR")).format(zdt)
    } catch (_: Exception) { "" }
}

private fun formatarDataProgressaoItem(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("dd 'de' MMMM", java.util.Locale.forLanguageTag("pt-BR")).format(zdt)
    } catch (_: Exception) { iso }
}
