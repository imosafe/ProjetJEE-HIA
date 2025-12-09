# ProjetJEE-HIA
# E-Sport Bracket Master "PROTEAMS"

**Lien du projet :** [https://github.com/imosafe/ProjetJEE-HIA](https://github.com/imosafe/ProjetJEE-HIA)

Une application web complète de gestion de tournois E-sport (League of Legends, Valorant, etc.), développée avec **Spring Boot** et **Thymeleaf**. Elle permet la gestion du cycle de vie d'un tournoi : de la création des équipes à la génération de l'arbre de compétition (bracket) et la saisie des scores.

## Fonctionnalités Clés

### Partie Publique & Joueurs
* **Authentification :** Inscription, connexion et gestion de profil.
* **Gestion d'Équipe :** * Création d'équipe avec logo et jeu de prédilection.
  * Génération de **liens d'invitation uniques** (UUID) pour recruter.
  * Rejoindre, quitter ou dissoudre une équipe.
* **Tournois :** * Consultation du catalogue.
  * Inscription aux tournois compatibles (selon le jeu de l'équipe).
  * Visualisation de l'arbre de compétition.

### Partie Administration
* **Back-office :** CRUD complet des utilisateurs, équipes et sponsors.
* **Gestion des Tournois :** Création, modification, association de sponsors.
* **Mécanique Sportive :**
    * **Génération d'arbre :** Algorithme automatique basé sur les puissances de 2.
    * **Gestion des Byes :** Qualification automatique si le nombre d'équipe est impair.
    * **Scores :** Saisie des résultats et propagation automatique des vainqueurs au tour suivant.

## Technologies Utilisées

* **Backend :** Java 17, Spring Boot 3 (Spring MVC, Spring Data JPA).
* **Frontend :** Thymeleaf, HTML5, CSS3 (Bootstrap), Bootstrap Icons.
* **Base de Données :** H2 Database (Mode Fichier persistant).
* **Outils :** Maven, Git.

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

## Données de Test 

L'application démarre avec une base de données pré-remplie via le fichier `src/main/resources/data.sql`. Vous pouvez utiliser ces comptes pour tester les fonctionnalités immédiatement.

| Pseudo | Mot de passe | Rôle | Équipe | Jeu | Scénario de test |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **admin** | `admin` | `ADMIN` | - | - | Accès Dashboard, création tournois, saisie scores. |
| **faker** | `1234` | `PLAYER` | **T1** (Leader) | LOL | Inscription tournoi, gestion membres. |
| **tenz** | `1234` | `PLAYER` | **Sentinels** (Leader) | VALORANT | Test incompatibilité jeu/tournoi. |

---

## Base de Données H2

Ce projet utilise une base de données **H2 en mode Fichier**.
Les données sont **persistantes** : elles sont sauvegardées dans un fichier `crmdb.mv.db` à la racine du projet, même après l'arrêt du serveur.

### Accéder à la Console H2
Pour exécuter des requêtes SQL manuelles ou vérifier les tables :

1.  Allez sur : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2.  **IMPORTANT** : Utilisez exactement ces paramètres de connexion :

| Champ | Valeur |
| :--- | :--- |
| **Driver Class** | `org.h2.Driver` |
| **JDBC URL** | `jdbc:h2:file:./crmdb;AUTO_SERVER=TRUE` |
| **User Name** | `sa` |
| **Password** | **vide** |

---

## Auteurs

Projet réalisé dans le cadre du module JEE à CY Tech.

* **Imane SAFE**
* **Hichem Chergui**
* **Angel Fernandez Argoul**

---
*Année universitaire 2024-2025*