package com.orangedigitalcenter.monbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orangedigitalcenter.monbudget.ui.components.EtatVide
import com.orangedigitalcenter.monbudget.ui.components.SelecteurMois
import com.orangedigitalcenter.monbudget.ui.components.couleurDepuisHex
import com.orangedigitalcenter.monbudget.ui.theme.CouleurDepense
import com.orangedigitalcenter.monbudget.ui.theme.CouleurRevenu
import com.orangedigitalcenter.monbudget.ui.viewmodel.AppViewModelProvider
import com.orangedigitalcenter.monbudget.ui.viewmodel.DashboardViewModel
import com.orangedigitalcenter.monbudget.util.formatGnf

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SelecteurMois(etat.mois, viewModel::moisPrecedent, viewModel::moisSuivant) }

        val bilan = etat.bilan
        if (etat.chargement) {
            item { Text("Chargement…") }
        } else if (etat.estVide || bilan == null) {
            item { EtatVide("Aucune transaction ce mois-ci.\nAppuyez sur « Ajouter » pour commencer.") }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Solde du mois", style = MaterialTheme.typography.titleSmall)
                        Text(
                            formatGnf(bilan.solde),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (bilan.solde >= 0) CouleurRevenu else CouleurDepense
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Revenus : ${formatGnf(bilan.totalRevenus)}", color = CouleurRevenu)
                            Text("Dépenses : ${formatGnf(bilan.totalDepenses)}", color = CouleurDepense)
                        }
                    }
                }
            }

            if (etat.budgetsDepasses.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚠ Budgets dépassés", style = MaterialTheme.typography.titleSmall, color = CouleurDepense)
                            etat.budgetsDepasses.forEach {
                                Text("${it.categorie.nom} : ${formatGnf(-it.restantGnf)} au-dessus du plafond")
                            }
                        }
                    }
                }
            }

            item { Text("Dépenses par catégorie", style = MaterialTheme.typography.titleMedium) }
            val maxTotal = bilan.repartitionDepenses.maxOfOrNull { it.total } ?: 1L
            items(bilan.repartitionDepenses, key = { it.categorieId }) { ligne ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ligne.nom)
                        Text(formatGnf(ligne.total))
                    }
                    // Barre proportionnelle à la plus grosse dépense
                    Box(
                        Modifier
                            .fillMaxWidth(ligne.total.toFloat() / maxTotal)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(couleurDepuisHex(ligne.couleur))
                    )
                }
            }
        }
    }
}
