# Data Transfer Objects (DTOs)

All DTOs are Java records defined in `src/main/java/org/roomly/dto/`. They map directly to the GraphQL types in the schema and are what resolvers return to clients.

---

## HouseholdDTO

Represents a household and all its associated resources.

**Java record:** `org.roomly.dto.HouseholdDTO`  
**GraphQL type:** `Household`

| Field                | Type               | Nullable | Description                                          |
|----------------------|--------------------|----------|------------------------------------------------------|
| `id`                 | `String`           | no       | Unique household ID (7-char, lowercase alphanumeric) |
| `name`               | `String`           | no       | Display name of the household                        |
| `joinCode`           | `String`           | no       | 6-character join code (uppercase alphanumeric)       |
| `membersLimit`       | `int`              | no       | Maximum allowed members                              |
| `owner`              | `ProfileDTO`       | yes      | Profile of the household owner (null if unset)       |
| `members`            | `List<ProfileDTO>` | no       | All member profiles                                  |
| `sharedInventory`    | `InventoryDTO`     | no       | The household's shared inventory                     |
| `sharedShoppingList` | `ShoppingListDTO`  | no       | The household's shared shopping list                 |
| `membersCount`       | `int`              | no       | Current member count                                 |

---

## ProfileDTO

Represents a user's identity within a household.

**Java record:** `org.roomly.dto.ProfileDTO`  
**GraphQL type:** `Profile`

| Field          | Type              | Nullable | Description                          |
|----------------|-------------------|----------|--------------------------------------|
| `id`           | `String`          | no       | Unique profile ID                    |
| `nickname`     | `String`          | no       | Display name within the household    |
| `avatar`       | `AvatarDTO`       | no       | The profile's avatar                 |
| `inventory`    | `InventoryDTO`    | no       | The profile's personal inventory     |
| `shoppingList` | `ShoppingListDTO` | no       | The profile's personal shopping list |

---

## AvatarDTO

Represents an avatar with its associated color.

**Java record:** `org.roomly.dto.AvatarDTO`  
**GraphQL type:** `Avatar`

| Field       | Type     | Nullable | Description                            |
|-------------|----------|----------|----------------------------------------|
| `name`      | `String` | no       | Avatar name (e.g. `Dog`, `Fox`)        |
| `colorName` | `String` | no       | Color name (e.g. `Red`, `LightBlue`)   |
| `colorHex`  | `String` | no       | Color hex code (e.g. `#ff0100`)        |

---

## AvailableAvatarsAndColorsDTO

Lists all selectable avatars and colors. Returned by `availableAvatarsAndColors`.

**Java record:** `org.roomly.dto.AvailableAvatarsAndColorsDTO`  
**GraphQL type:** `AvatarsAndColors`

| Field     | Type             | Nullable | Description                               |
|-----------|------------------|----------|-------------------------------------------|
| `avatars` | `List<String>`   | no       | List of available avatar name identifiers |
| `colors`  | `List<ColorDTO>` | no       | List of available colors                  |

---

## ColorDTO

Represents a named color option.

**Java record:** `org.roomly.dto.ColorDTO`  
**GraphQL type:** `Color`

| Field  | Type     | Nullable | Description                              |
|--------|----------|----------|------------------------------------------|
| `name` | `String` | no       | Color name (e.g. `Red`, `LightBlue`)     |
| `hex`  | `String` | no       | Hex color code (e.g. `#ff0100`)          |

---

## ShoppingListDTO

Represents a shopping list and its items.

**Java record:** `org.roomly.dto.ShoppingListDTO`  
**GraphQL type:** `ShoppingList`

| Field   | Type                        | Nullable | Description             |
|---------|-----------------------------|----------|-------------------------|
| `id`    | `int`                       | no       | Unique shopping list ID |
| `items` | `List<ShoppingListItemDTO>` | no       | All items in this list  |

---

## ShoppingListItemDTO

Represents a single product entry in a shopping list.

**Java record:** `org.roomly.dto.ShoppingListItemDTO`  
**GraphQL type:** `ShoppingListItem`

