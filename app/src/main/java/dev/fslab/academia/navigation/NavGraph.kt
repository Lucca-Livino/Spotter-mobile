package dev.fslab.academia.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    data object Cadastro : Screen("cadastro")

    // Comum
    data object Home : Screen("home")
    data object Perfil : Screen("perfil")
    data object Configuracoes : Screen("configuracoes")
    data object Chat : Screen("chat")
    data object ChatDetalhe : Screen("chat_detalhe")
    data object Notificacoes : Screen("notificacoes")

    // Aluno
    data object Treinos : Screen("treinos")
    data object TreinoDetalhe : Screen("treino_detalhe/{id}") {
        fun comId(id: String) = "treino_detalhe/$id"
    }
    data object TreinoCriar : Screen("treino_criar")
    data object TreinoEditar : Screen("treino_editar/{id}") {
        fun comId(id: String) = "treino_editar/$id"
    }
    data object ExercicioCriarParaTreino : Screen("exercicio_criar_para_treino")
    data object SessaoAtiva : Screen("sessao_ativa?treinoId={treinoId}") {
        fun iniciar(treinoId: String) = "sessao_ativa?treinoId=$treinoId"
        fun retomar() = "sessao_ativa"
    }
    data object Historico : Screen("historico")
    data object HistoricoProgressao : Screen("historico_progressao")
    data object ExercicioCatalogo : Screen("exercicio_catalogo")
    data object Aparelhos : Screen("aparelhos")
    data object ExercicioDetalhe : Screen("exercicio_detalhe/{id}") {
        fun comId(id: String) = "exercicio_detalhe/$id"
    }
    data object ExercicioCriar : Screen("exercicio_criar")
    data object ExercicioEditar : Screen("exercicio_editar/{id}") {
        fun comId(id: String) = "exercicio_editar/$id"
    }

    // Treinador
    data object TreinadorHome : Screen("treinador_home")
    data object TreinadorAlunos : Screen("treinador_alunos")
    data object TreinadorAlunoDetalhe : Screen("treinador_aluno_detalhe")
    data object TreinadorCriarTreino : Screen("treinador_criar_treino")
}
