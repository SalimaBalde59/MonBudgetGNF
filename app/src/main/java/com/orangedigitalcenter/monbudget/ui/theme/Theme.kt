package com.orangedigitalcenter.monbudget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CouleurRevenu = Color(0xFF2E7D32)
val CouleurDepense = Color(0xFFC62828)

private val Couleurs = lightColorScheme(
    primary = Color(0xFFFF6F00),        // orange
    onPrimary = Color.White,
    secondary = Color(0xFF2E7D32),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFC62828)
)

// Aucun texte en dessous de 14 sp (lisibilité sur petit écran)
private val Textes = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 14.sp)
)

@Composable
fun MonBudgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Couleurs, typography = Textes, content = content)
}
