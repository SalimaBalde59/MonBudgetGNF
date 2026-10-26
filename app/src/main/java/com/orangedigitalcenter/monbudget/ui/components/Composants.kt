package com.orangedigitalcenter.monbudget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orangedigitalcenter.monbudget.util.formatMois
import java.time.YearMonth

/** Couleur d'une catégorie stockée en "#RRGGBB" (gris si le code est invalide). */
fun couleurDepuisHex(hex: String): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }

@Composable
fun SelecteurMois(mois: YearMonth, onPrecedent: () -> Unit, onSuivant: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

    }
}

@Composable
fun EtatVide(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Barre de progression ; passe au rouge en cas de dépassement. */
@Composable
fun BarreProgression(pourcentage: Int, alerte: Boolean, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { (pourcentage / 100f).coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth(),
        color = if (alerte) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    )
}
