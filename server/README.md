# Billiard Table Reservation API

## Overview

This REST API manages billiard table reservations with JWT authentication, player registration, and reservation comments. The architecture follows REST principles with clear separation of concerns across layers.

## High-Level Architecture

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

## Authentication and Authorization

### JWT System (JSON Web Tokens)

#### Principles

- **Authentication**: Verifies user identity
- **Authorization**: Controls access rights on resources
- **Stateless sessions**: No server-side session storage, everything is in the token

#### Implementation

- **Algorithm**: HS512 (HMAC with SHA-512)
- **Library**: `jjwt-api 0.12.6`
- **Custom claims**:
    - `sub`: User identifier
    - `own`: List of reservation indexes owned by user
    - `ply`: List of reservation indexes where user participates

#### Token Structure

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

## Layered Architecture

### 1. Controller Layer

#### ReservationOperationController

- **Responsibility**: Reservation CRUD and actions
- **Main endpoints**:
    - `POST /reservations`: Create
    - `GET /reservations/{id}`: Read
    - `PUT /reservations/{id}`: Update
    - `DELETE /reservations/{id}`: Delete
    - `POST /reservations/{id}/register`: Register to reservation
    - `DELETE /reservations/{id}/unregister`: Unregister from reservation
    - `POST /reservations/{id}/comment`: Add comment

#### ReservationResourceController

- **Responsibility**: Resource and collection access
- **Main endpoints**:
    - `GET /reservations`: Reservation listing
    - `GET /reservations/{id}/players`: Reservation players

#### UserResourceController et UserOperationController

- **Responsibility**: User management and authentication
- **Main endpoints**:
    - `POST /users/login`: Sign in
    - `POST /users/logout`: Sign out
    - Full user CRUD

### 2. Service Layer (Business Logic)

#### ReservationOperationService

```java
@Service
public class ReservationOperationService {
    // Business logic for reservation operations
    // JWT claim management
    // Business rule validation
}
```

**Responsibilities**:

- Validate business rules
- Update JWT claims after each operation
- Manage relationships between users and reservations

#### ReservationResourceService

```java
@Service 
public class ReservationResourceService {
    // Resource access logic
    // DTO transformations
    // Data filtering
}
```

### 3. DAO Layer (Data Access Objects)

#### Architecture AbstractListDao

```java
public abstract class AbstractListDao<T> implements Dao<T> {
    protected final List<T> collection = new ArrayList<>();
    // Generic in-memory list implementation
}
```

**Principles**:

- **In-memory storage**: `List<T>` as persistence layer
- **Soft deletion**: Deleted elements become `null`
- **Index-based identity**: List index is used as identifier
- **Type safety**: Generic abstractions for reuse

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

**Specific behavior**:

- Handles deleted reservations explicitly
- Owner-based lookup (`findByOwner`)
- Player-based lookup (`findByPlayer`)

## Data Models (Entities)

### Reservation

```java
public class Reservation {
    private final String id;           // Stable UUID (8 chars)
    private String tableId;            // Billiard table id
    private final String ownerId;      // Reservation owner
    private LocalDateTime startTime;   // Start time
    private LocalDateTime endTime;     // End time
    private List<String> players;      // Players (max 4)
    private List<Comment> comments;    // Reservation comments
}
```

**Business rules**:

- **Maximum 4 players** per reservation
- **Stable ID**: UUID generated at creation and never changed
- **Automatic owner participation**: creator is automatically included as a player

### Comment

```java
public class Comment {
    private final String authorId;    // Comment author
    private final String content;     // Content
}
```

### User

```java
public class User {
    private String id;               // Unique identifier
    private String name;             // Display name
    private String password;         // Password (hashed)
}
```

## DTOs (Data Transfer Objects)

### DTO Role

DTOs are the API contract boundary and are used to:

- **Hide** internal entity structure
- **Control** exposed payloads
- **Support** safe API evolution

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

## Filters (Cross-Cutting Concerns)

### 1. AuthenticationFilter

```java
@Order(1)
public class AuthenticationFilter extends HttpFilter
```

**Responsibility**: JWT verification

- Extract token from `Authorization: Bearer`
- Validate signature and expiration
- Store user info in request attributes

### 2. AuthorizationFilter  

```java
@Order(2)
public class AuthorizationFilter extends HttpFilter
```

**Responsibility**: resource access control

- Check reservation-level permissions
- Validate `own` and `ply` claims
- Prevent unauthorized access

