# Journal — NomDuProjet (ALTN72, 2026-27)

**Groupe :** Adem BRAHMI, Kais DILMI

> Ce journal se complète **au fil de l'eau**, pas la veille du dépôt.
> Date limite de dépôt sur Moodle : **5 novembre 2026 à 23 h 59**.
> Les éléments marqués `À COMPLÉTER` sont à remplir ; supprimer les consignes en citation une fois la section rédigée.

---

## README

> Objectif : l'enseignant doit pouvoir lancer l'application sans se poser la moindre question.

- **Prérequis :** Java `À COMPLÉTER` (identique au TP fil rouge), Maven `À COMPLÉTER`, base de données `À COMPLÉTER` (MariaDB par défaut)
- **Configuration :** variables d'environnement à définir (aucun secret n'est versionné)

  | Variable      | Rôle                       | Exemple de valeur                         |
  | ------------- | -------------------------- | ----------------------------------------- |
  | `DB_URL`      | URL de connexion à la base | `jdbc:mariadb://localhost:3306/nomdubase` |
  | `DB_USER`     | Utilisateur de la base     | `À COMPLÉTER`                             |
  | `DB_PASSWORD` | Mot de passe de la base    | _(à ne jamais écrire ici)_                |

- **Initialiser la base :** `db/create.sql` _(ou procédure : `À COMPLÉTER`)_
  - Les dates des données de démonstration sont générées **relativement à l'instant présent** : `À COMPLÉTER (mécanisme retenu)`
- **Lancer :** `mvn spring-boot:run`
- **Lancer les tests :** `mvn verify`
- **Application déployée :** https://`À COMPLÉTER`
- **Comptes de test :**

  | Profil                                | Login         | Mot de passe  |
  | ------------------------------------- | ------------- | ------------- |
  | Profil 1 : Responsable d'exploitation | `À COMPLÉTER` | `À COMPLÉTER` |
  | Profil 2 : `À COMPLÉTER`              | `À COMPLÉTER` | `À COMPLÉTER` |

