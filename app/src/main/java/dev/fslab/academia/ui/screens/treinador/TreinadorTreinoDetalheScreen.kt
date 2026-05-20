package dev.fslab.academia.ui.screens.treinador

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.model.TreinoExercicioDetalheData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.TreinadorSelecaoAlunoBottomSheet
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinoDeletarUiState
import dev.fslab.academia.ui.viewmodel.TreinoDetalheUiState
import dev.fslab.academia.ui.viewmodel.TreinoDuplicarUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel

@Composable
fun TreinadorTreinoDetalheScreen(
    treinoId: String,
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    onExcluido: () -> Unit,
    viewModel: TreinoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current
    val detalheState by viewModel.detalheState.collectAsState()
    val deletarState by viewModel.deletarState.collectAsState()
    val duplicarState by viewModel.duplicarState.collectAsState()

    var mostrarDialogoExcluir by remember { mutableStateOf(false) }
    var mostrarBottomSheetDuplicar by remember { mutableStateOf(false) }

    LaunchedEffect(treinoId) { viewModel.carregarDetalhe(treinoId) }

    LaunchedEffect(deletarState) {
        if (deletarState is TreinoDeletarUiState.Success) {
            viewModel.resetDeletar()
            onExcluido()
        }
    }

    LaunchedEffect(duplicarState) {
        when (duplicarState) {
            is TreinoDuplicarUiState.Success -> {
                Toast.makeText(context, "Treino duplicado com sucesso!", Toast.LENGTH_SHORT).show()
                mostrarBottomSheetDuplicar = false
                viewModel.resetDuplicar()
            }
            is TreinoDuplicarUiState.Error -> {
                Toast.makeText(context, (duplicarState as TreinoDuplicarUiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetDuplicar()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            val sucesso = detalheState as? TreinoDetalheUiState.Success
            val isTemplate = sucesso?.treino?.usuarioId == null
            AcademiaAppBar(
                title = if (isTemplate) "Detalhes do Template" else "Detalhes do Treino",
                showBackButton = true,
                onBackClick = onBack,
                actions = {
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
                    val estaDuplicando = duplicarState is TreinoDuplicarUiState.Loading
                    DetalheConteudoTemplate(
                        treino = s.treino,
                        isTemplate = s.treino.usuarioId == null,
                        carregando = deletarState is TreinoDeletarUiState.Loading || estaDuplicando,
                        onDuplicarClick = { mostrarBottomSheetDuplicar = true }
                    )
                }
            }
        }
    }

    if (mostrarDialogoExcluir) {
        val carregando = deletarState is TreinoDeletarUiState.Loading
        val sucesso = detalheState as? TreinoDetalheUiState.Success
        val isTemplate = sucesso?.treino?.usuarioId == null
        AlertDialog(
            onDismissRequest = { if (!carregando) mostrarDialogoExcluir = false },
            containerColor = colors.surface,
            title = {
                Text(
                    if (isTemplate) "Excluir template?" else "Excluir treino?",
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    if (isTemplate) {
                        "Esta ação não pode ser desfeita e não afetará os treinos já duplicados para clientes."
                    } else {
                        "Esta ação não pode ser desfeita e removerá o treino deste cliente."
                    },
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

    if (mostrarBottomSheetDuplicar) {
        TreinadorSelecaoAlunoBottomSheet(
            onDismiss = { mostrarBottomSheetDuplicar = false },
            onAlunoSelecionado = { alunoId ->
                viewModel.duplicarParaCliente(treinoId, alunoId)
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
private fun DetalheConteudoTemplate(
    treino: TreinoData,
    isTemplate: Boolean,
    carregando: Boolean,
    onDuplicarClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
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
                            if (isTemplate) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.primary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "TEMPLATE",
                                            color = colors.textOnPrimary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    treino.descricao?.takeIf { it.isNotBlank() }?.let { desc ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            desc,
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EstatisticaTreino("Exercícios", treino.exercicios.size.toString())
                    EstatisticaTreino("Séries", totalSeries.toString())
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
                            if (isTemplate) {
                                "Edite o template para adicionar exercícios."
                            } else {
                                "Edite o treino para adicionar exercícios."
                            },
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        if (isTemplate && treino.exercicios.isNotEmpty()) {
            item {
                Button(
                    onClick = onDuplicarClick,
                    enabled = !carregando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    )
                ) {
                    if (carregando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.textOnPrimary)
                    } else {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicar para cliente")
                        Spacer(Modifier.size(8.dp))
                        Text("DUPLICAR PARA CLIENTE", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
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
                    rotulo = "${item.series}x ${item.repeticoes ?: "1"}"
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
