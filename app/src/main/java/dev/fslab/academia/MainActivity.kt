package dev.fslab.academia

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fslab.academia.navigation.Screen
import dev.fslab.academia.navigation.navigateSafely
import dev.fslab.academia.navigation.popBackStackSafely
import dev.fslab.academia.network.CookieManager
import dev.fslab.academia.network.GoogleSignInHelper
import dev.fslab.academia.model.UserTipo
import dev.fslab.academia.ui.screens.HomeScreen
import dev.fslab.academia.ui.screens.ProfileScreen
import dev.fslab.academia.ui.screens.PlaceholderScreen
import dev.fslab.academia.ui.screens.aluno.AparelhosScreen
import dev.fslab.academia.ui.screens.aluno.HistoricoProgressaoScreen
import dev.fslab.academia.ui.screens.aluno.HistoricoScreen
import dev.fslab.academia.ui.screens.aluno.SessaoDetalheScreen
import dev.fslab.academia.ui.viewmodel.HistoricoViewModel
import dev.fslab.academia.ui.screens.aluno.ExercicioCatalogoScreen
import dev.fslab.academia.ui.screens.aluno.ExercicioDetalheScreen
import dev.fslab.academia.ui.screens.aluno.ExercicioFormScreen
import dev.fslab.academia.ui.screens.aluno.SessaoAtivaScreen
import dev.fslab.academia.ui.screens.aluno.TreinoDetalheScreen
import dev.fslab.academia.ui.screens.aluno.TreinoFormScreen
import dev.fslab.academia.ui.screens.aluno.TreinosScreen
import dev.fslab.academia.ui.screens.auth.CadastroScreen
import dev.fslab.academia.ui.screens.auth.LoginScreen
import dev.fslab.academia.ui.screens.chat.ChatDetailScreen
import dev.fslab.academia.ui.screens.chat.ChatScreen
import dev.fslab.academia.ui.screens.treinador.TreinadorAlunoDetalheScreen
import dev.fslab.academia.ui.screens.treinador.TreinadorAlunosScreen
import dev.fslab.academia.ui.screens.treinador.TreinadorHomeScreen
import dev.fslab.academia.ui.theme.AcademiaTheme
import dev.fslab.academia.ui.viewmodel.AuthState
import dev.fslab.academia.ui.viewmodel.AuthViewModel
import dev.fslab.academia.ui.viewmodel.ExercicioViewModel
import dev.fslab.academia.ui.viewmodel.CadastroViewModel
import dev.fslab.academia.ui.viewmodel.PerfilViewModel
import dev.fslab.academia.ui.viewmodel.SessaoUiState
import dev.fslab.academia.ui.viewmodel.SessaoViewModel
import dev.fslab.academia.ui.viewmodel.ThemeMode
import dev.fslab.academia.ui.viewmodel.ThemeViewModel


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val perfilViewModel: PerfilViewModel by viewModels()
    private val exercicioViewModel: ExercicioViewModel by viewModels()
    private val cadastroViewModel: CadastroViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val idToken = GoogleSignInHelper.getIdTokenFromResult(result.data)
            if (idToken != null) {
                authViewModel.loginWithGoogle(idToken)
            } else {
                authViewModel.setError("Falha ao obter credencial Google. Verifique a configuração do app.")
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            authViewModel.setError("Login com Google cancelado ou falhou.")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            AcademiaApp(
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                perfilViewModel = perfilViewModel,
                exercicioViewModel = exercicioViewModel,
                cadastroViewModel = cadastroViewModel,
                onGoogleSignIn = {
                    val intent = GoogleSignInHelper.getSignInIntent(this)
                    googleSignInLauncher.launch(intent)
                }
            )
        }
    }
}

