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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.academia.R
import dev.fslab.academia.model.User
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.PerfilUiState
import dev.fslab.academia.ui.viewmodel.PerfilViewModel
import dev.fslab.academia.ui.viewmodel.ThemeMode
import dev.fslab.academia.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    currentUser: User?,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onOpenPerfil: () -> Unit = {},
    themeViewModel: ThemeViewModel = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val themeMode by themeViewModel.themeMode.collectAsState()
    val perfilState by perfilViewModel.uiState.collectAsState()

    var mostrarConfirmLogout by remember { mutableStateOf(false) }
    var mostrarConfirmDesvincular by remember { mutableStateOf(false) }
    var mostrarConfirmExcluir by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (perfilState is PerfilUiState.Idle) {
            perfilViewModel.carregarPerfil(currentUser?.tipo ?: UserTipo.ALUNO)
        }
    }

    val treinadorVinculado = when (val s = perfilState) {
        is PerfilUiState.SuccessAluno -> s.profile.treinadorId != null
        else -> false
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AcademiaAppBar(
                title = "Configurações",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = dimens.screenPaddingH)
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(dimens.spaceLg))

            // ── Perfil ───────────────────────────────────────────────────────
            SecaoTitulo("Perfil")
            Spacer(Modifier.height(dimens.spaceSm))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenPerfil() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentUser?.image?.takeIf { it.isNotBlank() } ?: R.drawable.no_profile_photo)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(2.dp, colors.primary.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentUser?.email ?: "—",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = when (currentUser?.tipo) {
                                UserTipo.TREINADOR -> "Treinador"
                                else -> "Aluno"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(dimens.spaceLg))

            // ── Aparência ─────────────────────────────────────────────────────
            SecaoTitulo("Aparência")
            Spacer(Modifier.height(dimens.spaceSm))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    ThemeOption(
                        icone = Icons.Filled.BrightnessAuto,
                        label = "Sistema",
                        descricao = "Segue o tema do dispositivo",
                        selecionado = themeMode == ThemeMode.SYSTEM,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.SYSTEM) },
                        mostrarDivisor = true
                    )
                    ThemeOption(
                        icone = Icons.Filled.Brightness7,
                        label = "Claro",
                        descricao = "Sempre tema claro",
                        selecionado = themeMode == ThemeMode.LIGHT,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                        mostrarDivisor = true
                    )
                    ThemeOption(
                        icone = Icons.Filled.Brightness4,
                        label = "Escuro",
                        descricao = "Sempre tema escuro",
                        selecionado = themeMode == ThemeMode.DARK,
                        onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                        mostrarDivisor = false
                    )
                }
            }

            Spacer(Modifier.height(dimens.spaceLg))

            // ── Conta ─────────────────────────────────────────────────────────
            SecaoTitulo("Conta")
            Spacer(Modifier.height(dimens.spaceSm))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    if (treinadorVinculado) {
                        ContaItem(
                            icone = Icons.Filled.LinkOff,
                            label = "Desvincular treinador",
                            descricao = "Remover vínculo com o treinador atual",
                            iconColor = colors.featureOrange,
                            onClick = { mostrarConfirmDesvincular = true },
                            mostrarDivisor = true
                        )
                    }
                    ContaItem(
                        icone = Icons.AutoMirrored.Filled.Logout,
                        label = "Sair",
                        descricao = "Encerrar sessão neste dispositivo",
                        iconColor = colors.primary,
                        onClick = { mostrarConfirmLogout = true },
                        mostrarDivisor = true
                    )
                    ContaItem(
                        icone = Icons.Filled.PersonRemove,
                        label = "Excluir conta",
                        descricao = "Apagar permanentemente sua conta e dados",
                        iconColor = colors.error,
                        labelColor = colors.error,
                        onClick = { mostrarConfirmExcluir = true },
                        mostrarDivisor = false
                    )
                }
            }

            Spacer(Modifier.height(dimens.spaceLg * 2))

            Text(
                text = "Versão 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(dimens.spaceLg))
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────
    if (mostrarConfirmLogout) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmLogout = false },
            containerColor = colors.surface,
            title = { Text("Sair", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja encerrar sua sessão?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { mostrarConfirmLogout = false; onLogout() }) {
                    Text("Sair", color = colors.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmLogout = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    if (mostrarConfirmDesvincular) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmDesvincular = false },
            containerColor = colors.surface,
            title = { Text("Desvincular treinador", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Você perderá acesso ao plano de treino do seu treinador. Deseja continuar?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmDesvincular = false
                    perfilViewModel.desvincularTreinador(onSuccess = {})
                }) {
                    Text("Desvincular", color = colors.featureOrange, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmDesvincular = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    if (mostrarConfirmExcluir) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmExcluir = false },
            containerColor = colors.surface,
            title = { Text("Excluir conta", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Esta ação é irreversível. Todos os seus dados serão apagados permanentemente.", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmExcluir = false
                    perfilViewModel.deletarConta(context, onSuccess = onLogout)
                }) {
                    Text("Excluir conta", color = colors.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmExcluir = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SecaoTitulo(titulo: String) {
    val colors = LocalAcademiaColors.current
    Text(
        text = titulo.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = colors.textSecondary
    )
}

@Composable
private fun ThemeOption(
    icone: ImageVector,
    label: String,
    descricao: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    mostrarDivisor: Boolean
) {
    val colors = LocalAcademiaColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selecionado) colors.primary.copy(alpha = 0.12f)
                        else colors.inputBorder.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icone,
                    contentDescription = null,
                    tint = if (selecionado) colors.primary else colors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selecionado) colors.primary else colors.textPrimary
                )
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
            if (selecionado) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (mostrarDivisor) {
            HorizontalDivider(
                color = colors.inputBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ContaItem(
    icone: ImageVector,
    label: String,
    descricao: String,
    iconColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit,
    mostrarDivisor: Boolean
) {
    val colors = LocalAcademiaColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icone,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = labelColor ?: colors.textPrimary
                )
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
        if (mostrarDivisor) {
            HorizontalDivider(
                color = colors.inputBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