- **Documentation de l'API (Swagger / OpenAPI) :** `À COMPLÉTER` (ex. `/swagger-ui.html`)
- **Données de démonstration :** `À COMPLÉTER` (stations, types d'équipements, incidents à 47 h / 48 h / 72 h, incidents affectés, incidents résolus…)
- **Limites connues :** `À COMPLÉTER`

---

## ARCHITECTURE

- **Dépôt :** https://`À COMPLÉTER`
- **Schéma :** ![architecture](docs/architecture.png)

### Organisation en couches

| Couche                   | Package       | Responsabilité                                                  |
| ------------------------ | ------------- | --------------------------------------------------------------- |
| Contrôleurs (web + REST) | `À COMPLÉTER` | Réception des requêtes, appel des services, aucune règle métier |
| Services                 | `À COMPLÉTER` | Règles de gestion (RG1 à RG11), transactions                    |
| Repositories             | `À COMPLÉTER` | Accès aux données (Spring Data JPA)                             |
| Modèle                   | `À COMPLÉTER` | Entités et énumérations                                         |
| Exceptions               | `À COMPLÉTER` | Exceptions personnalisées et gestion centralisée                |
| Sécurité                 | `À COMPLÉTER` | Authentification, autorisations, hachage des mots de passe      |

### Modèle de données

> Schéma du modèle (diagramme de classes ou MCD) : `docs/modele-donnees.png`

Entités : Station · Équipement · Incident · Prestataire · Évaluation · Utilisateur · `autres entités ajoutées`

### Technologies utilisées

`À COMPLÉTER` (Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf, bibliothèque de graphiques, Apache Commons CSV, etc., avec versions)

---

## TESTS

- **Stratégie :** `À COMPLÉTER` (pyramide de tests, unitaires vs intégration, ce qui est mocké ou non)
- **Commande :** `mvn verify`
- **Horloge :** `java.time.Clock` injectée dans le service pour tester les seuils horaires

### Comportements couverts

| Comportement                                                                | Règle     | Type de test          | Classe de test | Statut |
| --------------------------------------------------------------------------- | --------- | --------------------- | -------------- | ------ |
| Incident non affecté ouvert depuis 47 h → vert                              | RG3       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Incident non affecté ouvert depuis 48 h pile → orange                       | RG3       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Incident non affecté ouvert depuis 72 h pile → rouge                        | RG3       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Incident affecté → état de présentation distinct (jamais vert/orange/rouge) | RG4       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Incident résolu absent des incidents actifs                                 | RG6       | Intégration           | `À COMPLÉTER`  | ☐      |
| Historique ordonné du plus récent au plus ancien                            | RG7       | Intégration           | `À COMPLÉTER`  | ☐      |
| Incident résolu non affectable / non réaffectable                           | RG5       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Date de résolution postérieure ou égale à l'ouverture                       | RG5       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Évaluation refusée si incident non résolu ou sans prestataire               | RG8       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Une seule évaluation par incident                                           | RG8       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Note hors de 1 à 5 refusée                                                  | RG8       | Unitaire              | `À COMPLÉTER`  | ☐      |
| Référence d'équipement unique                                               | RG9       | Intégration           | `À COMPLÉTER`  | ☐      |
| Incident inexistant → réponse HTTP 404                                      | F12       | Intégration (MockMvc) | `À COMPLÉTER`  | ☐      |
| Accès interdit selon le profil (côté serveur)                               | RG11      | Intégration (MockMvc) | `À COMPLÉTER`  | ☐      |
| Utilisateur non connecté redirigé vers la connexion                         | F1        | Intégration (MockMvc) | `À COMPLÉTER`  | ☐      |
| `À COMPLÉTER` (import / export)                                             | Lot alpha | `À COMPLÉTER`         | `À COMPLÉTER`  | ☐      |
| `À COMPLÉTER` (agrégations des graphiques)                                  | Lot alpha | `À COMPLÉTER`         | `À COMPLÉTER`  | ☐      |

### Comportements non couverts

| Comportement  | Pourquoi      |
| ------------- | ------------- |
| `À COMPLÉTER` | `À COMPLÉTER` |

_(Optionnel)_ Couverture mesurée : `À COMPLÉTER` %

---

## CHOIX TECHNIQUES

### Droits par profil

> Tableau du §5.6 : indiquer pour chaque fonctionnalité **Complet**, **Lecture seule** ou **Interdit**.
> Toute restriction est appliquée **côté serveur** (RG11) : masquer un bouton ne suffit pas.

| N°    | Fonctionnalité                        | Profil 1 : Responsable d'exploitation | Profil 2 : Agent |
| ----- | ------------------------------------- | ------------------------------------- | ---------------- |
| F2–F3 | Consulter équipements et fiches       | ✅                                    | ✅               |
| F4    | Déclarer un incident                  | ✅                                    | ✅               |
| F5–F6 | Consulter / modifier un incident      | ✅                                    | ✅               |
| F7    | Affecter ou réaffecter un prestataire | ✅                                    | ❌               |
| F8    | Déclarer un incident résolu           | ✅                                    | ❌               |
| F9    | Consulter l'historique                | ✅                                    | ✅               |
| F10   | Évaluer un prestataire                | ✅                                    | ❌               |
| F11   | Tableau de bord                       | ✅                                    | ❌               |

**Justification des choix de droits :** `À COMPLÉTER`

**Champs modifiables d'un incident (F6) et par quel profil :** `À COMPLÉTER`

**Mécanisme d'application côté serveur :** `À COMPLÉTER` (ex. règles d'autorisation Spring Security par URL, annotations de méthode…)

### Exceptions

- **Choix :** `@ControllerAdvice` ou `ResponseStatusException` ? `À COMPLÉTER`
- **Pourquoi :** `À COMPLÉTER`
- **Exceptions personnalisées créées :**

  | Exception     | Cas d'usage                 | Code HTTP | Message (en français) |
  | ------------- | --------------------------- | --------- | --------------------- |
  | `À COMPLÉTER` | Ressource inexistante       | 404       | `À COMPLÉTER`         |
  | `À COMPLÉTER` | Donnée invalide             | 400       | `À COMPLÉTER`         |
  | `À COMPLÉTER` | Transition d'état interdite | 409       | `À COMPLÉTER`         |

