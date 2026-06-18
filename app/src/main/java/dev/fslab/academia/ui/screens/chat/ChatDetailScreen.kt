package dev.fslab.academia.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.MensagemConversaData
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.components.AcademiaAppBar
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.theme.LocalDimens
import dev.fslab.academia.ui.viewmodel.ChatEnviarUiState
import dev.fslab.academia.ui.viewmodel.ChatMensagensUiState
import dev.fslab.academia.ui.viewmodel.ChatViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.border

@Composable
fun ChatDetailScreen(
    conversaId: String,
    userTipo: UserTipo,
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(conversaId) {
        viewModel.carregarMensagens(conversaId)
    }

    DisposableEffect(conversaId) {
        viewModel.bindSocket(conversaId)
        onDispose {
            viewModel.unbindSocket(conversaId)
        }
    }

    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current

    Scaffold(
        topBar = {
            AcademiaAppBar(
                title = title,
                subtitle = "Online agora",
                showBackButton = showBack,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = colors.textPrimary)
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        ChatConversationBody(
            conversaId = conversaId,
            userTipo = userTipo,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
fun ChatConversationBody(
    conversaId: String?,
    userTipo: UserTipo,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current
    val mensagensState by viewModel.mensagensState.collectAsState()
    val enviarState by viewModel.enviarState.collectAsState()
    val listState = rememberLazyListState()

    var texto by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        // Data Separator (Ref: Chat.jpg "Hoje, 10:23")
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Surface(
                color = colors.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Hoje, 10:23",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        when (val state = mensagensState) {
            ChatMensagensUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is ChatMensagensUiState.Success -> {
                val mensagensOrdenadas = state.mensagens.sortedByDescending { it.enviadaEm }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                    reverseLayout = true,
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(mensagensOrdenadas, key = { it.id }) { mensagem ->
                        MensagemItem(
                            mensagem = mensagem,
                            souRemetente = mensagem.remetenteTipo.uppercase() == userTipo.name
                        )
                    }
                }
            }
            else -> Spacer(modifier = Modifier.weight(1f))
        }

        if (!conversaId.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    placeholder = {
                        Text(text = "Digite sua mensagem...", color = colors.textSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(12.dp))

                FloatingActionButton(
                    onClick = {
                        if (texto.isNotBlank()) {
                            viewModel.enviarMensagem(conversaId, texto)
                            texto = ""
                        }
                    },
                    containerColor = colors.primary,
                    contentColor = colors.textOnPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MensagemItem(
    mensagem: MensagemConversaData,
    souRemetente: Boolean
) {
    val colors = LocalAcademiaColors.current
    val dimens = LocalDimens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (souRemetente) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!souRemetente) {
            // Avatar (Ref: Chat.jpg)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T", // Placeholder for actual avatar
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            horizontalAlignment = if (souRemetente) Alignment.End else Alignment.Start
        ) {
            if (!souRemetente) {
                Text(
                    text = "Treinador Marcos", // Should come from conversation data
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (souRemetente) 20.dp else 4.dp,
                        bottomEnd = if (souRemetente) 4.dp else 20.dp
                    ))
                    .background(if (souRemetente) Color.Transparent else colors.surface)
                    .then(
                        if (souRemetente) Modifier.border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = mensagem.conteudo,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatarHora(mensagem.enviadaEm),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
                if (souRemetente) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓✓", // Simplified status
                        color = if (mensagem.lidaEm != null) colors.primary else colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun formatarHora(raw: String): String {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    return runCatching { OffsetDateTime.parse(raw).format(fmt) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw).format(fmt) }.getOrNull()
        ?: raw.take(5)
}
