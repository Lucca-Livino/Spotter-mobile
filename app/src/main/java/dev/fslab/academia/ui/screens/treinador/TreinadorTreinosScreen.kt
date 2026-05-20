package dev.fslab.academia.ui.screens.treinador

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.TreinoData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinoListUiState
import dev.fslab.academia.ui.viewmodel.TreinoViewModel

@Composable
fun TreinadorTreinosScreen(
    onNavigateTab: (String) -> Unit,
    onAbrirDetalhe: (String) -> Unit = {},
    onCriar: () -> Unit = {},
    viewModel: TreinoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    val treinos = (uiState as? TreinoListUiState.Success)?.treinos.orEmpty()
    val templates = treinos.filter { it.usuarioId == null }
    val treinosClientes = treinos.filter { it.usuarioId != null }
    val isLoading = uiState is TreinoListUiState.Loading || uiState is TreinoListUiState.Idle

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Treinos",
                subtitle = if (treinos.isNotEmpty()) "${treinos.size} treinos" else null,
                showBackButton = false,
                actions = {
                    IconButton(onClick = { viewModel.carregar() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        bottomBar = {
            val currentIndex = treinadorNavItems.indexOfFirst { it.route == "treinador_treinos" }
            TreinadorNavigationBar(
                selectedIndex = if (currentIndex >= 0) currentIndex else 1,
                onItemSelected = { idx ->
                    val route = treinadorNavItems[idx].route
                    onNavigateTab(route)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCriar,
                containerColor = colors.primary,
                contentColor = colors.textOnPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo template", fontWeight = FontWeight.Bold) }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                } else if (uiState is TreinoListUiState.Error) {
                    item {
                        CardErroTemplate((uiState as TreinoListUiState.Error).message) { viewModel.carregar() }
                    }
                } else {
                    item {
                        Text(
                            text = "TEMPLATES",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (templates.isEmpty()) {
                        item { CardVazioTemplate() }
                    } else {
                        items(templates, key = { it.id }) { treino ->
                            TemplateCard(
                                treino = treino,
                                onClick = { onAbrirDetalhe(treino.id) }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        Text(
                            text = "TREINOS DE CLIENTES",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (treinosClientes.isEmpty()) {
                        item { CardVazioTreinoCliente() }
                    } else {
                        items(treinosClientes, key = { it.id }) { treino ->
                            TreinoClienteCard(
                                treino = treino,
                                onClick = { onAbrirDetalhe(treino.id) }
                            )
                        }
                    }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TemplateCard(treino: TreinoData, onClick: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val descricao = treino.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(colors.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TEMPLATE",
                                color = colors.textOnPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        treino.nome,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        descricao,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary)
                }
            }
        }
    }
}

@Composable
private fun TreinoClienteCard(treino: TreinoData, onClick: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val descricao = treino.descricao?.takeIf { it.isNotBlank() } ?: "Sem descrição"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors.surface,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        treino.nome,
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        descricao,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = colors.primary)
                }
            }
        }
    }
}

@Composable
private fun CardErroTemplate(mensagem: String, onTentarNovamente: () -> Unit) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Erro ao carregar templates",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(mensagem, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onTentarNovamente,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary
                )
            ) { Text("Tentar novamente") }
        }
    }
}

@Composable
private fun CardVazioTemplate() {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Nenhum template cadastrado",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Crie seu primeiro template tocando no botão 'Novo template'. Templates facilitam a prescrição para seus alunos.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CardVazioTreinoCliente() {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Nenhum treino de cliente",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Crie um treino a partir do perfil do cliente para ele aparecer aqui.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
