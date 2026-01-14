# Sentiment Analysis Backend (sa-backend)

`sa-backend` est une API REST développée avec **Spring Boot**, permettant d’enregistrer des avis clients et d’en analyser automatiquement le sentiment (positif, neutre ou négatif).

Ce projet a pour objectif de démontrer la conception d’un **backend Java moderne**, structuré et extensible, prêt à être intégré dans une application front-end (Angular, React…) ou dans un écosystème de microservices.

L’application s’appuie sur des **services d’infrastructure conteneurisés via Docker** (base de données, outil d’administration), tandis que l’API Spring Boot est exécutée localement.

---

## 🎯 Objectifs du projet

Ce projet a été conçu pour illustrer :

- La conception d’une API REST claire et cohérente avec Spring Boot
- Une architecture backend en couches (Controller / Service / Repository)
- L’intégration d’un **service externe** (API Hugging Face) avec gestion de fallback
- La persistance des données via JPA
- L’utilisation de **Docker pour fournir l’infrastructure locale**
- Des bonnes pratiques applicables à un contexte professionnel
- Un backend prêt à évoluer vers un environnement de production

---

## Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation et exécution](#installation-et-exécution)
- [Infrastructure locale avec Docker](#-infrastructure-locale-avec-docker)
- [Fonctionnement](#fonctionnement)
- [Documentation de l’API](#documentation-de-lapi)
- [Endpoints de l'API](#endpoints-de-lapi)
- [Technologies utilisées](#technologies-utilisées)
- [Bonnes pratiques mises en œuvre](#-bonnes-pratiques-mises-en-œuvre)
- [Points d’évolution possibles](#points-dévolution-possibles)

---

## Fonctionnalités

- Soumission d’avis clients via une API REST
- Analyse automatique du sentiment du texte
- Persistance des avis et des clients en base de données
- Consultation et filtrage des avis par type de sentiment
- Intégration d’un service d’analyse externe avec gestion des erreurs
- Tests unitaires et d’intégration (JUnit, MockMvc)

---

## Architecture

L’application suit une architecture en couches, favorisant la lisibilité, la testabilité et l’évolutivité :

```
src/
 └── main/
     └── java/ld/sa_backend/
         ├── controller      → endpoints REST (Customer, Review)
         ├── dto             → objets de transfert (DTO)
         ├── entity          → entités JPA (Customer, Review)
         ├── enums           → types métier (ReviewType)
         ├── exception       → gestion centralisée des erreurs
         ├── external        → intégration API Hugging Face
         ├── repository      → accès aux données (Spring Data JPA)
         └── service         → logique métier (analyse de sentiment)
 └── resources/
     ├── application.properties
     └── docker-compose.yml
```

---

## Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- IDE recommandé : IntelliJ IDEA, Eclipse ou VS Code

---

## Installation et exécution

### 1. Cloner le dépôt

```bash
git clone https://github.com/Cherrygolo/sa-backend.git
cd sa-backend
```

### 2. Lancer l’infrastructure (base de données)

```bash
cd src/main/resources
docker-compose up -d
```

### 3. Lancer l’API Spring Boot

```bash
./mvnw spring-boot:run
```

L’API est accessible à l’adresse :

```
http://localhost:8080/api/v1
```

---

## 🐳 Infrastructure locale avec Docker

Le projet utilise **Docker Compose** pour fournir les services d’infrastructure nécessaires au fonctionnement de l’API.

Les services conteneurisés sont :

- **MariaDB** : base de données relationnelle
- **Adminer** : interface web d’administration de la base

L’API Spring Boot est exécutée localement et se connecte à la base MariaDB exposée par Docker.

### Services exposés

- MariaDB : `localhost:3307`
- Adminer : http://localhost:8081

### Connexion à la base via Adminer

- Système : MySQL / MariaDB  
- Serveur : `db`  
- Utilisateur : `root`  
- Mot de passe : `root`

---

## Fonctionnement

L’API peut fonctionner selon deux modes, en fonction de la présence d’un token d’accès à l’API Hugging Face.

### 🔹 Avec un token Hugging Face

Lorsque le token est fourni, l’application utilise le modèle :
`nlptown/bert-base-multilingual-uncased-sentiment`

Le token doit être renseigné dans un fichier `config.properties` :

```
HUGGINGFACE_TOKEN=VOTRE_TOKEN_ICI
```

Étapes de l’analyse :

1. Le texte est envoyé à l’API Hugging Face via une requête HTTP POST
2. Le modèle renvoie un score de 1 à 5 étoiles
3. Le score est converti en type métier :

| Étoiles | Sentiment |
|-------|-----------|
| 1 – 2 | NEGATIVE  |
| 3     | NEUTRAL   |
| 4 – 5 | POSITIVE  |

En cas d’erreur ou de réponse invalide, un **fallback automatique** renvoie un sentiment `NEUTRAL`.

---

### 🔹 Sans token Hugging Face

Si aucun token n’est fourni, une analyse simplifiée est appliquée :

- Recherche de mots-clés négatifs (`ne`, `n'`, `pas`)
- Présence détectée → `NEGATIVE`
- Sinon → `POSITIVE`

⚠️ Cette méthode est volontairement basique et sert uniquement de solution de secours.

---

## Documentation de l’API

La documentation de l’API est fournie directement dans ce README à travers des **exemples concrets de requêtes et de réponses JSON**.

L’intégration de Swagger / OpenAPI est identifiée comme une **évolution naturelle**, afin d’automatiser la documentation et faciliter l’intégration avec des clients externes.

---

## Endpoints de l'API

### Clients

**POST /api/v1/customer**

```json
{
  "email": "alice@example.com",
  "phone": "0601020304"
}
```

**GET /api/v1/customer**

---

### Avis

**POST /api/v1/review**

```json
{
  "text": "Service rapide et équipe très sympathique !",
  "customerId": 1
}
```

**GET /api/v1/review**

**GET /api/v1/review?type=POSITIVE**

---

## Technologies utilisées

- Java 17
- Spring Boot 3
- Spring Web / REST
- Spring Data JPA / Hibernate
- Maven
- JUnit / MockMvc
- Docker
- Docker Compose
- MariaDB
- Adminer

---

## Bonnes pratiques mises en œuvre

- Architecture en couches (Controller / Service / Repository)
- Séparation Entity / DTO
- Validation des entrées utilisateur
- Gestion centralisée des exceptions
- Intégration externe isolée et testable
- Fallback automatique en cas d’indisponibilité d’un service externe
- Utilisation de Docker pour l’infrastructure locale
- Code modulaire et extensible

---

## Points d’évolution possibles

Les évolutions suivantes sont volontairement identifiées afin de démontrer la capacité du projet à évoluer vers un contexte de production :

- Conteneurisation complète de l’API Spring Boot
- Ajout de Swagger / OpenAPI
- Sécurisation de l’API (JWT / Spring Security)
- Centralisation de la configuration via variables d’environnement
- Séparation des environnements (dev / prod)
- Ajout d’un front-end (Angular ou React)
- Renforcement de la couverture de tests
- Mise en place de métriques et de monitoring

---

## Conclusion

Ce projet met l’accent sur la **qualité du code**, la **clarté de l’architecture** et des **choix techniques réfléchis**, notamment l’utilisation de Docker pour l’infrastructure locale.

Il constitue une base saine pour une API REST Java prête à être intégrée dans un environnement professionnel.