- **Côté pages web :** `À COMPLÉTER` (message compréhensible, aucune trace technique affichée)
- **Usage de `ResponseEntity` :** `À COMPLÉTER` (où, pourquoi, ou « non utilisé » avec raison)

### Principes SOLID

| Principe                           | Appliqué ?    | Où / comment / ce que cela change concrètement — ou justification de la non-pertinence |
| ---------------------------------- | ------------- | -------------------------------------------------------------------------------------- |
| **S** — Responsabilité unique      | `À COMPLÉTER` | `À COMPLÉTER`                                                                          |
| **O** — Ouvert / fermé             | `À COMPLÉTER` | `À COMPLÉTER`                                                                          |
| **L** — Substitution de Liskov     | `À COMPLÉTER` | `À COMPLÉTER`                                                                          |
| **I** — Ségrégation des interfaces | `À COMPLÉTER` | `À COMPLÉTER`                                                                          |
| **D** — Inversion des dépendances  | `À COMPLÉTER` | `À COMPLÉTER`                                                                          |

> Au moins un principe appliqué et documenté ; les autres justifiés honnêtement.

### Persistance

- **Requêtes dérivées du nom de méthode :** `À COMPLÉTER` (repository, méthode, besoin)
- **Requêtes JPQL explicites :** `À COMPLÉTER` (repository, méthode, besoin)
- **Requête SQL native (une seule) :**
  - Où : `À COMPLÉTER`
  - Pourquoi une requête native ici : `À COMPLÉTER`
  - **Analyse critique** (avantages, inconvénients, portabilité, maintenance, sécurité, alternatives JPQL envisagées) : `À COMPLÉTER`
- **Script de création de la base :** `db/create.sql` — `À COMPLÉTER`

### `@Transactional`

| Où (classe / méthode) | Pourquoi (RG10 : opération atomique, données modifiées) |
| --------------------- | ------------------------------------------------------- |
| `À COMPLÉTER`         | `À COMPLÉTER`                                           |

### Sécurité

