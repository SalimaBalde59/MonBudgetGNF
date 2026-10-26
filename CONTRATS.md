# CONTRATS — Partie données (Responsable données)

Ne pas modifier seul ces fichiers : prévenir le groupe, et incrémenter `version` dans `AppDatabase` si une entité change.

## Entités Room (`data/local/entity/Entities.kt`)
| Entité | Table | Champs |
|---|---|---|
| Categorie | categories | id, nom, type (REVENU/DEPENSE), couleur ("#RRGGBB") |
| Transaction | transactions | id, categorieId, montantGnf (> 0), type, date (epoch ms), modePaiement (ESPECES/MOBILE_MONEY), note? |
| BudgetMensuel | budgets_mensuels | id, categorieId, mois ("yyyy-MM"), plafondGnf — unique (categorieId, mois) |
| ObjectifEpargne | objectifs_epargne | id, nom, cibleGnf, montantActuelGnf, dateCible? |

Clés étrangères : Transaction → Categorie (RESTRICT), BudgetMensuel → Categorie (CASCADE).
12 catégories préchargées à la création de la base (`CategoriesParDefaut`).

## Modèles du domaine (`domain/model/Models.kt`)
TransactionAvecCategorie, TotalParCategorie, BilanMensuel, BudgetAvecSuivi, ObjectifAvecProgression
+ helpers `YearMonth.debutMillis()`, `finMillis()`, `cle()`, `calculerPourcentage()`.

## Interfaces Repository (`domain/repository/`)
- CategorieRepository : getAll, getByType, ajouter, modifier, supprimer (IllegalStateException si catégorie utilisée)
- TransactionRepository : getTransactions(mois, categorieId?), getBilan(mois), ajouter, modifier, supprimer
- BudgetRepository : getBudgetsAvecSuivi(mois), definirBudget(categorieId, mois, plafondGnf), supprimerBudget(id)
- ObjectifRepository : getAll, ajouter, modifier, supprimer, ajouterVersement(id, montantGnf)

Les implémentations sont dans `data/repository/` (construites avec les DAO de `AppDatabase`).
Erreurs de validation de base : `IllegalArgumentException` avec un message en français (à afficher par le ViewModel).

## Construction (exemple)
```kotlin
val db = AppDatabase.getInstance(context)
val transactions: TransactionRepository = TransactionRepositoryImpl(db.transactionDao())
val budgets: BudgetRepository = BudgetRepositoryImpl(db.budgetDao(), db.categorieDao(), db.transactionDao())
```

## Couche ViewModel (`ui/viewmodel/`)
| ViewModel | État exposé | Actions |
|---|---|---|
| DashboardViewModel | DashboardUiState (mois, bilan, budgetsDepasses, estVide) | moisPrecedent, moisSuivant |
| TransactionsViewModel | TransactionsUiState (mois, filtre, categories, transactions) | moisPrecedent/Suivant, filtrerParCategorie, supprimer |
| TransactionFormViewModel | FormulaireUiState (champs, erreurs, enregistre) | onMontantChange, onTypeChange, onCategorieChange, onModePaiementChange, onDateChange, onNoteChange, enregistrer |
| BudgetsObjectifsViewModel | BudgetsObjectifsUiState (budgets, objectifs, erreur) | definirBudget, supprimerBudget, ajouterObjectif, verser, supprimerObjectif (renvoient true si la saisie est valide) |
