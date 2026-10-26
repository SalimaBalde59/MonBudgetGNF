package com.orangedigitalcenter.monbudget

import android.app.Application
import com.orangedigitalcenter.monbudget.data.local.AppDatabase
import com.orangedigitalcenter.monbudget.data.repository.BudgetRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.CategorieRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.ObjectifRepositoryImpl
import com.orangedigitalcenter.monbudget.data.repository.TransactionRepositoryImpl
import com.orangedigitalcenter.monbudget.domain.repository.BudgetRepository
import com.orangedigitalcenter.monbudget.domain.repository.CategorieRepository
import com.orangedigitalcenter.monbudget.domain.repository.ObjectifRepository
import com.orangedigitalcenter.monbudget.domain.repository.TransactionRepository

/** Conteneur simple : crée la base et les Repository une seule fois. */
class AppContainer(application: Application) {
    private val db = AppDatabase.getInstance(application)

    val categorieRepository: CategorieRepository = CategorieRepositoryImpl(db.categorieDao())
    val transactionRepository: TransactionRepository = TransactionRepositoryImpl(db.transactionDao())
    val budgetRepository: BudgetRepository =
        BudgetRepositoryImpl(db.budgetDao(), db.categorieDao(), db.transactionDao())
    val objectifRepository: ObjectifRepository = ObjectifRepositoryImpl(db.objectifDao())
}

class MonBudgetApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
