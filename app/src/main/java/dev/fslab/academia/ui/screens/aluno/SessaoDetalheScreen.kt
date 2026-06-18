package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.SessaoData
import dev.fslab.academia.model.SessaoExercicioData
import dev.fslab.academia.model.SessaoSerieData
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.AcademiaColors
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.HistoricoViewModel
import dev.fslab.academia.ui.viewmodel.SessaoDetalheUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessaoDetalheScreen(
    sessaoId: String,
    onBack: () -> Unit = {},
    viewModel: HistoricoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val state by viewModel.sessaoDetalheState.collectAsState()

    LaunchedEffect(sessaoId) {
        viewModel.carregarSessaoDetalhe(sessaoId)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = when (val s = state) {
                    is SessaoDetalheUiState.Success -> s.sessao.treinoNome
                    else -> "Detalhes da sessão"
                },
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        when (val s = state) {
            is SessaoDetalheUiState.Idle, SessaoDetalheUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is SessaoDetalheUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(s.message, color = colors.errorText, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.carregarSessaoDetalhe(sessaoId) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) { Text("Tentar novamente", color = colors.textOnPrimary) }
                    }
                }
            }
            is SessaoDetalheUiState.Success -> {
                SessaoDetalheConteudo(sessao = s.sessao, colors = colors, padding = padding)
            }
        }
    }
}

@Composable
private fun SessaoDetalheConteudo(
    sessao: SessaoData,
    colors: AcademiaColors,
    padding: PaddingValues
) {
    val duracaoMin = calcularDuracaoDetalhe(sessao.inicio, sessao.fim)
    val dataFormatada = formatarDataDetalhe(sessao.inicio)
    val exerciciosConcluidos = sessao.exercicios.count { it.concluido }
    val totalExercicios = sessao.exercicios.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                dataFormatada,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                            if (duracaoMin != null) {
                                Text(
                                    "⏱ $duracaoMin min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        val (badgeBg, badgeFg, badgeText) = when (sessao.status) {
                            "CONCLUIDA" -> Triple(colors.primary.copy(alpha = 0.15f), colors.primary, "CONCLUÍDA")
                            "CANCELADA" -> Triple(colors.error.copy(alpha = 0.15f), colors.error, "CANCELADA")
                            else -> Triple(colors.featureOrange.copy(alpha = 0.15f), colors.featureOrange, "EM ANDAMENTO")
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(badgeText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = badgeFg)
                        }
                    }
                    HorizontalDivider(color = colors.inputBorder.copy(alpha = 0.5f))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$exerciciosConcluidos/$totalExercicios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text("exercícios", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        val seriesConcluidas = sessao.exercicios.sumOf { ex -> ex.series.count { it.status == "CONCLUIDA" } }
                        val seriesTotal = sessao.exercicios.sumOf { it.series.size }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$seriesConcluidas/$seriesTotal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text("séries", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        val volumeTotal = sessao.exercicios.sumOf { ex ->
                            ex.series.filter { it.status == "CONCLUIDA" }.sumOf { serie ->
                                val carga = serie.cargaUtilizada?.toDoubleOrNull() ?: 0.0
                                val reps = serie.repeticoesRealizadas ?: 1
                                carga * reps
                            }
                        }
                        if (volumeTotal > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "%.0f kg".format(volumeTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text("volume", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        if (sessao.exercicios.isNotEmpty()) {
            item {
                Text(
                    "Exercícios realizados",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
            items(sessao.exercicios.sortedBy { it.ordem }) { exercicio ->
                ExercicioDetalheCard(exercicio = exercicio, colors = colors)
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ExercicioDetalheCard(exercicio: SessaoExercicioData, colors: AcademiaColors) {
    val eTempo = exercicio.exercicio.tipo == TipoExercicio.TEMPO
    val seriesConcluidas = exercicio.series.count { it.status == "CONCLUIDA" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (exercicio.concluido) colors.primary.copy(alpha = 0.18f)
                            else colors.lightGray.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = if (exercicio.concluido) colors.primary else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        exercicio.exercicio.nome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$seriesConcluidas de ${exercicio.series.size} séries concluídas",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                if (exercicio.concluido) {
                    Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Filled.RadioButtonUnchecked, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            if (exercicio.series.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exercicio.series.forEach { serie ->
                        SerieDetalheRow(serie = serie, eTempo = eTempo, colors = colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun SerieDetalheRow(serie: SessaoSerieData, eTempo: Boolean, colors: AcademiaColors) {
    val (bg, fg) = when (serie.status) {
        "CONCLUIDA" -> colors.primary.copy(alpha = 0.12f) to colors.primary
        "PULADA" -> colors.error.copy(alpha = 0.10f) to colors.error
        else -> colors.lightGray.copy(alpha = 0.3f) to colors.textSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Série ${serie.numeroSerie}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = fg
        )
        val detalhe = when {
            eTempo && serie.tempoRealizadoSegundos != null -> {
                val m = serie.tempoRealizadoSegundos / 60
                val s = serie.tempoRealizadoSegundos % 60
                if (m > 0) "${m}min ${s}s" else "${s}s"
            }
            serie.cargaUtilizada != null && serie.repeticoesRealizadas != null ->
                "${serie.cargaUtilizada} kg × ${serie.repeticoesRealizadas} rep"
            serie.cargaUtilizada != null -> "${serie.cargaUtilizada} kg"
            serie.repeticoesRealizadas != null -> "${serie.repeticoesRealizadas} rep"
            serie.status == "PULADA" -> "pulada"
            else -> "—"
        }
        Text(detalhe, style = MaterialTheme.typography.bodySmall, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

private fun calcularDuracaoDetalhe(inicio: String?, fim: String?): Long? {
    if (inicio == null || fim == null) return null
    return try {
        val i = Instant.parse(inicio)
        val f = Instant.parse(fim)
        java.time.Duration.between(i, f).toMinutes().takeIf { it > 0 }
    } catch (_: Exception) { null }
}

private fun formatarDataDetalhe(inicio: String?): String {
    if (inicio == null) return ""
    return try {
        val instant = Instant.parse(inicio)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy · HH'h'mm",
            java.util.Locale.forLanguageTag("pt-BR")).format(zdt)
    } catch (_: Exception) { inicio }
}
