package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MetricasPesoCard(metricas = data.metricas, colors = colors)
        }

        if (data.entradas.size >= 2) {
            item {
                GraficoPeso(entradas = data.entradas, colors = colors)
            }
        }

        item {
            Text(
                "Registros",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
        }

        items(data.entradas.sortedByDescending { it.dataAvaliacao }) { entrada ->
            EntradaPesoCard(entrada = entrada, colors = colors)
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
    val pontos = entradas.sortedBy { it.dataAvaliacao }.map { it.pesoKg.toFloat() }
    val minPeso = (pontos.minOrNull() ?: 0f) - 1f
    val maxPeso = (pontos.maxOrNull() ?: 0f) + 1f
    val amplitude = (maxPeso - minPeso).coerceAtLeast(0.1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Evolução do Peso", color = colors.textPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val w = size.width
                val h = size.height
                val step = if (pontos.size > 1) w / (pontos.size - 1) else w

                val pathLine = Path()
                val pathFill = Path()

                pontos.forEachIndexed { i, valor ->
                    val x = i * step
                    val y = h - ((valor - minPeso) / amplitude) * h
                    if (i == 0) {
                        pathLine.moveTo(x, y)
                        pathFill.moveTo(x, h)
                        pathFill.lineTo(x, y)
                    } else {
                        val prevX = (i - 1) * step
                        val prevY = h - ((pontos[i - 1] - minPeso) / amplitude) * h
                        val cx = (prevX + x) / 2
                        pathLine.cubicTo(cx, prevY, cx, y, x, y)
                        pathFill.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }
                pathFill.lineTo((pontos.size - 1) * step, h)
                pathFill.close()

                drawPath(
                    path = pathFill,
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.primary.copy(alpha = 0.25f), colors.primary.copy(alpha = 0.02f)),
                        startY = 0f, endY = h
                    )
                )
                drawPath(
                    path = pathLine,
                    color = colors.primary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Pontos
                pontos.forEachIndexed { i, valor ->
                    val x = i * step
                    val y = h - ((valor - minPeso) / amplitude) * h
                    drawCircle(color = colors.surface, radius = 5.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = colors.primary, radius = 3.5f.dp.toPx(), center = Offset(x, y))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val entradasOrdenadas = entradas.sortedBy { it.dataAvaliacao }
                Text(formatarDataCurta(entradasOrdenadas.firstOrNull()?.dataAvaliacao), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Text(formatarDataCurta(entradasOrdenadas.lastOrNull()?.dataAvaliacao), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
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
