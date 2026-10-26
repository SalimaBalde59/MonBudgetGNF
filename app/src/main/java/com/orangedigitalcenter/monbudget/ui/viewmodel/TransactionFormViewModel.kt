package com.orangedigitalcenter.monbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ModePaiement
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository
import com.orangedigitalcenter.monbudget.domain.rules.ErreursSaisie
import com.orangedigitalcenter.monbudget.domain.rules.SaisieRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Champs saisis par l'utilisateur. */
private data class ChampsFormulaire(
    val type: TypeTransaction = TypeTransaction.DEPENSE,
    val montantTexte: String = "",
    val categorieId: Long? = null,
    val modePaiement: ModePaiement = ModePaiement.ESPECES,
    val dateMillis: Long,
    val note: String = "",
    val erreurs: ErreursSaisie = ErreursSaisie(),
    val enregistre: Boolean = false,
    val erreurGenerale: String? = null
)

data class FormulaireUiState(
    val type: TypeTransaction,
    val montantTexte: String,
    val categorieId: Long?,
    val categories: List<Categorie>,   // seulement celles du type choisi
    val modePaiement: ModePaiement,
    val dateMillis: Long,
    val note: String,
    val erreurs: ErreursSaisie,
    val enregistre: Boolean,
    val erreurGenerale: String?
)

class TransactionFormViewModel(
    private val transactions: TransactionRepository,
    private val categories: CategorieRepository,
    private val maintenant: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val champs = MutableStateFlow(ChampsFormulaire(dateMillis = maintenant()))

    val uiState: StateFlow<FormulaireUiState> = combine(champs, categories.getAll()) { c, toutes ->
        FormulaireUiState(
            type = c.type,
            montantTexte = c.montantTexte,
            categorieId = c.categorieId,
            categories = toutes.filter { it.type == c.type },
            modePaiement = c.modePaiement,
            dateMillis = c.dateMillis,
            note = c.note,
            erreurs = c.erreurs,
            enregistre = c.enregistre,
            erreurGenerale = c.erreurGenerale
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        FormulaireUiState(
            TypeTransaction.DEPENSE, "", null, emptyList(), ModePaiement.ESPECES,
            champs.value.dateMillis, "", ErreursSaisie(), false, null
        )
    )

    fun onMontantChange(texte: String) =
        champs.update { it.copy(montantTexte = texte, erreurs = it.erreurs.copy(montant = null)) }

    /** Changer de type remet la catégorie à zéro (une catégorie est soit revenu, soit dépense). */
    fun onTypeChange(type: TypeTransaction) =
        champs.update { it.copy(type = type, categorieId = null, erreurs = ErreursSaisie()) }

    fun onCategorieChange(id: Long) =
        champs.update { it.copy(categorieId = id, erreurs = it.erreurs.copy(categorie = null)) }

    fun onModePaiementChange(mode: ModePaiement) = champs.update { it.copy(modePaiement = mode) }
    fun onDateChange(epochMillis: Long) = champs.update { it.copy(dateMillis = epochMillis) }
    fun onNoteChange(note: String) = champs.update { it.copy(note = note) }

    fun enregistrer() {
        viewModelScope.launch {
            val etat = champs.value
            val categorie = etat.categorieId?.let { id -> categories.getAll().first().find { it.id == id } }
            val erreurs = SaisieRules.validerTransaction(etat.montantTexte, categorie, etat.type)
            if (!erreurs.estValide) {
                champs.update { it.copy(erreurs = erreurs) }
                return@launch
            }
            try {
                transactions.ajouter(
                    Transaction(
                        categorieId = categorie!!.id,
                        montantGnf = SaisieRules.parseMontant(etat.montantTexte)!!,
                        type = etat.type,
                        date = etat.dateMillis,
                        modePaiement = etat.modePaiement,
                        note = etat.note.trim().ifEmpty { null }
                    )
                )
                champs.update { it.copy(enregistre = true, erreurGenerale = null) }
            } catch (e: Exception) {
                champs.update { it.copy(erreurGenerale = "Enregistrement impossible. Réessayez.") }
            }
        }
    }
}
