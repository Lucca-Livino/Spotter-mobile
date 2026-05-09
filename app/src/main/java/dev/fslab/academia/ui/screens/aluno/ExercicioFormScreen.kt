package dev.fslab.academia.ui.screens.aluno

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.AparelhoData
import dev.fslab.academia.model.AparelhoEntradaRequest
import dev.fslab.academia.model.AtualizarExercicioRequest
import dev.fslab.academia.model.CriarExercicioRequest
import dev.fslab.academia.model.MusculoData
import dev.fslab.academia.model.MusculoEntradaRequest
import dev.fslab.academia.model.TipoExercicio
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AparelhoSelectionBottomSheet
import dev.fslab.academia.ui.components.MusculoSelectionBottomSheet
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.ExercicioDetalheUiState
import dev.fslab.academia.ui.viewmodel.ExercicioSalvarUiState
import dev.fslab.academia.ui.viewmodel.ExercicioViewModel
private data class MusculoSelecionado(
    val id: String,
    val nome: String,
    val grupoMuscular: String,
    val tipoAtivacao: String
)

private data class AnimacaoSelecionada(
    val uri: Uri,
    val nomeArquivo: String,
    val mimeType: String,
    val tamanhoBytes: Long
)

@Composable
fun ExercicioFormScreen(
    exercicioId: String?,
    onBack: () -> Unit,
    onSalvo: (String) -> Unit,
    viewModel: ExercicioViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current
    val ehEdicao = exercicioId != null

    val detalheState by viewModel.detalheState.collectAsState()
    val salvarState by viewModel.salvarState.collectAsState()

    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var tipoExercicio by remember { mutableStateOf(TipoExercicio.REPETICAO) }
    var tipoOriginal by remember { mutableStateOf<TipoExercicio?>(null) }
    var musculosSelecionados by remember { mutableStateOf<List<MusculoSelecionado>>(emptyList()) }
    var aparelhosSelecionados by remember { mutableStateOf<List<AparelhoData>>(emptyList()) }
    var animacao by remember { mutableStateOf<AnimacaoSelecionada?>(null) }
    var animacaoUrlAtual by remember { mutableStateOf<String?>(null) }
    var removerAnimacao by remember { mutableStateOf(false) }
    var formularioInicializado by remember { mutableStateOf(!ehEdicao) }

    var nomeErro by remember { mutableStateOf<String?>(null) }
    var musculosErro by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var mostrarAvisoTipo by remember { mutableStateOf(false) }
    var tipoParaMudar by remember { mutableStateOf<TipoExercicio?>(null) }

    var mostrarMusculos by remember { mutableStateOf(false) }
    var mostrarAparelhos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.resetSalvar() }

    LaunchedEffect(exercicioId) {
        if (ehEdicao) {
            viewModel.carregarDetalhe(exercicioId!!)
        }
    }

    LaunchedEffect(detalheState) {
        if (ehEdicao && !formularioInicializado) {
            val sucesso = detalheState as? ExercicioDetalheUiState.Success
            if (sucesso != null) {
                val ex = sucesso.exercicio
                nome = ex.nome
                descricao = ex.descricao.orEmpty()
                animacaoUrlAtual = ex.animacaoUrl
                val tipoCarregado = TipoExercicio.fromApi(ex.tipoExercicio)
                tipoExercicio = tipoCarregado
                tipoOriginal = tipoCarregado
                musculosSelecionados = ex.musculos.map {
                    MusculoSelecionado(
                        id = it.musculoId,
                        nome = it.nome,
                        grupoMuscular = it.grupoMuscular,
                        tipoAtivacao = it.tipoAtivacao
                    )
                }
                aparelhosSelecionados = ex.aparelhos.map {
                    AparelhoData(id = it.aparelhoId, nome = it.nome, descricao = it.descricao)
                }
                formularioInicializado = true
            }
        }
    }

    LaunchedEffect(salvarState) {
        when (val s = salvarState) {
            is ExercicioSalvarUiState.Success -> {
                val id = s.exercicio.id
                viewModel.resetSalvar()
                onSalvo(id)
            }
            is ExercicioSalvarUiState.Error -> {
                erroGeral = s.message
                if (s.campo == "nome") nomeErro = s.message
                if (s.campo == "musculos") musculosErro = s.message
            }
            else -> Unit
        }
    }

    val launcherAnimacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val cr = context.contentResolver
            val mime = cr.getType(uri) ?: "video/webm"
            val nomeArq = obterNomeArquivo(uri, cr) ?: if (mime == "image/gif") "anim.gif" else "anim.webm"
            val tamanho = obterTamanhoArquivo(uri, cr)
            animacao = AnimacaoSelecionada(uri = uri, nomeArquivo = nomeArq, mimeType = mime, tamanhoBytes = tamanho)
            removerAnimacao = false
        }
    }

    val carregando = salvarState is ExercicioSalvarUiState.Loading
    val carregandoDetalhe = ehEdicao && detalheState is ExercicioDetalheUiState.Loading

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = if (ehEdicao) "Editar exercício" else "Criar exercício",
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
                        label = { Text("Nome do exercício") },
                        placeholder = { Text("Ex: Supino reto") },
                        singleLine = true,
                        isError = nomeErro != null,
                        supportingText = nomeErro?.let { { Text(it, color = colors.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = camposCores()
                    )
                }

                item {
                    SeletorTipoExercicio(
                        tipoAtual = tipoExercicio,
                        onSelecionar = { novo ->
                            if (ehEdicao && tipoOriginal != null && tipoOriginal != novo) {
                                tipoParaMudar = novo
                                mostrarAvisoTipo = true
                            } else {
                                tipoExercicio = novo
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição (opcional)") },
                        placeholder = { Text("Detalhes da execução, postura, observações…") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        colors = camposCores()
                    )
                }

                item {
                    BlocoAnimacao(
                        animacao = animacao,
                        animacaoUrlAtual = animacaoUrlAtual,
                        onSelecionar = { launcherAnimacao.launch("*/*") },
                        onRemover = {
                            if (animacao != null) {
                                animacao = null
                            } else if (!animacaoUrlAtual.isNullOrBlank()) {
                                animacaoUrlAtual = null
                                removerAnimacao = true
                            }
                        }
                    )
                }

                item {
                    SecaoCabecalho(
                        titulo = "Músculos",
                        subtitulo = "Pelo menos um músculo é obrigatório",
                        onAdicionar = { mostrarMusculos = true },
                        contagem = musculosSelecionados.size,
                        erro = musculosErro
                    )
                }

                items(musculosSelecionados, key = { "m_${it.id}" }) { sel ->
                    ItemMusculoSelecionado(
                        musculo = sel,
                        onAlterarTipo = { novoTipo ->
                            musculosSelecionados = musculosSelecionados.map {
                                if (it.id == sel.id) it.copy(tipoAtivacao = novoTipo) else it
                            }
                        },
                        onRemover = {
                            musculosSelecionados = musculosSelecionados.filter { it.id != sel.id }
                        }
                    )
                }

                item {
                    SecaoCabecalho(
                        titulo = "Aparelhos",
                        subtitulo = "Opcional — peso livre não exige aparelho",
                        onAdicionar = { mostrarAparelhos = true },
                        contagem = aparelhosSelecionados.size,
                        erro = null
                    )
                }

                items(aparelhosSelecionados, key = { "a_${it.id}" }) { ap ->
                    ItemAparelhoSelecionado(
                        aparelho = ap,
                        onRemover = {
                            aparelhosSelecionados = aparelhosSelecionados.filter { it.id != ap.id }
                        }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }

                item {
                    Button(
                        onClick = {
                            erroGeral = null
                            nomeErro = null
                            musculosErro = null

                            if (nome.isBlank()) {
                                nomeErro = "Nome é obrigatório"
                                return@Button
                            }
                            if (musculosSelecionados.isEmpty()) {
                                musculosErro = "Selecione pelo menos um músculo"
                                return@Button
                            }

                            val musculosReq = musculosSelecionados.map {
                                MusculoEntradaRequest(it.id, it.tipoAtivacao)
                            }
                            val aparelhosReq = aparelhosSelecionados
                                .map { AparelhoEntradaRequest(it.id) }
                                .takeIf { it.isNotEmpty() }

                            val animBytes = animacao?.let {
                                runCatching {
                                    context.contentResolver.openInputStream(it.uri)?.use { s -> s.readBytes() }
                                }.getOrNull()
                            }
                            val animMime = animacao?.mimeType
                            val animNome = animacao?.nomeArquivo

                            if (ehEdicao) {
                                val req = AtualizarExercicioRequest(
                                    nome = nome.trim(),
                                    descricao = descricao.trim().takeIf { it.isNotBlank() },
                                    tipoExercicio = tipoExercicio.apiValue,
                                    musculos = musculosReq,
                                    aparelhos = aparelhosReq ?: emptyList()
                                )
                                viewModel.atualizar(
                                    id = exercicioId!!,
                                    request = req,
                                    removerAnimacao = removerAnimacao && animBytes == null,
                                    animacaoBytes = animBytes,
                                    animacaoNome = animNome,
                                    animacaoMimeType = animMime
                                )
                            } else {
                                val req = CriarExercicioRequest(
                                    nome = nome.trim(),
                                    descricao = descricao.trim().takeIf { it.isNotBlank() },
                                    tipoExercicio = tipoExercicio.apiValue,
                                    musculos = musculosReq,
                                    aparelhos = aparelhosReq
                                )
                                viewModel.criar(
                                    request = req,
                                    animacaoBytes = animBytes,
                                    animacaoNome = animNome,
                                    animacaoMimeType = animMime
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
                                if (ehEdicao) "Salvar alterações" else "Criar exercício",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (mostrarAvisoTipo && tipoParaMudar != null) {
        val colors = LocalAcademiaColors.current
        AlertDialog(
            onDismissRequest = { mostrarAvisoTipo = false; tipoParaMudar = null },
            containerColor = colors.surface,
            icon = { Icon(Icons.Filled.Warning, null, tint = colors.featureOrange) },
            title = {
                Text(
                    "Atenção: dados históricos",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Séries registradas para este exercício usam as métricas do tipo anterior " +
                        "(${if (tipoOriginal == TipoExercicio.REPETICAO) "repetições/carga" else "tempo"}). " +
                        "O sistema não guarda qual tipo estava ativo em cada sessão — " +
                        "esses dados continuarão existindo mas podem aparecer inconsistentes nos gráficos de progressão.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tipoExercicio = tipoParaMudar!!
                        mostrarAvisoTipo = false
                        tipoParaMudar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.featureOrange,
                        contentColor = colors.textOnPrimary
                    )
                ) { Text("Entendido, alterar tipo", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAvisoTipo = false; tipoParaMudar = null }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    if (mostrarMusculos) {
        MusculoSelectionBottomSheet(
            selecionados = musculosSelecionados.map { it.id }.toSet(),
            onConfirmar = { ids ->
                musculosSelecionados = musculosSelecionados.filter { it.id in ids }
            },
            onConfirmarComDados = { dados ->
                val existentesPorId = musculosSelecionados.associateBy { it.id }
                musculosSelecionados = dados.map { dado ->
                    existentesPorId[dado.id] ?: MusculoSelecionado(
                        id = dado.id,
                        nome = dado.nome,
                        grupoMuscular = dado.grupoMuscular,
                        tipoAtivacao = "PRIMARIO"
                    )
                }
                if (musculosSelecionados.isNotEmpty()) musculosErro = null
                mostrarMusculos = false
            },
            onDismiss = { mostrarMusculos = false }
        )
    }

    if (mostrarAparelhos) {
        AparelhoSelectionBottomSheet(
            selecionados = aparelhosSelecionados.map { it.id }.toSet(),
            onConfirmar = { ids ->
                aparelhosSelecionados = aparelhosSelecionados.filter { it.id in ids }
            },
            onConfirmarComDados = { dados ->
                aparelhosSelecionados = dados
                mostrarAparelhos = false
            },
            onDismiss = { mostrarAparelhos = false }
        )
    }

    val erro = erroGeral
    if (erro != null && nomeErro == null && musculosErro == null) {
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
private fun camposCores() = OutlinedTextFieldDefaults.colors(
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
private fun BlocoAnimacao(
    animacao: AnimacaoSelecionada?,
    animacaoUrlAtual: String?,
    onSelecionar: () -> Unit,
    onRemover: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val temAnimacaoNova = animacao != null
    val temAnimacaoExistente = !temAnimacaoNova && !animacaoUrlAtual.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AttachFile, null, tint = colors.primary)
                Spacer(Modifier.size(8.dp))
                Text(
                    "Animação (.webm ou .gif)",
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            when {
                temAnimacaoNova -> {
                    val a = animacao!!
                    Text(
                        "${a.nomeArquivo} • ${formatarTamanho(a.tamanhoBytes)}",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                temAnimacaoExistente -> {
                    Text(
                        "Animação atual mantida. Selecione outra para substituir.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    Text(
                        "Nenhum arquivo selecionado.",
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSelecionar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.textOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AttachFile, null)
                    Spacer(Modifier.size(6.dp))
                    Text(if (temAnimacaoNova || temAnimacaoExistente) "Substituir" else "Selecionar")
                }
                if (temAnimacaoNova || temAnimacaoExistente) {
                    OutlinedButton(
                        onClick = onRemover,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Close, null, tint = colors.error)
                        Spacer(Modifier.size(6.dp))
                        Text("Remover", color = colors.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecaoCabecalho(
    titulo: String,
    subtitulo: String,
    onAdicionar: () -> Unit,
    contagem: Int,
    erro: String?
) {
    val colors = LocalAcademiaColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitulo,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(contagem.toString()) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = colors.primary.copy(alpha = 0.18f),
                    labelColor = colors.primary
                )
            )
            Spacer(Modifier.size(6.dp))
            Button(
                onClick = onAdicionar,
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
        if (erro != null) {
            Text(
                erro,
                color = colors.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ItemMusculoSelecionado(
    musculo: MusculoSelecionado,
    onAlterarTipo: (String) -> Unit,
    onRemover: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary, modifier = Modifier.size(18.dp))
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
                IconButton(onClick = onRemover) {
                    Icon(Icons.Filled.Close, "Remover", tint = colors.textSecondary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = musculo.tipoAtivacao == "PRIMARIO",
                    onClick = { onAlterarTipo("PRIMARIO") },
                    label = { Text("Primário") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.lightGray,
                        labelColor = colors.textPrimary,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.textOnPrimary
                    )
                )
                FilterChip(
                    selected = musculo.tipoAtivacao == "SECUNDARIO",
                    onClick = { onAlterarTipo("SECUNDARIO") },
                    label = { Text("Secundário") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.lightGray,
                        labelColor = colors.textPrimary,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.textOnPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ItemAparelhoSelecionado(
    aparelho: AparelhoData,
    onRemover: () -> Unit
) {
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
            IconButton(onClick = onRemover) {
                Icon(Icons.Filled.Close, "Remover", tint = colors.textSecondary)
            }
        }
    }
}

private fun obterNomeArquivo(uri: Uri, cr: android.content.ContentResolver): String? {
    return runCatching {
        cr.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }.getOrNull()
}

private fun obterTamanhoArquivo(uri: Uri, cr: android.content.ContentResolver): Long {
    return runCatching {
        cr.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getLong(idx) else 0L
        } ?: 0L
    }.getOrDefault(0L)
}

@Composable
private fun SeletorTipoExercicio(
    tipoAtual: TipoExercicio,
    onSelecionar: (TipoExercicio) -> Unit
) {
    val colors = LocalAcademiaColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Tipo de medição",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoOpcao(
                    selecionado = tipoAtual == TipoExercicio.REPETICAO,
                    icone = Icons.Filled.Refresh,
                    titulo = "Repetição",
                    subtitulo = "Conta reps e carga",
                    corAtiva = colors.primary,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { onSelecionar(TipoExercicio.REPETICAO) }
                )
                TipoOpcao(
                    selecionado = tipoAtual == TipoExercicio.TEMPO,
                    icone = Icons.Filled.Timer,
                    titulo = "Tempo",
                    subtitulo = "Conta segundos e carga",
                    corAtiva = colors.primary,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { onSelecionar(TipoExercicio.TEMPO) }
                )
            }
            if (tipoAtual == TipoExercicio.TEMPO) {
                Text(
                    "Ideal para prancha, isometria, wall sit e similares.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun TipoOpcao(
    selecionado: Boolean,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    corAtiva: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val bgColor = if (selecionado) corAtiva.copy(alpha = 0.15f) else colors.background
    val borderColor = if (selecionado) corAtiva else colors.lightGray.copy(alpha = 0.5f)
    val textColor = if (selecionado) corAtiva else colors.textSecondary

    Box(
        modifier = modifier
            .border(
                width = if (selecionado) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icone, null, tint = textColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(titulo, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitulo, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

private fun formatarTamanho(bytes: Long): String {
    if (bytes <= 0) return "tamanho desconhecido"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.0f KB".format(kb)
    val mb = kb / 1024.0
    return "%.1f MB".format(mb)
}
