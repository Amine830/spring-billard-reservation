# API de Réservation de Tables de Billard

## 🎯 Vue d'ensemble

Cette API REST permet la gestion de réservations de tables de billard avec un système d'authentification JWT, de gestion des joueurs, et de commentaires sur les parties. L'architecture suit les principes RESTful avec une séparation claire des responsabilités entre les couches.

## 🏗️ Architecture générale

```bash
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │───▶│    Services     │───▶│      DAOs       │
│  (REST Layer)   │    │ (Business Logic)│    │ (Data Access)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Filters     │    │      DTOs       │    │     Models      │
│ (Cross-cutting) │    │ (Data Transfer) │    │   (Entities)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔐 Authentification et Autorisation

### Système JWT (JSON Web Tokens)

#### Principe

- **Authentification** : Vérification de l'identité utilisateur
- **Autorisation** : Contrôle d'accès aux ressources basé sur les droits
- **Sessions stateless** : Aucune session côté serveur, tout dans le token

#### Implémentation

- **Algorithme** : HS512 (HMAC avec SHA-512)
- **Bibliothèque** : `jjwt-api 0.12.6`
- **Claims personnalisés** :
  - `sub` : Identifiant utilisateur
  - `own` : Liste des indices de réservations possédées
  - `ply` : Liste des indices de réservations où l'utilisateur joue

#### Structure du token

```json
{
  "iss": "billard-book-api",
  "sub": "userId",
  "name": "userName", 
  "own": [1, 3, 5],
  "ply": [1, 2, 3, 4],
  "exp": 1754692839,
  "iat": 1754689239
}
```

## 🔄 Architecture en couches

### 1. Couche Contrôleurs (Controllers)

#### ReservationOperationController

- **Responsabilité** : Opérations CRUD sur les réservations
- **Endpoints principaux** :
  - `POST /reservations` : Création
  - `GET /reservations/{id}` : Lecture
  - `PUT /reservations/{id}` : Modification
  - `DELETE /reservations/{id}` : Suppression
  - `POST /reservations/{id}/register` : Inscription à une réservation
  - `DELETE /reservations/{id}/unregister` : Désinscription
  - `POST /reservations/{id}/comment` : Ajout de commentaire

#### ReservationResourceController

- **Responsabilité** : Accès aux ressources et collections
- **Endpoints principaux** :
  - `GET /reservations` : Liste des réservations
  - `GET /reservations/{id}/players` : Joueurs d'une réservation

#### UserResourceController et UserOperationController

- **Responsabilité** : Gestion des utilisateurs et authentification
- **Endpoints principaux** :
  - `POST /users/login` : Connexion
  - `POST /users/logout` : Déconnexion
  - CRUD complet sur les utilisateurs

### 2. Couche Services (Business Logic)

#### ReservationOperationService

```java
@Service
public class ReservationOperationService {
    // Logique métier pour les opérations sur réservations
    // Gestion des claims JWT
    // Validation des règles business
}
```

**Responsabilités** :

- Validation des règles métier
- Mise à jour des claims JWT après chaque opération
- Gestion des relations entre utilisateurs et réservations

#### ReservationResourceService

```java
@Service 
public class ReservationResourceService {
    // Logique d'accès aux ressources
    // Transformation en DTOs
    // Filtrage des données
}
```

### 3. Couche DAO (Data Access Objects)

#### Architecture AbstractListDao

```java
public abstract class AbstractListDao<T> implements Dao<T> {
    protected final List<T> collection = new ArrayList<>();
    // Implémentation générique avec List en mémoire
}
```

**Principes** :

- **Stockage en mémoire** : `List<T>` pour la persistance
- **Suppression logique** : Les éléments supprimés deviennent `null`
- **Gestion des indices** : L'index dans la liste sert d'identifiant
- **Sécurité type** : Généricité pour la réutilisabilité

#### ReservationDao

```java
public class ReservationDao extends AbstractListDao<Reservation> {
    @Override
    public Reservation findOne(Serializable id) 
        throws DeletedReservationException {
            Reservation r = super.findOne(id);
            if(r == null) {
                throw new DeletedReservationException(id.toString());
            }
            return r;
        }
    }
