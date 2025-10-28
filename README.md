# Sentiment Analysis Backend (sa-backend)

`sa-backend` est un service Spring Boot fournissant une API REST pour enregistrer et analyser les avis des clients.  
L’application évalue automatiquement si un avis est positif, neutre ou négatif, en se basant sur le contenu textuel envoyé via l’API.

Ce projet illustre la mise en œuvre d’un backend Java moderne, modulable et prêt à être intégré dans un écosystème de microservices ou une application front-end (Angular, React, etc.).

---

## Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation et exécution](#installation-et-exécution)
- [Fonctionnement](#fonctionnement)
- [Endpoints de l'API](#endpoints-de-lapi)
- [Technologies utilisées](#technologies-utilisées)
- [Bonnes pratiques intégrées](#bonnes-pratiques-intégrées)
- [Points d’évolution possibles](#points-dévolution-possibles)

---

## Fonctionnalités


<summary>Voir les fonctionnalités</summary>

- Soumission d’un avis client via une API REST
- Analyse automatique du sentiment du texte
- Persistance des avis en base de données (ex. H2, PostgreSQL, MySQL)
- Consultation des avis enregistrés
- API documentée avec Swagger / OpenAPI (si activé)
- Tests unitaires et d’intégration avec JUnit et MockMvc

</details>

---

## Architecture


L’application suit une architecture en couches claire et testable :
```
src/
 └── main/
     └── java/ld/sa_backend/
         ├── controller      → endpoints REST pour Customer et Review
         ├── dto             → objets de transfert (DTOs)
         ├── entity          → classes JPA : Review.java, Customer.java
         ├── enums           → ReviewType.java
         ├── exception       → classes pour gérer les exceptions
         ├── external        → intégrations externes (ex : API Hugging Face)
         ├── repository      → interfaces Spring Data JPA
         └── service         → logique métier pour l’analyse des sentiments
 └── resources/    → fichiers de configuration
```


## Prérequis

- Java 17+
- Maven 3.8+
- (Optionnel) Docker pour le déploiement
- IDE : IntelliJ IDEA, Eclipse ou VS Code avec extensions Java


## Installation et exécution


1. Cloner le dépôt

```bash
git clone https://github.com/chillo-tech/sa-backend.git
cd sa-backend
```

2. Lancer l’application
```bash
./mvnw spring-boot:run
```
L’API est disponible par défaut sur : http://localhost:8080/api/


## Fonctionnement

L’API peut fonctionner de deux manières, selon que vous ayez renseigné ou non un token d’accès à l’API Hugging Face :

### Avec un token Hugging Face

Si un token valide est fourni, l’API utilise le modèle pré-entraîné nlptown/bert-base-multilingual-uncased-sentiment
 pour analyser le texte.

Ce dernier peut être obtenu gratuitement après création d'un compte sur Hugging Face (cf https://huggingface.co/docs/hub/security-tokens)

Étapes de l'analyse :

L’API lit le token depuis le fichier config.properties :

HUGGINGFACE_TOKEN=VOTRE_TOKEN_ICI

Le texte à analyser est envoyé au modèle via une requête HTTP POST.

Le modèle renvoie un score de sentiment sous forme d’étoiles (de 1 à 5).

Le score est converti en ReviewType :

| Étoiles | Sentiment |
| ------- | --------- |
| 1 ou 2  | NEGATIVE  |
| 3       | NEUTRAL   |
| 4 ou 5  | POSITIVE  |

L’API retourne le sentiment correspondant.

En cas d’erreur HTTP ou si le modèle renvoie un résultat vide/malformé, l’API retourne NEUTRAL.

#### Exemple de fichier config.properties
```
# Fichier de configuration pour l'API Hugging Face
HUGGINGFACE_TOKEN=hf_XXXXXXXXXXXXXXXXXXXX
```

### Sans token Hugging Face

Si aucun token n’est renseigné ou si le fichier config.properties est absent, l’API utilise une analyse basique basée sur des mots-clés négatifs (ne, n', pas).

Si le texte contient ces mots, le sentiment renvoyé sera NEGATIVE.

Sinon, le sentiment sera POSITIVE.

⚠️ Cette méthode est simpliste et ne reflète pas la nuance complète du texte.




## Endpoints de l'API

### Clients

#### Créer un client

**POST /api/v1/customer**

```json
{
  "email": "alice@example.com",
  "phone": "0601020304"
}
```

Réponse :

```json
{
  "id": 1,
  "email": "alice@example.com",
  "phone": "0601020304"
}
```

#### Récupérer tous les clients

**GET /api/v1/customer**

Réponse : 

```json
[
  {
    "id": 1,
    "email": "alice@example.com",
    "phone": "0601020304"
  },
  {
    "id": 2,
    "email": "bob@example.com",
    "phone": "0605060708"
  }
]
```

### Avis (Reviews)

#### Créer un avis

**POST /api/v1/review**

```json
{
  "text": "Service rapide et équipe très sympathique !",
  "customerId": 1
}
```

Réponse :

```json
{
  "id": 42,
  "text": "Service rapide et équipe très sympathique !",
  "type": "POSITIVE",
  "customer": {
    "id": 1,
    "email": "alice@example.com",
    "phone": "0601020304"
  }
}
```

#### Récupérer tous les avis

**GET /api/v1/review**

Réponse :

```json
[
  {
    "id": 42,
    "text": "Service rapide et équipe très sympathique !",
    "type": "POSITIVE",
    "customer": {
      "id": 1,
      "email": "alice@example.com",
      "phone": "0601020304"
    }
  },
  {
    "id": 43,
    "text": "Le produit est arrivé en retard.",
    "type": "NEGATIVE",
    "customer": {
      "id": 2,
      "email": "bob@example.com",
      "phone": "0605060708"
    }
  }
]
```

### Récupérer les avis filtrés par type

**GET /api/v1/review?type=POSITIVE**

type : le type d’avis (POSITIVE, NEGATIVE, NEUTRAL)

Exemple : 

**GET /api/v1/review?type=POSITIVE**

Réponse : 

```json
[
  {
    "id": 42,
    "text": "Service rapide et équipe très sympathique !",
    "type": "POSITIVE",
    "customer": {
      "id": 1,
      "email": "alice@example.com",
      "phone": "0601020304"
    }
  },
  {
    "id": 44,
    "text": "Excellent produit, je recommande !",
    "type": "POSITIVE",
    "customer": {
      "id": 3,
      "email": "charlie@example.com",
      "phone": "0608091011"
    }
  }
]
```

## Technologies utilisées

- Spring Boot 3

- Spring Web / REST

- Spring Data JPA / Hibernate

- Maven

(Optionnel) Swagger / OpenAPI


## Bonnes pratiques intégrées

- Architecture MVC claire et modulaire

- Validation des entrées (@Valid, @NotBlank, etc.)

- Gestion centralisée des exceptions (@ControllerAdvice)

- Injection de dépendances via Spring IoC


## Points d’évolution possibles

- Authentification JWT pour restreindre l’accès à certaines routes

- Déploiement via Docker Compose avec base PostgreSQL

- Mise en place d’un front-end pour la saisie et la visualisation des avis

- Ajout de tests unitaires et d’intégration pour :
  - Vérifier la logique d’analyse des avis
  - Tester les endpoints REST
  - Couverture des cas limites (avis longs, texte vide, caractères spéciaux)
  - Tests d’intégration avec différentes bases de données
  - Tests de sécurité sur les endpoints



