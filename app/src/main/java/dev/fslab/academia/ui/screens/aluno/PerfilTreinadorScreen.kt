package dev.fslab.academia.ui.screens.aluno

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.PerfilTreinadorUiState
import dev.fslab.academia.ui.viewmodel.PerfilTreinadorViewModel
import dev.fslab.academia.ui.viewmodel.SolicitacaoEnvioState

private val TURNOS_LABEL = mapOf("MANHA" to "Manhã", "TARDE" to "Tarde", "NOITE" to "Noite")

@Composable
fun PerfilTreinadorScreen(
    treinadorId: String,
    onBack: () -> Unit,
    viewModel: PerfilTreinadorViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val uiState by viewModel.uiState.collectAsState()
    val envioState by viewModel.envioState.collectAsState()

    LaunchedEffect(treinadorId) { viewModel.carregar(treinadorId) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = colors.textPrimary)
                }
                Text(
                    text = "Perfil do Treinador",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is PerfilTreinadorUiState.Loading, PerfilTreinadorUiState.Idle -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is PerfilTreinadorUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Person, null, tint = colors.textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = colors.textSecondary)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onBack) { Text("← Voltar", color = colors.primary) }
                    }
                }
            }
            is PerfilTreinadorUiState.Success -> {
                val t = state.treinador
                val especialidades = t.especializacao.split(",").map { it.trim() }.filter { it.isNotBlank() }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.surface.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!t.urlFoto.isNullOrBlank()) {
                                AsyncImage(
                                    model = t.urlFoto,
                                    contentDescription = t.nome,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(18.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = t.nome.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(""),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(t.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("CREF ${t.cref}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary.copy(alpha = 0.7f))
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    t.turnos.forEach { turno ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.primary.copy(alpha = 0.12f))
                                                .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
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
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (especialidades.isNotEmpty()) {
                        SectionLabel("Especialização")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            especialidades.forEach { esp ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .border(1.dp, colors.surface.copy(alpha = 0.3f), CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(esp, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (t.graduacao.isNotBlank()) {
                        SectionLabel("Graduação")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.School, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(t.graduacao, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (!t.apresentacao.isNullOrBlank()) {
                        SectionLabel("Apresentação")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Text(t.apresentacao, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary.copy(alpha = 0.85f))
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    when (envioState) {
                        is SolicitacaoEnvioState.Enviado -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.primary.copy(alpha = 0.1f))
                                    .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Solicitação enviada!", style = MaterialTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "O treinador receberá sua solicitação e poderá aceitá-la ou recusá-la.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        else -> {
                            Button(
                                onClick = { viewModel.solicitar(treinadorId) },
                                enabled = envioState !is SolicitacaoEnvioState.Enviando,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)
                            ) {
                                if (envioState is SolicitacaoEnvioState.Enviando) {
                                    CircularProgressIndicator(color = colors.textOnPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Enviando solicitação...")
                                } else {
                                    Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Solicitar Treinador", fontWeight = FontWeight.Bold)
                                }
                            }
                            if (envioState is SolicitacaoEnvioState.Erro) {
                                Spacer(Modifier.height(8.dp))
                                Text((envioState as SolicitacaoEnvioState.Erro).message, color = colors.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = colors.textSecondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
