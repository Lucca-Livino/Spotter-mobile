package dev.fslab.academia.ui.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Application.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = getApplication<Application>().themeDataStore.data.first()
            val raw = prefs[THEME_MODE_KEY]
            _themeMode.value = runCatching { ThemeMode.valueOf(raw ?: "SYSTEM") }
                .getOrDefault(ThemeMode.SYSTEM)
        }
    }

    fun setThemeMode(newMode: ThemeMode) {
        _themeMode.value = newMode
        viewModelScope.launch {
            getApplication<Application>().themeDataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = newMode.name
            }
        }
    }

    fun toggle() {
        val next = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(next)
    }
}
