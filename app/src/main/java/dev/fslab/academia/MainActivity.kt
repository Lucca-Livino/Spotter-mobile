package dev.fslab.academia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fslab.academia.navigation.Screen
import dev.fslab.academia.navigation.navigateSafely
import dev.fslab.academia.navigation.popBackStackSafely
import dev.fslab.academia.network.CookieManager
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.screens.HomeScreen
import dev.fslab.academia.ui.screens.aluno.AparelhosScreen
import dev.fslab.academia.ui.screens.aluno.ExercicioCatalogoScreen
import dev.fslab.academia.ui.screens.aluno.ExercicioDetalheScreen
import dev.fslab.academia.ui.screens.aluno.ExercicioFormScreen
import dev.fslab.academia.ui.screens.aluno.SessaoAtivaScreen
import dev.fslab.academia.ui.screens.aluno.TreinoDetalheScreen
import dev.fslab.academia.ui.screens.aluno.TreinoFormScreen
import dev.fslab.academia.ui.screens.aluno.TreinosScreen
import dev.fslab.academia.ui.screens.auth.LoginScreen
import dev.fslab.academia.ui.screens.treinador.TreinadorHomeScreen
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.viewmodel.AuthState
import dev.fslab.academia.ui.viewmodel.AuthViewModel
import dev.fslab.academia.ui.viewmodel.SessaoUiState
import dev.fslab.academia.ui.viewmodel.SessaoViewModel
import dev.fslab.academia.ui.viewmodel.ThemeMode
import dev.fslab.academia.ui.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            AcademiaApp(
                authViewModel = authViewModel,
                themeViewModel = themeViewModel
            )
        }
    }
}

