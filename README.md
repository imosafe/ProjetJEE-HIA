# PROTEAMS - E-Sport Bracket Master

**Projet JEE - Université CY Tech (2024-2025)**

Une application web complète de gestion de tournois E-sport (League of Legends, Valorant, etc.), développée selon l'architecture **JEE** avec **Spring Boot** et **Thymeleaf**. Elle gère l'intégralité du cycle de vie d'un tournoi : de la création des équipes et le recrutement via liens uniques, jusqu'à la génération automatique de l'arbre de compétition (bracket) et la saisie des scores.

---

## Technologies Utilisées

* **Backend :** Java 17, Spring Boot 3 (Spring MVC, Spring Data JPA).
* **Frontend :** Thymeleaf, HTML5, CSS3 (Bootstrap 5), Bootstrap Icons.
* **Base de Données :** H2 Database (Mode Fichier persistant - embarquée).
* **Outils :** Maven, Git.

---

## Fonctionnalités

### Partie Publique & Joueurs
* **Authentification & Profil :** Inscription, connexion sécurisée et gestion de profil utilisateur (Relation 1-1).
* **Gestion d'Équipe (Logique Métier) :**
    * Création d'équipe avec logo et jeu de prédilection.
    * **Système d'invitation :** Génération de liens uniques (UUID) pour recruter des joueurs sans intervention admin.
    * Rejoindre, quitter ou dissoudre une équipe.
* **Tournois :**
    * Catalogue des tournois disponibles.
    * Inscription conditionnelle (vérification de la compatibilité du jeu de l'équipe).
    * Visualisation dynamique de l'arbre de compétition.

### Partie Administration
* **Back-office :** CRUD complet des utilisateurs, équipes et sponsors.
* **Gestion des Relations Complexes :**
    * Association/Dissociation de **Sponsors** aux tournois (Relation N-N).
    * Gestion des matchs et des scores.
* **Mécanique Sportive (Algorithme) :**
    * **Génération d'arbre :** Algorithme automatique basé sur les puissances de 2.
    * **Gestion des Byes :** Qualification automatique si le nombre d'équipes est impair.
    * **Propagation :** Mise à jour automatique du bracket lors de la saisie d'un score.

---

## Installation et Lancement

### Prérequis
* Java JDK 17 installé.
* Maven installé.

### Étapes

1.  **Cloner le dépôt :**
    ```bash
    git clone [https://github.com/imosafe/ProjetJEE-HIA.git](https://github.com/imosafe/ProjetJEE-HIA.git)
    cd ProjetJEE-HIA
    ```

2.  **Lancer l'application :**
    ```bash
    mvn spring-boot:run
    ```

3.  **Accéder au site :**
    Ouvrez votre navigateur sur : [http://localhost:8080](http://localhost:8080)

---

## Base de Données H2 & Données de Test

L'application démarre avec une base de données **persistante** (fichier `crmdb.mv.db` à la racine) et pré-remplie.

### Comptes de Test (Ready-to-use)

| Pseudo | Mot de passe | Rôle | Équipe | Scénario de test |
| :--- | :--- | :--- | :--- | :--- |
| **admin** | `admin` | `ADMIN` | - | Accès Dashboard, création tournois, saisie scores, gestion Sponsors. |
| **faker** | `1234` | `PLAYER` | **T1** | Inscription tournoi LOL, recrutement membres. |
| **tenz** | `1234` | `PLAYER` | **Sentinels** | Test incompatibilité jeu (ne peut pas s'inscrire à un tournoi LOL). |

### Accès Console H2
Pour vérifier les tables ou exécuter du SQL :
1.  URL : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2.  **JDBC URL :** `jdbc:h2:file:./crmdb;AUTO_SERVER=TRUE`
3.  **User :** `sa` / **Password :** (vide)

---

## Auto Évaluation & Conformité

Ce projet a été réalisé en respectant scrupuleusement les exigeances du projet JEE. Voici le détail de notre conformité par rapport à la grille d'évaluation :

### 1. Respect des Exigences Fonctionnelles (10/10) 
L'application dépasse le simple CRUD pour proposer une véritable logique métier.

* **Gestion des 5 Entités & Relations Complexes:**
    Nous avons implémenté un modèle de données complet respectant les cardinalités imposées :
    * **1-N (OneToMany) :** Une *Équipe* possède plusieurs *Joueurs* ; un *Tournoi* possède plusieurs *Matchs*.
    * **N-N (ManyToMany) :** Un *Tournoi* est financé par plusieurs *Sponsors*, et un *Sponsor* peut financer plusieurs tournois.
    * **1-1 (OneToOne) :** Un *Utilisateur* est lié à un *Profil*.
* **Logique Métier Avancée:**
    * Algorithme de **génération d'arbre de tournoi** (Bracket) gérant les puissances de 2 et les nombres d'équipes impairs (Byes). On a également corrigé et testé l'algorithme tout en se basant sur l'approche du livrable "Tourneo".
    * Système d'**invitation par UUID** pour rejoindre une équipe.
* **Association Graphique:**
    L'interface permet d'associer et dissocier dynamiquement les entités (ex: Ajout de sponsors à un tournoi via une interface dédiée).

### 2. Respect des Exigences Techniques (5/5) [cite: 25]
* **Architecture MVC :** Séparation stricte entre Entités, Contrôleurs et Vues Thymeleaf.
* **Verbes HTTP :** Utilisation correcte de `GET` (affichage), `POST` (traitement) et des redirections (`Post-Redirect-Get`).
***Interface :** Utilisation de **Bootstrap** pour un rendu esthétique et responsive.

### 3. Livrable et Base de Données
* **BDD H2 Embarquée :** Configurée en mode fichier (`jdbc:h2:file:./crmdb`) et incluse dans le repository, permettant de tester le projet immédiatement.
* **Git :** Historique de commits réguliers montrant la collaboration de tous les membres.

---

## Auteurs

* **Imane SAFE**
* **Hichem Chergui**
* **Angel Fernandez Argoul**

*Projet réalisé pour le module JEE - CY Tech 2024*