### 3. DateCacheFilter

```java
@Order(3)
public class DateCacheFilter extends HttpFilter
```

**Responsibility**: conditional HTTP cache management

- `Last-Modified` and `If-Modified-Since` headers
- `304 Not Modified` responses for bandwidth optimization
- Cache invalidation on write operations

**Logic**:

```java
// GET: cache validation
if (ifModifiedSince >= lastModified.getTime()) {
    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    return;
}

// POST/PUT/DELETE: cache invalidation
lastModifiedMap.put(url, new Date());
```

### 4. ETagFilter

```java
@Order(4) 
public class ETagFilter extends HttpFilter
```

**Responsibility**: ETag management for consistency

- Generate content-based ETags
- Validate through `If-None-Match`
- Reduce conflicting updates

## Design Principles

### 1. Stable Identifiers

**Problem solved**: reservation IDs changed after updates
**Solution**: immutable UUID generated at creation time

```java
public Reservation(String tableId, String creatorId, ...) {
    this.id = UUID.randomUUID().toString().substring(0, 8);
    // ID never changes after creation
}
```

### 2. Semantic HTTP Status Codes

| Code | Meaning | Usage |
|------|---------------|-------|
| 200  | OK | Resource found and returned |
| 201  | Created | Resource created successfully |
| 302  | Found | Resource temporarily moved |
| 304  | Not Modified | Resource unchanged (cache hit) |
| 400  | Bad Request | Invalid request (for example, missing parameters) |
| 401  | Unauthorized | Authentication required |
| 403  | Forbidden | Access denied (for example, insufficient rights) |
| 404  | Not Found | Resource does not exist |
| 410  | Gone | Resource existed but was deleted |
| 409  | Conflict | Resource conflict (for example, duplicate) |
| 422  | Unprocessable Entity | Validation failed |

**404 vs 410 logic**:

```java
// 404: ID never existed
throw new NameNotFoundException("Reservation not found");

// 410: ID existed but reservation was deleted (null entry in list)
if (hasDeletedElements && validUuidFormat) {
    throw new DeletedReservationException("Reservation deleted");
}
```

### 3. Parent-Child Resource Consistency

**Smart cache behavior**: updating a subresource invalidates its parent resource cache

```java
// POST /reservations/{id}/comment invalidates /reservations/{id} cache
if (url.matches("/reservations/[^/]+/.*")) {
    String parentUrl = url.replaceFirst("(/reservations/[^/]+)/.*", "$1");
    lastModifiedMap.put(parentUrl, now);
}
```

## Technologies Used

### Backend

- **Spring Boot 3.3.5**: Core framework
- **Java 21**: Programming language
- **Maven**: Dependency management
- **Jackson**: JSON/XML serialization
- **JWT (jjwt 0.12.6)**: Authentication
- **Jakarta Servlet API**: HTTP filters

### Patterns Used

- **DAO Pattern**: Data access
- **DTO Pattern**: Data transfer objects
- **Filter Chain**: Request processing
- **Service Layer**: Business logic
- **Dependency Injection**: Inversion of control

## OpenAPI Specification

The full OpenAPI specification is available here:

[OpenAPI Specification (Billard-Book-api.yaml)](openapi/Billard-Book-api.yaml)

## Implementation Details

### Error Handling

```java
try {
    // Business operation
} catch (NameNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
} catch (DeletedReservationException e) {
    return ResponseEntity.status(HttpStatus.GONE).build();
} catch (InvalidNameException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
}
```

### Custom Serialization

```java
@JsonDeserialize(using = LocalDateTimeDeserializer.class)
private LocalDateTime startTime;

// Handles "null" string values in input
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
```

### Connection Management

```java
@Component
public class ConnectionManager {
    // Central JWT token management
    // User extraction and validation
    // Claim refresh logic
}
```

## Architecture Strengths

1. **Separation of concerns**: each layer has a precise role
2. **Extensibility**: easy to add endpoints and features
3. **Testability**: modular architecture supports isolated testing
4. **Performance**: intelligent HTTP caching strategy
5. **Security**: robust authentication and authorization model
6. **Standards**: aligned with REST and HTTP conventions

## Quality Metrics

- **Tests**: 89/89 (100% pass rate)
- **Stability**: non-deterministic behaviors removed
- **Performance**: HTTP cache reduces bandwidth usage
- **Maintainability**: structured and documented codebase

---

This technical documentation reflects the current optimized API state. The architecture is ready for production-oriented hardening and future evolution.
