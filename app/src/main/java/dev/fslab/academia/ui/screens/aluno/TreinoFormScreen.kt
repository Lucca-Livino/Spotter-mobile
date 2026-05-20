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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.DiaSemana
import dev.fslab.academia.model.ExercicioData
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.model.TreinoExercicioItemRequest
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.DiaSemanaSelectionBottomSheet
import dev.fslab.academia.ui.components.ExercicioPickerBottomSheet
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinoDetalheUiState
import dev.fslab.academia.ui.viewmodel.TreinoExercicioPatchUpdate
import dev.fslab.academia.ui.viewmodel.TreinoListUiState
import dev.fslab.academia.ui.viewmodel.TreinoSalvarUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel

private data class ItemForm(
    val vinculoId: String?,
    val exercicioId: String,
    val exercicioNome: String,
    val exercicioDescricao: String?,
    val tipoExercicio: TipoExercicio = TipoExercicio.REPETICAO,
    val series: Int,
    val repeticoes: String? = null,
    val duracaoSugeridaSegundos: Int? = null,
    val cargaSugerida: Double?,
    val tempoDescansoSegundos: Int,
    val ordemExecucao: Int,
    val originalSeries: Int? = null,
    val originalRepeticoes: String? = null,
    val originalDuracao: Int? = null,
    val originalCarga: Double? = null,
    val originalTempoDescanso: Int? = null,
    val originalOrdem: Int? = null
)

