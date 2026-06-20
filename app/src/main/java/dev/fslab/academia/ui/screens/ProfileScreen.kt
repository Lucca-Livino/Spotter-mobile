package dev.fslab.academia.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Check
import dev.fslab.academia.R
import dev.fslab.academia.model.AcademiaData
import dev.fslab.academia.model.AlunoProfileData
import dev.fslab.academia.model.Genero
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.theme.LocalAcademiaColors
import dev.fslab.academia.ui.viewmodel.PerfilUiState
import dev.fslab.academia.ui.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userTipo: UserTipo,
    onBack: () -> Unit,
    onHistoricoPeso: ((String) -> Unit)? = null,
    viewModel: PerfilViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = LocalAcademiaColors.current
    val uiState by viewModel.uiState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val allAcademias by viewModel.academias.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.carregarPerfil(userTipo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        when (val state = uiState) {
            is PerfilUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            is PerfilUiState.SuccessAluno -> {
                AlunoProfileContent(
                    profile = state.profile,
                    allAcademias = allAcademias,
                    isUpdating = isUpdating,
                    onSave = { updatedData, uri, academiasIds ->
                        viewModel.atualizarAluno(
                            context = context,
                            id = state.profile.id,
                            nome = updatedData.nome,
                            dataNascimento = updatedData.dataNascimento ?: "",
                            sexo = updatedData.sexo?.valor ?: "M",
                            peso = updatedData.pesoKg,
                            altura = updatedData.alturaCm,
                            academiasIds = academiasIds,
                            fotoUri = uri,
                            onSuccess = onBack
                        )
                    },
                    onDeleteAccount = {
                        viewModel.deletarConta(context, onSuccess = onBack)
                    },
                    onHistoricoPeso = onHistoricoPeso?.let { cb -> { cb(state.profile.id) } },
                    onDesvincularTreinador = {
                        viewModel.desvincularTreinador(onSuccess = {})
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            is PerfilUiState.SuccessTreinador -> {
                TreinadorProfileContent(
                    profile = state.profile,
                    allAcademias = allAcademias,
                    isUpdating = isUpdating,
                    onSave = { updatedData, uri, academiasIds ->
                        viewModel.atualizarTreinador(
                            context = context,
                            id = state.profile.id,
                            nome = updatedData.nome,
                            cref = updatedData.cref ?: "",
                            graduacao = updatedData.graduacao ?: "",
                            especializacao = updatedData.especializacao ?: "",
                            academiasIds = academiasIds,
                            fotoUri = uri,
                            onSuccess = onBack
                        )
                    },
                    onDeleteAccount = {
                        viewModel.deletarConta(context, onSuccess = onBack)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            is PerfilUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = colors.errorText)
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun AlunoProfileContent(
    profile: AlunoProfileData,
    allAcademias: List<AcademiaData>,
    isUpdating: Boolean,
    onSave: (AlunoProfileData, Uri?, List<String>) -> Unit,
    onDeleteAccount: () -> Unit,
    onHistoricoPeso: (() -> Unit)? = null,
    onDesvincularTreinador: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current

    var nome by remember { mutableStateOf(profile.nome) }
    var dataNascimento by remember { mutableStateOf(profile.dataNascimento.orEmpty()) }
    var telefone by remember { mutableStateOf(profile.telefone.orEmpty()) }
    var peso by remember { mutableStateOf(profile.pesoKg?.toString().orEmpty()) }
    var altura by remember { mutableStateOf(profile.alturaCm?.toString().orEmpty()) }
    var generoSelected by remember { mutableStateOf(profile.sexo ?: Genero.MASCULINO) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDesvincularDialog by remember { mutableStateOf(false) }

    // IDs das academias selecionadas
    var selectedAcademias by remember {
        mutableStateOf(profile.academias?.map { it.id }?.toSet() ?: emptySet())
    }

    // Resetar prévia local quando a foto do servidor mudar (sucesso do upload)
    LaunchedEffect(profile.urlFoto) {
        imageUri = null
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageSource = remember(imageUri, profile.urlFoto) {
                imageUri ?: profile.urlFoto ?: R.drawable.no_profile_photo
            }
            
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
            )
            IconButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primary)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ProfileField(label = "Nome completo", value = nome, onValueChange = { nome = it })
        ProfileField(label = "E-mail", value = profile.email.orEmpty(), onValueChange = {}, enabled = false, trailingIcon = {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.textSecondary)
        })
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileField(label = "Data de nascimento", value = dataNascimento, onValueChange = { dataNascimento = it }, trailingIcon = {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                })
            }
            Box(modifier = Modifier.weight(1f)) {
                ProfileField(label = "Telefone", value = telefone, onValueChange = { telefone = it }, trailingIcon = {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "DADOS FÍSICOS", color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileField(label = "Peso atual (kg)", value = peso, onValueChange = { peso = it })
            }
            Box(modifier = Modifier.weight(1f)) {
                ProfileField(label = "Altura (cm)", value = altura, onValueChange = { altura = it })
            }
        }

        if (onHistoricoPeso != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onHistoricoPeso,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver histórico de peso", color = colors.primary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Gênero", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenderButton(label = "Masculino", selected = generoSelected == Genero.MASCULINO, onClick = { generoSelected = Genero.MASCULINO }, modifier = Modifier.weight(1f))
            GenderButton(label = "Feminino", selected = generoSelected == Genero.FEMININO, onClick = { generoSelected = Genero.FEMININO }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "MINHAS UNIDADES", color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        
        MultiSelectAcademias(
            allAcademias = allAcademias,
            selectedIds = selectedAcademias,
            onToggle = { id ->
                selectedAcademias = if (selectedAcademias.contains(id)) {
                    selectedAcademias - id
                } else {
                    selectedAcademias + id
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val updated = profile.copy(
                    nome = nome,
                    dataNascimento = dataNascimento,
                    sexo = generoSelected,
                    pesoKg = peso.toDoubleOrNull(),
                    alturaCm = altura.toIntOrNull()
                )
                onSave(updated, imageUri, selectedAcademias.toList())
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
            enabled = !isUpdating
        ) {
            if (isUpdating) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.ExtraBold)
            }
        }

        if (onDesvincularTreinador != null && profile.treinadorId != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = { showDesvincularDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpdating
            ) {
                Text("DESVINCULAR TREINADOR", color = colors.error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdating
        ) {
            Text("EXCLUIR MINHA CONTA", color = colors.errorText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }

    if (showDesvincularDialog) {
        AlertDialog(
            onDismissRequest = { showDesvincularDialog = false },
            containerColor = colors.surface,
            title = { Text("Desvincular treinador?", color = colors.textPrimary) },
            text = { Text("Você perderá o acesso aos treinos do seu treinador atual.", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDesvincularDialog = false
                    onDesvincularTreinador?.invoke()
                }) {
                    Text("DESVINCULAR", color = colors.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDesvincularDialog = false }) {
                    Text("CANCELAR", color = colors.textPrimary)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Conta", color = Color.White) },
            text = { Text("Esta ação é irreversível. Deseja mesmo excluir sua conta e todos os seus dados?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) {
                    Text("EXCLUIR", color = colors.errorText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCELAR", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
fun TreinadorProfileContent(
    profile: dev.fslab.academia.model.TreinadorProfileData,
    allAcademias: List<AcademiaData>,
    isUpdating: Boolean,
    onSave: (dev.fslab.academia.model.TreinadorProfileData, Uri?, List<String>) -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAcademiaColors.current
    val context = LocalContext.current

    var nome by remember { mutableStateOf(profile.nome) }
    var cref by remember { mutableStateOf(profile.cref.orEmpty()) }
    var graduacao by remember { mutableStateOf(profile.graduacao.orEmpty()) }
    var especializacao by remember { mutableStateOf(profile.especializacao.orEmpty()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    // IDs das academias selecionadas
    var selectedAcademias by remember { 
        mutableStateOf(profile.academias?.map { it.id }?.toSet() ?: emptySet()) 
    }

    // Resetar prévia local quando a foto do servidor mudar (sucesso do upload)
    LaunchedEffect(profile.urlFoto) {
        imageUri = null
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            val imageSource = remember(imageUri, profile.urlFoto) {
                imageUri ?: profile.urlFoto ?: R.drawable.no_profile_photo
            }
            
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
            )
            IconButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.primary)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ProfileField(label = "Nome completo", value = nome, onValueChange = { nome = it })
        ProfileField(label = "CREF", value = cref, onValueChange = { cref = it })
        ProfileField(label = "E-mail", value = profile.email.orEmpty(), onValueChange = {}, enabled = false)
        ProfileField(label = "Graduação", value = graduacao, onValueChange = { graduacao = it })
        ProfileField(label = "Especialização", value = especializacao, onValueChange = { especializacao = it })

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "UNIDADES DE ATENDIMENTO", color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        
        MultiSelectAcademias(
            allAcademias = allAcademias,
            selectedIds = selectedAcademias,
            onToggle = { id ->
                selectedAcademias = if (selectedAcademias.contains(id)) {
                    selectedAcademias - id
                } else {
                    selectedAcademias + id
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val updated = profile.copy(
                    nome = nome,
                    cref = cref,
                    graduacao = graduacao,
                    especializacao = especializacao
                )
                onSave(updated, imageUri, selectedAcademias.toList())
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
            enabled = !isUpdating
        ) {
            if (isUpdating) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdating
        ) {
            Text("EXCLUIR MINHA CONTA", color = colors.errorText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Conta", color = Color.White) },
            text = { Text("Esta ação é irreversível. Deseja mesmo excluir sua conta e todos os seus dados?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    onDeleteAccount()
                }) {
                    Text("EXCLUIR", color = colors.errorText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCELAR", color = colors.textPrimary)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    prefix: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val colors = LocalAcademiaColors.current
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = prefix?.let { { Text(it, color = colors.textSecondary) } },
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                disabledContainerColor = colors.surface.copy(alpha = 0.5f),
                unfocusedBorderColor = colors.inputBorder,
                focusedBorderColor = colors.primary.copy(alpha = 0.5f),
                disabledBorderColor = colors.inputBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.textPrimary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAcademiaColors.current
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) colors.primary else colors.surface)
            .border(1.dp, if (selected) colors.primary else colors.inputBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectAcademias(
    allAcademias: List<AcademiaData>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    val colors = LocalAcademiaColors.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        allAcademias.forEach { academia ->
            val isSelected = selectedIds.contains(academia.id)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(academia.id) },
                label = { Text(academia.nome, fontSize = 12.sp) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.textSecondary,
                    selectedContainerColor = colors.primary,
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = colors.inputBorder,
                    selectedBorderColor = colors.primary,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
