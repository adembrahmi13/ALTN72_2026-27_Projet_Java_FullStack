# Journal — Guardian (ALTN72, 2026-27)

**Groupe :** Adem BRAHMI, Kais DILMI

**Mise à jour :** 30 octobre 2026

**Date limite Moodle :** 5 novembre 2026 à 23 h 59.

Ce journal décrit l'état actuel du dépôt et distingue les réalisations des travaux restants.

## README

Guardian permet de consulter les équipements des stations, de déclarer une panne et de consulter les incidents.

### Prérequis

- **Java :** JDK 25 pour le compilateur Maven actuel. Le POM indique encore `java.version=21`, mais configure `source=25` et `target=25` ; harmonisation restante.
- **Maven :** validation réalisée avec Maven 3.9.11.
- **Base :** MariaDB en fonctionnement normal ; H2 en mémoire pour les tests.
- **Framework :** Spring Boot 4.1.1.

### Configuration

| Variable | Rôle | Valeur |
| --- | --- | --- |
| `DB_URL` | Connexion MariaDB | Par défaut : `jdbc:mariadb://localhost:3307/guardian_bdd` |
| `DB_USER` | Utilisateur MariaDB | Par défaut : `root` ; adapter au poste |
| `DB_PASSWORD` | Mot de passe MariaDB | À définir localement ; défaut vide |
| `DEMO_MOT_DE_PASSE` | Mot de passe initial des comptes de démonstration | Obligatoire au démarrage, choisi localement |

Exemple PowerShell, après remplacement des valeurs locales :

~~~powershell
$env:DB_URL = "jdbc:mariadb://localhost:3307/guardian_bdd"
$env:DB_USER = "<utilisateur MariaDB>"
$env:DB_PASSWORD = "<mot de passe MariaDB>"
$env:DEMO_MOT_DE_PASSE = "<mot de passe de démonstration>"
mvn spring-boot:run
~~~

### Initialisation et lancement