| Field     | Type            | Nullable | Description                       |
|-----------|-----------------|----------|-----------------------------------|
| `id`      | `int`           | no       | Unique item ID                    |
| `product` | `ProductDTO`    | no       | The associated product            |
| `count`   | `int`           | no       | Quantity                          |
| `addedAt` | `LocalDateTime` | no       | Timestamp when the item was added |
| `notes`   | `String`        | yes      | Optional free-text notes          |

---

## InventoryDTO

Represents an inventory and its items.

**Java record:** `org.roomly.dto.InventoryDTO`  
**GraphQL type:** `Inventory`

| Field   | Type                     | Nullable | Description                 |
|---------|--------------------------|----------|-----------------------------|
| `id`    | `int`                    | no       | Unique inventory ID         |
| `items` | `List<InventoryItemDTO>` | no       | All items in this inventory |

---

## InventoryItemDTO

Represents a single product entry in an inventory.

**Java record:** `org.roomly.dto.InventoryItemDTO`  
**GraphQL type:** `InventoryItem`

| Field     | Type            | Nullable | Description                       |
|-----------|-----------------|----------|-----------------------------------|
| `id`      | `int`           | no       | Unique item ID                    |
| `product` | `ProductDTO`    | no       | The associated product            |
| `count`   | `int`           | no       | Quantity in stock                 |
| `addedAt` | `LocalDateTime` | no       | Timestamp when the item was added |
| `notes`   | `String`        | yes      | Optional storage notes            |

---

## ProductDTO

Represents a product, sourced from the local database or OpenFoodFacts.

**Java record:** `org.roomly.dto.ProductDTO`  
**GraphQL type:** `Product`

| Field      | Type     | Nullable | Description                                    |
|------------|----------|----------|------------------------------------------------|
| `id`       | `int`    | no       | Unique product ID                              |
| `barcode`  | `String` | yes      | EAN/UPC barcode (validated by `@ValidBarcode`) |
| `name`     | `String` | no       | Product name                                   |
| `brand`    | `String` | no       | Brand name                                     |
| `quantity` | `String` | no       | Quantity string (e.g. `"500g"`)                |

---

## EventDTO

Represents a calendar event within a household.

**Java record:** `org.roomly.dto.EventDTO`  
**GraphQL type:** `Event`

| Field         | Type               | Nullable | Description                               |
|---------------|--------------------|----------|-------------------------------------------|
| `id`          | `int`              | no       | Unique event ID                           |
| `name`        | `String`           | no       | Event name                                |
| `description` | `String`           | yes      | Optional description                      |
| `startTime`   | `LocalDateTime`    | no       | Event start datetime                      |
| `endTime`     | `LocalDateTime`    | no       | Event end datetime                        |
| `householdId` | `String`           | no       | ID of the household this event belongs to |
| `creator`     | `ProfileDTO`       | no       | Profile that created the event            |
| `attendees`   | `List<ProfileDTO>` | no       | Profiles attending the event              |

---

## TransactionDTO

Represents a financial transaction between two household members.

**Java record:** `org.roomly.dto.TransactionDTO`  
**GraphQL type:** `Transaction`

| Field       | Type            | Nullable | Description                                 |
|-------------|-----------------|----------|---------------------------------------------|
| `id`        | `int`           | no       | Unique transaction ID                       |
| `title`     | `String`        | no       | Description of the transaction              |
| `sendAt`    | `LocalDateTime` | no       | Timestamp when the transaction was recorded |
| `amount`    | `double`        | no       | Transaction amount (must be ≥ 0.01)         |
| `sender`    | `ProfileDTO`    | no       | Profile that sent/initiated the transaction |
| `recipient` | `ProfileDTO`    | no       | Profile that received the transaction       |
| `type`      | `String`        | no       | `INCOME` or `EXPENSE`                       |

---

## NotificationDTO

Represents a notification delivered to a profile.

**Java record:** `org.roomly.notifications.dto.NotificationDTO`  
**REST endpoint:** `GET /api/notifications`

| Field       | Type            | Nullable | Description                          |
|-------------|-----------------|----------|--------------------------------------|
| `id`        | `String`        | no       | Unique notification ID               |
| `title`     | `String`        | no       | Short notification title             |
| `message`   | `String`        | no       | Full notification message body       |
| `timestamp` | `LocalDateTime` | no       | When the notification was created    |
| `profileId` | `String`        | no       | ID of the profile this was sent to   |