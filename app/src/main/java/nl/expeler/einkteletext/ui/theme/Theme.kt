package nl.expeler.einkteletext.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EinkColorScheme = lightColorScheme(
    primary = EinkInk,
    secondary = EinkDarkGray,
    tertiary = EinkMidGray,
    background = EinkPaper,
    surface = EinkPaper,
    surfaceVariant = EinkLightGray,
    outline = EinkBorder,
    onPrimary = EinkPaper,
    onSecondary = EinkPaper,
    onBackground = EinkInk,
    onSurface = EinkInk,
    onSurfaceVariant = EinkDarkGray,
)

@Composable
fun EinkTeletextTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = EinkPaper.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = EinkColorScheme,
        typography = TeletextTypography,
        content = content
    )
}
