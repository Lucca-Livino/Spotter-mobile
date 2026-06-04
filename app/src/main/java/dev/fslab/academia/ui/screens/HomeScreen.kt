package dev.fslab.academia.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import dev.fslab.academia.R
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.DiaSemana as DiaSemanaEnum
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.ui.theme.AcademiaColors
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
    val days = mutableListOf<DiaSemana>()
    val formatter = DateTimeFormatter.ofPattern("EEE", Locale("pt", "BR"))
    
    // Gerar 6 dias (2 dias antes, hoje, 3 dias depois) para replicar o design do Figma
    for (i in -2..3) {
        val date = today.plusDays(i.toLong())
        val isToday = i == 0
        val abrev = if (isToday) "HOJE" else {
            val formatted = date.format(formatter).uppercase(Locale.getDefault())
            if (formatted.length > 3) formatted.substring(0, 3) else formatted
        }.replace(".", "")
        days.add(DiaSemana(abrev, date.dayOfMonth, isToday))
    }
    return days
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
    homeViewModel: HomeViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current
    var mostrarMaisMenu by remember { mutableStateOf(false) }
    val homeUiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.carregarTreinoDoDia()
    }

    // Solicitar permissão de notificações no Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { /* ignorar resultado por enquanto */ }
        
        LaunchedEffect(Unit) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
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
                items = alunoNavItems,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Header: avatar + saudação + streak ────────────────────────
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = if (nome.isBlank()) "Olá!" else "Olá, $nome",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                // Ações do header: streak + toggle + logout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge streak
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
                            contentDescription = "Streak",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "15 Dias",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    // Logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(36.dp)
                    ) {
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

            // ── Calendário semanal ───────────────────────────────────
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
                                fontSize = 10.sp,
                                fontWeight = if (isHoje) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHoje) colors.textOnPrimary.copy(alpha = 0.8f) else colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dia.numero}",
                                fontSize = if (isHoje) 24.sp else 18.sp,
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

            if (homeUiState is HomeUiState.SemTreino) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    text = "Nenhum treino hoje",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Banner sessão em andamento ───────────────────────────
            if (temSessaoAtiva) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onRetomarSessao() }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Retomar treino", tint = colors.primary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "TREINO EM ANDAMENTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Toque para retomar",
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Recado do Treinador ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recado do Treinador",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "VER MAIS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface.copy(alpha = 0.5f))
                    .border(1.dp, colors.inputBorder.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.lightGray)
                            .border(1.dp, colors.inputBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .border(2.dp, colors.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Treinador Marcos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(text = "10:30", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fala Lucas! Hoje é dia de aumentar a carga no supino. Foca na descida controlada (3s). Bom treino! \uD83D\uDC4A",
                        fontSize = 14.sp,
                        color = colors.textPrimary.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Quick Actions ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Triple(Icons.Filled.Scale, "Registrar Peso", colors.primary),
                    Triple(Icons.Filled.WaterDrop, "Beber Água", colors.featureBlue) // Usando featureBlue
                ).forEach { (icon, label, iconColor) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface.copy(alpha = 0.5f))
                            .border(1.dp, colors.inputBorder.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .clickable { }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) }
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