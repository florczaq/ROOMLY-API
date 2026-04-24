# 🏠 ROOMLY - Household Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GraphQL](https://img.shields.io/badge/GraphQL-Enabled-E10098.svg)](https://graphql.org/)
[![H2 Database](https://img.shields.io/badge/H2-In--Memory-blue.svg)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **A comprehensive household management platform** designed to streamline daily household operations, from shopping lists and inventory management to financial tracking and event scheduling.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Current Features](#-current-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
  - [GraphQL Schema](#graphql-schema)
  - [Current Queries](#current-queries)
  - [Current Mutations](#current-mutations)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Testing](#-testing)
- [Roadmap](#-roadmap)
- [Future Features](#-future-features)

---

## 🎯 Overview

**ROOMLY** is a modern, scalable household management system built with Spring Boot and GraphQL. It enables multiple users to collaborate within households, managing shared resources, tracking expenses, coordinating events, and maintaining shopping lists and inventories.

### Core Concepts

- **Households**: Central organizational units that group users and resources
- **Profiles**: User representations within households (one user can have multiple profiles)
- **Multi-tenancy**: Support for users participating in multiple households
- **Shared Resources**: Shopping lists, inventories, and events can be personal or shared
- **Financial Tracking**: Inter-user transactions and expense tracking (entity ready)

---

## ✨ Current Features

### 🏡 Household Management
- ✅ Create households with custom join codes (6 characters)
- ✅ Join existing households using join codes
- ✅ Set member limits (1-30 members)
- ✅ Household ownership tracking
- ✅ Retrieve household information

### 👤 Profile & Avatar System
- ✅ Create user profiles with customizable avatars
- ✅ Avatar name and color selection
- ✅ Retrieve available avatars and colors
- ✅ Nickname assignment per household
- ✅ Profile association with households

### 🔍 Product Lookup
- ✅ Barcode-based product search
- ✅ Integration with OpenFoodFacts API
- ✅ Retrieve product details (name, brand, quantity)

### 🗄️ Data Entities (Backend Ready)
The following entities are fully implemented in the backend and ready for API implementation:
- ✅ Shopping Lists (personal and shared)
- ✅ Shopping List Items with purchase tracking
- ✅ Products with barcode support
- ✅ Inventory (personal and shared)
- ✅ Events with attendee management
- ✅ Financial Transactions

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21
- **API**: GraphQL (Spring for GraphQL)
- **Database**: H2 (in-memory, development) / PostgreSQL (production-ready)
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle

### Key Dependencies
- Spring Boot Starter Web MVC
- Spring Boot Starter GraphQL
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter WebFlux (for external API calls)
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

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x
- (Optional) PostgreSQL for production

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/roomly.git
   cd roomly
   ```

2. **Set up environment variables**
   
   Create a `.env` file in the root directory:
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

5. **Access the application**
   - GraphQL Endpoint: `http://localhost:8080/graphql`
   - GraphiQL Interface: `http://localhost:8080/graphiql` (if enabled)

### Development Mode

The application currently uses H2 in-memory database for development:
```properties
spring.datasource.url=jdbc:h2:mem:roomlydb
spring.jpa.hibernate.ddl-auto=create-drop
```

For production, configure PostgreSQL in `application.properties`.

---

## 📖 API Documentation

### GraphQL Schema

#### Current Queries

```graphql
type Query {
    # Get household information
    household(householdId: String!): String!
    
    # Get detailed household information
    householdInfo(householdId: String!): String!
    
    # Search for products by barcode
    products(barcode: String!, key: String): ProductDetails!
    
    # Get available avatars and colors
    availableAvatarsAndColors: AvatarsAndColors!
}
```

#### Current Mutations

```graphql
type Mutation {
    # Create a new household
    createHousehold(
        name: String!
        membersLimit: Int!
        nickname: String!
        avatarName: String!
        avatarColorName: String!
    ): Household!
    
    # Join an existing household
    joinHousehold(
        nickname: String!
        avatarName: String!
        avatarColorName: String!
        joinCode: String!
    ): User!
}
```

#### Types

```graphql
type Household {
    id: String!
    name: String!
    joinCode: String!
    membersLimit: Int!
}

type User {
    nickname: String!
    avatar: Avatar
}

type Avatar {
    name: String!
    colorName: String
    colorHex: String
}

type ProductDetails {
    barcode: String
    name: String
    brand: String
    quantity: String
}

type AvatarsAndColors {
    avatars: [String]!
    colors: [Color]!
}

type Color {
    name: String!
    hex: String!
}
```

### Example Usage

#### Create a Household

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
    }
}
```

#### Join a Household

```graphql
mutation {
    joinHousehold(
        nickname: "Jane"
        avatarName: "avatar2"
        avatarColorName: "red"
        joinCode: "ABC123"
    ) {
        nickname
        avatar {
            name
            colorName
            colorHex
        }
    }
}
```

#### Get Available Avatars

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

#### Search Product by Barcode

```graphql
query {
    products(barcode: "3017620422003") {
        barcode
        name
        brand
        quantity
    }
}
```

---

## 🗄️ Database Schema

### Entities (11 Tables)

#### Core Entities
1. **Account** - User authentication
2. **Profile** - User profiles within households
3. **Household** - Household groupings
4. **RefreshToken** - JWT refresh tokens

#### Feature Entities
5. **ShoppingList** - Shopping lists (personal/shared)
6. **ShoppingListItem** - Items in shopping lists
7. **Product** - Product catalog
8. **Inventory** - Inventory management (personal/shared)
9. **Event** - Calendar events with attendees
10. **Transaction** - Financial transactions between profiles
11. **Notification** - User notifications (basic implementation)

### Entity Details

#### ShoppingList
- Belongs to a Household
- Optional owner (null = shared with household)
- Contains multiple ShoppingListItems
- Cascade: ALL, orphanRemoval=true

#### ShoppingListItem
- References a Product
- Tracks count, purchase status, timestamps
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
- Ready for inventory items implementation

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
- Supports household financial tracking

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

### Test Structure

```
src/test/java/org/roomly/
├── RoomlyApplicationTests.java           # Context loading
├── integrationTests/
│   ├── AvatarsIntegrationTest.java      # Avatar system tests
│   └── HouseholdIntegrationTest.java    # Household operations tests
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.roomly.integrationTests.HouseholdIntegrationTest"

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Test Reports

Test reports are generated in: `build/reports/tests/test/index.html`

---

## 🗺️ Roadmap

### Phase 1: Core Foundation ✅ (COMPLETED)
- ✅ Project setup and configuration
- ✅ Database schema design
- ✅ Entity modeling (all 11 entities)
- ✅ Basic GraphQL API setup
- ✅ Household management (create/join)
- ✅ Profile and avatar system
- ✅ Product lookup integration

### Phase 2: Shopping List Feature (IN PROGRESS)
- [ ] Shopping list GraphQL mutations and queries
- [ ] Create personal and shared shopping lists
- [ ] Add/remove/update shopping list items
- [ ] Mark items as purchased
- [ ] Shopping list item notes
- [ ] Product barcode scanning

### Phase 3: Product Management (PLANNED)
- [ ] Product CRUD operations
- [ ] Manual product entry
- [ ] Product search and filtering
- [ ] Product categories
- [ ] Barcode management
- [ ] Product image support

### Phase 4: Inventory System (PLANNED)
- [ ] Create inventory items
- [ ] Inventory item CRUD operations
- [ ] Stock level tracking
- [ ] Low stock notifications
- [ ] Expiration date tracking
- [ ] Inventory item categories
- [ ] Move items between inventories

### Phase 5: Event Calendar (PLANNED)
- [ ] Event GraphQL mutations and queries
- [ ] Create/update/delete events
- [ ] Event attendee management
- [ ] Event reminders
- [ ] Recurring events
- [ ] Event notifications
- [ ] Calendar view support

### Phase 6: Financial Transactions (PLANNED)
- [ ] Transaction CRUD operations
- [ ] Transaction history
- [ ] Balance calculations
- [ ] Expense splitting
- [ ] Transaction categories
- [ ] Monthly reports
- [ ] Export functionality

### Phase 7: Advanced Features (FUTURE)
- [ ] Real-time updates with GraphQL subscriptions
- [ ] Push notifications
- [ ] File attachments (receipts, photos)
- [ ] Recipe management
- [ ] Meal planning
- [ ] Budget tracking
- [ ] Shopping list templates
- [ ] Import/export data

### Phase 8: Production Readiness (FUTURE)
- [ ] PostgreSQL migration
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] API documentation (Swagger/GraphQL Playground)
- [ ] Performance optimization
- [ ] Caching layer (Redis)
- [ ] Rate limiting
- [ ] API versioning

---

## 🔮 Future Features

### Shopping List Enhancements
- **Smart Suggestions**: AI-based product suggestions based on purchase history
- **Collaborative Shopping**: Real-time collaborative list editing
- **Voice Integration**: Add items via voice commands
- **Price Tracking**: Track product prices over time
- **Receipt Scanning**: Automatically add items from receipt photos
- **Store Layout**: Organize list by store aisle
- **Sharing**: Share lists with non-household members (temporary access)

### Product Database Features
- **Nutrition Information**: Detailed nutritional data
- **Allergen Tracking**: Mark and filter by allergens
- **Brand Preferences**: User-preferred brands
- **Product Alternatives**: Suggest alternative products
- **Price Comparison**: Compare prices across stores
- **Product Reviews**: User reviews and ratings
- **Custom Products**: User-defined product catalog
- **Product Tags**: Organic, vegan, gluten-free, etc.

### Inventory Enhancements
- **Expiration Alerts**: Notifications for expiring items
- **Inventory Analytics**: Usage patterns and trends
- **Auto-reorder**: Automatic shopping list population based on inventory
- **Batch Operations**: Move multiple items at once
- **Inventory Templates**: Predefined inventory setups
- **Barcode Scanning**: Quick inventory updates via barcode
- **Inventory History**: Track inventory changes over time
- **Photo Support**: Item photos in inventory

### Event Calendar Features
- **Event Types**: Meals, chores, appointments, reminders
- **Recurring Events**: Daily, weekly, monthly patterns
- **Event Reminders**: Multiple reminder times
- **Event Colors**: Color-code events by type
- **Event Templates**: Reusable event templates
- **Calendar Sync**: Integration with Google Calendar, Outlook
- **Event History**: Past event logs
- **Attendee RSVP**: Accept/decline event invitations
- **Event Comments**: Discussion threads on events
- **Event Attachments**: Files, links, photos

### Transaction & Finance Features
- **Expense Categories**: Categorize expenses (groceries, utilities, etc.)
- **Split Options**: Even split, percentage, custom amounts
- **Recurring Transactions**: Rent, subscriptions
- **Payment Methods**: Track payment types (cash, card, etc.)
- **Settlement Suggestions**: Who owes whom
- **Receipt Attachments**: Link receipts to transactions
- **Budget Limits**: Set spending limits per category
- **Financial Reports**: Monthly/yearly summaries
- **Export Formats**: CSV, PDF exports
- **Currency Support**: Multi-currency tracking

### Notification System
- **Notification Types**: Shopping, inventory, events, transactions
- **Notification Preferences**: User-configurable
- **Push Notifications**: Mobile push support
- **Email Notifications**: Email summaries
- **In-app Notifications**: Real-time updates
- **Notification History**: View past notifications

### Advanced Features
- **Recipe Database**: Store and share recipes
- **Meal Planning**: Plan meals for the week
- **Automatic Shopping Lists**: Generate lists from meal plans
- **Household Analytics**: Usage statistics and insights
- **Data Import/Export**: Backup and restore
- **API Webhooks**: External integrations
- **Mobile Apps**: Native iOS/Android apps
- **Progressive Web App**: Installable web app
- **Dark Mode**: UI theme support
- **Localization**: Multi-language support

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
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   ├── ProductsRepository.java
│   │   │   │   ├── ShoppingListRepository.java
│   │   │   │   └── TransactionsRepository.java
│   │   │   ├── resolvers/           # GraphQL resolvers
│   │   │   │   ├── AvatarsResolver.java
│   │   │   │   ├── EventsResolver.java
│   │   │   │   ├── HouseholdResolver.java
│   │   │   │   ├── InventoryResolver.java
│   │   │   │   ├── ProductsResolver.java
│   │   │   │   ├── ShoppingListResolver.java
│   │   │   │   └── TransactionsResolver.java
│   │   │   ├── security/            # Security & authentication
│   │   │   └── services/            # Business logic
│   │   │       ├── AvatarService.java
│   │   │       ├── ColorsService.java
│   │   │       ├── EventsService.java
│   │   │       ├── ExternalApiService.java
│   │   │       ├── HouseholdService.java
│   │   │       ├── InventoryService.java
│   │   │       ├── ProductsService.java
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
│           └── integrationTests/
│               ├── AvatarsIntegrationTest.java
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

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Use Lombok annotations for boilerplate code
- Write unit tests for new features
- Document public APIs with JavaDoc
- Keep methods small and focused

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Florczak Mikołaj** - everything (design, implementation, documentation, testing, database, security, and more...)

---

## 🙏 Acknowledgments

- [OpenFoodFacts](https://world.openfoodfacts.org/) for product data API
- [Spring Boot](https://spring.io/projects/spring-boot) community
- [GraphQL](https://graphql.org/) community

---

## 📞 Support

For questions and support, please open an issue on GitHub.

---

## 📊 Project Status

**Current Version**: 0.0.1  
**Status**: 🚧 In Development  
**Last Updated**: April 23, 2026

### Implementation Status

| Feature | Status | API Exposed |
|---------|--------|-------------|
| Household Management | ✅ Complete | ✅ Yes |
| Profile & Avatars | ✅ Complete | ✅ Yes |
| Product Lookup | ✅ Complete | ✅ Yes |
| Shopping Lists | 🏗️ Backend Ready | ❌ No |
| Inventory | 🏗️ Backend Ready | ❌ No |
| Events | 🏗️ Backend Ready | ❌ No |
| Transactions | 🏗️ Backend Ready | ❌ No |
| Notifications | 🚧 Partial | ❌ No |

---

**Happy Household Managing! 🏠✨**