1. Démarrer MariaDB.
2. Exécuter manuellement `src/main/resources/schema.sql` avec un client MariaDB.
3. Définir les variables ci-dessus.
4. Lancer `mvn spring-boot:run`.
5. Ouvrir [la connexion locale](http://localhost:8080/connexion).

Le script supprime et recrée les tables du domaine et les utilisateurs : il prépare une base de démonstration, sans préserver les données existantes. L'ordre de création de `station` a été corrigé : après `USE guardian_bdd` et après les suppressions. Son exécution sur MariaDB vierge reste à vérifier.

`InitialisationUtilisateursDemo` crée les comptes absents au démarrage avec BCrypt :

| Profil | Login | Prénom | Mot de passe initial |
| --- | --- | --- | --- |
| Responsable d'exploitation | `responsable` | Camille | Valeur locale de `DEMO_MOT_DE_PASSE` |
| Agent de signalement | `agent` | Alex | Valeur locale de `DEMO_MOT_DE_PASSE` |

Changer cette variable ne change pas le mot de passe des comptes déjà créés. Aucun secret local n'est reproduit dans ce journal.

- **Tests :** `mvn verify`, sans serveur MariaDB nécessaire.
- **Déploiement :** aucune URL fournie à ce stade.
- **API incidents et Swagger / OpenAPI :** non implémentés (F12).
- **Démonstration SQL :** deux stations, trois équipements (distributeur, portique, borne), deux prestataires, deux incidents dont un résolu, et une évaluation.
- **Dates :** encore fixes en juin 2026 ; génération relative à maintenant et cas 47 h / 48 h / 72 h restant à réaliser.
- **Limites :** F6 à F12 et les deux fonctions transverses restent à développer. Le prestataire et l'évaluation sont consultables dans F5, sans formulaire d'affectation ni de saisie.
- **Héritage du TP :** les pages programmeurs sont conservées et restylées ; leur table n'est pas créée par le script Guardian. Leur fonctionnement sur une base initialisée uniquement avec ce script n'a pas été validé.

## ARCHITECTURE

**Dépôt :** [ALTN72_2026-27_Projet_Java_FullStack](https://github.com/adembrahmi13/ALTN72_2026-27_Projet_Java_FullStack).

~~~mermaid
flowchart LR
    Navigateur --> Securite[Spring Security]
    Securite --> Controleurs[Spring MVC]
    Controleurs --> Services[Services métier]
    Services --> Repositories[Spring Data JPA]
    Repositories --> Base[(MariaDB / H2 en tests)]
    Controleurs --> Vues[Thymeleaf et CSS commun]
    Vues --> Navigateur
~~~

### Organisation en couches

Packages relatifs à `altn72.tpfilrouge`.

| Couche | Emplacement | Responsabilité |
| --- | --- | --- |
| Contrôleurs web | `controleur` | Routes, formulaires, modèles de vues et redirections |
| Services | `service` | Déclaration, recherche des ressources, détail et durée d'incident |
| Repositories | `repository` ; interfaces héritées aussi dans `modele` | Accès aux données JPA |
| Modèle | `modele` | Entités et enum Profil |
| Exceptions | `exception` | Ressource introuvable et gestion centralisée des 404 |
| Sécurité | `configuration` et services utilisateur | Authentification, BCrypt, comptes de démonstration |
| Présentation | `templates` et `static/css/style.css` | Vues HTML, fragment d'en-tête et styles communs |

### Modèle de données

~~~mermaid
erDiagram
    STATION ||--o{ EQUIPEMENT : contient
    EQUIPEMENT ||--o{ INCIDENT : concerne
    PRESTATAIRE o|--o{ INCIDENT : prend_en_charge
    INCIDENT ||--o| EVALUATION : possede
~~~

`Utilisateur` porte le profil `RESPONSABLE_EXPLOITATION` ou `AGENT_SIGNALEMENT`.
`Programmeur` est hérité du TP, hors domaine Guardian.
`DetailIncident` est un record de présentation, non persisté.

### Technologies

Java 25, Maven, Spring Boot 4.1.1, Spring MVC, Spring Data JPA / Hibernate,
Spring Security, Thymeleaf et son intégration Spring Security, Jakarta Validation,
pilote MariaDB, H2, JUnit Jupiter et MockMvc. Les versions des dépendances sont
gérées par le parent Spring Boot. Aucune bibliothèque CSV ou graphique n'est
encore ajoutée. L'interface utilise du CSS local et des polices système.

## TESTS

- **Stratégie :** intégration Spring Boot avec MockMvc, services et repositories réels, base H2 en mémoire en mode MariaDB. Aucun service métier mocké.
- **Configuration :** `src/test/resources/application.properties`, schéma JPA `create-drop`, script MariaDB désactivé.
- **Isolation :** transactions et retour arrière pour les tests de données.
- **Commande :** `mvn verify`.
- **Dernière validation après refonte HTML/CSS :** 16 tests, aucun échec ni erreur.
- **Horloge :** `Clock` injectable ; horloge fixe UTC pour les durées F5.
- **Couverture chiffrée :** non mesurée, aucun rapport JaCoCo configuré.

### Comportements couverts

| Fonctionnalité | Scénarios | Classe | Nombre |
| --- | --- | --- | --- |
| F1 | Connexion publique, visiteur redirigé, identifiants invalides refusés | `ConnexionTest`, dans `TpFilRougeApplicationTests.java` | 3 |
| F2 | Stations affichées et station absente en 404 | `EquipementsParStationTest` | 2 |
| F3 | Incidents triés par ouverture décroissante et équipement absent en 404 | `FicheEquipementTest` | 2 |
| F4 | Création avec valeurs imposées, description vide refusée, équipement absent en 404 | `DeclarationIncidentTest` | 3 |
| F5 | Agent sans prestataire/évaluation, responsable avec prestataire/évaluation et durée arrêtée, affectation sans évaluation, incident absent en 404, visiteur redirigé, lien depuis l'équipement | `DetailIncidentTest` | 6 |

F4 vérifie qu'un statut et un prestataire envoyés dans un formulaire manipulé
ne remplacent pas les valeurs imposées par le service. F5 vérifie le HTML rendu
et les durées exactes en jours, heures et minutes.

### Comportements non couverts

| Comportement | Situation |
| --- | --- |
| Connexion réussie et déconnexion complète | Tests dédiés à ajouter |
| Indicateurs 47 h / 48 h / 72 h et état affecté (RG3–RG4) | F11 non réalisée |
| Affectation, réaffectation et résolution (RG5–RG6) | F7 et F8 non réalisées |
| Historique global trié par résolution (RG7) | F9 non réalisée ; tri par ouverture d'équipement testé |
| Refus d'évaluation avant résolution, doublons, notes invalides (RG8) | Saisie F10 absente ; contraintes présentes sans tests dédiés |
| Référence d'équipement unique (RG9) | Contrainte présente, test de violation absent |
| Restrictions propres aux profils (RG11) | Routes actuelles communes aux deux profils ; restrictions futures à ajouter |
| API incidents, import/export et agrégations graphiques | Fonctionnalités absentes |
| Script SQL MariaDB | Tests sur H2 uniquement |
| Aspect visuel et comportement mobile réels | Pas de test navigateur ni de capture visuelle effectués |

## CHOIX TECHNIQUES

### Droits par profil

> Tableau du §5.6 : indiquer pour chaque fonctionnalité **Complet**, **Lecture seule** ou **Interdit**.
> Toute restriction est appliquée **côté serveur** (RG11) : masquer un bouton ne suffit pas.

| N° | Fonctionnalité | Profil 1 : Responsable d'exploitation | Profil 2 : Agent |
| --- | --- | --- | --- |
| F2–F3 | Consulter équipements et fiches | ✅ | ✅ |
| F4 | Déclarer un incident | ✅ | ✅ |
| F5–F6 | Consulter / modifier un incident | ✅ | ✅ |
| F7 | Affecter ou réaffecter un prestataire | ✅ | ❌ |
| F8 | Déclarer un incident résolu | ✅ | ❌ |
| F9 | Consulter l'historique | ✅ | ✅ |
| F10 | Évaluer un prestataire | ✅ | ❌ |
| F11 | Tableau de bord | ✅ | ❌ |

Ce tableau présente la répartition des droits retenue. Les fonctionnalités encore
absentes et les restrictions restant à implémenter sont indiquées dans le suivi interne.

Les deux profils consultent et signalent les pannes. La cible réserve au
responsable la coordination des prestataires, la clôture et l'évaluation.
Pour F6, aucun champ d'incident n'est actuellement modifiable par une route dédiée ;
champs autorisés, profils et conditions restent à définir.

`SecurityConfig` laisse publiques la connexion et les ressources CSS/images puis
exige une authentification pour toute autre requête. `UtilisateurConnecte`
fournit les rôles. Aucune règle `hasRole` différenciant les profils métier
n'est encore configurée. Les restrictions futures devront être appliquées
côté serveur et testées.

### Déclaration F4 et détail F5

**F4 :** description obligatoire avec `@NotBlank`. Le service utilise l'équipement
de l'URL, la description saisie et l'horloge ; il impose le statut « Non affecté »,
sans prestataire ni résolution.

**F5 :** le lien sur la description dans `ficheEquipement.html` ouvre
`GET /incidents/{id}`. `IncidentController` appelle
`IncidentService.consulterDetail` et rend `detailIncident.html`.

La page affiche description, équipement, station, statut, dates et durée,
nom et contact du prestataire éventuel, note, commentaire éventuel et date
de l'évaluation. Des messages indiquent explicitement les absences.

La durée est calculée dans le service avec `Clock`, sans stockage en base :
- ouvert : instant courant moins ouverture ;
- résolu : résolution moins ouverture.

L'affichage en jours, heures et minutes reste actualisé à chaque consultation
et ne continue pas à augmenter après résolution.
`HorlogeConfig` fournit l'horloge réelle ; les tests utilisent une horloge fixe.

`Prestataire` et `Evaluation` ont été ajoutés au modèle Java.
`Incident.prestataireId` conserve l'écriture compatible avec F4 et une relation
`@ManyToOne` en lecture seule (`insertable=false, updatable=false`)
permet de consulter le prestataire. `EvaluationRepository.findByIncident_Id`
retrouve l'évaluation. L'unicité de `evaluation.incident_id` est définie dans JPA
et SQL. Les contrôles métier complets de saisie restent à implémenter en F10.

### Exceptions

- **Guardian :** `RessourceIntrouvableException`, gérée par `GestionnaireExceptionsWeb` avec `@ControllerAdvice`.
- **Justification :** recherche dans les services et réponse 404 centralisée, sans répéter le traitement dans chaque contrôleur.
- **Message :** « Incident introuvable (identifiant …). », même principe pour station et équipement.
- **Validation F4 :** `@Valid` et `BindingResult` réaffichent le formulaire avec un message français ; aucune exception personnalisée 400 pour ce cas.
- **Transitions interdites / 409 :** aucune exception dédiée à ce stade.
- **Pages :** `erreur.html` affiche le message et propose un retour à l'accueil.
- **`ResponseEntity` :** non utilisé pour les routes Guardian actuelles, qui retournent des vues et redirections. Les routes héritées du TP utilisent aussi `ResponseStatusException`.

### Principes SOLID

| Principe | Application ou justification |
| --- | --- |
| S — Responsabilité unique | Contrôleurs pour requêtes/vues, services pour règles et durée, repositories pour persistance, CSS pour présentation |
| O — Ouvert / fermé | Aucun mécanisme d'extension dédié revendiqué ; pas de hiérarchie ajoutée sans besoin concret |
| L — Substitution de Liskov | Pas de hiérarchie métier personnalisée à démontrer |
| I — Ségrégation des interfaces | Repositories séparés par entité ; pas d'interface métier générale imposant des méthodes inutiles |
| D — Inversion des dépendances | Injection par constructeur des interfaces Spring Data et de `Clock`, remplaçable en test |

### Persistance

| Requête dérivée | Besoin |
| --- | --- |
| `StationRepository.findAllByOrderByNomAsc` | Stations triées par nom |
| `EquipementRepository.findByStation_Id` | Équipements d'une station |
| `IncidentRepository.findByEquipement_IdOrderByDateHeureOuvertureDesc` | Incidents d'un équipement, récents en premier |
| `EvaluationRepository.findByIncident_Id` | Évaluation éventuelle |
| `UtilisateurRepository.findByLogin` | Utilisateur pour authentification |

**JPQL explicite :** aucune requête `@Query` actuellement.
**SQL natif dans un repository :** absent ; exigence restant à satisfaire.
Le script d'initialisation ne remplace pas cette exigence. Le futur choix devra
comparer JPQL et SQL natif, justifier le besoin MariaDB, étudier portabilité
et maintenance, et employer des paramètres liés.

**Script :** `src/main/resources/schema.sql`, ordre de création de station corrigé
et unicité de l'évaluation ajoutée. Les tests H2 génèrent le schéma depuis les
entités ; ils ne valident pas l'exécution du script MariaDB.

### Transactions

| Où | Pourquoi |
| --- | --- |
| `IncidentService.consulterDetail`, `@Transactional(readOnly = true)` | Regrouper les lectures du détail et de l'évaluation sans modification métier |
| Repositories Spring Data | Transactions standard, dont l'insertion F4 |
| `ProgrammeurService.supprimerProgrammeur` et `modifierProgrammeur` (TP) | Vérification/suppression et lecture/modification dans une transaction |
| Tests de données | Retour arrière après chaque test |

Les futures opérations modifiant plusieurs données devront être regroupées
dans une transaction de service pour respecter RG10.

### Sécurité

- Hachage par `BCryptPasswordEncoder` de Spring Security.
- Configuration et secrets locaux via variables d'environnement.
- Connexion avec redirection vers l'accueil, message d'échec, session authentifiée.
- Déconnexion par POST `/deconnexion`, invalidation de session et suppression de `JSESSIONID`.
- Protection CSRF conservée ; formulaires Thymeleaf et jetons CSRF dans les tests POST.
- En-tête commun avec prénom, profil et déconnexion.
- API incidents absente ; sécurité et erreurs à valider lors de F12.

### Interface et CSS commun

Toutes les pages HTML utilisent `static/css/style.css`, y compris les anciennes
pages programmeurs auparavant liées au CDN Bootstrap.

- Variables CSS partagées : vert profond `#126b65`, texte bleu ardoise `#1c3046`, fond clair `#f3f6fa`, surfaces blanches.
- En-tête Guardian avec liens Accueil et Stations et équipements.
- Cartes arrondies, ombres légères, espacements et titres harmonisés.
- Boutons, champs, formulaires et messages d'erreur/information cohérents.
- Stations en cartes ; fiches structurées en listes de description.
- Tableaux dans des conteneurs avec défilement horizontal sur petit écran.
- Présentation dédiée pour connexion et erreur avec la même palette.
- Métadonnée viewport sur chaque page et styles mobiles sous 760 px.
- Focus clavier visible, libellés conservés, régions de tableaux accessibles au clavier.
- Respect de `prefers-reduced-motion` et polices système sans chargement externe.

La couleur d'erreur ne représente pas une urgence d'incident.
Le calcul des indicateurs et les constantes 48 h / 72 h de F11 restent à développer.
Les tests valident les templates et parcours couverts ; la vérification visuelle
dans un navigateur reste à effectuer.

## LOT ALPHA

### Import / export de données — piste 9

- **Besoin envisagé :** alimenter le réseau et exploiter les incidents hors de l'application.
- **État :** choix du journal initial, sans implémentation actuelle.
- **Périmètre restant à décider :** entités importables, format CSV, encodage, filtres d'export et gestion des doublons de référence.
- **Choix et essais :** aucune bibliothèque ni route dédiée ajoutée.
- **Erreurs :** définir traitement des fichiers et lignes invalides, import atomique ou partiel.
- **Droits :** à décider et à appliquer côté serveur.
- **Fonctionnement et tests :** aucun parcours disponible ou testé.
- **Difficultés :** pas de retour d'implémentation à ce stade.
- **Suite :** fixer le périmètre, développer, tester et fournir un exemple reproductible.

### Visualisation des données — piste 2

- **Besoin envisagé :** comprendre la répartition des pannes et les résultats d'intervention.
- **État :** non implémentée ; bibliothèque et calculs à choisir.

| Graphique envisagé | Données | Type envisagé, à confirmer | État |
| --- | --- | --- | --- |
| Incidents par station | Nombre par station | Barres | Non réalisé |
| Incidents par type d'équipement | Nombre par type | Barres | Non réalisé |
| Délais de résolution | Durée des incidents clos par période | Courbe ou barres | Non réalisé |
| Notes des prestataires | Moyenne des évaluations par prestataire | Barres | Non réalisé |
| Évolution des incidents | Nombre d'ouvertures par période | Courbe | Non réalisé |

- **Choix et essais :** aucune bibliothèque graphique, route ou agrégation ajoutée.
- **Fonctionnement et tests :** aucun graphique disponible ou testé.
- **Difficultés :** pas de retour d'implémentation à ce stade.
- **Suite :** choisir les graphiques retenus, les filtres et droits, développer les agrégations et vérifier leurs résultats.

La refonte CSS améliore le socle et n'est pas comptée comme une fonctionnalité
transverse du lot alpha.

## BILAN

### Contributions

| Membre | Éléments connus | Informations restantes |
| --- | --- | --- |
| Adem BRAHMI | Travaux demandés dans cette session : F5, tests, documentation et harmonisation CSS | Attribution des travaux antérieurs à confirmer par le groupe et Git |
| Kais DILMI | Membre du groupe | Contributions détaillées non précisées dans les échanges ; à renseigner par le groupe |

### Difficultés rencontrées

F5 nécessitait des entités Java pour les tables prestataire et évaluation déjà
présentes dans le SQL. Elles ont été ajoutées sans remplacer le champ
`prestataireId` de F4. L'horloge injectable rend les durées exactes testables,
y compris après résolution.

L'interface mélangeait des pages peu stylées et des pages Bootstrap.
Une feuille locale commune et un en-tête partagé assurent désormais la cohérence.

Ces difficultés concernent les travaux de cette session ; la difficulté principale
du projet pour le groupe reste à confirmer au bilan final.

### Fonctionnalités non réalisées et pourquoi

F6 à F12, les deux fonctions transverses et le déploiement restent à réaliser.
Aucun motif d'abandon communiqué : ce sont des travaux restants.
La validation MariaDB, l'affichage navigateur et le lancement sur clone propre
restent également à effectuer.

### Trois points retenus des travaux actuels

1. Séparer règles métier, persistance et rendu facilite l'ajout d'une fiche sans casser la déclaration.
2. Injecter une horloge rend les calculs de durée et les tests reproductibles.
3. Centraliser CSS et fragments HTML assure la cohérence visuelle ; les droits restent contrôlés côté serveur.

## Suivi interne

| Fonctionnalité | État |
| --- | --- |
| F1 Connexion / déconnexion | Implémentée ; tests partiels |
| F2 Équipements par station | Implémentée ; tests partiels |
| F3 Fiche équipement | Implémentée et testée |
| F4 Déclarer un incident | Implémentée et testée |
| F5 Détail d'un incident | Implémentée ; 6 tests dédiés |
| F6 Modifier un incident | Non réalisée |
| F7 Affecter / réaffecter | Non réalisée |
| F8 Résoudre un incident | Non réalisée |
| F9 Historique des résolus | Non réalisée ; incidents passés visibles dans la fiche équipement |
| F10 Évaluer un prestataire | Saisie absente ; lecture disponible dans F5 |
| F11 Tableau de bord | Non réalisé |
| F12 API incidents + Swagger | Non réalisée |
| Lot alpha 1 : import / export | Non réalisé |
| Lot alpha 2 : graphiques | Non réalisé |
| Harmonisation CSS de toutes les pages | Réalisée ; vérification visuelle navigateur restante |
| Déploiement | URL non fournie |

### Vérifications avant dépôt

- [ ] Valider le lancement sur clone propre avec ce README.
- [ ] Exécuter le script corrigé sur MariaDB vierge.
- [ ] Générer des dates relatives et les cas 47 h / 48 h / 72 h.
- [ ] Terminer F6 à F12 et vérifier RG1 à RG11.
- [ ] Appliquer et tester les restrictions propres aux profils.
- [ ] Réaliser et documenter les deux fonctions transverses.
- [ ] Ajouter et justifier les requêtes JPQL et native demandées.
- [ ] Vérifier toutes les pages dans un navigateur sur ordinateur et mobile.
- [ ] Nettoyer les restes du TP, commentaires obsolètes et traces console.
- [ ] Harmoniser la version Java du POM.
- [ ] Documenter le déploiement et tester depuis un autre ordinateur.
- [ ] Confirmer les contributions et finaliser le bilan de groupe.
- [ ] Relancer `mvn verify` sur la copie de livraison.
- [ ] Vérifier secrets et historique Git.
- [ ] Déposer sur Moodle avant le 5 novembre 2026 à 23 h 59.
