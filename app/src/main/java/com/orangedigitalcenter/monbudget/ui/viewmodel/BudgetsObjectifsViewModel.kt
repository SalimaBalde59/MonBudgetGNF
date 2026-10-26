package com.orangedigitalcenter.monbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.domain.model.BudgetAvecSuivi
import com.orangedigitalcenter.monbudget.domain.model.ObjectifAvecProgression
import com.orangedigitalcenter.monbudget.domain.repository.BudgetRepository
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import com.orangedigitalcenter.monbudget.domain.repository.ObjectifRepository
import com.orangedigitalcenter.monbudget.domain.rules.SaisieRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BudgetsObjectifsUiState(
    val mois: YearMonth,
    val budgets: List<BudgetAvecSuivi> = emptyList(),
    val categoriesDepense: List<Categorie> = emptyList(),
    val objectifs: List<ObjectifAvecProgression> = emptyList(),
    val erreur: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsObjectifsViewModel(
    private val budgets: BudgetRepository,
    private val objectifs: ObjectifRepository,
    categories: CategorieRepository,
    moisInitial: YearMonth = YearMonth.now()
) : ViewModel() {

    private val mois = MutableStateFlow(moisInitial)
    private val erreur = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BudgetsObjectifsUiState> = mois
        .flatMapLatest { m ->
            combine(
                budgets.getBudgetsAvecSuivi(m),
                categories.getByType(TypeTransaction.DEPENSE),
                objectifs.getAll(),
                erreur
            ) { b, cats, o, e ->
                BudgetsObjectifsUiState(mois = m, budgets = b, categoriesDepense = cats, objectifs = o, erreur = e)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsObjectifsUiState(moisInitial))

    fun moisPrecedent() { mois.value = mois.value.minusMonths(1) }
    fun moisSuivant() { mois.value = mois.value.plusMonths(1) }
    fun effacerErreur() { erreur.value = null }

    /** Renvoie true si la saisie est valide (l'écran peut alors fermer la boîte de dialogue). */
    fun definirBudget(categorieId: Long?, plafondTexte: String): Boolean {
        if (categorieId == null) { erreur.value = "Choisissez une catégorie."; return false }
        SaisieRules.erreurMontant(plafondTexte)?.let { erreur.value = it; return false }
        val plafond = SaisieRules.parseMontant(plafondTexte)!!
        erreur.value = null
        viewModelScope.launch { budgets.definirBudget(categorieId, mois.value, plafond) }
        return true
    }

    fun supprimerBudget(budgetId: Long) {
        viewModelScope.launch { budgets.supprimerBudget(budgetId) }
    }

    fun ajouterObjectif(nom: String, cibleTexte: String): Boolean {
        SaisieRules.erreurObjectif(nom, cibleTexte)?.let { erreur.value = it; return false }
        val cible = SaisieRules.parseMontant(cibleTexte)!!
        erreur.value = null
        viewModelScope.launch { objectifs.ajouter(ObjectifEpargne(nom = nom.trim(), cibleGnf = cible)) }
        return true
    }

    fun verser(objectifId: Long, montantTexte: String): Boolean {
        SaisieRules.erreurMontant(montantTexte)?.let { erreur.value = it; return false }
        val montant = SaisieRules.parseMontant(montantTexte)!!
        erreur.value = null
        viewModelScope.launch { objectifs.ajouterVersement(objectifId, montant) }
        return true
    }

    fun supprimerObjectif(objectif: ObjectifEpargne) {
        viewModelScope.launch { objectifs.supprimer(objectif) }
    }
}
