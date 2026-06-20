package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.HistoricoPesoData
import dev.fslab.academia.model.HistoricoPesoEntrada
import dev.fslab.academia.model.HistoricoPesoMetricas
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.AcademiaColors
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.HistoricoPesoUiState
import dev.fslab.academia.ui.viewmodel.HistoricoPesoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class PeriodoFiltro(val label: String, val meses: Long?) {
    UM_MES("1m", 1),
    TRES_MESES("3m", 3),
    SEIS_MESES("6m", 6),
    UM_ANO("1a", 12),
    TUDO("tudo", null)
}

@Composable
fun HistoricoPesoScreen(
    alunoId: String,
    onBack: () -> Unit = {},
    viewModel: HistoricoPesoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalAcademiaColors.current

    LaunchedEffect(alunoId) {
        viewModel.carregar(alunoId)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Histórico de Peso",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.backgroundGradientStart, colors.backgroundGradientEnd)))
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HistoricoPesoUiState.Loading, HistoricoPesoUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colors.primary)
                }
                is HistoricoPesoUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.FitnessCenter, null, tint = colors.textSecondary, modifier = Modifier.size(48.dp))
                        Text("Sem registros de peso", color = colors.textSecondary, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Atualize seu peso no perfil para começar a acompanhar sua evolução.",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                is HistoricoPesoUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = colors.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is HistoricoPesoUiState.Success -> {
                    HistoricoPesoConteudo(data = state.data, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun HistoricoPesoConteudo(data: HistoricoPesoData, colors: AcademiaColors) {
    var periodoSelecionado by remember { mutableStateOf(PeriodoFiltro.TUDO) }

    val entradasFiltradas = remember(periodoSelecionado, data.entradas) {
        val meses = periodoSelecionado.meses
        if (meses == null) {
            data.entradas
        } else {
            val limite = LocalDate.now().minusMonths(meses)
            data.entradas.filter { entrada ->
                try {
                    val dataEntrada = LocalDate.parse(entrada.dataAvaliacao.substring(0, 10))
                    !dataEntrada.isBefore(limite)
                } catch (_: Exception) { true }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MetricasPesoCard(metricas = data.metricas, colors = colors)
        }

        item {
            FiltrosPeriodo(
                selecionado = periodoSelecionado,
                onSelecionar = { periodoSelecionado = it },
                colors = colors
            )
        }

        if (entradasFiltradas.size >= 2) {
            item {
                GraficoPeso(entradas = entradasFiltradas, colors = colors)
            }
        }

        item {
            val quantidade = entradasFiltradas.size
            Text(
                "Registros ($quantidade)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
        }

        if (entradasFiltradas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum registro no período selecionado",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(entradasFiltradas.sortedByDescending { it.dataAvaliacao }) { entrada ->
                EntradaPesoCard(entrada = entrada, colors = colors)
            }
        }
    }
}

@Composable
private fun FiltrosPeriodo(
    selecionado: PeriodoFiltro,
    onSelecionar: (PeriodoFiltro) -> Unit,
    colors: AcademiaColors
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(PeriodoFiltro.entries) { periodo ->
            val ativo = periodo == selecionado
            FilterChip(
                selected = ativo,
                onClick = { onSelecionar(periodo) },
                label = {
                    Text(
                        periodo.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (ativo) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(alpha = 0.15f),
                    selectedLabelColor = colors.primary,
                    containerColor = colors.surface,
                    labelColor = colors.textSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = ativo,
                    selectedBorderColor = colors.primary,
                    borderColor = colors.lightGray.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun MetricasPesoCard(metricas: HistoricoPesoMetricas, colors: AcademiaColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Métricas", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = colors.lightGray.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricaItem(
                    label = "Atual",
                    valor = metricas.pesoAtualKg?.let { "%.1f kg".format(it) } ?: "—",
                    destaque = true,
                    colors = colors
                )
                MetricaItem(
                    label = "Mínimo",
                    valor = metricas.pesoMinimoKg?.let { "%.1f kg".format(it) } ?: "—",
                    colors = colors
                )
                MetricaItem(
                    label = "Máximo",
                    valor = metricas.pesoMaximoKg?.let { "%.1f kg".format(it) } ?: "—",
                    colors = colors
                )
            }

            HorizontalDivider(color = colors.lightGray.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Variação total", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    val variacao = metricas.variacaoTotalKg
                    val variacaoText = when {
                        variacao == null -> "—"
                        variacao > 0 -> "+%.1f kg".format(variacao)
                        else -> "%.1f kg".format(variacao)
                    }
                    val variacaoCor = when {
                        variacao == null -> colors.textSecondary
                        variacao > 0 -> colors.featureOrange
                        else -> colors.primary
                    }
                    Text(variacaoText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = variacaoCor)
                }

                metricas.tendencia?.let { tendencia ->
                    val (icone, cor, label) = when (tendencia) {
                        "SUBINDO" -> Triple(Icons.AutoMirrored.Filled.TrendingUp, colors.featureOrange, "Subindo")
                        "DESCENDO" -> Triple(Icons.AutoMirrored.Filled.TrendingDown, colors.primary, "Descendo")
                        else -> Triple(Icons.AutoMirrored.Filled.TrendingFlat, colors.textSecondary, "Estável")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(cor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(icone, null, tint = cor, modifier = Modifier.size(18.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = cor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            metricas.variacaoUltimaSemanKg?.let { variacao ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Última semana", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    val varText = if (variacao > 0) "+%.1f kg".format(variacao) else "%.1f kg".format(variacao)
                    val varCor = if (variacao > 0) colors.featureOrange else colors.primary
                    Text(varText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = varCor)
                }
            }

            Text(
                "${metricas.totalRegistros} registros",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun MetricaItem(label: String, valor: String, destaque: Boolean = false, colors: AcademiaColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (destaque) colors.primary else colors.textPrimary
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
    }
}

@Composable
private fun GraficoPeso(entradas: List<HistoricoPesoEntrada>, colors: AcademiaColors) {
    val entradasOrdenadas = remember(entradas) { entradas.sortedBy { it.dataAvaliacao } }
    val pontos = remember(entradasOrdenadas) { entradasOrdenadas.map { it.pesoKg.toFloat() } }

    if (pontos.size < 2) return

    val minPeso = (pontos.minOrNull() ?: 0f)
    val maxPeso = (pontos.maxOrNull() ?: 0f)
    val range = (maxPeso - minPeso).coerceAtLeast(0.1f)

    var selectedIndex by remember(pontos) { mutableIntStateOf(pontos.lastIndex) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Evolução do Peso",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            fun indiceMaisProximo(offsetX: Float, canvasWidth: Float): Int {
                if (pontos.size <= 1) return 0
                return pontos.indices.minByOrNull { i ->
                    val px = if (pontos.size > 1) i * canvasWidth / (pontos.size - 1) else canvasWidth / 2f
                    kotlin.math.abs(offsetX - px)
                } ?: 0
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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
                fun yAt(v: Float): Float = padTop + chartH * (1f - (v - minPeso) / range)

                val pathLine = Path()
                val pathFill = Path()

                pontos.forEachIndexed { i, valor ->
                    val x = xAt(i)
                    val y = yAt(valor)
                    if (i == 0) {
                        pathLine.moveTo(x, y)
                        pathFill.moveTo(x, h - padBottom)
                        pathFill.lineTo(x, y)
                    } else {
                        pathLine.lineTo(x, y)
                        pathFill.lineTo(x, y)
                    }
                }
                pathFill.lineTo(xAt(pontos.lastIndex), h - padBottom)
                pathFill.close()

                drawPath(
                    path = pathFill,
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.primary.copy(alpha = 0.25f), colors.primary.copy(alpha = 0.02f)),
                        startY = padTop, endY = h - padBottom
                    )
                )
                drawPath(
                    path = pathLine,
                    color = colors.primary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                pontos.forEachIndexed { i, valor ->
                    val x = xAt(i)
                    val y = yAt(valor)
                    if (i == selectedIndex) {
                        drawLine(
                            color = colors.primary.copy(alpha = 0.3f),
                            start = Offset(x, padTop),
                            end = Offset(x, h - padBottom),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawCircle(color = colors.primary, radius = 6.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = colors.surface, radius = 3.dp.toPx(), center = Offset(x, y))
                    } else {
                        drawCircle(color = colors.surface, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = colors.primary.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(x, y))
                    }
                }
            }

            val si = selectedIndex.coerceIn(0, pontos.lastIndex)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatarDataCurta(entradasOrdenadas.getOrNull(si)?.dataAvaliacao),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
                Text(
                    "%.1f kg".format(pontos[si]),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun EntradaPesoCard(entrada: HistoricoPesoEntrada, colors: AcademiaColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        formatarDataCompleta(entrada.dataAvaliacao),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    entrada.imc?.let { imc ->
                        val categoria = when {
                            imc < 18.5 -> "Abaixo do peso"
                            imc < 25.0 -> "Peso normal"
                            imc < 30.0 -> "Sobrepeso"
                            else -> "Obesidade"
                        }
                        Text(
                            "IMC %.1f · %s".format(imc, categoria),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.1f kg".format(entrada.pesoKg),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                entrada.alturaCm?.let {
                    Text("${it} cm", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }
        }
    }
}

private fun formatarDataCurta(iso: String?): String {
    if (iso == null) return ""
    return try {
        val date = LocalDate.parse(iso.substring(0, 10))
        date.format(DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("pt-BR")))
    } catch (_: Exception) { iso.take(10) }
}

private fun formatarDataCompleta(iso: String): String {
    return try {
        val date = LocalDate.parse(iso.substring(0, 10))
        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")))
    } catch (_: Exception) { iso.take(10) }
}