- **Hachage des mots de passe :** `À COMPLÉTER` (algorithme, bibliothèque)
- **Gestion des secrets :** `À COMPLÉTER` (variables d'environnement / profil non versionné)
- **Protection des pages et de l'API :** `À COMPLÉTER`

### Autres décisions de conception

`À COMPLÉTER` (calcul de l'indicateur d'urgence, constantes des seuils 48 h / 72 h et leur emplacement unique, état de présentation d'un incident affecté, choix de la vue, base de données…)

---

## LOT ALPHA

### Fonctionnalité transverse 1 : Import / export de données (piste 9)

- **Besoin :** `À COMPLÉTER`
- **Périmètre envisagé :**
  - Import : `À COMPLÉTER` (stations ? équipements ? format ? validation ? gestion des doublons sur la référence, RG9 ?)
  - Export : `À COMPLÉTER` (historique des incidents ? format CSV ? filtres ?)
- **Choix et essais :** `À COMPLÉTER` (Spring MVC, Apache Commons CSV, gestion de l'encodage et du séparateur…)
- **Gestion des erreurs :** `À COMPLÉTER` (fichier invalide, lignes en erreur, import atomique ou partiel)
- **Droits par profil :** `À COMPLÉTER`
- **Ce qui fonctionne :** `À COMPLÉTER`
- **Difficultés :** `À COMPLÉTER`
- **Tests associés :** `À COMPLÉTER`
- **Reste à faire / limites :** `À COMPLÉTER`

### Fonctionnalité transverse 2 : Visualisation des données (piste 2)

- **Besoin :** `À COMPLÉTER`
- **Périmètre envisagé :**

  | Graphique                             | Donnée        | Type de graphique | Statut |
  | ------------------------------------- | ------------- | ----------------- | ------ |
  | Répartition des incidents par station | `À COMPLÉTER` | `À COMPLÉTER`     | ☐      |
  | Répartition par type d'équipement     | `À COMPLÉTER` | `À COMPLÉTER`     | ☐      |
  | Évolution des délais de résolution    | `À COMPLÉTER` | `À COMPLÉTER`     | ☐      |
  | Note moyenne des prestataires         | `À COMPLÉTER` | `À COMPLÉTER`     | ☐      |
  | Tendances (incidents dans le temps)   | `À COMPLÉTER` | `À COMPLÉTER`     | ☐      |

- **Choix et essais :** `À COMPLÉTER` (bibliothèque de graphiques, requêtes d'agrégation JPQL, endpoint REST ou rendu côté serveur…)
- **Ce qui fonctionne :** `À COMPLÉTER`
- **Difficultés :** `À COMPLÉTER`
- **Tests associés :** `À COMPLÉTER`
- **Reste à faire / limites :** `À COMPLÉTER`

### Fonctionnalités supplémentaires (facultatif)

> Toute réalisation supplémentaire pleinement fonctionnelle, testée et documentée peut être valorisée.

`À COMPLÉTER ou supprimer`

---

## BILAN

### Contribution de chaque membre

| Membre     | Contribution principale | Fonctionnalités / parties du code |
| ---------- | ----------------------- | --------------------------------- |
| Prénom NOM | `À COMPLÉTER`           | `À COMPLÉTER`                     |
| Prénom NOM | `À COMPLÉTER`           | `À COMPLÉTER`                     |
| Prénom NOM | `À COMPLÉTER`           | `À COMPLÉTER`                     |

### Plus grande difficulté et comment elle a été surmontée

`À COMPLÉTER`

### Fonctionnalités non réalisées et pourquoi

`À COMPLÉTER` (ou « Aucune »)

### Trois points à retenir du cours et du projet

1. `À COMPLÉTER`
2. `À COMPLÉTER`
3. `À COMPLÉTER`

---

## Suivi interne (à retirer avant le dépôt si souhaité)

| Fonctionnalité                | Statut | Responsable |
| ----------------------------- | ------ | ----------- |
| F1 Connexion / déconnexion    | ☐      |             |
| F2 Équipements par station    | ☐      |             |
| F3 Fiche équipement           | ☐      |             |
| F4 Déclarer un incident       | ☐      |             |
| F5 Détail d'un incident       | ☐      |             |
| F6 Modifier un incident       | ☐      |             |
| F7 Affecter / réaffecter      | ☐      |             |
| F8 Résoudre un incident       | ☐      |             |
| F9 Historique des résolus     | ☐      |             |
| F10 Évaluer un prestataire    | ☐      |             |
| F11 Tableau de bord           | ☐      |             |
| F12 API REST + Swagger        | ☐      |             |
| Lot alpha 1 : import / export | ☐      |             |
| Lot alpha 2 : graphiques      | ☐      |             |
| Déploiement                   | ☐      |             |

### Liste de contrôle avant dépôt

- [ ] Un clone propre se lance avec le seul README
- [ ] La base se crée et se remplit (cas limites 47 h, 48 h, 72 h couverts)
- [ ] Les deux profils se connectent ; restrictions effectives côté serveur
- [ ] F1 à F12 opérationnelles, RG1 à RG11 respectées
- [ ] Deux fonctionnalités transverses démontrables et décrites
- [ ] L'URL déployée fonctionne depuis un autre ordinateur
- [ ] `mvn verify` passe sur la copie propre
- [ ] Aucun import inutilisé, code commenté, `System.out.println` ou `printStackTrace`
- [ ] Seuils 48 h / 72 h définis à un seul endroit (constantes nommées)
- [ ] Aucune règle métier dans les contrôleurs ou les vues
- [ ] Exceptions personnalisées en français, aucun `catch` vide
- [ ] Au moins un principe SOLID documenté, les autres justifiés
- [ ] Repositories : requêtes dérivées + JPQL + une seule requête native analysée
- [ ] Aucun mot de passe ou secret en clair, ni en base ni dans Git
- [ ] `journal.md` complet
- [ ] Historique Git régulier pour chaque membre
- [ ] Dépôt unique déposé sur Moodle avant le 5 novembre 2026 à 23 h 59
