package dev.fslab.academia.ui.screens.treinador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinadorAlunosUiState
import dev.fslab.academia.ui.viewmodel.TreinadorAlunosViewModel
import dev.fslab.academia.ui.viewmodel.TreinadorClienteUi
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val diasSemanaLabels = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

@Composable
fun TreinadorAlunosScreen(
    modifier: Modifier = Modifier,
    onOpenCliente: (String) -> Unit = {},
    onNavigateTab: (String) -> Unit = {},
    viewModel: TreinadorAlunosViewModel = viewModel(),
    autoLoad: Boolean = true
) {
    val colors = LocalAcademiaColors.current
    var navSelected by remember { mutableIntStateOf(3) } // Clientes is index 3
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val diaFiltro by viewModel.diaFiltro.collectAsState()

    LaunchedEffect(autoLoad) {
        if (autoLoad) {
            viewModel.carregar()
        }
    }

    val hoje = LocalDate.now()
    val diaHojeIdx = hoje.dayOfWeek.value % 7

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Clientes",
                subtitle = "Treinador"
            )
        },
        bottomBar = {
            TreinadorNavigationBar(
                selectedIndex = navSelected,
                onItemSelected = { index ->
                    navSelected = index
                    val route = treinadorNavItems[index].route
                    if (index != 3) {
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
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Buscar cliente...", color = colors.textSecondary, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.inputBorder,
                        cursorColor = colors.primary
                    )
                )
            }

            // Day Filter
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "FILTRAR POR DIA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Todos" Button
                    FilterChip(
                        label = "Todos",
                        isSelected = diaFiltro == null,
                        onClick = { viewModel.onDiaFiltroChange(null) }
                    )

                    diasSemanaLabels.forEachIndexed { index, dia ->
                        val isHoje = index == diaHojeIdx
                        FilterChip(
                            label = dia,
                            isSelected = diaFiltro == index,
                            isHoje = isHoje,
                            onClick = { viewModel.onDiaFiltroChange(index) }
                        )
                    }
                }
            }

            // List
            when (val state = uiState) {
                is TreinadorAlunosUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Carregando...", color = colors.textSecondary)
                    }
                }
                is TreinadorAlunosUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = colors.error, textAlign = TextAlign.Center)
                    }
                }
                is TreinadorAlunosUiState.Empty -> {
                    EmptyState()
                }
                is TreinadorAlunosUiState.Success -> {
                    val list = state.clientesFiltrados
                    if (list.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "${list.size} cliente${if (list.size != 1) "s" else ""}",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(list) { cliente ->
                                ClienteCard(
                                    cliente = cliente,
                                    diaHojeIdx = diaHojeIdx,
                                    onClick = { onOpenCliente(cliente.id) }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    isHoje: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                when {
                    isSelected -> colors.primary
                    isHoje -> colors.primary.copy(alpha = 0.1f)
                    else -> colors.surface
                }
            )
            .border(
                1.dp,
                when {
                    isSelected -> colors.primary
                    isHoje -> colors.primary.copy(alpha = 0.3f)
                    else -> colors.inputBorder
                },
                RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isSelected -> colors.textOnPrimary
                    isHoje -> colors.primary
                    else -> colors.textSecondary
                }
            )
            if (isHoje && !isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
            }
        }
    }
}

@Composable
private fun ClienteCard(
    cliente: TreinadorClienteUi,
    diaHojeIdx: Int,
    onClick: () -> Unit
) {
    val colors = LocalAcademiaColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.inputBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarCliente(nome = cliente.nome, size = 42.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nome,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val diasTexto = diasTreinoTexto(cliente.diasTreino)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GroupOff,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = diasTexto,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                cliente.ultimoTreino?.let {
                    Text(
                        text = "Último treino: ${formatarRelativo(it)}",
                        fontSize = 10.sp,
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        // Visual Days Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(7) { index ->
                val temTreino = cliente.diasTreino.contains(index)
                val isHoje = index == diaHojeIdx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            when {
                                temTreino && isHoje -> colors.primary
                                temTreino -> colors.primary.copy(alpha = 0.4f)
                                else -> colors.inputBorder.copy(alpha = 0.5f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun AvatarCliente(nome: String, size: androidx.compose.ui.unit.Dp) {
    val colors = LocalAcademiaColors.current
    val iniciais = nome.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.15f))
            .border(1.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciais,
            fontSize = (size.value / 2.5f).sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
    }
}

@Composable
private fun EmptyState() {
    val colors = LocalAcademiaColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.inputBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.GroupOff,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Nenhum cliente encontrado",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            "Tente outro filtro ou busca",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun diasTreinoTexto(dias: Set<Int>): String {
    if (dias.isEmpty()) return "Sem dias definidos"
    return dias.sorted().joinToString(", ") { diasSemanaLabels.getOrNull(it) ?: "" }
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
