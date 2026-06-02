package dev.fslab.academia.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PALETA DE CORES DA APLICAÇÃO - FÁBRICA 4
 *
 * Baseado no Design System oficial (TrainingApp no Figma).
 * O Modo Escuro é o padrão identitário.
 */

// ==========================================
// CORES PRINCIPAIS (DESIGN SYSTEM)
// ==========================================
val PrimaryNeon = Color(0xFF5DD62C)        // Verde Neon (Botões, ícones ativos)
val SecondaryDarkGreen = Color(0xFF16A34A) // Verde escuro (Para contraste no tema claro)
val PrimaryLightGreen = Color(0xFFF8FAFC)  // Fundo principal para tema claro (Slate 50)

// ==========================================
// SUPERFÍCIES E FUNDOS (DARK MODE FOCUSED)
// ==========================================
val DarkBg = Color(0xFF0F0F0F)             // Fundo principal do App (Preto profundo Figma)
val SurfaceDark = Color(0xFF1A1A1A)        // Superfície de Cards e Inputs (Figma)
val DarkInputBg = Color(0xFF1A1A1A)        // Fundo dos Inputs no Dark Mode
val DarkBgGradient = Color(0xFF0F0F0F)     // Base do background gradiente


// ==========================================
// TEXTOS E TIPOGRAFIA
// ==========================================
val TextWhite = Color(0xFFF1F5F9)          // Texto Principal no Dark (Slate 100)
val TextDarkOnPrimary = Color(0xFF0F0F0F)  // Texto Preto (Usado SOBRE o botão Verde Neon)
val TextGraySecondary = Color(0xFF94A3B8)  // Texto secundário (Slate 400 - placeholders)
val DarkTextTertiary = Color(0xFF64748B)   // Textos terciários e detalhes sutis


// ==========================================
// BORDAS E ÍCONES
// ==========================================
val DarkInputBorder = Color(0xFF334155)    // Borda dos inputs dark (Figma border)
val DarkIconColor = Color(0xFF5DD62C)      // Ícones de destaque
val IconMuted = Color(0xFF94A3B8)          // Ícones inativos ou secundários


// ==========================================
// CORES DE ESTADO (FEEDBACK)
// ==========================================
val ErrorBackground = Color(0xFF2E1212)    // Fundo para erro no dark
val ErrorText = Color(0xFFCF6679)          // Texto de erro / Ação Destrutiva (Excluir Conta)
val ErrorButton = Color(0xFFCF6679)        // Vermelho padrão de alerta
val SuccessBackground = Color(0xFF1E293B)  // Fundo de sucesso sutil
val SuccessText = Color(0xFF5DD62C)        // Texto de sucesso (verde neon)


// ==========================================
// TEMA CLARO (LIGHT MODE — PALETA VERDE ORGÂNICA)
// ==========================================
val LightBg = Color(0xFFFEFDF8)            // Fundo principal (#fefdf8 — creme levíssimo)
val LightPrimary = Color(0xFF77AB59)       // Verde sage (#77ab59 — brand primário claro)
val LightTextOnPrimary = Color(0xFF1A2E12) // Texto sobre botões primários (dark forest, 6.5:1)
val SurfaceWhite = Color(0xFFFFFFFF)       // Superfície dos Cards
val SurfaceLight = Color(0xFFEBF3D5)       // Input background (verde-cinza suave)
val TextPrimaryLight = Color(0xFF0F1E0B)   // Texto primário (quase-preto esverdeado)
val TextSecondaryLight = Color(0xFF4B6B3E) // Texto secundário (verde escuro, 5.2:1 no bg)
val TextTertiaryLight = Color(0xFF6A8D5E)  // Texto terciário / placeholder
val InputBorderLight = Color(0xFFC3D9A8)   // Bordas claras (verde-cinza)
val LightGray = Color(0xFFE4EDCC)          // Dividers e fundos sutis

// ==========================================
// DASHBOARD ADMIN & FEATURES CARDS (Opcional)
// ==========================================
val CardBlue = Color(0xFF7C6AF6)
val CardOrange = Color(0xFFD97706)
val CardGreen = Color(0xFF16A34A)
val CardYellow = Color(0xFFFBBF24)
val CardPurple = Color(0xFFA855F7)
