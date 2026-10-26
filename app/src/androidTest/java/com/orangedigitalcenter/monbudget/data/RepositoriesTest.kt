package com.orangedigitalcenter.monbudget.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.orangedigitalcenter.monbudget.data.local.AppDatabase
import com.orangedigitalcenter.monbudget.data.local.entity.Categorie
import com.orangedigitalcenter.monbudget.data.local.entity.ModePaiement
import com.orangedigitalcenter.monbudget.data.local.entity.ObjectifEpargne
import com.orangedigitalcenter.monbudget.data.local.entity.Transaction
import com.orangedigitalcenter.monbudget.data.local.entity.TypeTransaction
import com.orangedigitalcenter.monbudget.data.repository.BudgetRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.CategorieRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.ObjectifRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.TransactionRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class RepositoriesTest {

    private lateinit var db: AppDatabase
    private lateinit var categories: CategorieRepositoryImpl
    private lateinit var transactions: TransactionRepositoryImpl
    private lateinit var budgets: BudgetRepositoryImpl
    private lateinit var objectifs: ObjectifRepositoryImpl

    private val mois = YearMonth.now()
    private val maintenant = System.currentTimeMillis()

    @Before
    fun creerBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categories = CategorieRepositoryImpl(db.categorieDao())
        transactions = TransactionRepositoryImpl(db.transactionDao())
        budgets = BudgetRepositoryImpl(db.budgetDao(), db.categorieDao(), db.transactionDao())
        objectifs = ObjectifRepositoryImpl(db.objectifDao())
    }

    @After
    fun fermerBase() = db.close()

    @Test
    fun bilan_calcule_revenus_depenses_et_solde() = runBlocking {
        val salaire = categories.ajouter(Categorie(nom = "Salaire", type = TypeTransaction.REVENU, couleur = "#2E7D32"))
        val transport = categories.ajouter(Categorie(nom = "Transport", type = TypeTransaction.DEPENSE, couleur = "#F57C00"))

        transactions.ajouter(Transaction(categorieId = salaire, montantGnf = 2_000_000, type = TypeTransaction.REVENU, date = maintenant, modePaiement = ModePaiement.MOBILE_MONEY))
        transactions.ajouter(Transaction(categorieId = transport, montantGnf = 150_000, type = TypeTransaction.DEPENSE, date = maintenant, modePaiement = ModePaiement.ESPECES))

        val bilan = transactions.getBilan(mois).first()
        assertEquals(2_000_000L, bilan.totalRevenus)
        assertEquals(150_000L, bilan.totalDepenses)
        assertEquals(1_850_000L, bilan.solde)
        assertEquals(1, bilan.repartitionDepenses.size)
    }

    @Test
    fun budget_signale_le_depassement() = runBlocking {
        val nourriture = categories.ajouter(Categorie(nom = "Nourriture", type = TypeTransaction.DEPENSE, couleur = "#43A047"))
        budgets.definirBudget(nourriture, mois, plafondGnf = 100_000)

        transactions.ajouter(Transaction(categorieId = nourriture, montantGnf = 60_000, type = TypeTransaction.DEPENSE, date = maintenant, modePaiement = ModePaiement.ESPECES))
        var suivi = budgets.getBudgetsAvecSuivi(mois).first().single()
        assertEquals(60, suivi.pourcentage)
        assertFalse(suivi.depasse)

        transactions.ajouter(Transaction(categorieId = nourriture, montantGnf = 70_000, type = TypeTransaction.DEPENSE, date = maintenant, modePaiement = ModePaiement.ESPECES))
        suivi = budgets.getBudgetsAvecSuivi(mois).first().single()
        assertTrue(suivi.depasse)
        assertEquals(-30_000L, suivi.restantGnf)
    }

    @Test
    fun objectif_progresse_avec_les_versements() = runBlocking {
        val id = objectifs.ajouter(ObjectifEpargne(nom = "Moto", cibleGnf = 1_000_000))
        objectifs.ajouterVersement(id, 250_000)
        val o = objectifs.getAll().first().single()
        assertEquals(25, o.pourcentage)
        assertFalse(o.atteint)
    }

    @Test(expected = IllegalStateException::class)
    fun categorie_utilisee_ne_peut_pas_etre_supprimee() {
        runBlocking {
            val id = categories.ajouter(Categorie(nom = "Santé", type = TypeTransaction.DEPENSE, couleur = "#E53935"))
            transactions.ajouter(Transaction(categorieId = id, montantGnf = 10_000, type = TypeTransaction.DEPENSE, date = maintenant, modePaiement = ModePaiement.ESPECES))
            categories.supprimer(db.categorieDao().getById(id)!!)
        }
    }
}
