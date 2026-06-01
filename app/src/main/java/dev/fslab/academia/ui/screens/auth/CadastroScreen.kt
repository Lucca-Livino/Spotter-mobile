package dev.fslab.academia.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.academia.model.AcademiaData
import dev.fslab.academia.model.Genero
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.CadastroUiState
import dev.fslab.academia.ui.viewmodel.CadastroViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CadastroViewModel = viewModel()
) {
    val colors = LocalAcademiaColors.current
    val uiState by viewModel.uiState.collectAsState()
    val academias by viewModel.academias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Conta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState is CadastroUiState.DadosPerfil) viewModel.voltarParaConta()
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    if (targetState is CadastroUiState.DadosPerfil) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "cadastro_step"
            ) { state ->
                when (state) {
                    CadastroUiState.DadosConta -> StepConta(viewModel)
                    CadastroUiState.DadosPerfil -> StepPerfil(viewModel, academias)
                    CadastroUiState.Carregando -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    CadastroUiState.Sucesso -> {
                        LaunchedEffect(Unit) { onSuccess() }
                    }
                    is CadastroUiState.Erro -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(24.dp)
                        ) {
                            Text(state.mensagem, color = colors.errorText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.voltarParaConta() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                            ) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepConta(viewModel: CadastroViewModel) {
    val colors = LocalAcademiaColors.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Primeiro, os dados da sua conta", color = colors.textSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        CadastroField("Nome completo", viewModel.nome, { viewModel.nome = it }, Icons.Default.Person)
        CadastroField("E-mail", viewModel.email, { viewModel.email = it }, Icons.Default.Email, enabled = !viewModel.isSocial)
        
        if (!viewModel.isSocial) {
            CadastroField("Senha", viewModel.senha, { viewModel.senha = it }, Icons.Default.Lock, isPassword = true)
        }

        Spacer(Modifier.height(16.dp))
        Text("Eu sou:", color = colors.textSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserTypeButton("ALUNO", viewModel.tipo == UserTipo.ALUNO, { viewModel.tipo = UserTipo.ALUNO }, Modifier.weight(1f))
            UserTypeButton("TREINADOR", viewModel.tipo == UserTipo.TREINADOR, { viewModel.tipo = UserTipo.TREINADOR }, Modifier.weight(1f))
        }

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {
                viewModel.avancarParaPerfil()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
        ) {
            Text("PRÓXIMO PASSO", fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepPerfil(viewModel: CadastroViewModel, academias: List<AcademiaData>) {
    val colors = LocalAcademiaColors.current
    
    // --- Lógica Date Picker ---
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("") }
    var rawDateValue by remember { mutableStateOf("") } // Formato YYYY-MM-DD
    
    val datePickerState = rememberDatePickerState()
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        selectedDateText = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        rawDateValue = date.toString() // YYYY-MM-DD
                    }
                    showDatePicker = false
                }) { Text("Confirmar", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    var sexo by remember { mutableStateOf(Genero.MASCULINO) }
    var academiaSelecionada by remember { mutableStateOf<AcademiaData?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Campos Aluno
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }

    // Campos Treinador
    var cref by remember { mutableStateOf("") }
    var graduacao by remember { mutableStateOf("") }
    var especializacao by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Agora, complete seu perfil", color = colors.textSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        // Campo Data de Nascimento Amigável
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Data de Nascimento", color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            Box {
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecionar data", color = Color(0xFF525252)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true, 
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF262626),
                        focusedBorderColor = colors.primary.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF1A1C19),
                        focusedContainerColor = Color(0xFF1A1C19)
                    )
                )
                // Overlay invisível para capturar o clique em toda a área
                Box(
                    Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                )
            }
        }
        
        Text("Gênero", color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserTypeButton("MASCULINO", sexo == Genero.MASCULINO, { sexo = Genero.MASCULINO }, Modifier.weight(1f))
            UserTypeButton("FEMININO", sexo == Genero.FEMININO, { sexo = Genero.FEMININO }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))

        // Dropdown Academias
        Text("Unidade / Academia", color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = academiaSelecionada?.nome ?: "Selecione uma unidade",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = outlinedFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1A1C19)).border(1.dp, Color(0xFF262626))
            ) {
                if (academias.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Carregando unidades...", color = Color.Gray) },
                        onClick = { }
                    )
                }
                academias.forEach { academia ->
                    DropdownMenuItem(
                        text = { Text(academia.nome, color = Color.White) },
                        onClick = {
                            academiaSelecionada = academia
                            expanded = false
                        }
                    )
                }
            }
        }
        Text(
            text = "(Você poderá vincular outras unidades após o cadastro em seu perfil)",
            color = colors.textSecondary.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        Spacer(Modifier.height(20.dp))

        if (viewModel.tipo == UserTipo.ALUNO) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) { CadastroField("Peso (kg)", peso, { peso = it }, null) }
                Box(Modifier.weight(1f)) { CadastroField("Altura (m)", altura, { altura = it }, null) }
            }
        } else {
            CadastroField("CREF", cref, { cref = it }, null)
            CadastroField("Graduação", graduacao, { graduacao = it }, null)
            CadastroField("Especialização", especializacao, { especializacao = it }, null)
        }

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {
                val acId = academiaSelecionada?.id ?: ""
                if (viewModel.tipo == UserTipo.ALUNO) {
                    viewModel.finalizarCadastroAluno(rawDateValue, sexo.valor, acId, peso.toDoubleOrNull(), altura.toDoubleOrNull())
                } else {
                    viewModel.finalizarCadastroTreinador(rawDateValue, sexo.valor, acId, cref, graduacao, especializacao)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
        ) {
            Text("CONCLUIR CADASTRO", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun CadastroField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    val colors = LocalAcademiaColors.current
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
            colors = outlinedFieldColors()
        )
    }
}

@Composable
fun UserTypeButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAcademiaColors.current
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) colors.primary else Color(0xFF1A1C19))
            .border(1.dp, if (selected) colors.primary else Color(0xFF262626), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.Black else colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color(0xFF1A1C19),
    focusedContainerColor = Color(0xFF1A1C19),
    unfocusedBorderColor = Color(0xFF262626),
    focusedBorderColor = LocalAcademiaColors.current.primary.copy(alpha = 0.5f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLeadingIconColor = LocalAcademiaColors.current.primary,
    unfocusedLeadingIconColor = Color(0xFF525252)
)
