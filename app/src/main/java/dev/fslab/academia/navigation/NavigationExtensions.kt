package dev.fslab.academia.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

private object NavigationThrottle {
    @Volatile
    var lastNavigationTime = 0L
    const val DEBOUNCE_MS = 300L

    fun canNavigate(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastNavigationTime) > DEBOUNCE_MS
    }

    fun recordNavigation() {
        lastNavigationTime = System.currentTimeMillis()
    }
}

fun NavController.navigateSafely(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    val currentEntry = currentBackStackEntry
    if (currentEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
        && NavigationThrottle.canNavigate()
    ) {
        NavigationThrottle.recordNavigation()
        if (builder != null) {
            navigate(route) {
                launchSingleTop = true
                builder()
            }
        } else {
            navigate(route) {
                launchSingleTop = true
            }
        }
    }
}

fun NavController.popBackStackSafely(): Boolean {
    val currentEntry = currentBackStackEntry
    return if (currentEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
        && NavigationThrottle.canNavigate()
    ) {
        NavigationThrottle.recordNavigation()
        popBackStack()
    } else {
        false
    }
}