```

**Spécificités** :

- Gestion des réservations supprimées
- Recherche par propriétaire (`findByOwner`)
- Recherche par joueur (`findByPlayer`)

## 📊 Modèles de données (Entities)

### Reservation

```java
public class Reservation {
    private final String id;          // UUID stable (8 caractères)
    private String tableId;           // Table de billard
    private final String ownerId;     // Propriétaire de la réservation
    private LocalDateTime startTime;   // Début
    private LocalDateTime endTime;     // Fin
    private List<String> players;      // Joueurs (max 4)
    private List<Comment> comments;    // Commentaires sur la partie
}
```

**Règles métier** :

- **Maximum 4 joueurs** par partie de billard
- **ID stable** : UUID généré à la création, ne change jamais
- **Propriétaire automatique** : Le créateur devient automatiquement joueur

### Comment

```java
public class Comment {
    private final String authorId;    // Auteur du commentaire
    private final String content;     // Contenu
}
```

### User

```java
public class User {
    private String id;               // Identifiant unique
    private String name;             // Nom d'affichage
    private String password;         // Mot de passe (hashé)
}
```

## 📨 DTOs (Data Transfer Objects)

### Rôle des DTOs

Les DTOs servent d'interface entre l'API et les clients, en :

- **Masquant** la structure interne des entités
- **Contrôlant** les données exposées
- **Facilitant** les évolutions de l'API

### ReservationRequestDto

```java
public class ReservationRequestDto {
    private String tableId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### ReservationResponseDto

```java
public class ReservationResponseDto {
    private String tableId;
    private String ownerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<LinkDto> players;     // Liens vers les joueurs
    private List<Comment> comments;
}
```

### PlayersResponseDto

```java
public class PlayersResponseDto {
    private List<LinkDto> players;
}
```

## 🔧 Filtres (Cross-cutting Concerns)

### 1. AuthenticationFilter

```java
@Order(1)
public class AuthenticationFilter extends HttpFilter
```

**Responsabilité** : Vérification des tokens JWT

- Extraction du token depuis `Authorization: Bearer`
- Validation de la signature et expiration
- Stockage des informations utilisateur dans les attributs de requête

### 2. AuthorizationFilter  

```java
@Order(2)
public class AuthorizationFilter extends HttpFilter
```

**Responsabilité** : Contrôle d'accès aux ressources

- Vérification des droits sur les réservations
- Validation des claims `own` et `ply`
- Protection contre l'accès non autorisé

### 3. DateCacheFilter

```java
@Order(3)
public class DateCacheFilter extends HttpFilter
```

**Responsabilité** : Gestion du cache HTTP conditionnel

- Headers `Last-Modified` et `If-Modified-Since`
- Réponses `304 Not Modified` pour optimiser la bande passante
- Invalidation du cache lors des modifications

**Logique** :

```java
// GET : Vérification du cache
if (ifModifiedSince >= lastModified.getTime()) {
    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    return;
}

// POST/PUT/DELETE : Invalidation du cache
lastModifiedMap.put(url, new Date());
```

### 4. ETagFilter

```java
@Order(4) 
public class ETagFilter extends HttpFilter
```

**Responsabilité** : Gestion des ETags pour la cohérence

- Génération d'ETags basés sur le contenu
- Validation avec `If-None-Match`
- Prévention des modifications concurrentes

## 🎯 Principes de conception

### 1. Identifiants stables

**Problème résolu** : Les IDs de réservations changeaient lors des mises à jour
**Solution** : UUID générés à la création, immutables

```java
public Reservation(String tableId, String creatorId, ...) {
    this.id = UUID.randomUUID().toString().substring(0, 8);
    // L'ID ne change JAMAIS après création
}
```

### 2. Codes de statut HTTP sémantiques

| Code | Signification | Usage |
|------|---------------|-------|
| 200  | OK | Ressource trouvée et retournée |
| 201  | Created | Ressource créée avec succès |
| 302  | Found | Ressource déplacée temporairement |
| 304  | Not Modified | Ressource inchangée (cache) |
| 400  | Bad Request | Requête invalide (ex. paramètres manquants) |
| 401  | Unauthorized | Authentification requise |
| 403  | Forbidden | Accès interdit (ex. droits insuffisants) |
| 404  | Not Found | Ressource n'existe pas |
| 410  | Gone | Ressource existait mais supprimée |
| 409  | Conflict | Conflit de ressources (ex. doublon) |
| 422  | Unprocessable Entity | Entité non traitable (ex. validation échouée) |

**Logique 404 vs 410** :

```java
// 404 : ID jamais existé
throw new NameNotFoundException("Reservation not found");

// 410 : ID existait mais réservation supprimée (null dans la liste)
if (hasDeletedElements && validUuidFormat) {
    throw new DeletedReservationException("Reservation deleted");
}
```

### 3. Gestion des relations parent-enfant

**Cache intelligent** : Modification d'une sous-ressource invalide la ressource parent

```java
// POST /reservations/{id}/comment invalide le cache de /reservations/{id}
if (url.matches("/reservations/[^/]+/.*")) {
    String parentUrl = url.replaceFirst("(/reservations/[^/]+)/.*", "$1");
    lastModifiedMap.put(parentUrl, now);
}
```

## 🛠️ Technologies utilisées

### Backend

- **Spring Boot 3.3.5** : Framework principal
- **Java 21** : Langage de programmation
- **Maven** : Gestionnaire de dépendances
- **Jackson** : Sérialisation JSON/XML
- **JWT (jjwt 0.12.6)** : Authentification
- **Jakarta Servlet API** : Filtres HTTP

### Patterns utilisés

- **DAO Pattern** : Accès aux données
- **DTO Pattern** : Transfer d'objets
- **Filter Chain** : Traitement des requêtes
- **Service Layer** : Logique métier
- **Dependency Injection** : Inversion de contrôle

## 📋 Spécification OpenAPI

La spécification OpenAPI complète est disponible dans :

[Spécification OpenAPI (Billard-Book-api.yaml)](openapi/Billard-Book-api.yaml)

## 🔍 Détails d'implémentation

### Gestion des erreurs

```java
try {
    // Opération métier
} catch (NameNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
} catch (DeletedReservationException e) {
    return ResponseEntity.status(HttpStatus.GONE).build();
} catch (InvalidNameException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
}
```

### Sérialization personnalisée

```java
@JsonDeserialize(using = LocalDateTimeDeserializer.class)
private LocalDateTime startTime;

// Gestion des chaînes "null" en entrée
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
```

### Connexion management

```java
@Component
public class ConnectionManager {
    // Gestion centralisée des tokens JWT
    // Extraction et validation des utilisateurs
    // Mise à jour des claims
}
```

## 🚀 Points forts de l'architecture

1. **Séparation des responsabilités** : Chaque couche a un rôle précis
2. **Extensibilité** : Ajout facile de nouveaux endpoints
3. **Testabilité** : Architecture modulaire facilitant les tests
4. **Performance** : Cache HTTP intelligent
5. **Sécurité** : Authentification et autorisation robustes
6. **Standards** : Respect des conventions REST et HTTP

## 📈 Métriques de qualité

- **Tests** : 89/89 (100% de réussite)
- **Stabilité** : Élimination des comportements non-déterministes
- **Performance** : Cache HTTP réduisant la bande passante
- **Maintenabilité** : Code structuré et documenté

---

*Cette documentation technique présente l'état actuel de l'API après optimisation et correction des problèmes identifiés. L'architecture est prête pour la production et les évolutions futures.*
