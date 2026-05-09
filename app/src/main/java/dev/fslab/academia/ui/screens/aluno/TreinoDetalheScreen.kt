package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.model.TreinoExercicioDetalheData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinoDeletarUiState
import dev.fslab.academia.ui.viewmodel.TreinoDetalheUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel

@Composable
fun TreinoDetalheScreen(
    treinoId: String,
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    onExcluido: () -> Unit,
    onIniciarSessao: (String) -> Unit = {},
    viewModel: TreinoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val detalheState by viewModel.detalheState.collectAsState()
    val deletarState by viewModel.deletarState.collectAsState()

    var mostrarDialogoExcluir by remember { mutableStateOf(false) }

    LaunchedEffect(treinoId) { viewModel.carregarDetalhe(treinoId) }

    LaunchedEffect(deletarState) {
        if (deletarState is TreinoDeletarUiState.Success) {
            viewModel.resetDeletar()
            onExcluido()
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Detalhes do treino",
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    val sucesso = detalheState as? TreinoDetalheUiState.Success
                    if (sucesso != null) {
                        IconButton(onClick = { onEditar(sucesso.treino.id) }) {
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
                TreinoDetalheUiState.Idle, TreinoDetalheUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is TreinoDetalheUiState.Error -> {
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
                                    "Não foi possível carregar o treino",
                                    color = colors.textPrimary,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(s.message, color = colors.textSecondary)
                                Button(
                                    onClick = { viewModel.carregarDetalhe(treinoId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.textOnPrimary
                                    )
                                ) { Text("Tentar novamente") }
                            }
                        }
                    }
                }
                is TreinoDetalheUiState.Success -> {
                    DetalheConteudoTreino(
                        treino = s.treino,
                        carregando = deletarState is TreinoDeletarUiState.Loading,
                        onEditar = { onEditar(s.treino.id) },
                        onExcluir = { mostrarDialogoExcluir = true },
                        onIniciarSessao = { onIniciarSessao(s.treino.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogoExcluir) {
        val carregando = deletarState is TreinoDeletarUiState.Loading
        AlertDialog(
            onDismissRequest = { if (!carregando) mostrarDialogoExcluir = false },
            containerColor = colors.surface,
            title = { Text("Excluir treino?", color = colors.textPrimary) },
            text = {
                Text(
                    "Esta ação não pode ser desfeita.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletar(treinoId) },
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

    val erroDeletar = deletarState as? TreinoDeletarUiState.Error
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
private fun DetalheConteudoTreino(
    treino: TreinoData,
    carregando: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onIniciarSessao: () -> Unit = {}
) {
    val colors = LocalAcademiaColors.current
    val dias = treino.diasSemana.orEmpty().mapNotNull(DiaSemana::fromApi)
    val totalSeries = treino.exercicios.sumOf { it.series }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                            Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary)
                        }
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                treino.nome,
                                color = colors.textPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            treino.ordem?.let { ordem ->
                                Text(
                                    "Posição $ordem na rotina",
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    treino.descricao?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            desc,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (dias.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dias.forEach { dia ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(dia.display) },
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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EstatisticaTreino("Exercícios", treino.exercicios.size.toString())
                    EstatisticaTreino("Séries", totalSeries.toString())
                    EstatisticaTreino("Dias", dias.size.toString())
                }
            }
        }

        if (treino.exercicios.isNotEmpty()) {
            item {
                Text(
                    "Composição",
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(treino.exercicios.sortedBy { it.ordemExecucao }, key = { it.id }) { item ->
                ExercicioDoTreinoCard(item)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Sem exercícios",
                            color = colors.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Edite o treino para adicionar exercícios.",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        if (treino.exercicios.isNotEmpty()) {
            item {
                Button(
                    onClick = onIniciarSessao,
                    enabled = !carregando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar treino")
                    Spacer(Modifier.size(6.dp))
                    Text("INICIAR TREINO", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

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
private fun EstatisticaTreino(rotulo: String, valor: String) {
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
private fun ExercicioDoTreinoCard(item: TreinoExercicioDetalheData) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.ordemExecucao.toString(),
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.exercicio.nome,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    item.exercicio.descricao?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            desc,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2
                        )
                    }
                }
                Icon(Icons.Filled.Bolt, null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AtributoChip(
                    icone = Icons.Filled.Repeat,
                    rotulo = "${item.series}x ${item.repeticoes}"
                )
                AtributoChip(
                    icone = Icons.Filled.Timer,
                    rotulo = formatarDescanso(item.tempoDescansoSegundos)
                )
                item.cargaSugerida?.let { carga ->
                    AtributoChip(
                        icone = Icons.Filled.FitnessCenter,
                        rotulo = "${carga} kg"
                    )
                }
            }
        }
    }
}

@Composable
private fun AtributoChip(icone: androidx.compose.ui.graphics.vector.ImageVector, rotulo: String) {
    val colors = LocalAcademiaColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.lightGray)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(4.dp))
        Text(
            rotulo,
            color = colors.textSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun formatarDescanso(segundos: Int): String {
    if (segundos <= 0) return "sem descanso"
    if (segundos < 60) return "${segundos}s"
    val min = segundos / 60
    val rest = segundos % 60
    return if (rest == 0) "${min}min" else "${min}min ${rest}s"
}
