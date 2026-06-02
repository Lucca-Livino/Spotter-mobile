package dev.fslab.academia.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.components.AppNavigationBar
import dev.fslab.academia.ui.components.MAIS_ROUTE
import dev.fslab.academia.ui.components.MaisMenuBottomSheet
import dev.fslab.academia.ui.components.TreinadorNavigationBar
import dev.fslab.academia.ui.components.alunoNavItems
import dev.fslab.academia.ui.components.treinadorNavItems
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.ChatViewModel
import dev.fslab.academia.ui.viewmodel.ConversaIniciarUiState
import dev.fslab.academia.ui.viewmodel.ConversaListUiState
import dev.fslab.academia.ui.viewmodel.ConversaClienteUi
import dev.fslab.academia.ui.viewmodel.ConversasViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreen(
    userTipo: UserTipo,
    onNavigateTab: (String) -> Unit,
    onOpenConversa: (String) -> Unit,
    viewModel: ConversasViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    var mostrarMaisMenu by remember { mutableStateOf(false) }

    val listState by viewModel.uiState.collectAsState()
    val iniciarState by viewModel.iniciarState.collectAsState()
    val navegarConversa by viewModel.navegarConversa.collectAsState()

    val conversaIdAluno by chatViewModel.conversaId.collectAsState()

    LaunchedEffect(Unit) {
        if (userTipo == UserTipo.TREINADOR) {
            viewModel.carregar()
        } else {
            chatViewModel.iniciarConversaAluno()
        }
    }

    LaunchedEffect(navegarConversa) {
        val id = navegarConversa
        if (!id.isNullOrBlank()) {
            viewModel.consumirNavegacao()
            onOpenConversa(id)
        }
    }

    DisposableEffect(conversaIdAluno) {
        val id = conversaIdAluno
        if (!id.isNullOrBlank()) {
            chatViewModel.bindSocket(id)
        }
        onDispose {
            if (!id.isNullOrBlank()) {
                chatViewModel.unbindSocket(id)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (userTipo == UserTipo.ALUNO) {
                AppNavigationBar(
                    items = alunoNavItems,
                    selectedIndex = 2,
                    onItemSelected = { idx ->
                        val route = alunoNavItems[idx].route
                        when {
                            route == MAIS_ROUTE -> mostrarMaisMenu = true
                            else -> onNavigateTab(route)
                        }
                    }
                )
            } else {
                TreinadorNavigationBar(
                    selectedIndex = 2,
                    onItemSelected = { index ->
                        val route = treinadorNavItems[index].route
                        if (index != 2) {
                            onNavigateTab(route)
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (userTipo == UserTipo.TREINADOR) {
                TreinadorChatLista(
                    listState = listState,
                    iniciarState = iniciarState,
                    onAbrir = { alunoId -> viewModel.iniciarConversa(alunoId) }
                )
            } else {
                ChatConversationBody(
                    conversaId = conversaIdAluno,
                    userTipo = userTipo,
                    viewModel = chatViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (mostrarMaisMenu) {
        MaisMenuBottomSheet(
            onDismiss = { mostrarMaisMenu = false },
            onNavegar = { route ->
                mostrarMaisMenu = false
                onNavigateTab(route)
            }
        )
    }
}

@Composable
private fun TreinadorChatLista(
    listState: ConversaListUiState,
    iniciarState: ConversaIniciarUiState,
    onAbrir: (String) -> Unit
) {
    val colors = LocalAcademiaColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mensagens",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        val totalNaoLidas = (listState as? ConversaListUiState.Success)
            ?.clientes?.sumOf { it.mensagensNaoLidas } ?: 0
        if (totalNaoLidas > 0) {
            Text(
                text = "$totalNaoLidas não lida${if (totalNaoLidas != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar (Ref: Lista...png)
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Buscar cliente...", color = colors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            ),
            shape = RoundedCornerShape(28.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (listState) {
            ConversaListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is ConversaListUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listState.clientes, key = { it.alunoId }) { cliente ->
                        ConversaClienteItem(
                            cliente = cliente,
                            onAbrir = onAbrir
                        )
                    }
                }
            }
            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem mensagens", color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun ConversaClienteItem(
    cliente: ConversaClienteUi,
    onAbrir: (String) -> Unit
) {
    val colors = LocalAcademiaColors.current
    val hasUnread = cliente.mensagensNaoLidas > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrir(cliente.alunoId) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with Badge (Ref: Lista...png)
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cliente.nome.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase(),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (hasUnread) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp),
                    color = colors.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = if (cliente.mensagensNaoLidas > 99) "99+" else cliente.mensagensNaoLidas.toString(),
                        color = colors.textOnPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cliente.nome,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = cliente.ultimaMensagemEm?.let { formatarData(it) } ?: "Agora",
                    color = if (hasUnread) colors.primary else colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (cliente.ultimaMensagemEm != null) "Toque para ver a conversa" else "Inicie uma conversa",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatarData(raw: String): String {
    return runCatching {
        val dt = OffsetDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: "Seg"
}
