package dev.fslab.academia.ui.screens.treinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinadorClienteUi
import dev.fslab.academia.ui.viewmodel.TreinadorHomeUiState
import dev.fslab.academia.ui.viewmodel.TreinadorHomeViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab")

@Composable
fun TreinadorHomeScreen(
    nome: String = "",
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
        if (autoLoad) {
            viewModel.carregar()
        }
    }

    val hoje = LocalDate.now()
    val diaHoje = dayOfWeekIndex(hoje.dayOfWeek)
    val clientes = (uiState as? TreinadorHomeUiState.Success)?.clientes.orEmpty()
    val clientesHoje = clientes.filter { it.diasTreino.contains(diaHoje) }
    val dataFormatada = formatarDataExtenso(hoje)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            TreinadorNavigationBar(
                selectedIndex = navSelected,
                onItemSelected = { index ->
                    navSelected = index
                    val route = treinadorNavItems[index].route
                    if (index != 0) {
                        onNavigateTab(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Treinador",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = colors.primary
                        )
                        Text(
                            text = "Painel",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        if (nome.isNotBlank()) {
                            Text(
                                text = "Ola, $nome",
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = dataFormatada,
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.inputBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notificacoes",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.inputBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Sair",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    diasSemana.forEachIndexed { index, dia ->
                        val isHoje = index == diaHoje
                        val temCliente = clientes.any { it.diasTreino.contains(index) }
                        val dataDia = hoje.minusDays(diaHoje.toLong()).plusDays(index.toLong())
                        Column(
                            modifier = Modifier
                                .width(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isHoje) colors.primary else colors.surface
                                )
                                .border(
                                    1.dp,
                                    if (isHoje) colors.primary else colors.inputBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dia,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHoje) colors.textOnPrimary else colors.textSecondary
                            )
                            Text(
                                text = dataDia.dayOfMonth.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHoje) colors.textOnPrimary else colors.textPrimary
                            )
                            if (temCliente) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isHoje) colors.textOnPrimary.copy(alpha = 0.5f)
                                            else colors.primary
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                when (val state = uiState) {
                    TreinadorHomeUiState.Loading -> {
                        Text(
                            text = "Carregando clientes...",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    TreinadorHomeUiState.Empty -> {
                        Text(
                            text = "Nenhum cliente vinculado ainda.",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is TreinadorHomeUiState.Error -> {
                        Text(
                            text = state.message,
                            fontSize = 11.sp,
                            color = colors.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    else -> Unit
                }

                if (clientesHoje.isNotEmpty()) {
                    Text(
                        text = "Treinam hoje (${clientesHoje.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        clientesHoje.forEach { cliente ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .padding(bottom = 4.dp)
                            ) {
                                AvatarCliente(nome = cliente.nome, size = 48.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cliente.nome.split(" ").firstOrNull().orEmpty(),
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.inputBorder)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "Clientes",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Meus Clientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = clientes.size.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }

                    Text(
                        text = "Ver todos ->",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onOpenClientes() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    clientes.take(4).forEach { cliente ->
                        ClienteResumoCard(
                            cliente = cliente,
                            onClick = { onOpenCliente(cliente.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteResumoCard(
    cliente: TreinadorClienteUi,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.inputBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarCliente(nome = cliente.nome, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cliente.nome,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val diasTexto = diasTreinoTexto(cliente.diasTreino)
            val ultimo = cliente.ultimoTreino?.let { formatarRelativo(it) }
            val info = listOfNotNull(
                diasTexto.takeIf { it.isNotBlank() },
                ultimo?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            Text(
                text = info,
                fontSize = 10.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Detalhes",
            tint = colors.textSecondary
        )
    }
}

@Composable
private fun AvatarCliente(nome: String, size: androidx.compose.ui.unit.Dp) {
    val colors = LocalAcademiaColors.current
    val iniciais = nome.split(" ").take(2).joinToString(" ") { it.firstOrNull()?.uppercase() ?: "" }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.2f))
            .border(1.dp, colors.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciais,
            fontSize = (size.value / 3.2f).sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
    }
}

private fun dayOfWeekIndex(dayOfWeek: DayOfWeek): Int {
    return dayOfWeek.value % 7
}

private fun formatarDataExtenso(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
    val texto = date.format(formatter)
    return texto.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale("pt", "BR")) else char.toString()
    }
}

private fun diasTreinoTexto(dias: Set<Int>): String {
    return dias.sorted().joinToString(" · ") { diasSemana.getOrNull(it) ?: "" }.trim()
}

private fun formatarRelativo(data: LocalDate): String {
    val hoje = LocalDate.now()
    val dias = ChronoUnit.DAYS.between(data, hoje)
    return when (dias) {
        0L -> "hoje"
        1L -> "ontem"
        in 2..6 -> "$dias dias"
        in 7..13 -> "1 semana"
        else -> "${dias / 7} semanas"
    }
}

@Preview(showBackground = true)
@Composable
private fun TreinadorHomeScreenPreview() {
    AcademiaTheme(darkTheme = true) {
        TreinadorHomeScreen(nome = "Marcos", autoLoad = false)
    }
}
