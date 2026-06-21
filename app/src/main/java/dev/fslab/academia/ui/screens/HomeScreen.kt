package dev.fslab.academia.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.academia.R
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.components.alunoNavItemsWithBadge
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.AlunoVinculoState
import dev.fslab.academia.ui.viewmodel.HomeUiState
import dev.fslab.academia.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Dados e Utilitários ──────────────────────────────────────────────────────

private data class DiaSemana(val abrev: String, val numero: Int, val hoje: Boolean = false)

@RequiresApi(Build.VERSION_CODES.O)
private fun generateWeekDays(): List<DiaSemana> {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEE", Locale("pt", "BR"))
    return (-2..3).map { i ->
        val date = today.plusDays(i.toLong())
        val isToday = i == 0
        val abrev = if (isToday) "HOJE" else {
            val formatted = date.format(formatter).uppercase(Locale.getDefault())
            if (formatted.length > 3) formatted.substring(0, 3) else formatted
        }.replace(".", "")
        DiaSemana(abrev, date.dayOfMonth, isToday)
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier, shape: RoundedCornerShape = RoundedCornerShape(8.dp)) {
    val colors = LocalAcademiaColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    Box(modifier = modifier.clip(shape).background(colors.surface.copy(alpha = alpha)))
}

// ─── HomeScreen ───────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nome: String = "",
    fotoUrl: String? = null,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenExercicios: () -> Unit = {},
    onOpenTreinos: () -> Unit = {},
    onRetomarSessao: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {},
    temSessaoAtiva: Boolean = false,
    onIniciarTreino: (String) -> Unit = {},
    onAbrirTreinoDoDia: (String) -> Unit = {},
    onBuscarTreinador: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    chatBadgeCount: Int = 0
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val context = LocalContext.current
    var mostrarMaisMenu by remember { mutableStateOf(false) }
    val homeUiState by homeViewModel.uiState.collectAsState()
    val streakState by homeViewModel.streak.collectAsState()
    val vinculoState by homeViewModel.vinculoState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        homeViewModel.carregarDados()
    }

    LaunchedEffect(homeUiState) {
        if (homeUiState !is HomeUiState.Loading) isRefreshing = false
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(permission)
            }
        }
    }

    val diasSemana = remember { generateWeekDays() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            AppNavigationBar(
                items = alunoNavItemsWithBadge(chatBadgeCount),
                selectedIndex = 0,
                onItemSelected = { idx ->
                    val route = alunoNavItems[idx].route
                    when {
                        route == MAIS_ROUTE -> mostrarMaisMenu = true
                        route == "treinos" -> onOpenTreinos()
                        else -> onNavigateTab(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true; homeViewModel.carregarDados() },
            state = pullRefreshState,
            modifier = Modifier.padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.screenPaddingH)
        ) {
            Spacer(modifier = Modifier.height(dimens.spaceLg))

            // ── Header: avatar + saudação + streak ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(48.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(fotoUrl ?: R.drawable.no_profile_photo)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(2.dp, colors.primary.copy(alpha = 0.3f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .border(2.dp, colors.background, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "BEM-VINDO DE VOLTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = if (nome.isBlank()) "Olá!" else "Olá, $nome",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge streak — shimmer quando carregando
                    if (streakState.dias == null) {
                        ShimmerBox(
                            modifier = Modifier.size(width = 80.dp, height = 32.dp),
                            shape = RoundedCornerShape(20.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surface.copy(alpha = 0.5f))
                                .border(1.dp, colors.surface.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = if ((streakState.dias ?: 0) > 0) colors.featureOrange else colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val streakLabel = when {
                                (streakState.dias ?: 0) == 1 -> "1 Dia"
                                (streakState.dias ?: 0) > 1 -> "${streakState.dias} Dias"
                                else -> "0 Dias"
                            }
                            Text(
                                text = streakLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if ((streakState.dias ?: 0) > 0) colors.primary else colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sair",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Calendário semanal ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val treinoDodia = (homeUiState as? HomeUiState.ComTreino)?.treino
                diasSemana.forEach { dia ->
                    val isHoje = dia.hoje
                    val treinoHoje = if (isHoje) treinoDodia else null
                    val alturaCell = when {
                        isHoje && treinoHoje != null -> 124.dp
                        isHoje -> 88.dp
                        else -> 80.dp
                    }
                    val larguraCell = if (isHoje) 64.dp else 56.dp

                    Box(
                        modifier = Modifier.size(width = larguraCell, height = alturaCell),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(
                                    elevation = if (isHoje) 15.dp else 0.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = colors.primary.copy(alpha = 0.3f),
                                    spotColor = colors.primary.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isHoje) colors.primary else colors.surface.copy(alpha = 0.5f))
                                .border(
                                    width = 1.dp,
                                    color = if (isHoje) Color.Transparent else colors.surface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .then(
                                    if (isHoje && treinoHoje != null)
                                        Modifier.clickable { onAbrirTreinoDoDia(treinoHoje.id) }
                                    else Modifier
                                )
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = dia.abrev,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isHoje) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHoje) colors.textOnPrimary.copy(alpha = 0.8f) else colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dia.numero}",
                                style = if (isHoje) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = if (isHoje) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isHoje) colors.textOnPrimary else colors.textSecondary
                            )
                            if (isHoje) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.textOnPrimary.copy(alpha = 0.5f))
                                )
                            }
                            if (isHoje && treinoHoje != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Icon(
                                    Icons.Filled.FitnessCenter,
                                    contentDescription = "Treino hoje",
                                    tint = colors.textOnPrimary.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (isHoje && homeUiState is HomeUiState.Loading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator(
                                    color = colors.textOnPrimary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Banner Treinador ──────────────────────────────────────────────
            when (val vinculo = vinculoState) {
                is AlunoVinculoState.SemTreinador -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary.copy(alpha = 0.08f))
                            .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable { onBuscarTreinador() }
                            .padding(dimens.cardPadding)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "ENCONTRE SEU TREINADOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Busque um profissional para te acompanhar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                is AlunoVinculoState.SolicitacaoPendente -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.featureOrange.copy(alpha = 0.08f))
                            .border(1.dp, colors.featureOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(dimens.cardPadding)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.featureOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.featureOrange,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "SOLICITAÇÃO ENVIADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.featureOrange,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Aguardando resposta de ${vinculo.treinadorNome}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> Unit
            }

            // ── Banner sessão em andamento ─────────────────────────────────────
            if (temSessaoAtiva) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onRetomarSessao() }
                        .padding(dimens.cardPadding)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Retomar treino",
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "TREINO EM ANDAMENTO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Toque para retomar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Card Treino de Hoje ────────────────────────────────────────────
            when (val state = homeUiState) {
                is HomeUiState.Loading -> {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                is HomeUiState.ComTreino -> {
                    val treino = state.treino
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TREINO DE HOJE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = treino.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    if (!treino.descricao.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = treino.descricao,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                            maxLines = 2
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.FitnessCenter,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onAbrirTreinoDoDia(treino.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, colors.primary.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "Ver Treino",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary
                                    )
                                }
                                Button(
                                    onClick = { onIniciarTreino(treino.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primary,
                                        contentColor = colors.textOnPrimary
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Iniciar",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                is HomeUiState.SemTreino -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface.copy(alpha = 0.5f))
                            .border(1.dp, colors.surface.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhum treino para hoje",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Descanse ou crie um novo treino",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenTreinos,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Ver meus treinos", color = colors.textOnPrimary)
                            }
                        }
                    }
                }

                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.errorBackground.copy(alpha = 0.3f))
                            .padding(dimens.cardPadding)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> Unit
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Ações Rápidas ─────────────────────────────────────────────────
            Text(
                text = "Acesso Rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.History,
                    label = "Histórico",
                    iconColor = colors.featureBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab("historico") }
                )
                QuickActionCard(
                    icon = Icons.Filled.FitnessCenter,
                    label = "Exercícios",
                    iconColor = colors.featureGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenExercicios
                )
                QuickActionCard(
                    icon = Icons.Filled.Chat,
                    label = "Chat",
                    iconColor = colors.featureCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab("chat") }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
        } // PullToRefreshBox
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) },
            onLogout = onLogout
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface.copy(alpha = 0.5f))
            .border(1.dp, colors.surface.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true, name = "Dark Theme")
@Composable
fun TelaInicialDarkPreview() {
    AcademiaTheme(darkTheme = true) {
        HomeScreen(isDarkTheme = true)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true, name = "Light Theme")
@Composable
fun TelaInicialLightPreview() {
    AcademiaTheme(darkTheme = false) {
        HomeScreen(isDarkTheme = false)
    }
}
