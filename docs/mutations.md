# GraphQL Mutations

All mutations are sent as HTTP POST to `/graphql`. Most mutations require a valid JWT access token in the `Authorization: Bearer <token>` header.

---

## Household Mutations

### `createHousehold`

Creates a new household and automatically creates the authenticated user's first profile in it. Also initializes a shared inventory and shared shopping list for the household.

- **Auth required:** yes
- **Returns:** `Household!`

| Argument          | Type     | Required | Description                                    |
|-------------------|----------|----------|------------------------------------------------|
| `name`            | `String` | yes      | Display name for the household                 |
| `membersLimit`    | `Int`    | yes      | Maximum number of members (1–30)               |
| `nickname`        | `String` | yes      | The creator's nickname in this household       |
| `avatarName`      | `String` | yes      | Avatar name (from `availableAvatarsAndColors`) |
| `avatarColorName` | `String` | yes      | Color name (from `availableAvatarsAndColors`)  |

```graphql
mutation {
    createHousehold(
        name: "My Family"
        membersLimit: 6
        nickname: "Dad"
        avatarName: "DOG_WHITE"
        avatarColorName: "BLUE"
    ) {
        id
        name
        joinCode
        membersLimit
        membersCount
        owner {
            id
            nickname
        }
    }
}
```

---

## Profile Mutations

### `joinHousehold`

Joins an existing household using its join code. Creates a new profile for the authenticated user in that household, along with a personal inventory and personal shopping list.

- **Auth required:** yes
- **Returns:** `Profile!`

| Argument          | Type     | Required | Description                                              |
|-------------------|----------|----------|----------------------------------------------------------|
| `joinCode`        | `String` | yes      | The household's 6-character join code (case-insensitive) |
| `nickname`        | `String` | yes      | The user's nickname in this household                    |
| `avatarName`      | `String` | yes      | Avatar name (from `availableAvatarsAndColors`)           |
| `avatarColorName` | `String` | yes      | Color name (from `availableAvatarsAndColors`)            |

