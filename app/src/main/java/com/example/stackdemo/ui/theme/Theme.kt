package com.example.stackdemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary          = BStackYellow,
    onPrimary        = BStackDark,
    secondary        = BStackDark,
    onSecondary      = BStackWhite,
    background       = BStackWhite,
    surface          = BStackSurface,
    onBackground     = BStackDarkGray,
    onSurface        = BStackDarkGray,
    error            = BStackError
)

@Composable
fun StackDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content
    )
}
