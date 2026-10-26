package com.orangedigitalcenter.monbudget.ui

import com.orangedigitalcenter.monbudget.MainDispatcherRule
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ModePaiement
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.fakes.FakeBudgetRepository
import com.orangedigitalcenter.monbudget.fakes.FakeCategorieRepository
import com.orangedigitalcenter.monbudget.fakes.FakeObjectifRepository
import com.orangedigitalcenter.monbudget.fakes.FakeTransactionRepository
import com.orangedigitalcenter.monbudget.ui.viewmodel.BudgetsObjectifsViewModel
import com.orangedigitalcenter.monbudget.ui.viewmodel.DashboardViewModel
import com.orangedigitalcenter.monbudget.ui.viewmodel.TransactionFormViewModel
import com.orangedigitalcenter.monbudget.ui.viewmodel.TransactionsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val mois = YearMonth.of(2026, 10)
    private val aujourdhui = mois.atDay(15).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val salaire = Categorie(1, "Salaire", TypeTransaction.REVENU, "#2E7D32")
    private val transport = Categorie(2, "Transport", TypeTransaction.DEPENSE, "#F57C00")
    private val nourriture = Categorie(3, "Nourriture", TypeTransaction.DEPENSE, "#43A047")

    /** Un StateFlow construit avec stateIn(WhileSubscribed) n'émet que s'il est collecté. */
    private fun TestScope.collecter(flow: StateFlow<*>) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect { } }
    }

    private fun depense(categorieId: Long, montant: Long) = Transaction(
        categorieId = categorieId, montantGnf = montant, type = TypeTransaction.DEPENSE,
        date = aujourdhui, modePaiement = ModePaiement.ESPECES
    )

    // ---------- Tableau de bord ----------

    @Test fun dashboard_calcule_le_solde_du_mois() = runTest {
        val cats = FakeCategorieRepository(listOf(salaire, transport))
        val trans = FakeTransactionRepository(cats)
        trans.ajouter(Transaction(categorieId = 1, montantGnf = 2_000_000, type = TypeTransaction.REVENU, date = aujourdhui, modePaiement = ModePaiement.MOBILE_MONEY))
        trans.ajouter(depense(2, 150_000))

        val vm = DashboardViewModel(trans, FakeBudgetRepository(), mois)
        collecter(vm.uiState)

        val etat = vm.uiState.value
        assertFalse(etat.chargement)
        assertEquals(1_850_000L, etat.bilan?.solde)
        assertFalse(etat.estVide)
    }

    @Test fun dashboard_est_vide_sans_transaction_et_change_de_mois() = runTest {
        val cats = FakeCategorieRepository(listOf(transport))
        val vm = DashboardViewModel(FakeTransactionRepository(cats), FakeBudgetRepository(), mois)
        collecter(vm.uiState)

        assertTrue(vm.uiState.value.estVide)
        vm.moisSuivant()
        assertEquals(YearMonth.of(2026, 11), vm.uiState.value.mois)
        vm.moisPrecedent(); vm.moisPrecedent()
        assertEquals(YearMonth.of(2026, 9), vm.uiState.value.mois)
    }

    // ---------- Liste des transactions ----------

    @Test fun transactions_filtre_par_categorie_et_supprime() = runTest {
        val cats = FakeCategorieRepository(listOf(transport, nourriture))
        val trans = FakeTransactionRepository(cats)
        trans.ajouter(depense(2, 10_000))
        trans.ajouter(depense(3, 20_000))

        val vm = TransactionsViewModel(trans, cats, mois)
        collecter(vm.uiState)
        assertEquals(2, vm.uiState.value.transactions.size)

        vm.filtrerParCategorie(3)
        assertEquals(1, vm.uiState.value.transactions.size)
        assertEquals(3L, vm.uiState.value.transactions.single().categorie.id)

        vm.filtrerParCategorie(null)
        vm.supprimer(vm.uiState.value.transactions.first().transaction)
        assertEquals(1, vm.uiState.value.transactions.size)
    }

    // ---------- Formulaire ----------

    @Test fun formulaire_refuse_un_montant_invalide() = runTest {
        val cats = FakeCategorieRepository(listOf(transport))
        val trans = FakeTransactionRepository(cats)
        val vm = TransactionFormViewModel(trans, cats) { aujourdhui }
        collecter(vm.uiState)

        vm.onCategorieChange(2)
        vm.onMontantChange("abc")
        vm.enregistrer()

        assertNotNull(vm.uiState.value.erreurs.montant)
        assertFalse(vm.uiState.value.enregistre)
        assertTrue(trans.transactions.value.isEmpty())
    }

    @Test fun formulaire_enregistre_une_transaction_valide() = runTest {
        val cats = FakeCategorieRepository(listOf(transport))
        val trans = FakeTransactionRepository(cats)
        val vm = TransactionFormViewModel(trans, cats) { aujourdhui }
        collecter(vm.uiState)

        vm.onCategorieChange(2)
        vm.onMontantChange("25 000")
        vm.onModePaiementChange(ModePaiement.MOBILE_MONEY)
        vm.onNoteChange("  Taxi  ")
        vm.enregistrer()

        assertTrue(vm.uiState.value.enregistre)
        val t = trans.transactions.value.single()
        assertEquals(25_000L, t.montantGnf)
        assertEquals(ModePaiement.MOBILE_MONEY, t.modePaiement)
        assertEquals("Taxi", t.note)
        assertEquals(aujourdhui, t.date)
    }

    @Test fun formulaire_changer_de_type_filtre_les_categories_et_vide_la_selection() = runTest {
        val cats = FakeCategorieRepository(listOf(salaire, transport))
        val vm = TransactionFormViewModel(FakeTransactionRepository(cats), cats) { aujourdhui }
        collecter(vm.uiState)

        vm.onCategorieChange(2)
        assertEquals(listOf(transport), vm.uiState.value.categories)

        vm.onTypeChange(TypeTransaction.REVENU)
        assertEquals(listOf(salaire), vm.uiState.value.categories)
        assertNull(vm.uiState.value.categorieId)
    }

    // ---------- Budgets et objectifs ----------

    @Test fun budget_refuse_sans_categorie_ou_avec_plafond_invalide() = runTest {
        val cats = FakeCategorieRepository(listOf(transport))
        val budgets = FakeBudgetRepository()
        val vm = BudgetsObjectifsViewModel(budgets, FakeObjectifRepository(), cats, mois)
        collecter(vm.uiState)

        assertFalse(vm.definirBudget(null, "100000"))
        assertNotNull(vm.uiState.value.erreur)
        assertFalse(vm.definirBudget(2, "0"))
        assertTrue(budgets.appelsDefinir.isEmpty())

        assertTrue(vm.definirBudget(2, "100 000"))
        assertNull(vm.uiState.value.erreur)
        assertEquals(Triple(2L, mois, 100_000L), budgets.appelsDefinir.single())
    }

    @Test fun objectif_cree_puis_alimente_par_versements() = runTest {
        val objectifs = FakeObjectifRepository()
        val vm = BudgetsObjectifsViewModel(FakeBudgetRepository(), objectifs, FakeCategorieRepository(), mois)
        collecter(vm.uiState)

        assertFalse(vm.ajouterObjectif("", "500000"))
        assertTrue(vm.ajouterObjectif("Moto", "1 000 000"))
        val id = objectifs.objectifs.value.single().id

        assertFalse(vm.verser(id, "-5"))
        assertTrue(vm.verser(id, "250000"))
        val o = vm.uiState.value.objectifs.single()
        assertEquals(25, o.pourcentage)
        assertFalse(o.atteint)
    }
}