```graphql
mutation {
    joinHousehold(
        joinCode: "ABC123"
        nickname: "Jane"
        avatarName: "DOG_WHITE"
        avatarColorName: "RED"
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

---

### `updateProfile`

Updates one or more fields of an existing profile. All fields except `profileId` are optional — only provided fields are updated.

- **Auth required:** yes
- **Returns:** `Profile!`

| Argument          | Type     | Required | Description           |
|-------------------|----------|----------|-----------------------|
| `profileId`       | `String` | yes      | The profile to update |
| `nickname`        | `String` | no       | New nickname          |
| `avatarName`      | `String` | no       | New avatar name       |
| `avatarColorName` | `String` | no       | New avatar color name |

```graphql
mutation {
    updateProfile(
        profileId: "profile-id"
        nickname: "New Name"
        avatarColorName: "GREEN"
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

---

### `leaveHousehold`

Removes a profile from its household and deletes the profile. The authenticated user must own the profile.

- **Auth required:** yes
- **Returns:** `Boolean!`

| Argument    | Type     | Required | Description           |
|-------------|----------|----------|-----------------------|
| `profileId` | `String` | yes      | The profile to remove |

```graphql
mutation {
    leaveHousehold(profileId: "profile-id")
}
```

---

## Shopping List Mutations

### `addProductToShoppingList`

Adds a product to a shopping list, or increases its count if already present.

- **Auth required:** yes
- **Returns:** `ShoppingListItem!`

| Argument         | Type     | Required | Description                  |
|------------------|----------|----------|------------------------------|
| `productId`      | `Int`    | yes      | ID of the product to add     |
| `shoppingListId` | `Int`    | yes      | Target shopping list ID      |
| `count`          | `Int`    | yes      | Number of units to add       |
| `notes`          | `String` | no       | Optional notes for this item |

```graphql
mutation {
    addProductToShoppingList(
        productId: 42
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

---

### `removeProductFromShoppingList`

Removes or decreases the quantity of a product in a shopping list.

- **Auth required:** yes
- **Returns:** `ShoppingListItem!`

| Argument         | Type     | Required | Description                 |
|------------------|----------|----------|-----------------------------|
| `productId`      | `Int`    | yes      | ID of the product to remove |
| `shoppingListId` | `Int`    | yes      | Target shopping list ID     |
| `count`          | `Int`    | yes      | Number of units to remove   |
| `notes`          | `String` | no       | Optional notes              |

```graphql
mutation {
    removeProductFromShoppingList(
        productId: 42
        shoppingListId: 1
        count: 1
    ) {
        id
        product {
            name
        }
        count
    }
}
```

---

## Inventory Mutations

### `addProductToInventory`

Adds a product to an inventory, or increases its count if already present.

- **Auth required:** yes
- **Returns:** `InventoryItem!`

| Argument      | Type     | Required | Description              |
|---------------|----------|----------|--------------------------|
| `productId`   | `Int`    | yes      | ID of the product to add |
| `inventoryId` | `Int`    | yes      | Target inventory ID      |
| `count`       | `Int`    | yes      | Number of units to add   |
| `notes`       | `String` | no       | Optional storage notes   |

```graphql
mutation {
    addProductToInventory(
        productId: 42
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

---

### `removeProductFromInventory`

Removes or decreases the quantity of a product in an inventory.

- **Auth required:** yes
- **Returns:** `InventoryItem!`

| Argument      | Type     | Required | Description                 |
|---------------|----------|----------|-----------------------------|
| `productId`   | `Int`    | yes      | ID of the product to remove |
| `inventoryId` | `Int`    | yes      | Target inventory ID         |
| `count`       | `Int`    | yes      | Number of units to remove   |
| `notes`       | `String` | no       | Optional notes              |

```graphql
mutation {
    removeProductFromInventory(
        productId: 42
        inventoryId: 1
        count: 2
    ) {
        id
        product {
            name
        }
        count
    }
}
```

---

## Event Mutations

### `addEvent`

Creates a new event in the authenticated user's household. The caller's profile becomes the event creator.

- **Auth required:** yes
- **Returns:** `Event!`

| Argument      | Type       | Required | Description          |
|---------------|------------|----------|----------------------|
| `name`        | `String`   | yes      | Event name           |
| `description` | `String`   | no       | Optional description |
| `startTime`   | `DateTime` | yes      | Event start datetime |
| `endTime`     | `DateTime` | yes      | Event end datetime   |

```graphql
mutation {
    addEvent(
        name: "Family Dinner"
        description: "Monthly gathering"
        startTime: "2026-07-01T18:00:00"
        endTime: "2026-07-01T21:00:00"
    ) {
        id
        name
        description
        startTime
        endTime
        creator {
            nickname
        }
    }
}
```

---

### `updateEvent`

Updates an existing event. All fields except `eventId` are optional.

- **Auth required:** yes
- **Returns:** `Event!`

| Argument      | Type       | Required | Description               |
|---------------|------------|----------|---------------------------|
| `eventId`     | `Int`      | yes      | ID of the event to update |
| `name`        | `String`   | no       | New event name            |
| `description` | `String`   | no       | New description           |
| `startTime`   | `DateTime` | no       | New start datetime        |
| `endTime`     | `DateTime` | no       | New end datetime          |

```graphql
mutation {
    updateEvent(
        eventId: 1
        name: "Updated Dinner"
        startTime: "2026-07-01T19:00:00"
    ) {
        id
        name
        startTime
        endTime
    }
}
```

---

### `deleteEvent`

Deletes an event by ID.

- **Auth required:** yes
- **Returns:** `Boolean!`

| Argument  | Type  | Required | Description               |
|-----------|-------|----------|---------------------------|
| `eventId` | `Int` | yes      | ID of the event to delete |

```graphql
mutation {
    deleteEvent(eventId: 1)
}
```

---

## Transaction Mutations

### `addTransaction`

Records a financial transaction between two profiles in the same household. The authenticated user's profile is the sender.

- **Auth required:** yes
- **Returns:** `Transaction!`

| Argument      | Type     | Required | Description                    |
|---------------|----------|----------|--------------------------------|
| `title`       | `String` | yes      | Description of the transaction |
| `amount`      | `Float`  | yes      | Amount (must be > 0)           |
| `recipientId` | `String` | yes      | Profile ID of the recipient    |
| `type`        | `String` | yes      | `INCOME` or `EXPENSE`          |

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

---

### `deleteTransaction`

Deletes a transaction by ID.

- **Auth required:** yes
- **Returns:** `Boolean!`

| Argument        | Type  | Required | Description                     |
|-----------------|-------|----------|---------------------------------|
| `transactionId` | `Int` | yes      | ID of the transaction to delete |

```graphql
mutation {
    deleteTransaction(transactionId: 1)
}
```