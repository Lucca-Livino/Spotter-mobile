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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.academia.R
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.TreinadorClienteUi
import dev.fslab.academia.ui.viewmodel.TreinadorHomeUiState
import dev.fslab.academia.ui.viewmodel.TreinadorHomeViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val context = LocalContext.current
    var navSelected by remember { mutableIntStateOf(0) }
    
    // Solicitar permissão de notificações no Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { /* ignorar resultado */ }
        
        LaunchedEffect(Unit) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(autoLoad) {
        if (autoLoad) {
            viewModel.carregar()
        }
    }

    val hoje = LocalDate.now()
    val diaHoje = (hoje.dayOfWeek.value % 7) // 0=Dom, 1=Seg...
    val clientes = (uiState as? TreinadorHomeUiState.Success)?.clientes.orEmpty()
    val clientesHoje = clientes.filter { it.diasTreino.contains(diaHoje) }
    val dataFormatada = hoje.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))

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
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header Section ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(52.dp)) {
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
                                    .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "BEM-VINDO DE VOLTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = if (nome.isBlank()) "Olá!" else "Olá, $nome",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, Color(0xFF262626), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, Color(0xFF262626), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sair",
                                tint = colors.errorText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dataFormatada.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Conteúdo do Painel (Resumo) ─────────────────────────────
            // ... resto do conteúdo do treinador ...
            Text(
                text = "Alunos de Hoje",
                modifier = Modifier.padding(horizontal = 24.dp),
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            // (Placeholder para lista de alunos)
        }
    }
}
