package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LightColor = lightColorScheme(
    primary = Color(0xFF315EFB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4EAFF),
    onPrimaryContainer = Color(0xFF132A73),

    secondary = Color(0xFF0E9F9A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F7F3),
    onSecondaryContainer = Color(0xFF063F3D),

    tertiary = Color(0xFF16865D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F5E5),
    onTertiaryContainer = Color(0xFF083D2C),

    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF5F7FB),
    onBackground = Color(0xFF162033),
    surface = Color.White,
    onSurface = Color(0xFF182230),
    surfaceVariant = Color(0xFFE9EEF6),
    onSurfaceVariant = Color(0xFF556176),

    outline = Color(0xFF9AA6B6),
    outlineVariant = Color(0xFFD8E0EC),
    inverseSurface = Color(0xFF1C2637),
    inverseOnSurface = Color(0xFFF7F9FC),
    inversePrimary = Color(0xFFB8C7FF),
    scrim = Color.Black,
    surfaceTint = Color(0xFF315EFB),

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = Color(0xFFF0F4FA),
    surfaceContainerHigh = Color(0xFFE9EFF8),
    surfaceContainerHighest = Color(0xFFE1E9F4),
)


private val DarkColor = darkColorScheme(
    primary = Color(0xFF9BB1FF),
    onPrimary = Color(0xFF142A79),
    primaryContainer = Color(0xFF2F448B),
    onPrimaryContainer = Color(0xFFDEE5FF),

    secondary = Color(0xFF74DCD3),
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF00514C),
    onSecondaryContainer = Color(0xFFACF3EB),

    tertiary = Color(0xFF91D9B4),
    onTertiary = Color(0xFF003823),
    tertiaryContainer = Color(0xFF005237),
    onTertiaryContainer = Color(0xFFB0F2CC),

    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE8EEF8),
    surface = Color(0xFF101928),
    onSurface = Color(0xFFE8EEF8),
    surfaceVariant = Color(0xFF263247),
    onSurfaceVariant = Color(0xFFB8C4D8),

    outline = Color(0xFF8493AA),
    outlineVariant = Color(0xFF344157),
    inverseSurface = Color(0xFFE8EEF8),
    inverseOnSurface = Color(0xFF162033),
    inversePrimary = Color(0xFF315EFB),
    scrim = Color.Black,
    surfaceTint = Color(0xFF9BB1FF),

    surfaceContainerLowest = Color(0xFF080E19),
    surfaceContainerLow = Color(0xFF0D1523),
    surfaceContainer = Color(0xFF141F30),
    surfaceContainerHigh = Color(0xFF1C293D),
    surfaceContainerHighest = Color(0xFF263247),
)

// Nexa semantic colors
val colorPing = Color(0xFF16865D)
val colorPingRed = Color(0xFFDC4466)
val colorConfigType = Color(0xFF315EFB)
val colorFabActive = Color(0xFF16A34A)
val colorFabInactiveLight = Color(0xFF315EFB)
val colorFabInactiveDark = Color(0xFF547AFF)
val dividerColorLight = Color(0xFFD8E0EC)
val dividerColorDark = Color(0xFF344157)

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9353A3E) // Dark Gray
val toastNormalBgDark = Color(0xD94A4F54) // Darker Gray
val toastSuccessBg = Color(0xD9388E3C) // Green
val toastErrorBg = Color(0xD9D50000) // Red
val toastInfoBg = Color(0xD93F51B5) // Indigo Blue
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
    MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
)
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
