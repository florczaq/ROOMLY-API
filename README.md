# 🏠 ROOMLY - Household Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-Enabled-E10098.svg)](https://graphql.org/)
[![H2 Database](https://img.shields.io/badge/H2-In--Memory-blue.svg)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **A comprehensive household management platform** built with Spring Boot and GraphQL, featuring multi-tenant organization, shopping lists, inventory tracking, event scheduling, and transaction management with customizable user profiles.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [What's Implemented](#-whats-implemented)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)

---

## 🎯 Overview

**ROOMLY** is a modern household management system built with Spring Boot and GraphQL. The platform enables multiple users to collaborate within households, managing shared resources, tracking expenses, coordinating events, and maintaining shopping lists and inventories.

### Current Implementation

The project has a comprehensive implementation with:
- **Multi-tenant household system** with secure join codes and member management
- **User profile management** with customizable avatars and colors
- **Product lookup** integration with OpenFoodFacts API
- **Shopping list management** with item tracking and notes
- **Inventory management** with item tracking and timestamps
- **Event scheduling** with attendees, date filtering, and CRUD operations
- **Transaction tracking** for household financial management (add/delete)
- **Complete database schema** with 11 interconnected entities
- **JWT-based authentication** with refresh token support
- **Comprehensive GraphQL API** with 13 queries and 13 mutations

### Core Architecture

- **Households**: Central organizational units that group users and resources
- **Profiles**: User representations within households (one user can have multiple profiles)
- **Multi-tenancy**: Support for users participating in multiple households
- **Shared Resources**: Shopping lists and inventories can be personal or shared
- **Event Coordination**: Calendar events with attendee management
- **Financial Tracking**: Inter-profile transactions with type classification
- **Security**: JWT access and refresh tokens with Spring Security

---

## ✨ Current Features

### 🏡 Household Management
- ✅ Create households with custom join codes (6 characters)
- ✅ Join existing households using join codes
- ✅ Set member limits (1-30 members)
- ✅ Household ownership tracking
- ✅ Retrieve household information via GraphQL
- ✅ List all households
- ✅ View household members and owner

### 👤 Profile & Avatar System
- ✅ Create user profiles with customizable avatars
- ✅ Avatar name and color selection from predefined sets
- ✅ Retrieve available avatars and colors via GraphQL
- ✅ Nickname assignment per household
- ✅ Profile association with households
- ✅ Support for multiple profiles per user account
- ✅ Update profile information (nickname, avatar, color)
- ✅ Get individual profile details
- 🚧 Leave household functionality (schema defined)

### 🔍 Product Lookup
- ✅ Barcode-based product search via GraphQL
- ✅ Integration with OpenFoodFacts API
- ✅ Retrieve product details (name, brand, quantity)
- ✅ Get product by barcode query

### 🛒 Shopping List Management
- ✅ GraphQL schema for shopping lists defined
- ✅ Query individual shopping list by ID
- ✅ Query all shopping lists for a household
- ✅ Add products to shopping list by product and list ID
- ✅ Shopping list items with count tracking
- ✅ Notes support for shopping list items
- ✅ Timestamp tracking for added items

### 📦 Inventory Management
- ✅ GraphQL schema for inventory defined
- ✅ Query individual inventory by ID
- ✅ Query all inventories for a household
- ✅ Add products to inventory by product and inventory ID
- ✅ Inventory items with count tracking
- ✅ Notes support for inventory items
- ✅ Timestamp tracking for added items

### 📅 Event Management
- ✅ GraphQL schema for events defined
- ✅ Query events for a household with date range filtering
- ✅ Query individual event details
- ✅ Query events for a specific profile
- ✅ Create new events with start/end times
- ✅ Update existing events
- ✅ Delete events
- ✅ Event attendee tracking
- ✅ Event creator tracking

### 💰 Transaction Management
- ✅ GraphQL schema for transactions defined
- ✅ Query all transactions for a household
- ✅ Add new transactions
- ✅ Delete transactions
- ✅ Track sender and recipient profiles
- ✅ Transaction types (INCOME/EXPENSE)
- ✅ Amount and timestamp tracking

### 🗄️ Database & GraphQL Schema
All entities are fully implemented and exposed via GraphQL API:
- ✅ Account (authentication)
- ✅ Profile (user identity within household) - with GraphQL queries/mutations
- ✅ Household (organizational unit) - with GraphQL queries/mutations
- ✅ RefreshToken (JWT security)
- ✅ Shopping Lists (personal and shared) - with GraphQL queries/mutations
- ✅ Shopping List Items with timestamp tracking - with GraphQL mutations
- ✅ Products with barcode support - with GraphQL queries
- ✅ Inventory (personal and shared) - with GraphQL queries/mutations
- ✅ Inventory Items with timestamp tracking - with GraphQL mutations
- ✅ Events with attendee management - with GraphQL queries/mutations
- ✅ Transactions (financial tracking) - with GraphQL queries/mutations (add & delete)
- ⚠️ Notifications (basic structure) - entity only, no GraphQL API yet

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **API**: GraphQL (Spring for GraphQL)
- **Database**: H2 (in-memory, development)
- **Database Driver**: PostgreSQL (included for production use)
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle 8.x

### Key Dependencies
- Spring Boot Starter Web MVC
- Spring Boot Starter GraphQL
- GraphQL Java Extended Scalars (DateTime support)
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter WebFlux (for external API calls)
- Spring Boot Starter Cache + Caffeine
- JWT (io.jsonwebtoken:jjwt 0.12.6)
- Lombok
- H2 Database (development)
- PostgreSQL Driver (production)

---

## 🏗️ Architecture

### Entity Relationship Overview

```
Account (1) ──→ (N) Profile (N) ──→ (1) Household
                    │                       │
                    │                       ├──→ (N) ShoppingList
                    │                       │         └──→ (N) ShoppingListItem ──→ Product
                    │                       │
                    │                       ├──→ (N) Inventory
                    │                       │
                    │                       └──→ (N) Event (M:M) Profile (attendees)
                    │
                    └──→ Transactions (sender/recipient)
```

### Key Design Patterns

- **Multi-Tenancy**: Household-based data isolation
- **Separation of Concerns**: Account (auth) vs Profile (functionality)
- **Shared vs Personal Resources**: Owner-based resource sharing
  - `owner = null` → Shared with entire household
  - `owner != null` → Personal resource
- **Cascade Operations**: ShoppingList → ShoppingListItem (ALL, orphanRemoval)
- **Many-to-Many Relations**: Event ↔ Profile (attendees)
- **DateTime Support**: Custom scalar type for event scheduling and timestamps
- **Comprehensive GraphQL Schema**: All major features exposed via GraphQL API

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x
- (Optional) PostgreSQL for production

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd roomly
   ```

2. **Set up environment variables**
   
   Create a `.env` file in the root directory or set environment variables:
   ```properties
   JWT_SECRET_KEY=your-secret-key-here-minimum-256-bits
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the GraphQL API**
   - GraphQL Endpoint: `http://localhost:8080/graphql`
   - GraphiQL Interface: `http://localhost:8080/graphiql` (if enabled in configuration)

### Development Configuration

The application uses H2 in-memory database for development:
```properties
spring.datasource.url=jdbc:h2:mem:roomlydb
spring.jpa.hibernate.ddl-auto=create-drop
```

PostgreSQL driver is included and can be configured in `application.properties` for production deployment.

---

## 📖 API Documentation

### GraphQL API

The application exposes a comprehensive GraphQL API with 13 queries and 13 mutations covering all major household management operations including households, profiles, shopping lists, inventory, events, and transactions.

#### Available Queries

```graphql
type Query {
    # Household queries
    household(householdId: String!): Household!
    households: [Household]!
    
    # Profile queries
    profile(profileId: String!): Profile!
    
    # Shopping list queries
    shoppingList(id: Int!): ShoppingList!
    allShoppingLists(householdId: String!): [ShoppingList]!
    
    # Inventory queries
    inventory(id: Int!): Inventory!
    allInventories(householdId: String!): [Inventory]!
    
    # Product queries
    product(barcode: String!): Product
    
    # Avatar and color queries
    availableAvatarsAndColors: AvatarsAndColors!
    
    # Event queries
    events(householdId: String!, from: DateTime, to: DateTime): [Event]!
    event(eventId: Int!): Event!
    eventsForProfile(profileId: String!, from: DateTime, to: DateTime): Event!
    
    # Transaction queries
    transactions(householdId: String!): [Transaction]!
}
```

#### Available Mutations

```graphql
type Mutation {
    # Household mutations
    createHousehold(
        name: String!
        membersLimit: Int!
        nickname: String!
        avatarName: String!
        avatarColorName: String!
    ): Household!
    joinHousehold(
        nickname: String!
        avatarName: String!
        avatarColorName: String!
        joinCode: String!
    ): Profile!
    
    # Profile mutations
    updateProfile(
        profileId: String!
        nickname: String
        avatarName: String
        avatarColorName: String
    ): Profile!
    leaveHousehold(profileId: String!): Boolean!
    
    # Shopping list mutations
    addProductToShoppingList(
        productId: Int!
        shoppingListId: Int!
        count: Int!
        notes: String
    ): ShoppingListItem!
    
    # Inventory mutations
    addProductToInventory(
        productId: Int!
        inventoryId: Int!
        count: Int!
        notes: String
    ): InventoryItem!
    
    # Event mutations
    addEvent(
        name: String!
        description: String
        startTime: DateTime!
        endTime: DateTime!
    ): Event!
    updateEvent(
        eventId: Int!
        name: String
        description: String
        startTime: DateTime
        endTime: DateTime
    ): Event!
    deleteEvent(eventId: Int!): Boolean!
    
    # Transaction mutations
    addTransaction(
        title: String!
        amount: Float!
        recipientId: String!
        type: String!
    ): Transaction!
    deleteTransaction(transactionId: Int!): Boolean!
}
```

#### GraphQL Types

```graphql
scalar DateTime

type Household {
    id: String!
    name: String!
    joinCode: String!
    membersLimit: Int!
    owner: Profile
    members: [Profile]!
    sharedInventory: Inventory!
    sharedShoppingList: ShoppingList!
    membersCount: Int!
}

type Profile {
    id: String!
    nickname: String!
    avatar: Avatar!
    inventory: Inventory!
    shoppingList: ShoppingList!
}

type Avatar {
    name: String!
    colorName: String!
    colorHex: String!
}

type AvatarsAndColors {
    avatars: [String]!
    colors: [Color]!
}

type Color {
    name: String!
    hex: String!
}

type Product {
    id: Int!
    barcode: String
    name: String!
    brand: String!
    quantity: String!
}

type ShoppingList {
    id: Int!
    items: [ShoppingListItem]!
}

type ShoppingListItem {
    id: Int!
    product: Product!
    count: Int!
    addedAt: String!
    notes: String
}

type Inventory {
    id: Int!
    items: [InventoryItem]!
}

type InventoryItem {
    id: Int!
    product: Product!
    count: Int!
    addedAt: String!
    notes: String
}

type Event {
    id: Int!
    name: String!
    description: String
    startTime: DateTime!
    endTime: DateTime!
    householdId: String!
    creator: Profile!
    attendees: [Profile]!
}

type Transaction {
    id: Int!
    title: String!
    sendAt: String!
    amount: Float!
    sender: Profile!
    recipient: Profile!
    type: String!
}
```

### Example GraphQL Operations

#### Household Operations

**Create a Household**

```graphql
mutation {
    createHousehold(
        name: "My Family"
        membersLimit: 10
        nickname: "John"
        avatarName: "avatar1"
        avatarColorName: "blue"
    ) {
        id
        name
        joinCode
        membersLimit
        membersCount
    }
}
```

**Join a Household**

```graphql
mutation {
    joinHousehold(
        nickname: "Jane"
        avatarName: "avatar2"
        avatarColorName: "red"
        joinCode: "ABC123"
    ) {
        id
        nickname
        avatar {
            name
            colorName
            colorHex
        }
    }
}
```

**Get Household Information**

```graphql
query {
    household(householdId: "household-id") {
        id
        name
        joinCode
        membersLimit
        membersCount
        owner {
            id
            nickname
        }
        members {
            id
            nickname
            avatar {
                name
                colorName
                colorHex
            }
        }
    }
}
```

**Get All Households**

```graphql
query {
    households {
        id
        name
        membersCount
        membersLimit
    }
}
```

#### Profile Operations

**Get Profile**

```graphql
query {
    profile(profileId: "profile-id") {
        id
        nickname
        avatar {
            name
            colorName
            colorHex
        }
        inventory {
            id
            items {
                id
                product {
                    name
                }
                count
            }
        }
        shoppingList {
            id
            items {
                id
                product {
                    name
                }
                count
                addedAt
            }
        }
    }
}
```

**Update Profile**

```graphql
mutation {
    updateProfile(
        profileId: "profile-id"
        nickname: "NewNickname"
        avatarName: "avatar3"
        avatarColorName: "green"
    ) {
        id
        nickname
        avatar {
            name
            colorName
            colorHex
        }
    }
}
```

#### Shopping List Operations

**Get Shopping List**

```graphql
query {
    shoppingList(id: 1) {
        id
        items {
            id
            product {
                name
                brand
                barcode
            }
            count
            addedAt
            notes
        }
    }
}
```

**Get All Shopping Lists for Household**

```graphql
query {
    allShoppingLists(householdId: "household-id") {
        id
        items {
            id
            product {
                name
            }
            count
            addedAt
        }
    }
}
```

**Add Product to Shopping List**

```graphql
mutation {
    addProductToShoppingList(
        productId: 123
        shoppingListId: 1
        count: 2
        notes: "Get organic if available"
    ) {
        id
        product {
            name
            brand
        }
        count
        addedAt
        notes
    }
}
```

#### Inventory Operations

**Get Inventory**

```graphql
query {
    inventory(id: 1) {
        id
        items {
            id
            product {
                name
                brand
            }
            count
            addedAt
            notes
        }
    }
}
```

**Get All Inventories for Household**

```graphql
query {
    allInventories(householdId: "household-id") {
        id
        items {
            id
            product {
                name
            }
            count
        }
    }
}
```

**Add Product to Inventory**

```graphql
mutation {
    addProductToInventory(
        productId: 123
        inventoryId: 1
        count: 5
        notes: "Stored in pantry"
    ) {
        id
        product {
            name
            brand
        }
        count
        addedAt
        notes
    }
}
```

#### Product Operations

**Get Product by Barcode**

```graphql
query {
    product(barcode: "3017620422003") {
        id
        barcode
        name
        brand
        quantity
    }
}
```

#### Avatar and Color Operations

**Get Available Avatars and Colors**

```graphql
query {
    availableAvatarsAndColors {
        avatars
        colors {
            name
            hex
        }
    }
}
```

#### Event Operations

**Get Events for Household**

```graphql
query {
    events(
        householdId: "household-id"
        from: "2026-04-01T00:00:00Z"
        to: "2026-04-30T23:59:59Z"
    ) {
        id
        name
        description
        startTime
        endTime
        creator {
            id
            nickname
        }
        attendees {
            id
            nickname
        }
    }
}
```

**Get Single Event**

```graphql
query {
    event(eventId: 1) {
        id
        name
        description
        startTime
        endTime
        creator {
            nickname
        }
        attendees {
            nickname
        }
    }
}
```

**Add Event**

```graphql
mutation {
    addEvent(
        name: "Family Dinner"
        description: "Monthly family gathering"
        startTime: "2026-05-01T18:00:00Z"
        endTime: "2026-05-01T21:00:00Z"
    ) {
        id
        name
        description
        startTime
        endTime
    }
}
```

**Update Event**

```graphql
mutation {
    updateEvent(
        eventId: 1
        name: "Updated Event Name"
        startTime: "2026-05-01T19:00:00Z"
    ) {
        id
        name
        startTime
        endTime
    }
}
```

**Delete Event**

```graphql
mutation {
    deleteEvent(eventId: 1)
}
```

#### Transaction Operations

**Get Transactions for Household**

```graphql
query {
    transactions(householdId: "household-id") {
        id
        title
        amount
        sendAt
        type
        sender {
            id
            nickname
        }
        recipient {
            id
            nickname
        }
    }
}
```

**Add Transaction**

```graphql
mutation {
    addTransaction(
        title: "Groceries"
        amount: 45.50
        recipientId: "recipient-profile-id"
        type: "EXPENSE"
    ) {
        id
        title
        amount
        sendAt
        type
        sender {
            nickname
        }
        recipient {
            nickname
        }
    }
}
```

**Delete Transaction**

```graphql
mutation {
    deleteTransaction(transactionId: 1)
}
```

---

## 🗄️ Database Schema

### Implemented Entities (11 Tables)

#### Core Entities
1. **Account** - User authentication and authorization
2. **Profile** - User profiles within households
3. **Household** - Household groupings
4. **RefreshToken** - JWT refresh token management

#### Feature Entities
1. **ShoppingList** - Shopping lists (personal/shared)
2. **ShoppingListItem** - Items in shopping lists
3. **Product** - Product catalog with barcode support
4. **Inventory** - Inventory management (personal/shared)
5. **Event** - Calendar events with attendees
6. **Transaction** - Financial transactions between profiles
7. **Notification** - User notifications

### Entity Details

#### ShoppingList
- Belongs to a Household
- Optional owner (null = shared with household)
- Contains multiple ShoppingListItems
- Cascade: ALL, orphanRemoval=true

#### ShoppingListItem
- References a Product
- Tracks count and timestamps
- Added by user tracking
- Optional notes

#### Product
- Unique barcode
- Name, brand, quantity, unit
- Info source (EXTERNAL_API or MANUAL_ENTRY)
- Supports multiple quantity units (PIECE, GRAM, KILOGRAM, LITER, etc.)

#### Inventory
- Belongs to a Household
- Optional owner (null = shared with household)
- Named inventories (e.g., "Pantry", "Fridge")
- Entity structure ready for inventory items

#### Event
- Belongs to a Household
- Has a creator (Profile)
- Name, description, start/end times
- Many-to-Many relationship with Profiles (attendees)
- Join table: event_attendees

#### Transaction
- Sender and recipient (both Profiles)
- Title, amount, timestamp
- Transaction type (INCOME or EXPENSE)
- Financial tracking between household members

### Enumerations

```java
// Authentication method
enum AuthProvider {
    EMAIL_PASSWORD,
    DEVICE_ONLY
}

// Transaction types
enum TransactionType {
    INCOME,
    EXPENSE
}

// Product information source
enum ProductInfoSource {
    EXTERNAL_API,
    MANUAL_ENTRY
}

// Quantity units
enum QuantityUnits {
    PIECE, GRAM, KILOGRAM, LITER, MILLILITER,
    CUP, TABLESPOON, TEASPOON
}
```

### Key Relationships

- Account (1) → (N) Profile
- Profile (N) → (1) Household
- Household (1) → (1) Profile (owner)
- Household (1) → (N) ShoppingList
- Household (1) → (N) Inventory
- Household (1) → (N) Event
- ShoppingList (1) → (N) ShoppingListItem
- ShoppingListItem (N) → (1) Product
- Event (M) ↔ (N) Profile (attendees)
- Profile (sender) → (N) Transaction
- Profile (recipient) → (N) Transaction

---

## 🔐 Security

### Authentication Mechanisms

- **Email/Password**: Traditional authentication
- **Device-Only**: Device-based authentication without email

### JWT Implementation

- **Access Token**: Short-lived (1 hour)
- **Refresh Token**: Long-lived (30 days)
- **Secure Storage**: Refresh tokens stored in database
- **Token Rotation**: Refresh token rotation on use

### Security Configuration

```properties
security.jwt.refresh.token.expiration=2592000000  # 30 days
security.jwt.access.token.expiration=3600000       # 1 hour
security.jwt.secret=${JWT_SECRET_KEY}
```

### Multi-Device Support

- Account-based device tracking
- Multiple devices per account
- Device ID storage using @ElementCollection

---

## 🧪 Testing

### Current Test Coverage

The project includes integration tests for implemented features:

```
src/test/java/org/roomly/
├── RoomlyApplicationTests.java           # Application context loading
└── integrationTests/
    ├── AvatarsIntegrationTest.java      # Avatar system functionality
    └── HouseholdIntegrationTest.java    # Household operations
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.roomly.integrationTests.HouseholdIntegrationTest"

# Generate test reports
./gradlew test
```

Test reports are generated in: `build/reports/tests/test/index.html`

---

## 🎯 What's Implemented

### Core Foundation ✅
- ✅ Project setup and configuration
- ✅ Database schema design with 11 entities
- ✅ Comprehensive GraphQL API with Spring Boot
- ✅ JWT-based authentication and security
- ✅ Multi-tenant household system
- ✅ H2 in-memory database for development
- ✅ PostgreSQL driver ready for production

### Household Management ✅
- ✅ Create households with custom 6-character join codes
- ✅ Join existing households using join codes
- ✅ Set member limits (1-30 members)
- ✅ Household ownership tracking
- ✅ GraphQL queries for household(s)
- ✅ View household members and shared resources

### Profile & Avatar System ✅
- ✅ Create user profiles with customizable avatars
- ✅ Avatar name and color selection (predefined sets)
- ✅ Retrieve available avatars and colors
- ✅ Nickname assignment per household
- ✅ Multiple profiles per user account
- ✅ Profile query and update mutations

### Product Lookup ✅
- ✅ Product barcode lookup
- ✅ Integration with OpenFoodFacts API
- ✅ Product entity with barcode support
- ✅ GraphQL query for product retrieval

### Shopping Lists ✅
- ✅ Shopping list entity model
- ✅ Shopping list items with product references
- ✅ GraphQL queries (single and all lists)
- ✅ Add items to shopping list mutation (with list ID)
- ✅ Count tracking
- ✅ Item notes and timestamps

### Inventory ✅
- ✅ Inventory entity model
- ✅ Inventory items with product references
- ✅ GraphQL queries (single and all inventories)
- ✅ Add items to inventory mutation (with inventory ID)
- ✅ Count tracking
- ✅ Item notes and timestamps

### Events ✅
- ✅ Event entity model with attendees
- ✅ GraphQL queries with date filtering
- ✅ Add, update, and delete event mutations
- ✅ Event creator and attendee tracking
- ✅ DateTime support for event scheduling
- ✅ Profile-specific event queries

### Transactions ✅
- ✅ Transaction entity model
- ✅ GraphQL query for household transactions
- ✅ Add transaction mutation
- ✅ Delete transaction mutation
- ✅ Sender/recipient profile tracking
- ✅ Transaction types (INCOME/EXPENSE)
- ✅ Amount and timestamp tracking

---

## 📁 Project Structure

```
ROOMLY/
├── src/
│   ├── main/
│   │   ├── java/org/roomly/
│   │   │   ├── RoomlyApplication.java
│   │   │   ├── assets/              # Avatar and color assets
│   │   │   ├── config/              # Spring configuration
│   │   │   ├── controllers/         # REST controllers
│   │   │   │   └── AvatarController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entities/            # JPA entities
│   │   │   │   ├── Event.java
│   │   │   │   ├── Household.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── InventoryItem.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Profile.java
│   │   │   │   ├── ShoppingList.java
│   │   │   │   ├── ShoppingListItem.java
│   │   │   │   └── Transaction.java
│   │   │   ├── enums/               # Enumerations
│   │   │   ├── generators/          # ID generators
│   │   │   ├── notifications/       # Notification system
│   │   │   ├── repositories/        # Data repositories
│   │   │   │   ├── EventsRepository.java
│   │   │   │   ├── HouseholdRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   ├── ProductsRepository.java
│   │   │   │   ├── ProfileRepository.java
│   │   │   │   ├── ShoppingListItemRepository.java
│   │   │   │   ├── ShoppingListRepository.java
│   │   │   │   └── TransactionsRepository.java
│   │   │   ├── resolvers/           # GraphQL resolvers
│   │   │   │   ├── AvatarsResolver.java
│   │   │   │   ├── EventsResolver.java
│   │   │   │   ├── HouseholdResolver.java
│   │   │   │   ├── InventoryResolver.java
│   │   │   │   ├── ProductsResolver.java
│   │   │   │   ├── ProfileResolver.java
│   │   │   │   ├── ShoppingListResolver.java
│   │   │   │   └── TransactionsResolver.java
│   │   │   ├── security/            # Security & authentication
│   │   │   ├── cache/               # Custom LFU cache
│   │   │   │   ├── FrequencyTracker.java
│   │   │   │   ├── LfuCache.java
│   │   │   │   └── LfuCacheManager.java
│   │   │   └── services/            # Business logic
│   │   │       ├── AvatarService.java
│   │   │       ├── ColorsService.java
│   │   │       ├── EventsService.java
│   │   │       ├── ExternalApiService.java
│   │   │       ├── HouseholdOrchestrationService.java
│   │   │       ├── HouseholdService.java
│   │   │       ├── InventoryService.java
│   │   │       ├── ProductsService.java
│   │   │       ├── ProfileService.java
│   │   │       ├── ShoppingListItemService.java
│   │   │       ├── ShoppingListService.java
│   │   │       └── TransactionsService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── graphql/
│   │       │   └── schema.graphqls   # GraphQL schema
│   │       └── static/
│   └── test/
│       └── java/org/roomly/
│           ├── RoomlyApplicationTests.java
│           ├── cache/
│           │   └── FrequencyTrackerTest.java
│           └── integrationTests/
│               ├── AvatarsIntegrationTest.java
│               ├── CacheEvictionTest.java
│               └── HouseholdIntegrationTest.java
├── build.gradle                      # Gradle build configuration
├── settings.gradle
├── gradlew                          # Gradle wrapper (Unix)
├── gradlew.bat                      # Gradle wrapper (Windows)
├── compose.yaml                     # Docker Compose configuration
├── ERD_DIAGRAM.txt                  # Entity Relationship Diagram
└── README.md                        # This file
```

---

## 🤝 Contributing

This project is currently in active development. Contributions, issues, and feature requests are welcome.

### Development Guidelines

- Follow Java naming conventions
- Use Lombok annotations for boilerplate code
- Write unit tests for new features
- Document public APIs with Javadoc
- Keep methods small and focused
- Ensure GraphQL schema matches implementation

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Florczak Mikołaj** - Project creator and developer

---

## 🙏 Acknowledgments

- [OpenFoodFacts](https://world.openfoodfacts.org/) - Product data API
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [GraphQL Java](https://www.graphql-java.com/) - GraphQL implementation

---

## 📞 Support

For questions and support, please contact the project maintainer or open an issue in the project repository.

---

## 📊 Project Status

**Current Version**: 1.0.0  
**Status**: 🚧 In Active Development  
**Last Updated**: May 3, 2026

### Implementation Status

| Feature              | Backend | GraphQL API | Status      |
|----------------------|---------|-------------|-------------|
| Household Management | ✅       | ✅           | Complete    |
| Profile & Avatars    | ✅       | ✅           | Complete    |
| Product Lookup       | ✅       | ✅           | Complete    |
| Shopping Lists       | ✅       | ✅           | Complete    |
| Inventory            | ✅       | ✅           | Complete    |
| Events               | ✅       | ✅           | Complete    |
| Transactions         | ✅       | ✅           | Complete    |
| Notifications        | ✅       | ❌           | Entity Only |

**Note**: "Complete" indicates that both the backend entities and GraphQL schema are fully defined. Some operations marked with TODO in the schema may require additional implementation or testing.

### Technical Details

- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **Database**: H2 (development), PostgreSQL (production-ready)
- **API**: GraphQL with 13 queries and 13 mutations
- **Security**: JWT with refresh token rotation
- **Build Tool**: Gradle 8.x
- **Caching**: Custom LFU cache implementation with Caffeine
- **Custom Scalars**: DateTime support for event scheduling
- **Architecture**: Multi-tenant with household-based data isolation

---

**ROOMLY - Household Management Made Simple 🏠**
