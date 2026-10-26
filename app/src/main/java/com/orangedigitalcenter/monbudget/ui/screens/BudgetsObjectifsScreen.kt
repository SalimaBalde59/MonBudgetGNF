package com.orangedigitalcenter.monbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orangedigitalcenter.monbudget.ui.components.BarreProgression
import com.orangedigitalcenter.monbudget.ui.components.SelecteurMois
import com.orangedigitalcenter.monbudget.ui.viewmodel.AppViewModelProvider
import com.orangedigitalcenter.monbudget.ui.viewmodel.BudgetsObjectifsViewModel
import com.orangedigitalcenter.monbudget.util.formatGnf

private enum class Dialogue { AUCUN, BUDGET, OBJECTIF }

@Composable
fun BudgetsObjectifsScreen(viewModel: BudgetsObjectifsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogue by remember { mutableStateOf(Dialogue.AUCUN) }
    var versementPour by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SelecteurMois(etat.mois, viewModel::moisPrecedent, viewModel::moisSuivant) }

        // ----- Budgets -----
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budgets du mois", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.effacerErreur(); dialogue = Dialogue.BUDGET }) { Text("+ Définir") }
            }
        }
        if (etat.budgets.isEmpty()) item { Text("Aucun budget défini pour ce mois.") }
        items(etat.budgets, key = { "b" + it.budget.id }) { b ->
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(b.categorie.nom, style = MaterialTheme.typography.titleSmall)
                        Text("${b.pourcentage} %", color = if (b.depasse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    BarreProgression(b.pourcentage, b.depasse)
                    Text("${formatGnf(b.depenseGnf)} dépensés sur ${formatGnf(b.budget.plafondGnf)}")
                    Text(
                        if (b.depasse) "Dépassement de ${formatGnf(-b.restantGnf)}" else "Reste ${formatGnf(b.restantGnf)}",
                        color = if (b.depasse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    )
                    TextButton(onClick = { viewModel.supprimerBudget(b.budget.id) }) { Text("Supprimer le budget") }
                }
            }
        }

        // ----- Objectifs -----
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Objectifs d'épargne", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.effacerErreur(); dialogue = Dialogue.OBJECTIF }) { Text("+ Nouveau") }
            }
        }
        if (etat.objectifs.isEmpty()) item { Text("Aucun objectif d'épargne.") }
        items(etat.objectifs, key = { "o" + it.objectif.id }) { o ->
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(o.objectif.nom, style = MaterialTheme.typography.titleSmall)
                        Text(if (o.atteint) "Atteint ✓" else "${o.pourcentage} %")
                    }
                    BarreProgression(o.pourcentage, alerte = false)
                    Text("${formatGnf(o.objectif.montantActuelGnf)} sur ${formatGnf(o.objectif.cibleGnf)}")
                    Row {
                        TextButton(onClick = { viewModel.effacerErreur(); versementPour = o.objectif.id }) { Text("Verser") }
                        TextButton(onClick = { viewModel.supprimerObjectif(o.objectif) }) { Text("Supprimer") }
                    }
                }
            }
        }
    }

    when (dialogue) {
        Dialogue.BUDGET -> DialogueBudget(
            categories = etat.categoriesDepense.map { it.id to it.nom },
            erreur = etat.erreur,
            onValider = { id, plafond -> if (viewModel.definirBudget(id, plafond)) dialogue = Dialogue.AUCUN },
            onFermer = { dialogue = Dialogue.AUCUN }
        )
        Dialogue.OBJECTIF -> DialogueObjectif(
            erreur = etat.erreur,
            onValider = { nom, cible -> if (viewModel.ajouterObjectif(nom, cible)) dialogue = Dialogue.AUCUN },
            onFermer = { dialogue = Dialogue.AUCUN }
        )
        Dialogue.AUCUN -> Unit
    }

    versementPour?.let { id ->
        DialogueMontant(
            titre = "Verser sur l'objectif",
            erreur = etat.erreur,
            onValider = { montant -> if (viewModel.verser(id, montant)) versementPour = null },
            onFermer = { versementPour = null }
        )
    }
}

@Composable
private fun DialogueBudget(
    categories: List<Pair<Long, String>>,
    erreur: String?,
    onValider: (Long?, String) -> Unit,
    onFermer: () -> Unit
) {
    var categorieId by remember { mutableStateOf<Long?>(null) }
    var plafond by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Définir un budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.first }) { (id, nom) ->
                        FilterChip(selected = categorieId == id, onClick = { categorieId = id }, label = { Text(nom) })
                    }
                }
                OutlinedTextField(
                    value = plafond, onValueChange = { plafond = it },
                    label = { Text("Plafond (GNF)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                erreur?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = { onValider(categorieId, plafond) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueObjectif(erreur: String?, onValider: (String, String) -> Unit, onFermer: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var cible by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Nouvel objectif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom (ex. Moto)") }, singleLine = true)
                OutlinedTextField(
                    value = cible, onValueChange = { cible = it },
                    label = { Text("Montant à atteindre (GNF)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                erreur?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = { onValider(nom, cible) }) { Text("Créer") } },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueMontant(titre: String, erreur: String?, onValider: (String) -> Unit, onFermer: () -> Unit) {
    var montant by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text(titre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = montant, onValueChange = { montant = it },
                    label = { Text("Montant (GNF)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                erreur?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = { onValider(montant) }) { Text("Verser") } },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } }
    )
}
