package dev.fslab.academia.ui.screens.treinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinadorClienteUi
import dev.fslab.academia.ui.viewmodel.TreinadorHomeUiState
import dev.fslab.academia.ui.viewmodel.TreinadorHomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val DIAS_SEMANA = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

@Composable
fun TreinadorHomeScreen(
    nome: String = "",
    fotoUrl: String? = null,
    modifier: Modifier = Modifier,
    onOpenCliente: (String) -> Unit = {},
    onOpenClientes: () -> Unit = {},
    onNavigateTab: (String) -> Unit = {},
    onNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: TreinadorHomeViewModel = viewModel(),
    autoLoad: Boolean = true
) {
    val colors = LocalAcademiaColors.current
    var navSelected by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(autoLoad) {
        if (autoLoad) viewModel.carregar()
    }

    val hoje = LocalDate.now()
    val diaHoje = hoje.dayOfWeek.value % 7 // 0=Dom … 6=Sáb
    val inicioDaSemana = hoje.minusDays(diaHoje.toLong())
    val dataFormatada = hoje.format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
    )

    val clientes = (uiState as? TreinadorHomeUiState.Success)?.clientes.orEmpty()
    val clientesHoje = clientes.filter { it.diasTreino.contains(diaHoje) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            TreinadorNavigationBar(
                selectedIndex = navSelected,
                onItemSelected = { index ->
                    navSelected = index
                    val route = treinadorNavItems[index].route
                    if (index != 0) onNavigateTab(route)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TREINADOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = colors.primary
                    )
                    Text(
                        text = "Painel",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        lineHeight = 30.sp
                    )
                    Text(
                        text = dataFormatada.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onNotifications,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.inputBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificações",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.inputBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sair",
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Calendar strip ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DIAS_SEMANA.forEachIndexed { idx, label ->
                    val isHoje = idx == diaHoje
                    val diaMes = inicioDaSemana.plusDays(idx.toLong()).dayOfMonth
                    val temCliente = clientes.any { it.diasTreino.contains(idx) }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHoje) colors.primary else colors.surface)
                            .border(
                                width = 1.dp,
                                color = if (isHoje) Color.Transparent else colors.inputBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHoje) Color.Black else colors.textSecondary
                        )
                        Text(
                            text = diaMes.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHoje) Color.Black else colors.textPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (temCliente) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isHoje) Color.Black.copy(alpha = 0.4f) else colors.primary
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Treinam hoje ──────────────────────────────────────────────
            if (clientesHoje.isNotEmpty()) {
                Text(
                    text = "TREINAM HOJE (${clientesHoje.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    clientesHoje.forEach { cliente ->
                        AvatarChip(
                            cliente = cliente,
                            onClick = { onOpenCliente(cliente.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Divisor ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 20.dp)
                    .background(colors.inputBorder)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Meus Clientes ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Meus Clientes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (clientes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colors.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = clientes.size.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }
                }
                if (clientes.isNotEmpty()) {
                    Text(
                        text = "Ver todos →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenClientes() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentState = uiState
            when (currentState) {
                is TreinadorHomeUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                is TreinadorHomeUiState.Error -> {
                    Text(
                        text = currentState.message,
                        color = colors.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                is TreinadorHomeUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.inputBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum cliente vinculado",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                is TreinadorHomeUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        clientes.take(4).forEach { cliente ->
                            ClienteCardResumido(
                                cliente = cliente,
                                onClick = { onOpenCliente(cliente.id) }
                            )
                        }
                    }
                }
                else -> Unit
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AvatarChip(
    cliente: TreinadorClienteUi,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val iniciais = cliente.nome.split(" ").take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.15f))
                .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
        Text(
            text = cliente.nome.split(" ").firstOrNull() ?: "",
            fontSize = 10.sp,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ClienteCardResumido(
    cliente: TreinadorClienteUi,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    val iniciais = cliente.nome.split(" ").take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    val diasTexto = cliente.diasTreino.sorted()
        .joinToString(", ") { DIAS_SEMANA.getOrNull(it) ?: "" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.inputBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.15f))
                .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cliente.nome,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = diasTexto,
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                cliente.ultimoTreino?.let {
                    Text(
                        text = " · ${formatarRelativo(it)}",
                        fontSize = 10.sp,
                        color = colors.textSecondary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun formatarRelativo(data: LocalDate): String {
    val hoje = LocalDate.now()
    val dias = ChronoUnit.DAYS.between(data, hoje)
    return when (dias) {
        0L -> "hoje"
        1L -> "ontem"
        in 2..6 -> "há $dias dias"
        in 7..13 -> "há 1 semana"
        else -> "há ${dias / 7} semanas"
    }
}