@Composable
fun AcademiaApp(
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    sessaoViewModel: SessaoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val sessaoState by sessaoViewModel.uiState.collectAsState()
    val temSessaoAtiva = sessaoState is SessaoUiState.EmAndamento

    AcademiaTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        LaunchedEffect(Unit) {
            authViewModel.checkSession()
        }

        LaunchedEffect(authState) {
            when (val state = authState) {
                is AuthState.Success -> {
                    val targetRoute = if (state.user.tipo == UserTipo.TREINADOR) {
                        Screen.TreinadorHome.route
                    } else {
                        Screen.Home.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                AuthState.Idle -> navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
                else -> Unit
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    isDarkTheme = isDarkTheme,
                    isLoading = authState is AuthState.Loading,
                    errorMessage = (authState as? AuthState.Error)?.message,
                    onToggleTheme = { themeViewModel.toggle() },
                    onEsqueciSenha = { },
                    onRegister = { navController.navigateSafely(Screen.Cadastro.route) },
                    onLogin = { email, password ->
                        authViewModel.loginUser(email = email, password = password)
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    nome = currentUser?.name?.substringBefore(" ").orEmpty(),
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { themeViewModel.toggle() },
                    onLogout = { authViewModel.logout() },
                    onOpenExercicios = {
                        navController.navigateSafely(Screen.ExercicioCatalogo.route)
                    },
                    onOpenTreinos = {
                        navController.navigateSafely(Screen.Treinos.route)
                    },
                    temSessaoAtiva = temSessaoAtiva,
                    onRetomarSessao = {
                        navController.navigateSafely(Screen.SessaoAtiva.retomar())
                    },
                    onNavigateTab = { route ->
                        navController.navigateSafely(route)
                    }
                )
            }

            composable(Screen.TreinadorHome.route) {
                TreinadorHomeScreen(
                    nome = currentUser?.name?.substringBefore(" ").orEmpty(),
                    onOpenCliente = { _ -> },
                    onOpenClientes = { },
                    onNavigateTab = { _ -> },
                    onNotifications = { },
                    onLogout = { authViewModel.logout() }
                )
            }

            composable(Screen.ExercicioCatalogo.route) {
                ExercicioCatalogoScreen(
                    onBack = { navController.popBackStackSafely() },
                    onNavigateTab = { route ->
                        if (route == Screen.Home.route) {
                            navController.popBackStackSafely()
                        } else {
                            navController.navigateSafely(route)
                        }
                    },
                    onAbrirDetalhe = { id ->
                        navController.navigateSafely(Screen.ExercicioDetalhe.comId(id))
                    },
                    onCriar = {
                        navController.navigateSafely(Screen.ExercicioCriar.route)
                    }
                )
            }

            composable(Screen.Aparelhos.route) {
                AparelhosScreen(
                    onBack = { navController.popBackStackSafely() },
                    onNavigateTab = { route -> navController.navigateSafely(route) }
                )
            }

            composable(
                route = Screen.ExercicioDetalhe.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ExercicioDetalheScreen(
                    exercicioId = id,
                    onBack = { navController.popBackStackSafely() },
                    onEditar = { exId ->
                        navController.navigateSafely(Screen.ExercicioEditar.comId(exId))
                    },
                    onExcluido = { navController.popBackStackSafely() }
                )
            }

            composable(Screen.ExercicioCriar.route) {
                ExercicioFormScreen(
                    exercicioId = null,
                    onBack = { navController.popBackStackSafely() },
                    onSalvo = { id ->
                        navController.navigateSafely(Screen.ExercicioDetalhe.comId(id))
                    }
                )
            }

            composable(
                route = Screen.ExercicioEditar.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ExercicioFormScreen(
                    exercicioId = id,
                    onBack = { navController.popBackStackSafely() },
                    onSalvo = { exId ->
                        navController.popBackStackSafely()
                    }
                )
            }

            composable(Screen.Treinos.route) {
                TreinosScreen(
                    onBack = { navController.popBackStackSafely() },
                    onNavigateTab = { route ->
                        if (route == Screen.Home.route) {
                            navController.popBackStackSafely()
                        } else {
                            navController.navigateSafely(route)
                        }
                    },
                    onAbrirDetalhe = { id ->
                        navController.navigateSafely(Screen.TreinoDetalhe.comId(id))
                    },
                    onCriar = {
                        navController.navigateSafely(Screen.TreinoCriar.route)
                    }
                )
            }

            composable(
                route = Screen.TreinoDetalhe.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                TreinoDetalheScreen(
                    treinoId = id,
                    onBack = { navController.popBackStackSafely() },
                    onEditar = { tId ->
                        navController.navigateSafely(Screen.TreinoEditar.comId(tId))
                    },
                    onExcluido = { navController.popBackStackSafely() },
                    onIniciarSessao = { tId ->
                        navController.navigateSafely(Screen.SessaoAtiva.iniciar(tId))
                    }
                )
            }

            composable(
                route = Screen.SessaoAtiva.route,
                arguments = listOf(
                    navArgument("treinoId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                val treinoId = entry.arguments?.getString("treinoId")
                SessaoAtivaScreen(
                    treinoId = treinoId,
                    onBack = { navController.popBackStackSafely() },
                    viewModel = sessaoViewModel
                )
            }

            composable(Screen.TreinoCriar.route) { entry ->
                val novoId by entry.savedStateHandle
                    .getStateFlow<String?>("novo_exercicio_id", null)
                    .collectAsState()
                TreinoFormScreen(
                    treinoId = null,
                    onBack = { navController.popBackStackSafely() },
                    onSalvo = { id ->
                        navController.navigateSafely(Screen.TreinoDetalhe.comId(id))
                    },
                    onCriarExercicio = {
                        navController.navigateSafely(Screen.ExercicioCriarParaTreino.route)
                    },
                    novoExercicioId = novoId,
                    onConsumirNovoExercicio = {
                        entry.savedStateHandle["novo_exercicio_id"] = null
                    }
                )
            }

            composable(
                route = Screen.TreinoEditar.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val novoId by entry.savedStateHandle
                    .getStateFlow<String?>("novo_exercicio_id", null)
                    .collectAsState()
                TreinoFormScreen(
                    treinoId = id,
                    onBack = { navController.popBackStackSafely() },
                    onSalvo = { _ -> navController.popBackStackSafely() },
                    onCriarExercicio = {
                        navController.navigateSafely(Screen.ExercicioCriarParaTreino.route)
                    },
                    novoExercicioId = novoId,
                    onConsumirNovoExercicio = {
                        entry.savedStateHandle["novo_exercicio_id"] = null
                    }
                )
            }

            composable(Screen.ExercicioCriarParaTreino.route) {
                ExercicioFormScreen(
                    exercicioId = null,
                    onBack = { navController.popBackStackSafely() },
                    onSalvo = { idCriado ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("novo_exercicio_id", idCriado)
                        navController.popBackStackSafely()
                    }
                )
            }
        }
    }
}
