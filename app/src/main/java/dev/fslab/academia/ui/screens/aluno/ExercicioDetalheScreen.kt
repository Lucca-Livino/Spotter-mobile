package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.ExercicioAparelhoData
import dev.fslab.academia.model.ExercicioData
import dev.fslab.academia.model.ExercicioMusculoData
import dev.fslab.academia.model.RecordeExercicioData
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AnimacaoPlayer
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.ExercicioDeletarUiState
import dev.fslab.academia.ui.viewmodel.ExercicioDetalheUiState
import dev.fslab.academia.ui.viewmodel.ExercicioViewModel
import dev.fslab.academia.ui.viewmodel.HistoricoViewModel
import dev.fslab.academia.ui.viewmodel.RecordeUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExercicioDetalheScreen(
    exercicioId: String,
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    onExcluido: () -> Unit,
    viewModel: ExercicioViewModel = viewModel(),
    histViewModel: HistoricoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val detalheState by viewModel.detalheState.collectAsState()
    val deletarState by viewModel.deletarState.collectAsState()
    val recordeState by histViewModel.recordeState.collectAsState()

    var mostrarDialogoExcluir by remember { mutableStateOf(false) }
    var mostrarDialogoConflito by remember { mutableStateOf(false) }
    var mensagemConflito by remember { mutableStateOf("") }

    LaunchedEffect(exercicioId) {
        viewModel.carregarDetalhe(exercicioId)
        histViewModel.carregarRecorde(exercicioId)
    }

    LaunchedEffect(deletarState) {
        when (val s = deletarState) {
            is ExercicioDeletarUiState.Success -> {
                viewModel.resetDeletar()
                onExcluido()
            }
            is ExercicioDeletarUiState.Conflito -> {
                mensagemConflito = s.message
                mostrarDialogoConflito = true
                mostrarDialogoExcluir = false
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Detalhes",
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    val sucesso = detalheState as? ExercicioDetalheUiState.Success
                    if (sucesso != null) {
                        IconButton(onClick = { onEditar(sucesso.exercicio.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = colors.textPrimary)
                        }
                        IconButton(onClick = { mostrarDialogoExcluir = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = colors.error)
                        }
                    }
                }
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
            when (val s = detalheState) {
                ExercicioDetalheUiState.Idle, ExercicioDetalheUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is ExercicioDetalheUiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colors.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Não foi possível carregar o exercício",
                                    color = colors.textPrimary,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(s.message, color = colors.textSecondary)
                                Button(
                                    onClick = { viewModel.carregarDetalhe(exercicioId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.textOnPrimary
                                    )
                                ) { Text("Tentar novamente") }
                            }
                        }
                    }
                }
                is ExercicioDetalheUiState.Success -> {
                    DetalheConteudo(
                        exercicio = s.exercicio,
                        recordeState = recordeState,
                        carregando = deletarState is ExercicioDeletarUiState.Loading,
                        onEditar = { onEditar(s.exercicio.id) },
                        onExcluir = { mostrarDialogoExcluir = true }
                    )
                }
            }
        }
    }

    if (mostrarDialogoExcluir) {
        val carregando = deletarState is ExercicioDeletarUiState.Loading
        AlertDialog(
            onDismissRequest = { if (!carregando) mostrarDialogoExcluir = false },
            containerColor = colors.surface,
            title = { Text("Excluir exercício?", color = colors.textPrimary) },
            text = {
                Text(
                    "Esta ação não pode ser desfeita.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletar(exercicioId) },
                    enabled = !carregando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.textOnPrimary
                    )
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoExcluir = false },
                    enabled = !carregando
                ) { Text("Cancelar", color = colors.textSecondary) }
            }
        )
    }

    if (mostrarDialogoConflito) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConflito = false; viewModel.resetDeletar() },
            containerColor = colors.surface,
            title = { Text("Exercício em uso", color = colors.textPrimary) },
            text = { Text(mensagemConflito, color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoConflito = false
                        viewModel.deletar(exercicioId, soft = true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    )
                ) { Text("Desativar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoConflito = false; viewModel.resetDeletar() }
                ) { Text("Cancelar", color = colors.textSecondary) }
            }
        )
    }

    val erroDeletar = deletarState as? ExercicioDeletarUiState.Error
    if (erroDeletar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.resetDeletar() },
            containerColor = colors.surface,
            title = { Text("Falha ao excluir", color = colors.textPrimary) },
            text = { Text(erroDeletar.message, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetDeletar() }) {
                    Text("OK", color = colors.primary)
                }
            }
        )
    }
}

