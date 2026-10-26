# Mon Budget en GNF

Projet fil rouge n°11 — formation Kotlin, Orange Digital Center.
Application Android de suivi des revenus et dépenses (espèces et Mobile Money), budgets mensuels,
objectifs d'épargne et bilan du mois. 100 % hors ligne (Room).

## Architecture (MVVM)
```
ui/screens  ──►  ui/viewmodel  ──►  domain/repository (interfaces)  ──►  data/repository (impl)  ──►  data/local (Room)
                                     domain/model, domain/rules              data/local/dao, entity
```
- `ui/` : écrans Compose (Bilan, Transactions, Saisie, Budgets & Objectifs), thème, navigation par onglets.
- `ui/viewmodel/` : 4 ViewModels exposant un `StateFlow<UiState>` ; `AppViewModelProvider` les fabrique.
- `domain/rules/SaisieRules` : validation des saisies (sans Android, testable sur JVM).
- `util/Formats` : « 1 500 000 GNF », « octobre 2026 », « 06/10/2026 ».
- Voir `CONTRATS.md` pour le détail de la partie données.

## Lancer le projet
1. Ouvrir le dossier dans Android Studio (JDK 17), laisser Gradle synchroniser (`gradlew` inclus).
2. Run sur un émulateur ou un téléphone (minSdk 26).

## Tests
- Tests unitaires JVM (rapides, sans appareil) : `./gradlew test`
  - `FormatsTest`, `SaisieRulesTest` : formats, validation, pourcentages, bornes de mois
  - `ViewModelsTest` : les 4 ViewModels avec de faux Repository (`fakes/Fakes.kt`)
- Tests instrumentés (appareil ou émulateur) : `./gradlew connectedAndroidTest`
  - `RepositoriesTest` : vraie base Room en mémoire

## État
- [x] Entités Room, DAO, base avec 12 catégories préchargées
- [x] Repository (interfaces + implémentations)
- [x] ViewModels + règles de saisie
- [x] 4 écrans + navigation
- [x] Tests unitaires et instrumentés
- [ ] Icône de l'application, export des données (idées pour la suite)