@Composable
fun AcademiaApp(
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    perfilViewModel: PerfilViewModel,
    exercicioViewModel: ExercicioViewModel,
    cadastroViewModel: CadastroViewModel,
    onGoogleSignIn: () -> Unit = {},
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
    val exerciciosState by exercicioViewModel.uiState.collectAsState()
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
                    if (!state.user.hasProfile) {
                        // Se logou mas não tem perfil (ex: primeiro login social), 
                        // manda para a tela de completar cadastro pré-preenchido
                        cadastroViewModel.preencherDadosSociais(state.user.name, state.user.email)
                        
                        navController.navigate(Screen.Cadastro.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
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
                }

                AuthState.Idle -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Limpa absolutamente tudo
                        launchSingleTop = true
                    }
                }

                else -> Unit
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            enterTransition = {
                fadeIn(tween(220)) + slideInVertically(tween(220)) { (it * 0.04f).toInt() }
            },
            exitTransition = {
                fadeOut(tween(180)) + slideOutVertically(tween(180)) { -(it * 0.04f).toInt() }
            },
            popEnterTransition = {
                fadeIn(tween(220)) + slideInVertically(tween(220)) { -(it * 0.04f).toInt() }
            },
            popExitTransition = {
                fadeOut(tween(180)) + slideOutVertically(tween(180)) { (it * 0.04f).toInt() }
            }
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
                    },
                    onGoogleLogin = onGoogleSignIn
                )
            }

            composable(Screen.Cadastro.route) {
                CadastroScreen(
                    onBack = { navController.popBackStackSafely() },
                    onSuccess = {
                        val route = if (cadastroViewModel.tipo == UserTipo.TREINADOR) {
                            Screen.TreinadorHome.route
                        } else {
                            Screen.Home.route
                        }
                        navController.navigateSafely(route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    viewModel = cadastroViewModel
                )
            }

            composable(Screen.Home.route) {

                HomeScreen(
                    nome = currentUser?.name?.substringBefore(" ").orEmpty(),
                    fotoUrl = currentUser?.image,
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
                    fotoUrl = currentUser?.image,
                    onOpenCliente = { alunoId ->
                        navController.navigateSafely(Screen.TreinadorAlunoDetalhe.comId(alunoId))
                    },
                    onOpenClientes = {
                        navController.navigateSafely(Screen.TreinadorAlunos.route)
                    },
                    onNavigateTab = { route ->
                        navController.navigateSafely(route)
                    },
                    onNotifications = { },
                    onLogout = { authViewModel.logout() }
                )
            }

            composable(Screen.ExercicioCatalogo.route) {
                LaunchedEffect(Unit) {
                    exercicioViewModel.carregar()
                }

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
                    },
                    viewModel = exercicioViewModel
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

            composable(Screen.Historico.route) {
                val historicoViewModel: HistoricoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                HistoricoScreen(
                    onNavigateTab = { route -> navController.navigateSafely(route) },
                    onAbrirProgressao = { exercicioId, exercicioNome ->
                        navController.navigateSafely(
                            Screen.HistoricoProgressao.comId(exercicioId, exercicioNome)
                        )
                    },
                    onAbrirSessao = { sessaoId ->
                        navController.navigateSafely(Screen.SessaoDetalhe.comId(sessaoId))
                    },
                    viewModel = historicoViewModel
                )
            }

            composable(
                route = Screen.SessaoDetalhe.route,
                arguments = listOf(navArgument("sessaoId") { type = NavType.StringType })
            ) { entry ->
                val sessaoId = entry.arguments?.getString("sessaoId").orEmpty()
                val historicoViewModel: HistoricoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                SessaoDetalheScreen(
                    sessaoId = sessaoId,
                    onBack = { navController.popBackStackSafely() },
                    viewModel = historicoViewModel
                )
            }

            composable(
                route = Screen.HistoricoProgressao.route,
                arguments = listOf(
                    navArgument("exercicioId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("exercicioNome") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val exercicioId = entry.arguments?.getString("exercicioId").orEmpty()
                val exercicioNome = entry.arguments?.getString("exercicioNome").orEmpty()
                val historicoViewModel: HistoricoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                HistoricoProgressaoScreen(
                    exercicioId = exercicioId,
                    exercicioNome = exercicioNome,
                    onBack = { navController.popBackStackSafely() },
                    viewModel = historicoViewModel
                )
            }
            composable(Screen.Perfil.route) {
                ProfileScreen(
                    userTipo = currentUser?.tipo ?: UserTipo.ALUNO,
                    onBack = {
                        navController.popBackStackSafely()
                        authViewModel.checkSession()
                    },
                    viewModel = perfilViewModel
                )
            }

            composable(Screen.Configuracoes.route) {
                PlaceholderScreen(
                    titulo = "Configurações",
                    descricao = "Preferências do app — implementação futura",
                    onBack = { navController.popBackStackSafely() }
                )
            }

            composable(Screen.Chat.route) {
                ChatScreen(
                    userTipo = currentUser?.tipo ?: UserTipo.ALUNO,
                    onNavigateTab = { route -> navController.navigateSafely(route) },
                    onOpenConversa = { conversaId ->
                        navController.navigateSafely(Screen.ChatDetalhe.comId(conversaId))
                    }
                )
            }

            composable(
                route = Screen.ChatDetalhe.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val conversaId = entry.arguments?.getString("id").orEmpty()
                ChatDetailScreen(
                    conversaId = conversaId,
                    userTipo = currentUser?.tipo ?: UserTipo.ALUNO,
                    title = "Conversa",
                    showBack = true,
                    onBack = { navController.popBackStackSafely() }
                )
            }

            composable(Screen.Notificacoes.route) {
                PlaceholderScreen(
                    titulo = "Notificações",
                    descricao = "Central de notificações — implementação futura",
                    onBack = { navController.popBackStackSafely() }
                )
            }

            composable(Screen.TreinadorAlunos.route) {
                TreinadorAlunosScreen(
                    onOpenCliente = { alunoId ->
                        navController.navigateSafely(Screen.TreinadorAlunoDetalhe.comId(alunoId))
                    },
                    onNavigateTab = { route ->
                        navController.navigateSafely(route)
                    }
                )
            }

            composable(
                route = Screen.TreinadorAlunoDetalhe.route,
                arguments = listOf(navArgument("alunoId") { type = NavType.StringType })
            ) { entry ->
                val alunoId = entry.arguments?.getString("alunoId").orEmpty()
                TreinadorAlunoDetalheScreen(
                    alunoId = alunoId,
                    onBack = { navController.popBackStackSafely() },
                    onMontarTreino = { _, _ ->
                        navController.navigateSafely(Screen.TreinoCriar.route)
                    },
                    onAbrirTreino = { treinoId ->
                        navController.navigateSafely(Screen.TreinadorTreinoDetalhe.comId(treinoId))
                    }
                )
            }

            composable(Screen.TreinadorTreinos.route) {
                dev.fslab.academia.ui.screens.treinador.TreinadorTreinosScreen(
                    onNavigateTab = { route -> navController.navigateSafely(route) },
                    onAbrirDetalhe = { id ->
                        navController.navigateSafely(Screen.TreinadorTreinoDetalhe.comId(id))
                    },
                    onCriar = {
                        navController.navigateSafely(Screen.TreinoCriar.route)
                    }
                )
            }

            composable(
                route = Screen.TreinadorTreinoDetalhe.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                dev.fslab.academia.ui.screens.treinador.TreinadorTreinoDetalheScreen(
                    treinoId = id,
                    onBack = { navController.popBackStackSafely() },
                    onEditar = { tId ->
                        navController.navigateSafely(Screen.TreinoEditar.comId(tId))
                    },
                    onExcluido = { navController.popBackStackSafely() }
                )
            }
        }
    }
}
