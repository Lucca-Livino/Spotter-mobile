package dev.fslab.academia.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.fslab.academia.ui.theme.LocalAcademiaColors

data class NavItemData(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

const val MAIS_ROUTE = "mais"

val alunoNavItems = listOf(
    NavItemData("Início", Icons.Filled.Home, "home"),
    NavItemData("Treinos", Icons.Filled.FitnessCenter, "treinos"),
    NavItemData("Chat", Icons.AutoMirrored.Filled.Chat, "chat"),
    NavItemData("Histórico", Icons.Filled.History, "historico"),
    NavItemData("Mais", Icons.Filled.Menu, MAIS_ROUTE),
)

val treinadorNavItems = listOf(
    NavItemData("Início", Icons.Filled.Home, "treinador_home"),
    NavItemData("Treinos", Icons.Filled.FitnessCenter, "treinador_criar_treino"),
    NavItemData("Chat", Icons.AutoMirrored.Filled.Chat, "chat"),
    NavItemData("Clientes", Icons.Filled.Group, "treinador_alunos"),
    NavItemData("Perfil", Icons.Filled.Person, "perfil"),
)

@Composable
fun TreinadorNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val colors = LocalAcademiaColors.current

    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.primary
    ) {
        treinadorNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
@Composable
fun AppNavigationBar(
    items: List<NavItemData>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val colors = LocalAcademiaColors.current

    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.primary
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = colors.error) {
                                    Text(
                                        text = item.badgeCount.toString(),
                                        color = colors.textOnPrimary
                                    )
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.mediumGray,
                    unselectedTextColor = colors.mediumGray,
                    indicatorColor = colors.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
