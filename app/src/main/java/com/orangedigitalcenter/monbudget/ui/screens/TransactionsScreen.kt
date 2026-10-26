package com.orangedigitalcenter.monbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orangedigitalcenter.monbudget.data.local.entity.ModePaiement
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.ui.components.EtatVide
import com.orangedigitalcenter.monbudget.ui.components.SelecteurMois
import com.orangedigitalcenter.monbudget.ui.components.couleurDepuisHex
import com.orangedigitalcenter.monbudget.ui.theme.CouleurDepense
import com.orangedigitalcenter.monbudget.ui.theme.CouleurRevenu
import com.orangedigitalcenter.monbudget.ui.viewmodel.AppViewModelProvider
import com.orangedigitalcenter.monbudget.ui.viewmodel.TransactionsViewModel
import com.orangedigitalcenter.monbudget.util.formatDate
import com.orangedigitalcenter.monbudget.util.formatGnf

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    var aSupprimer by remember { mutableStateOf<Transaction?>(null) }

    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelecteurMois(etat.mois, viewModel::moisPrecedent, viewModel::moisSuivant)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = etat.categorieFiltreId == null,
                    onClick = { viewModel.filtrerParCategorie(null) },
                    label = { Text("Toutes") }
                )
            }
            items(etat.categories, key = { it.id }) { c ->
                FilterChip(
                    selected = etat.categorieFiltreId == c.id,
                    onClick = { viewModel.filtrerParCategorie(c.id) },
                    label = { Text(c.nom) }
                )
            }
        }

        if (!etat.chargement && etat.transactions.isEmpty()) {
            EtatVide("Aucune transaction pour cette période.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(etat.transactions, key = { it.transaction.id }) { ligne ->
                    val t = ligne.transaction
                    val estRevenu = t.type == TypeTransaction.REVENU
                    Card {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    ligne.categorie.nom,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = couleurDepuisHex(ligne.categorie.couleur)
                                )
                                Text(
                                    formatDate(t.date) + " · " +
                                        if (t.modePaiement == ModePaiement.MOBILE_MONEY) "Mobile Money" else "Espèces"
                                )
                                t.note?.let { Text(it) }
                            }
                            Column {
                                Text(
                                    (if (estRevenu) "+ " else "− ") + formatGnf(t.montantGnf),
                                    color = if (estRevenu) CouleurRevenu else CouleurDepense,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                TextButton(onClick = { aSupprimer = t }) { Text("Supprimer") }
                            }
                        }
                    }
                }
            }
        }
    }

    aSupprimer?.let { t ->
        AlertDialog(
            onDismissRequest = { aSupprimer = null },
            title = { Text("Supprimer cette transaction ?") },
            text = { Text(formatGnf(t.montantGnf) + " — cette action est définitive.") },
            confirmButton = {
                TextButton(onClick = { viewModel.supprimer(t); aSupprimer = null }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text("Annuler") } }
        )
    }
}
