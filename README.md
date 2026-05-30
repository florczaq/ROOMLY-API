# ROOMLY — Household Management API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-Enabled-E10098.svg)](https://graphql.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Production-336791.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A GraphQL API for managing shared households — shopping lists, inventories, events, transactions, and member profiles, all scoped to multi-tenant household units.

---

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API](#api)
- [Database Schema](#database-schema)
- [Security](#security)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## Overview

ROOMLY organizes users into **households** — isolated multi-tenant units that own shared resources. A single user account can belong to multiple households, each with its own member profile.

**Core capabilities:**

- **Household management** — create and join households via 6-character join codes, enforce member limits, track ownership
- **Profiles** — per-household identities with customizable avatars and nicknames; one account can hold multiple profiles
- **Shopping lists** — personal and shared lists per household, with product tracking, counts, notes, and timestamps
- **Inventory** — personal and shared inventories per household with the same item model as shopping lists
- **Product lookup** — barcode-based product search backed by the [OpenFoodFacts API](https://world.openfoodfacts.org/) with local caching
- **Events** — household calendar events with start/end times, attendee management, and date-range filtering
- **Transactions** — inter-profile financial records (INCOME/EXPENSE) with sender/recipient tracking
- **Authentication** — JWT access + refresh token flow with email/password and device-only modes

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| API | Spring for GraphQL |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA |
| Database (dev) | H2 in-memory |
| Database (prod) | PostgreSQL |
| HTTP client | Spring WebFlux `WebClient` |
| Caching | Caffeine + custom LFU cache |
| Build | Gradle 8.x |
| Utilities | Lombok |

---

## Architecture

### Entity relationships

```
Account (1) ──→ (N) Profile (N) ──→ (1) Household
                    │                       │
                    │                       ├──→ (N) ShoppingList
                    │                       │         └──→ (N) ShoppingListItem ──→ Product
                    │                       │
                    │                       ├──→ (N) Inventory
                    │                       │         └──→ (N) InventoryItem ──→ Product
                    │                       │
                    │                       └──→ (N) Event (M:M) Profile (attendees)
                    │
                    └──→ Transaction (sender / recipient)
```

### Key design decisions

**Account vs Profile separation** — `Account` handles authentication; `Profile` is the functional identity inside a household. This allows one user to participate in multiple households with different nicknames and avatars.

**Shared vs personal resources** — Shopping lists and inventories use an optional `owner` field.
- `owner = null` → shared with the entire household
- `owner != null` → personal to that profile

**Join code normalization** — Join codes are stored and looked up in uppercase. Input is normalized automatically, so lookups are case-insensitive.

**Cascade rules** — `ShoppingList → ShoppingListItem` uses `CascadeType.ALL` with `orphanRemoval = true`, so deleting a list removes its items.

**Custom scalars** — A `DateTime` scalar handles ISO-8601 datetimes for event scheduling.

---

## Getting Started

### Prerequisites

- Java 21+
- Gradle 8.x
- PostgreSQL (production) or no database setup required for development (H2 in-memory)

### Running locally (H2 / development)

```bash
git clone <repository-url>
cd ROOMLY-API

# Set required environment variable
export JWT_SECRET_KEY=your-secret-key-minimum-256-bits

./gradlew bootRun
```

The application starts on `http://localhost:8080`.

- GraphQL endpoint: `http://localhost:8080/graphql`
- GraphiQL interface: `http://localhost:8080/graphiql`

### Running with Docker Compose

```bash
docker compose up
```

This starts the API together with a PostgreSQL instance using the configuration in `compose.yaml`.

### Running with PostgreSQL

Set the `docker` Spring profile or configure `application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/roomlydb
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Environment variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET_KEY` | yes | HMAC secret for signing JWTs, minimum 256 bits |

---

## API

ROOMLY exposes a GraphQL API with **14 queries** and **13 mutations**.

All requests are `POST /graphql`. Most operations require a JWT access token:

```
Authorization: Bearer <access_token>
```

Obtain a token via the REST authentication endpoints (see [Security](#security)).

### Operations at a glance

**Queries**

| Query | Auth | Description |
|---|---|---|
| `household(householdId)` | required | Get a household by ID |
| `households` | required | List all households for the authenticated user |
| `householdByJoinCode(joinCode)` | — | Look up a household by join code |
| `profile(profileId)` | required | Get a profile with its inventory and shopping list |
| `shoppingList(id)` | required | Get a shopping list by ID |
| `allShoppingLists(householdId)` | required | List all shopping lists in a household |
| `inventory(id)` | required | Get an inventory by ID |
| `allInventories(householdId)` | required | List all inventories in a household |
| `product(barcode)` | — | Look up a product by barcode |
| `availableAvatarsAndColors` | — | List all avatar and color options |
| `events(householdId, from?, to?)` | required | List household events with optional date filter |
| `event(eventId)` | required | Get a single event |
| `eventsForProfile(profileId, from?, to?)` | required | List events for a profile |
| `transactions(householdId)` | required | List all transactions in a household |

**Mutations**

| Mutation | Auth | Description |
|---|---|---|
| `createHousehold(...)` | required | Create a household and initial profile |
| `joinHousehold(...)` | required | Join a household via join code |
| `updateProfile(...)` | required | Update profile nickname or avatar |
| `leaveHousehold(profileId)` | required | Remove a profile from its household |
| `addProductToShoppingList(...)` | required | Add or increment a product in a shopping list |
| `removeProductFromShoppingList(...)` | required | Remove or decrement a product in a shopping list |
| `addProductToInventory(...)` | required | Add or increment a product in an inventory |
| `removeProductFromInventory(...)` | required | Remove or decrement a product in an inventory |
| `addEvent(...)` | required | Create a household event |
| `updateEvent(...)` | required | Update an existing event |
| `deleteEvent(eventId)` | required | Delete an event |
| `addTransaction(...)` | required | Record a transaction between profiles |
| `deleteTransaction(transactionId)` | required | Delete a transaction |

For full argument lists, return types, and examples see:

- [docs/queries.md](docs/queries.md)
- [docs/mutations.md](docs/mutations.md)
- [docs/dto.md](docs/dto.md)

### Quick example

```graphql
# Create a household
mutation CreateHousehold {
    createHousehold(
        name: "My Family"
        membersLimit: 6
        nickname: "Dad"
        avatarName: "DOG_WHITE"
        avatarColorName: "BLUE"
    ) {
        id
        joinCode
    }
}

# Another user joins
mutation JoinHousehold {
    joinHousehold(
        joinCode: "ABC123"
        nickname: "Mum"
        avatarName: "DOG_WHITE"
        avatarColorName: "RED"
    ) {
        id
        nickname
    }
}

# Look up the household before joining (no auth needed)
query GetHouseholdByJoinCode {
    householdByJoinCode(joinCode: "ABC123") {
        name
        membersCount
        membersLimit
    }
}
```

---

## Database Schema

### Entities

| Entity | Description |
|---|---|
| `Account` | User credentials and authentication |
| `RefreshToken` | Stored refresh tokens linked to accounts |
| `Profile` | User identity within a specific household |
| `Household` | The central organizational unit |
| `ShoppingList` | A list of products to buy (personal or shared) |
| `ShoppingListItem` | A product entry in a shopping list |
| `Inventory` | A stored product collection (personal or shared) |
| `InventoryItem` | A product entry in an inventory |
| `Product` | Product catalog with barcode support |
| `Event` | A scheduled household event with attendees |
| `Transaction` | A financial record between two profiles |

### Enumerations

```java
enum TransactionType  { INCOME, EXPENSE }
enum ProductInfoSource { EXTERNAL_API, MANUAL_ENTRY }
enum QuantityUnits    { PIECE, GRAM, KILOGRAM, LITER, MILLILITER, CUP, TABLESPOON, TEASPOON }
enum AuthProvider     { EMAIL_PASSWORD, DEVICE_ONLY }
```

---

## Security

### Authentication

The API uses two authentication modes, both issuing JWT access and refresh tokens:

- **Email/password** — standard credential-based login via `POST /auth/login`
- **Device-only** — passwordless device authentication via `POST /auth/device`

Register a new account at `POST /auth/register`.

### Token lifecycle

| Token | Expiry | Storage |
|---|---|---|
| Access token | 1 hour | Client-side |
| Refresh token | 30 days | Database |

Refresh tokens rotate on use. Expired tokens are evicted by a scheduled job.

### Configuration

```properties
security.jwt.secret=${JWT_SECRET_KEY}
security.jwt.access.token.expiration=3600000    # 1 hour in ms
security.jwt.refresh.token.expiration=2592000000 # 30 days in ms
```

All GraphQL operations annotated with `@PreAuthorize("isAuthenticated()")` reject requests without a valid access token. A small number of operations (product lookup, avatar list, household join-code preview) are intentionally public.

---

## Testing

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "org.roomly.integrationTests.HouseholdIntegrationTest"
```

HTML reports: `build/reports/tests/test/index.html`

The test suite includes integration tests that exercise the full Spring context against an H2 database, covering household operations, avatar loading, cache eviction, and application context startup.

---

## Project Structure

```
src/
├── main/java/org/roomly/
│   ├── annotations/        # Custom validation annotations (@ValidBarcode, @Notifiable)
│   ├── assets/             # Avatar and color catalog JSON files
│   ├── cache/              # Custom LFU cache implementation
│   ├── config/             # Spring configuration (GraphQL, cache, Jackson, WebClient)
│   ├── controllers/        # REST controllers (avatar serving)
│   ├── dto/                # Data Transfer Objects (Java records)
│   ├── entities/           # JPA entities
│   ├── enums/              # Enumerations
│   ├── generators/         # ID and join code generators
│   ├── notifications/      # Notification entity and service
│   ├── repositories/       # Spring Data JPA repositories
│   ├── resolvers/          # GraphQL resolvers
│   ├── security/           # JWT, authentication controllers and services
│   ├── services/           # Business logic
│   └── utils/              # Avatar and color utilities
└── main/resources/
    ├── graphql/schema.graphqls
    └── application*.properties

docs/
├── queries.md              # All GraphQL queries documented
├── mutations.md            # All GraphQL mutations documented
└── dto.md                  # All DTOs and their fields
```

---

## Contributing

1. Follow standard Java naming conventions
2. Use Lombok to reduce boilerplate
3. Write integration tests for new GraphQL operations
4. Keep resolvers thin — delegate logic to service classes
5. Ensure the GraphQL schema and resolver signatures stay in sync

---

## Authors

**Florczak Mikołaj** — project creator and developer

---

## Acknowledgments

- [OpenFoodFacts](https://world.openfoodfacts.org/) — open product database
- [Spring for GraphQL](https://spring.io/projects/spring-graphql) — GraphQL integration
- [jjwt](https://github.com/jwtk/jjwt) — JWT implementation

---

## License

MIT License — see [LICENSE](LICENSE) for details.
