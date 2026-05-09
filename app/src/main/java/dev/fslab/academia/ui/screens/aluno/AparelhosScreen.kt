package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.AparelhoData
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.AparelhoUiState
import dev.fslab.academia.ui.viewmodel.AparelhoViewModel

@Composable
fun AparelhosScreen(
    onBack: () -> Unit,
    onNavigateTab: (String) -> Unit,
    viewModel: AparelhoViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val uiState by viewModel.uiState.collectAsState()

    var busca by remember { mutableStateOf("") }
    var mostrarMaisMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    LaunchedEffect(busca) { viewModel.carregar(nome = busca.ifBlank { null }) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Aparelhos",
                subtitle = (uiState as? AparelhoUiState.Success)?.let { "${it.aparelhos.size} aparelhos" },
                showBackButton = true,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { viewModel.carregar(nome = busca.ifBlank { null }) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        bottomBar = {
            AppNavigationBar(
                items = alunoNavItems,
                selectedIndex = 4,
                onItemSelected = { idx ->
                    val route = alunoNavItems[idx].route
                    if (route == MAIS_ROUTE) mostrarMaisMenu = true
                    else onNavigateTab(route)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(colors.backgroundGradientStart, colors.backgroundGradientEnd)))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar por nome") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textInput,
                    unfocusedTextColor = colors.textInput,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedContainerColor = colors.lightGray,
                    unfocusedContainerColor = colors.lightGray,
                    cursorColor = colors.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                AparelhoUiState.Idle, AparelhoUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is AparelhoUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            state.message,
                            color = colors.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is AparelhoUiState.Success -> {
                    if (state.aparelhos.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Build, null, tint = colors.textSecondary, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Nenhum aparelho encontrado", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.aparelhos, key = { it.id }) { aparelho ->
                                AparelhoCard(aparelho)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route -> onNavigateTab(route) }
        )
    }
}

@Composable
private fun AparelhoCard(aparelho: AparelhoData) {
    val colors = LocalAcademiaColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                aparelho.nome,
                color = colors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            aparelho.descricao?.takeIf { it.isNotBlank() }?.let { desc ->
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
