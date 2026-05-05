package dev.fslab.academia.ui.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.theme.LocalAcademiaColors

// ─── Dados mockados ───────────────────────────────────────────────────────────

private data class DiaSemana(val abrev: String, val numero: Int, val hoje: Boolean = false)

private val diasSemana = listOf(
    DiaSemana("DOM", 10),
    DiaSemana("SEG", 11),
    DiaSemana("HOJE", 12, hoje = true),
    DiaSemana("QUA", 13),
    DiaSemana("QUI", 14),
    DiaSemana("SEX", 15),
)

// ─── HomeScreen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nome: String = "",
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenExercicios: () -> Unit = {},
    onOpenTreinos: () -> Unit = {},
    onRetomarSessao: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {},
    temSessaoAtiva: Boolean = false
) {
    val colors = LocalAcademiaColors.current
    var mostrarMaisMenu by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header: avatar + nome + ações ────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Avatar + saudação
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(2.dp, colors.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Avatar",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        // Indicador online
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .border(2.dp, colors.background, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "BEM-VINDO DE VOLTA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Olá, $nome!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .width(100.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50)),
                            color = colors.primary,
                            trackColor = colors.lightGray,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }

                // Ações do header: streak + toggle + logout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Badge streak
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "15",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    // Toggle tema
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkTheme) "Modo claro" else "Modo escuro",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // ── Calendário semanal ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                diasSemana.forEach { dia ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (dia.hoje) colors.primary else colors.surface)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = dia.abrev,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dia.hoje) colors.textOnPrimary else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${dia.numero}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dia.hoje) colors.textOnPrimary else colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dia.hoje) colors.textOnPrimary else Color.Transparent
                                )
                        )
                    }
                }
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

            // ── Card do treino ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge "TREINO B"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TREINO B",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                                append("Peito e\n")
                            }
                            withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                                append("Tríceps")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estatísticas rápidas do treino
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple(Icons.Filled.Timer, "Duração", "60 min"),
                            Triple(Icons.Filled.LocalFireDepartment, "Intensidade", "Alta"),
                            Triple(Icons.Filled.FitnessCenter, "Exercícios", "8")
                        ).forEach { (icon, label, valor) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.lightGray)
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = valor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Progresso Semanal", fontSize = 12.sp, color = colors.textSecondary)
                        Text(text = "2/5 Concluídos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 2f / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = colors.primary,
                        trackColor = colors.lightGray,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botão INICIAR TREINO
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primary)
                            .clickable { }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = colors.textOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INICIAR TREINO",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = colors.textOnPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Recado do Treinador ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recado do Treinador",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = "VER MAIS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.size(46.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(colors.lightGray)
                            .border(2.dp, colors.primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .border(2.dp, colors.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Treinador Marcos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(text = "10:30", fontSize = 12.sp, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Fala Lucas! Hoje é dia de aumentar a carga no supino. Foca na descida controlada (3s). Bom treino! 👊",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Quick Actions ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple(Icons.Filled.Scale, "Registrar Peso", colors.primary),
                    Triple(Icons.Filled.WaterDrop, "Beber Água", colors.featureCyan)
                ).forEach { (icon, label, iconColor) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .clickable { }
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Dark Theme")
@Composable
fun TelaInicialDarkPreview() {
    AcademiaTheme(darkTheme = true) {
        HomeScreen(isDarkTheme = true)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Light Theme")
@Composable
fun TelaInicialLightPreview() {
    AcademiaTheme(darkTheme = false) {
        HomeScreen(isDarkTheme = false)
    }
}