@Composable
private fun DetalheConteudo(
    exercicio: ExercicioData,
    recordeState: RecordeUiState,
    carregando: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val primarios = exercicio.musculos.filter { it.tipoAtivacao == "PRIMARIO" }
    val secundarios = exercicio.musculos.filter { it.tipoAtivacao == "SECUNDARIO" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CardRecordeCompacto(recordeState = recordeState, colors = colors)
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colors.surface,
                    contentColor = colors.textPrimary
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Bolt, null, tint = colors.primary)
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                exercicio.nome,
                                color = colors.textPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            val escopo = if (exercicio.alunoId == null) "Exercício global" else "Exercício pessoal"
                            Text(
                                escopo,
                                color = colors.textSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    val descricao = exercicio.descricao?.takeIf { it.isNotBlank() }
                    if (descricao != null) {
                        Text(
                            descricao,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (!exercicio.animacaoUrl.isNullOrBlank()) {
            item {
                AnimacaoPreview(url = exercicio.animacaoUrl)
            }
        }

        item {
            CardEstatisticas(
                totalMusculos = exercicio.musculos.size,
                totalPrimarios = primarios.size,
                totalAparelhos = exercicio.aparelhos.size
            )
        }

        if (primarios.isNotEmpty()) {
            item {
                SecaoTitulo(
                    titulo = "Músculos primários",
                    icone = Icons.Filled.FitnessCenter,
                    contagem = primarios.size
                )
            }
            items(primarios, key = { "p_${it.musculoId}" }) { ItemMusculo(it, primario = true) }
        }

        if (secundarios.isNotEmpty()) {
            item {
                SecaoTitulo(
                    titulo = "Músculos secundários",
                    icone = Icons.Filled.FitnessCenter,
                    contagem = secundarios.size
                )
            }
            items(secundarios, key = { "s_${it.musculoId}" }) { ItemMusculo(it, primario = false) }
        }

        if (exercicio.aparelhos.isNotEmpty()) {
            item {
                SecaoTitulo(
                    titulo = "Aparelhos",
                    icone = Icons.Filled.Build,
                    contagem = exercicio.aparelhos.size
                )
            }
            items(exercicio.aparelhos, key = { "a_${it.aparelhoId}" }) { ItemAparelho(it) }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExcluir,
                    enabled = !carregando,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = colors.error)
                    Spacer(Modifier.size(6.dp))
                    Text("Excluir", color = colors.error, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onEditar,
                    enabled = !carregando,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    )
                ) {
                    Icon(Icons.Filled.Edit, null)
                    Spacer(Modifier.size(6.dp))
                    Text("Editar", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AnimacaoPreview(url: String) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            AnimacaoPlayer(
                url = url,
                contentDescription = "Animação do exercício",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CardEstatisticas(
    totalMusculos: Int,
    totalPrimarios: Int,
    totalAparelhos: Int
) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Estatistica("Músculos", totalMusculos.toString())
            Estatistica("Primários", totalPrimarios.toString())
            Estatistica("Aparelhos", totalAparelhos.toString())
        }
    }
}

@Composable
private fun Estatistica(rotulo: String, valor: String) {
    val colors = LocalAcademiaColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            color = colors.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            rotulo,
            color = colors.textSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SecaoTitulo(
    titulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    contagem: Int
) {
    val colors = LocalAcademiaColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(
            titulo,
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        AssistChip(
            onClick = {},
            label = { Text(contagem.toString()) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = colors.primary.copy(alpha = 0.18f),
                labelColor = colors.primary
            )
        )
    }
}

@Composable
private fun ItemMusculo(musculo: ExercicioMusculoData, primario: Boolean) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (primario) colors.primary.copy(alpha = 0.22f)
                        else colors.lightGray
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    null,
                    tint = if (primario) colors.primary else colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    musculo.nome,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    musculo.grupoMuscular,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun ItemAparelho(aparelho: ExercicioAparelhoData) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
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
                Icon(Icons.Filled.Build, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    aparelho.nome,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                aparelho.descricao?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        desc,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CardRecordeCompacto(
    recordeState: RecordeUiState,
    colors: dev.fslab.academia.ui.theme.AcademiaColors
) {
    val recorde = (recordeState as? RecordeUiState.Success)?.data ?: return
    val prOuro = colors.featureOrange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, prOuro.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = prOuro.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(prOuro.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = prOuro, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Recorde Pessoal", style = MaterialTheme.typography.labelSmall, color = prOuro, fontWeight = FontWeight.Bold)
                val valorPr = when (recorde.tipo) {
                    TipoExercicio.REPETICAO -> recorde.maiorCargaKg?.let {
                        val carga = if (it % 1 == 0.0) it.toInt().toString() else "%.1f".format(it)
                        "$carga kg" + (recorde.repeticoesNoPr?.let { r -> " × $r reps" } ?: "")
                    } ?: "—"
                    TipoExercicio.TEMPO -> recorde.melhorTempoSegundos?.let { s ->
                        val m = s / 60; val sec = s % 60
                        if (m > 0) "${m}m${sec}s" else "${sec}s"
                    } ?: "—"
                    TipoExercicio.DISTANCIA -> recorde.maiorDistanciaMetros?.let { d ->
                        if (d >= 1000) "${"%.2f".format(d / 1000)} km" else "${d.toInt()} m"
                    } ?: "—"
                }
                Text(valorPr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
            }
            val dataPr = when (recorde.tipo) {
                TipoExercicio.REPETICAO -> recorde.dataPrCarga
                TipoExercicio.TEMPO -> recorde.dataPrTempo
                TipoExercicio.DISTANCIA -> recorde.dataPrDistancia
            }
            if (dataPr != null) {
                val dataFormatada = try {
                    val zdt = Instant.parse(dataPr).atZone(ZoneId.systemDefault())
                    DateTimeFormatter.ofPattern("dd/MM/yy", Locale.forLanguageTag("pt-BR")).format(zdt)
                } catch (_: Exception) { "" }
                if (dataFormatada.isNotEmpty()) {
                    Text(dataFormatada, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                }
            }
        }
    }
}
