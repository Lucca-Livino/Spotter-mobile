package dev.fslab.academia.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.theme.LocalAcademiaColors

/**
 * LoginScreen - Tela de autenticação da aplicação Fábrica 4
 *
 * ⚠️ Para o toggle de tema funcionar, gerencie o estado no Activity/NavHost:
 *
 *   var isDark by remember { mutableStateOf(true) }
 *   AcademiaTheme(darkTheme = isDark) {
 *       LoginScreen(
 *           isDarkTheme = isDark,
 *           onToggleTheme = { isDark = !isDark }
 *       )
 *   }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onToggleTheme: () -> Unit = {},
    onEsqueciSenha: (String) -> Unit = {},
    onRegister: () -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    val colors = LocalAcademiaColors.current

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    // Fade-in ao entrar na tela
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "fadeIn"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.backgroundGradientStart,
                        colors.backgroundGradientEnd
                    )
                )
            )
    ) {
        // ── Conteúdo principal ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ─── Botão alternar tema ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface.copy(alpha = 0.7f))
                        .border(1.dp, colors.inputBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkTheme) "Modo claro" else "Modo escuro",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ─── Logo: ícone + nome ───────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(fadeAlpha)
                    .size(88.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = colors.primary.copy(alpha = 0.35f),
                        spotColor = colors.primary.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.primaryDark,
                                colors.surface
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.6f),
                                colors.primary.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = "Logo Spotter",
                    tint = colors.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Nome do app ──────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            letterSpacing = 3.sp
                        )
                    ) { append("Spot") }
                    withStyle(
                        SpanStyle(
                            color = colors.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            letterSpacing = 3.sp
                        )
                    ) { append("ter") }
                },
                modifier = Modifier.alpha(fadeAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ─── Slogan ───────────────────────────────────────────────
            Text(
                text = "PROFESSIONAL TRAINING CENTER",
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(fadeAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ─── Card do formulário ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(fadeAlpha)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface.copy(alpha = if (colors.isDark) 0.85f else 0.90f))
                    .border(
                        width = 1.dp,
                        color = colors.inputBorder,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Título do card
                    Text(
                        text = "Bem-vindo de volta",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Entre com suas credenciais para continuar",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 28.dp)
                    )

                    // ── E-MAIL ───────────────────────────────────────
                    Text(
                        text = "E-MAIL",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = {
                            Text("seu@email.com", color = colors.mediumGray, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = "Email",
                                tint = if (email.isNotEmpty()) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = colors.inputBorder,
                            focusedBorderColor = colors.primary.copy(alpha = 0.7f),
                            unfocusedContainerColor = colors.lightGray,
                            focusedContainerColor = colors.lightGray,
                            cursorColor = colors.primary,
                            focusedTextColor = colors.textInput,
                            unfocusedTextColor = colors.textInput
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── SENHA ─────────────────────────────────────────
                    Text(
                        text = "SENHA",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        placeholder = {
                            Text("••••••••", color = colors.mediumGray, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Senha",
                                tint = if (senha.isNotEmpty()) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = colors.inputBorder,
                            focusedBorderColor = colors.primary.copy(alpha = 0.7f),
                            unfocusedContainerColor = colors.lightGray,
                            focusedContainerColor = colors.lightGray,
                            cursorColor = colors.primary,
                            focusedTextColor = colors.textInput,
                            unfocusedTextColor = colors.textInput
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Esqueci minha senha ───────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Esqueci minha senha",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary,
                            modifier = Modifier.clickable { onEsqueciSenha(email) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Mensagem de erro ───────────────────────────────
                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Botão ENTRAR ──────────────────────────────────
                    Button(
                        onClick = { onLogin(email.trim(), senha) },
                        enabled = !isLoading && email.isNotBlank() && senha.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = colors.primary.copy(alpha = 0.4f),
                                spotColor = colors.primary.copy(alpha = 0.4f)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = colors.textOnPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "ENTRAR",
                                color = colors.textOnPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // ─── Rodapé ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .alpha(fadeAlpha),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ainda não é membro?  ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Comece agora",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.clickable { onRegister() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Dark Theme")
@Composable
fun LoginScreenPreview() {
    AcademiaTheme(darkTheme = true) {
        LoginScreen(isDarkTheme = true)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Light Theme")
@Composable
fun LoginScreenLightPreview() {
    AcademiaTheme(darkTheme = false) {
        LoginScreen(isDarkTheme = false)
    }
}