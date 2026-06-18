package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.fslab.academia.model.TreinadorData
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.BuscarTreinadorUiState
import dev.fslab.academia.ui.viewmodel.BuscarTreinadorViewModel

private val TURNOS_LABEL = mapOf("MANHA" to "Manhã", "TARDE" to "Tarde", "NOITE" to "Noite")

@Composable
fun BuscarTreinadorScreen(
    onBack: () -> Unit,
    onAbrirPerfil: (String) -> Unit,
    viewModel: BuscarTreinadorViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val uiState by viewModel.uiState.collectAsState()
    val search by viewModel.search.collectAsState()

    LaunchedEffect(Unit) { viewModel.carregar() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.textPrimary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ALUNO",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buscar Treinador",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = search,
                    onValueChange = { viewModel.onSearchChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (search.isEmpty()) {
                            Text("Nome ou especialidade...", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            when (val state = uiState) {
                is BuscarTreinadorUiState.Loading, BuscarTreinadorUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is BuscarTreinadorUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = colors.error, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.carregar() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) { Text("Tentar novamente") }
                        }
                    }
                }
                is BuscarTreinadorUiState.Success -> {
                    Text(
                        text = "${state.treinadores.size} treinador${if (state.treinadores.size != 1) "es" else ""} disponível${if (state.treinadores.size != 1) "is" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (state.treinadores.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum resultado", color = colors.textSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.treinadores, key = { it.id }) { treinador ->
                                TreinadorCard(
                                    treinador = treinador,
                                    onClick = { onAbrirPerfil(treinador.id) }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreinadorCard(treinador: TreinadorData, onClick: () -> Unit) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val especialidades = treinador.especializacao.split(",").map { it.trim() }.filter { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface.copy(alpha = 0.5f))
            .border(1.dp, colors.surface.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!treinador.urlFoto.isNullOrBlank()) {
            AsyncImage(
                model = treinador.urlFoto,
                contentDescription = treinador.nome,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = treinador.nome.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = treinador.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    treinador.turnos.take(2).forEach { turno ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = TURNOS_LABEL[turno] ?: turno,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (especialidades.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    especialidades.take(3).forEach { esp ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surface)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(esp, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                    }
                }
            }

            if (!treinador.apresentacao.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = treinador.apresentacao,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "CREF ${treinador.cref}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