@Composable
fun TreinoFormScreen(
    treinoId: String?,
    alunoId: String? = null,
    onBack: () -> Unit,
    onSalvo: (String) -> Unit,
    onCriarExercicio: () -> Unit = {},
    novoExercicioId: String? = null,
    onConsumirNovoExercicio: () -> Unit = {},
    viewModel: TreinoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val ehEdicao = treinoId != null

    val detalheState by viewModel.detalheState.collectAsState()
    val salvarState by viewModel.salvarState.collectAsState()

    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var descricaoOriginal by remember { mutableStateOf<String?>(null) }
    var ordemAutomatica by remember { mutableStateOf<Int?>(null) }
    var ordemOriginal by remember { mutableStateOf<Int?>(null) }
    var dias by remember { mutableStateOf<Set<DiaSemana>>(emptySet()) }
    var diasOriginal by remember { mutableStateOf<List<DiaSemana>?>(null) }
    var itens by remember { mutableStateOf<List<ItemForm>>(emptyList()) }
    var idsOriginais by remember { mutableStateOf<Set<String>>(emptySet()) }
    var formularioInicializado by remember { mutableStateOf(!ehEdicao) }

    var nomeErro by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }

    var mostrarPicker by remember { mutableStateOf(false) }
    var mostrarDias by remember { mutableStateOf(false) }
    var itemEditando by remember { mutableStateOf<ItemForm?>(null) }
    var exercicioPendente by remember { mutableStateOf<ExercicioData?>(null) }
    var idAutoSelecionar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(novoExercicioId) {
        val id = novoExercicioId
        if (!id.isNullOrBlank()) {
            idAutoSelecionar = id
            mostrarPicker = true
            onConsumirNovoExercicio()
        }
    }

    val listaState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!ehEdicao) viewModel.carregar()
    }
    LaunchedEffect(listaState) {
        if (!ehEdicao && ordemAutomatica == null) {
            val sucesso = listaState as? TreinoListUiState.Success
            val maxOrdem = sucesso?.treinos.orEmpty().mapNotNull { it.ordem }.maxOrNull() ?: 0
            ordemAutomatica = maxOrdem + 1
        }
    }

    LaunchedEffect(Unit) { viewModel.resetSalvar() }

    LaunchedEffect(treinoId) {
        if (ehEdicao) viewModel.carregarDetalhe(treinoId!!)
    }

    LaunchedEffect(detalheState) {
        if (ehEdicao && !formularioInicializado) {
            val sucesso = detalheState as? TreinoDetalheUiState.Success
            if (sucesso != null) {
                val t = sucesso.treino
                nome = t.nome
                descricao = t.descricao.orEmpty()
                descricaoOriginal = t.descricao
                ordemOriginal = t.ordem
                ordemAutomatica = t.ordem
                val diasParse = t.diasSemana.orEmpty().mapNotNull(DiaSemana::fromApi)
                dias = diasParse.toSet()
                diasOriginal = diasParse
                itens = t.exercicios
                    .sortedBy { it.ordemExecucao }
                    .map { item ->
                        ItemForm(
                            vinculoId = item.id,
                            exercicioId = item.exercicio.id,
                            exercicioNome = item.exercicio.nome,
                            exercicioDescricao = item.exercicio.descricao,
                            tipoExercicio = item.exercicio.tipo,
                            series = item.series,
                            repeticoes = item.repeticoes,
                            duracaoSugeridaSegundos = item.duracaoSugeridaSegundos,
                            cargaSugerida = item.cargaSugerida?.toDoubleOrNull(),
                            tempoDescansoSegundos = item.tempoDescansoSegundos,
                            ordemExecucao = item.ordemExecucao,
                            originalSeries = item.series,
                            originalRepeticoes = item.repeticoes,
                            originalDuracao = item.duracaoSugeridaSegundos,
                            originalCarga = item.cargaSugerida?.toDoubleOrNull(),
                            originalTempoDescanso = item.tempoDescansoSegundos,
                            originalOrdem = item.ordemExecucao
                        )
                    }
                idsOriginais = itens.mapNotNull { it.vinculoId }.toSet()
                formularioInicializado = true
            }
        }
    }

    LaunchedEffect(salvarState) {
        when (val s = salvarState) {
            is TreinoSalvarUiState.Success -> {
                val id = s.treino.id
                viewModel.resetSalvar()
                onSalvo(id)
            }
            is TreinoSalvarUiState.Error -> {
                erroGeral = s.message
                if (s.campo == "nome") nomeErro = s.message
            }
            else -> Unit
        }
    }

    val carregando = salvarState is TreinoSalvarUiState.Loading
    val carregandoDetalhe = ehEdicao && detalheState is TreinoDetalheUiState.Loading

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = if (ehEdicao) "Editar treino" else "Criar treino",
                showBackButton = true,
                onBackClick = onBack
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
            if (carregandoDetalhe) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
                return@Box
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = {
                            nome = it
                            if (nomeErro != null) nomeErro = null
                            if (erroGeral != null) erroGeral = null
                        },
                        label = { Text("Nome do treino") },
                        placeholder = { Text("Ex: Treino A - Peito e Tríceps") },
                        singleLine = true,
                        isError = nomeErro != null,
                        supportingText = nomeErro?.let { { Text(it, color = colors.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = camposCoresTreino()
                    )
                }
                item {
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição (opcional)") },
                        placeholder = { Text("Foco, observações…") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = camposCoresTreino()
                    )
                }
                item {
                    BotaoSelecionarDias(
                        dias = dias,
                        onAbrir = { mostrarDias = true }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Exercícios",
                                color = colors.textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Adicione exercícios e configure séries, repetições e descanso",
                                color = colors.textSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(itens.size.toString()) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colors.primary.copy(alpha = 0.18f),
                                labelColor = colors.primary
                            )
                        )
                        Spacer(Modifier.size(6.dp))
                        Button(
                            onClick = { mostrarPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.textOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Adicionar", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                items(itens, key = { it.exercicioId }) { item ->
                    val pode_subir = itens.indexOf(item) > 0
                    val pode_descer = itens.indexOf(item) < itens.size - 1
                    ItemTreinoCard(
                        item = item,
                        podeSubir = pode_subir,
                        podeDescer = pode_descer,
                        onSubir = {
                            val idx = itens.indexOf(item)
                            if (idx > 0) {
                                val nova = itens.toMutableList()
                                val tmp = nova[idx - 1]
                                nova[idx - 1] = nova[idx].copy(ordemExecucao = idx)
                                nova[idx] = tmp.copy(ordemExecucao = idx + 1)
                                itens = nova
                            }
                        },
                        onDescer = {
                            val idx = itens.indexOf(item)
                            if (idx < itens.size - 1) {
                                val nova = itens.toMutableList()
                                val tmp = nova[idx + 1]
                                nova[idx + 1] = nova[idx].copy(ordemExecucao = idx + 2)
                                nova[idx] = tmp.copy(ordemExecucao = idx + 1)
                                itens = nova
                            }
                        },
                        onEditar = { itemEditando = item },
                        onRemover = { itens = itens.filter { it.exercicioId != item.exercicioId } }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }

                item {
                    Button(
                        onClick = onClick@{
                            erroGeral = null
                            nomeErro = null

                            if (nome.isBlank()) {
                                nomeErro = "Nome é obrigatório"
                                return@onClick
                            }

                            val ordemInt = ordemAutomatica

                            val itensRequest = itens.mapIndexed { idx, item ->
                                TreinoExercicioItemRequest(
                                    exercicioId = item.exercicioId,
                                    series = item.series,
                                    repeticoes = if (item.tipoExercicio == TipoExercicio.REPETICAO) item.repeticoes else null,
                                    duracaoSugeridaSegundos = if (item.tipoExercicio == TipoExercicio.TEMPO) item.duracaoSugeridaSegundos else null,
                                    cargaSugerida = item.cargaSugerida,
                                    tempoDescansoSegundos = item.tempoDescansoSegundos,
                                    ordemExecucao = idx + 1
                                )
                            }

                            if (ehEdicao) {
                                val descricaoTrim = descricao.trim().ifBlank { null }
                                val descMudou = descricaoTrim != descricaoOriginal?.takeIf { it.isNotBlank() }
                                val descNula = descricaoOriginal?.isNotBlank() == true && descricaoTrim == null
                                val diasMudou = dias.toList().sortedBy { it.ordinal } !=
                                    diasOriginal.orEmpty().sortedBy { it.ordinal }
                                val diasNulo = (diasOriginal?.isNotEmpty() == true) && dias.isEmpty()

                                val novos = itens.filter { it.vinculoId == null }
                                val adicionarReq = novos.mapIndexed { _, item ->
                                    val idx = itens.indexOf(item)
                                    TreinoExercicioItemRequest(
                                        exercicioId = item.exercicioId,
                                        series = item.series,
                                        repeticoes = if (item.tipoExercicio == TipoExercicio.REPETICAO) item.repeticoes else null,
                                        duracaoSugeridaSegundos = if (item.tipoExercicio == TipoExercicio.TEMPO) item.duracaoSugeridaSegundos else null,
                                        cargaSugerida = item.cargaSugerida,
                                        tempoDescansoSegundos = item.tempoDescansoSegundos,
                                        ordemExecucao = idx + 1
                                    )
                                }
                                val atualizarReq = itens
                                    .filter { it.vinculoId != null }
                                    .mapNotNull { item ->
                                        val idx = itens.indexOf(item)
                                        val novaOrdem = idx + 1
                                        val mudouSeries = item.originalSeries != item.series
                                        val mudouReps = item.originalRepeticoes != item.repeticoes
                                        val mudouDuracao = item.originalDuracao != item.duracaoSugeridaSegundos
                                        val mudouCarga = item.originalCarga != item.cargaSugerida
                                        val mudouDesc = item.originalTempoDescanso != item.tempoDescansoSegundos
                                        val mudouOrdem = item.originalOrdem != novaOrdem
                                        if (!mudouSeries && !mudouReps && !mudouDuracao && !mudouCarga && !mudouDesc && !mudouOrdem) {
                                            return@mapNotNull null
                                        }
                                        TreinoExercicioPatchUpdate(
                                            id = item.vinculoId!!,
                                            series = if (mudouSeries) item.series else null,
                                            repeticoes = if (mudouReps) item.repeticoes else null,
                                            duracaoSugeridaSegundos = if (mudouDuracao) item.duracaoSugeridaSegundos else null,
                                            cargaSugerida = if (mudouCarga) item.cargaSugerida else null,
                                            cargaSugeridaExplicitamenteNula =
                                                mudouCarga && item.cargaSugerida == null,
                                            tempoDescansoSegundos = if (mudouDesc) item.tempoDescansoSegundos else null,
                                            ordemExecucao = if (mudouOrdem) novaOrdem else null
                                        )
                                    }
                                val idsAtuais = itens.mapNotNull { it.vinculoId }.toSet()
                                val removerReq = (idsOriginais - idsAtuais).toList()

                                viewModel.atualizar(
                                    id = treinoId!!,
                                    nome = if (nome.trim() != "") nome.trim() else null,
                                    descricao = if (descMudou && !descNula) descricaoTrim else null,
                                    descricaoExplicitamenteNula = descNula,
                                    diasSemana = if (diasMudou && !diasNulo) dias.toList() else null,
                                    diasSemanaExplicitamenteNulo = diasNulo,
                                    ordem = if (ordemInt != ordemOriginal) ordemInt else null,
                                    adicionar = adicionarReq,
                                    atualizar = atualizarReq,
                                    remover = removerReq
                                )
                            } else {
                                viewModel.criar(
                                    nome = nome.trim(),
                                    descricao = descricao.trim().ifBlank { null },
                                    alunoId = alunoId,
                                    diasSemana = dias.toList(),
                                    ordem = ordemInt,
                                    exercicios = itensRequest
                                )
                            }
                        },
                        enabled = !carregando,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.textOnPrimary
                        )
                    ) {
                        if (carregando) {
                            CircularProgressIndicator(
                                color = colors.textOnPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, null)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                if (ehEdicao) "Salvar alterações" else "Criar treino",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (mostrarPicker) {
        ExercicioPickerBottomSheet(
            idsExcluidos = itens.map { it.exercicioId }.toSet(),
            autoSelecionarId = idAutoSelecionar,
            onSelecionar = { ex ->
                exercicioPendente = ex
                idAutoSelecionar = null
                mostrarPicker = false
            },
            onCriarExercicio = {
                mostrarPicker = false
                onCriarExercicio()
            },
            onDismiss = {
                idAutoSelecionar = null
                mostrarPicker = false
            }
        )
    }

    if (mostrarDias) {
        DiaSemanaSelectionBottomSheet(
            selecionados = dias,
            onConfirmar = { selecionado ->
                dias = selecionado
                mostrarDias = false
            },
            onDismiss = { mostrarDias = false }
        )
    }

    val pendente = exercicioPendente
    if (pendente != null) {
        val tipoP = pendente.tipo
        DialogoConfigurarItem(
            titulo = "Configurar exercício",
            exercicioNome = pendente.nome,
            tipoExercicio = tipoP,
            inicial = null,
            onConfirmar = { series, reps, duracao, carga, descanso ->
                val novo = ItemForm(
                    vinculoId = null,
                    exercicioId = pendente.id,
                    exercicioNome = pendente.nome,
                    exercicioDescricao = pendente.descricao,
                    tipoExercicio = tipoP,
                    series = series,
                    repeticoes = reps,
                    duracaoSugeridaSegundos = duracao,
                    cargaSugerida = carga,
                    tempoDescansoSegundos = descanso,
                    ordemExecucao = itens.size + 1
                )
                itens = itens + novo
                exercicioPendente = null
            },
            onCancelar = { exercicioPendente = null }
        )
    }

    val edit = itemEditando
    if (edit != null) {
        DialogoConfigurarItem(
            titulo = "Editar exercício",
            exercicioNome = edit.exercicioNome,
            tipoExercicio = edit.tipoExercicio,
            inicial = edit,
            onConfirmar = { series, reps, duracao, carga, descanso ->
                itens = itens.map {
                    if (it.exercicioId == edit.exercicioId) {
                        it.copy(
                            series = series,
                            repeticoes = reps,
                            duracaoSugeridaSegundos = duracao,
                            cargaSugerida = carga,
                            tempoDescansoSegundos = descanso
                        )
                    } else it
                }
                itemEditando = null
            },
            onCancelar = { itemEditando = null }
        )
    }

    val erro = erroGeral
    if (erro != null && nomeErro == null) {
        AlertDialog(
            onDismissRequest = { erroGeral = null; viewModel.resetSalvar() },
            containerColor = colors.surface,
            title = { Text("Falha ao salvar", color = colors.textPrimary) },
            text = { Text(erro, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { erroGeral = null; viewModel.resetSalvar() }) {
                    Text("OK", color = colors.primary)
                }
            }
        )
    }
}

@Composable
private fun camposCoresTreino() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LocalAcademiaColors.current.surface,
    unfocusedContainerColor = LocalAcademiaColors.current.surface,
    focusedBorderColor = LocalAcademiaColors.current.primary,
    unfocusedBorderColor = LocalAcademiaColors.current.inputBorder,
    focusedTextColor = LocalAcademiaColors.current.textInput,
    unfocusedTextColor = LocalAcademiaColors.current.textInput,
    cursorColor = LocalAcademiaColors.current.primary,
    focusedLabelColor = LocalAcademiaColors.current.primary,
    unfocusedLabelColor = LocalAcademiaColors.current.textSecondary
)

@Composable
private fun BotaoSelecionarDias(
    dias: Set<DiaSemana>,
    onAbrir: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val resumo = if (dias.isEmpty()) "Toque para selecionar" else
        DiaSemana.values().filter { it in dias }.joinToString(" • ") { it.curto }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CalendarMonth, null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Dias da semana",
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    resumo,
                    color = if (dias.isEmpty()) colors.textSecondary else colors.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AssistChip(
                onClick = onAbrir,
                label = { Text(dias.size.toString()) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colors.primary.copy(alpha = 0.18f),
                    labelColor = colors.primary
                )
            )
        }
    }
}

@Composable
private fun ItemTreinoCard(
    item: ItemForm,
    podeSubir: Boolean,
    podeDescer: Boolean,
    onSubir: () -> Unit,
    onDescer: () -> Unit,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val eTempo = item.tipoExercicio == TipoExercicio.TEMPO
    val corTipo = if (eTempo) colors.featureOrange else colors.featureBlue
    val descricaoMetrica = if (eTempo) {
        val dur = item.duracaoSugeridaSegundos
        if (dur != null) "${item.series}x ${formatarSegundos(dur)}" else "${item.series}x (sem meta)"
    } else {
        "${item.series}x ${item.repeticoes.orEmpty()}" +
            (item.cargaSugerida?.let { " • ${it} kg" } ?: "")
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEditar),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(corTipo.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (eTempo) Icons.Filled.Timer else Icons.Filled.Refresh,
                        null, tint = corTipo, modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.exercicioNome,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$descricaoMetrica • ${formatarSegundos(item.tempoDescansoSegundos)}",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                IconButton(onClick = onSubir, enabled = podeSubir) {
                    Icon(
                        Icons.Filled.ArrowUpward, "Subir",
                        tint = if (podeSubir) colors.textPrimary else colors.textSecondary
                    )
                }
                IconButton(onClick = onDescer, enabled = podeDescer) {
                    Icon(
                        Icons.Filled.ArrowDownward, "Descer",
                        tint = if (podeDescer) colors.textPrimary else colors.textSecondary
                    )
                }
                IconButton(onClick = onRemover) {
                    Icon(Icons.Filled.Delete, "Remover", tint = colors.error)
                }
            }
        }
    }
}

