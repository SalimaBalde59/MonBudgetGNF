package com.orangedigitalcenter.monbudget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orangedigitalcenter.monbudget.data.local.entity.ModePaiement
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.ui.viewmodel.AppViewModelProvider
import com.orangedigitalcenter.monbudget.ui.viewmodel.TransactionFormViewModel
import com.orangedigitalcenter.monbudget.util.formatDate
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    onTermine: () -> Unit,
    viewModel: TransactionFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val etat by viewModel.uiState.collectAsStateWithLifecycle()
    var afficherDate by remember { mutableStateOf(false) }

    // Retour à l'écran précédent dès que l'enregistrement a réussi
    LaunchedEffect(etat.enregistre) { if (etat.enregistre) onTermine() }

    Column(
        Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nouvelle transaction", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = etat.type == TypeTransaction.DEPENSE,
                onClick = { viewModel.onTypeChange(TypeTransaction.DEPENSE) },
                label = { Text("Dépense") }
            )
            FilterChip(
                selected = etat.type == TypeTransaction.REVENU,
                onClick = { viewModel.onTypeChange(TypeTransaction.REVENU) },
                label = { Text("Revenu") }
            )
        }

        OutlinedTextField(
            value = etat.montantTexte,
            onValueChange = viewModel::onMontantChange,
            label = { Text("Montant (GNF)") },
            isError = etat.erreurs.montant != null,
            supportingText = { etat.erreurs.montant?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Catégorie", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(etat.categories, key = { it.id }) { c ->
                FilterChip(
                    selected = etat.categorieId == c.id,
                    onClick = { viewModel.onCategorieChange(c.id) },
                    label = { Text(c.nom) }
                )
            }
        }
        etat.erreurs.categorie?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Mode de paiement", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = etat.modePaiement == ModePaiement.ESPECES,
                onClick = { viewModel.onModePaiementChange(ModePaiement.ESPECES) },
                label = { Text("Espèces") }
            )
            FilterChip(
                selected = etat.modePaiement == ModePaiement.MOBILE_MONEY,
                onClick = { viewModel.onModePaiementChange(ModePaiement.MOBILE_MONEY) },
                label = { Text("Mobile Money") }
            )
        }

        OutlinedButton(onClick = { afficherDate = true }) { Text("Date : " + formatDate(etat.dateMillis)) }

        OutlinedTextField(
            value = etat.note,
            onValueChange = viewModel::onNoteChange,
            label = { Text("Note (facultatif)") },
            modifier = Modifier.fillMaxWidth()
        )

        etat.erreurGenerale?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(onClick = viewModel::enregistrer, modifier = Modifier.fillMaxWidth()) { Text("Enregistrer") }
        TextButton(onClick = onTermine, modifier = Modifier.fillMaxWidth()) { Text("Annuler") }
    }

    if (afficherDate) {
        // Le sélecteur travaille en UTC : on convertit vers le jour local choisi
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = Instant.ofEpochMilli(etat.dateMillis)
                .atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { afficherDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        val jour = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        // midi local : évite tout décalage de jour lié au fuseau
                        viewModel.onDateChange(
                            jour.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        )
                    }
                    afficherDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { afficherDate = false }) { Text("Annuler") } }
        ) { DatePicker(state = pickerState) }
    }
}
