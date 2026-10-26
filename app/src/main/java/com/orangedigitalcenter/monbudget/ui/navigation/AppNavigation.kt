package com.orangedigitalcenter.monbudget.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.orangedigitalcenter.monbudget.ui.screens.BudgetsObjectifsScreen
import com.orangedigitalcenter.monbudget.ui.screens.DashboardScreen
import com.orangedigitalcenter.monbudget.ui.screens.TransactionFormScreen
import com.orangedigitalcenter.monbudget.ui.screens.TransactionsScreen

private object Routes {
    const val TABLEAU = "tableau"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val SAISIE = "saisie"
}

private data class Onglet(val route: String, val libelle: String, val icone: String)

private val onglets = listOf(
    Onglet(Routes.TABLEAU, "Bilan", "📊"),
    Onglet(Routes.TRANSACTIONS, "Transactions", "💸"),
    Onglet(Routes.BUDGETS, "Budgets", "🎯")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val routeActuelle = navController.currentBackStackEntryAsState().value?.destination?.route
    val surOnglet = onglets.any { it.route == routeActuelle }

    Scaffold(
        bottomBar = {
            if (surOnglet) {
                NavigationBar {
                    onglets.forEach { o ->
                        NavigationBarItem(
                            selected = routeActuelle == o.route,
                            onClick = {
                                navController.navigate(o.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(o.icone) },
                            label = { Text(o.libelle) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (routeActuelle == Routes.TABLEAU || routeActuelle == Routes.TRANSACTIONS) {
                ExtendedFloatingActionButton(onClick = { navController.navigate(Routes.SAISIE) }) {
                    Text("+ Ajouter")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TABLEAU,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.TABLEAU) { DashboardScreen() }
            composable(Routes.TRANSACTIONS) { TransactionsScreen() }
            composable(Routes.BUDGETS) { BudgetsObjectifsScreen() }
            composable(Routes.SAISIE) { TransactionFormScreen(onTermine = { navController.popBackStack() }) }
        }
    }
}