@Composable
private fun DialogoConfigurarItem(
    titulo: String,
    exercicioNome: String,
    tipoExercicio: TipoExercicio = TipoExercicio.REPETICAO,
    inicial: ItemForm?,
    onConfirmar: (series: Int, repeticoes: String?, duracaoSegundos: Int?, cargaSugerida: Double?, tempoDescansoSegundos: Int) -> Unit,
    onCancelar: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val eTempo = tipoExercicio == TipoExercicio.TEMPO
    var seriesText by remember { mutableStateOf(inicial?.series?.toString() ?: "3") }
    var repsText by remember { mutableStateOf(inicial?.repeticoes ?: "10-12") }
    var duracaoText by remember { mutableStateOf(inicial?.duracaoSugeridaSegundos?.toString() ?: "") }
    var mostrarCarga by remember { mutableStateOf(eTempo && inicial?.cargaSugerida != null || !eTempo) }
    var cargaText by remember { mutableStateOf(inicial?.cargaSugerida?.toString().orEmpty()) }
    var descansoText by remember { mutableStateOf(inicial?.tempoDescansoSegundos?.toString() ?: "60") }
    var erro by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = colors.surface,
        title = {
            Column {
                Text(titulo, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                Text(exercicioNome, color = colors.primary, style = MaterialTheme.typography.labelLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = seriesText,
                    onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) seriesText = it },
                    label = { Text("Séries") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = camposCoresTreino()
                )
                if (eTempo) {
                    OutlinedTextField(
                        value = duracaoText,
                        onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) duracaoText = it },
                        label = { Text("Duração sugerida (segundos)") },
                        placeholder = { Text("Ex: 45") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = camposCoresTreino()
                    )
                } else {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text("Repetições (ex: 8-12)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = camposCoresTreino()
                    )
                }
                if (eTempo && !mostrarCarga) {
                    TextButton(onClick = { mostrarCarga = true }) {
                        Icon(Icons.Filled.Add, null, modifier = androidx.compose.ui.Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Adicionar carga (opcional)", color = colors.textSecondary)
                    }
                }
                if (!eTempo || mostrarCarga) {
                    OutlinedTextField(
                        value = cargaText,
                        onValueChange = { txt ->
                            val limpo = txt.replace(',', '.')
                            if (limpo.isEmpty() || limpo.toDoubleOrNull() != null) cargaText = limpo
                        },
                        label = { Text("Carga sugerida em kg (opcional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = camposCoresTreino()
                    )
                }
                OutlinedTextField(
                    value = descansoText,
                    onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) descansoText = it },
                    label = { Text("Descanso (segundos)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = camposCoresTreino()
                )
                erro?.let { Text(it, color = colors.error, style = MaterialTheme.typography.labelMedium) }
            }
        },
        confirmButton = {
            Button(
                onClick = onClick@{
                    val series = seriesText.toIntOrNull()
                    val descanso = descansoText.toIntOrNull()
                    val carga = cargaText.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                    val duracao = duracaoText.takeIf { it.isNotBlank() }?.toIntOrNull()
                    when {
                        series == null || series < 1 || series > 20 -> erro = "Séries entre 1 e 20"
                        !eTempo && repsText.isBlank() -> erro = "Informe as repetições"
                        eTempo && duracaoText.isNotBlank() && (duracao == null || duracao < 1) -> erro = "Duração inválida"
                        descanso == null || descanso < 0 || descanso > 3600 -> erro = "Descanso entre 0 e 3600s"
                        cargaText.isNotBlank() && (carga == null || carga <= 0) -> erro = "Carga deve ser positiva"
                        else -> onConfirmar(
                            series,
                            if (!eTempo) repsText.trim() else null,
                            if (eTempo) duracao else null,
                            carga,
                            descanso
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary
                )
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = colors.textSecondary)
            }
        }
    )
}

private fun formatarSegundos(s: Int): String {
    if (s <= 0) return "sem descanso"
    if (s < 60) return "${s}s"
    val min = s / 60
    val rest = s % 60
    return if (rest == 0) "${min}min" else "${min}min ${rest}s"
}
