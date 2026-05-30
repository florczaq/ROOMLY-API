# GraphQL Queries

All queries are sent as HTTP POST to `/graphql`. Most queries require a valid JWT access token in the `Authorization: Bearer <token>` header.

---

## Household Queries

### `household`

Returns a single household by ID.

- **Auth required:** yes
- **Returns:** `Household!`

| Argument      | Type     | Required | Description               |
|---------------|----------|----------|---------------------------|
| `householdId` | `String` | yes      | The household's unique ID |

```graphql
query {
    household(householdId: "abc1234") {
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
        sharedInventory {
            id
        }
        sharedShoppingList {
            id
        }
    }
}
```

---

### `households`

Returns all households the authenticated user belongs to.

- **Auth required:** yes
- **Returns:** `[Household]!`

```graphql
query {
    households {
        id
        name
        joinCode
        membersLimit
        membersCount
    }
}
```

---

### `householdByJoinCode`

Looks up a household by its join code. Join codes are case-insensitive (normalized to uppercase internally).

- **Auth required:** no
- **Returns:** `Household!`

| Argument   | Type     | Required | Description               |
|------------|----------|----------|---------------------------|
| `joinCode` | `String` | yes      | The 6-character join code |

```graphql
query {
    householdByJoinCode(joinCode: "ABC123") {
        id
        name
        membersCount
        membersLimit
    }
}
```

---

## Profile Queries

### `profile`

Returns a single profile by ID, including its personal inventory and shopping list.

- **Auth required:** yes
- **Returns:** `Profile!`

| Argument    | Type     | Required | Description             |
|-------------|----------|----------|-------------------------|
| `profileId` | `String` | yes      | The profile's unique ID |

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
                product { name }
                count
                addedAt
                notes
            }
        }
        shoppingList {
            id
            items {
                id
                product { name }
                count
                addedAt
            }
        }
    }
}
```

---

## Shopping List Queries

### `shoppingList`

Returns a single shopping list by ID.

- **Auth required:** yes
- **Returns:** `ShoppingList!`

| Argument | Type  | Required | Description            |
|----------|-------|----------|------------------------|
| `id`     | `Int` | yes      | The shopping list's ID |

```graphql
query {
    shoppingList(id: 1) {
        id
        items {
            id
            product {
                id
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

---

### `allShoppingLists`

Returns all shopping lists belonging to a household (both shared and personal).

- **Auth required:** yes
- **Returns:** `[ShoppingList]!`

| Argument      | Type     | Required | Description               |
|---------------|----------|----------|---------------------------|
| `householdId` | `String` | yes      | The household's unique ID |

```graphql
query {
    allShoppingLists(householdId: "abc1234") {
        id
        items {
            id
            product { name }
            count
            addedAt
        }
    }
}
```

---

## Inventory Queries

### `inventory`

Returns a single inventory by ID.

- **Auth required:** yes
- **Returns:** `Inventory!`

| Argument | Type  | Required | Description        |
|----------|-------|----------|--------------------|
| `id`     | `Int` | yes      | The inventory's ID |

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

---

### `allInventories`

Returns all inventories belonging to a household (both shared and personal).

- **Auth required:** yes
- **Returns:** `[Inventory]!`

| Argument      | Type     | Required | Description               |
|---------------|----------|----------|---------------------------|
| `householdId` | `String` | yes      | The household's unique ID |

```graphql
query {
    allInventories(householdId: "abc1234") {
        id
        items {
            id
            product { name }
            count
        }
    }
}
```

---

## Product Queries

### `product`

Looks up a product by barcode. If not cached locally, fetches from the OpenFoodFacts API and stores the result.

- **Auth required:** no
- **Returns:** `Product` (nullable — returns null if product not found)

| Argument  | Type     | Required | Description            |
|-----------|----------|----------|------------------------|
| `barcode` | `String` | yes      | EAN/UPC barcode string |

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

---

## Avatar & Color Queries

### `availableAvatarsAndColors`

Returns all available avatar names and color options. Used when creating or updating a profile.

- **Auth required:** no
- **Returns:** `AvatarsAndColors!`

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

---

## Event Queries

### `events`

Returns events for a household, with optional date range filtering.

- **Auth required:** yes
- **Returns:** `[Event]!`

| Argument      | Type       | Required | Description                                      |
|---------------|------------|----------|--------------------------------------------------|
| `householdId` | `String`   | yes      | The household's unique ID                        |
| `from`        | `DateTime` | no       | Filter events starting on or after this datetime |
| `to`          | `DateTime` | no       | Filter events ending on or before this datetime  |

```graphql
query {
    events(
        householdId: "abc1234"
        from: "2026-06-01T00:00:00"
        to: "2026-06-30T23:59:59"
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

---

### `event`

Returns a single event by ID.

- **Auth required:** yes
- **Returns:** `Event!`

| Argument  | Type  | Required | Description    |
|-----------|-------|----------|----------------|
| `eventId` | `Int` | yes      | The event's ID |

```graphql
query {
    event(eventId: 1) {
        id
        name
        description
        startTime
        endTime
        householdId
        creator {
            nickname
        }
        attendees {
            nickname
        }
    }
}
```

---

### `eventsForProfile`

Returns events associated with a specific profile, with optional date range filtering.

- **Auth required:** yes
- **Returns:** `Event!`

| Argument    | Type       | Required | Description             |
|-------------|------------|----------|-------------------------|
| `profileId` | `String`   | yes      | The profile's unique ID |
| `from`      | `DateTime` | no       | Filter start date       |
| `to`        | `DateTime` | no       | Filter end date         |

```graphql
query {
    eventsForProfile(
        profileId: "profile-id"
        from: "2026-06-01T00:00:00"
    ) {
        id
        name
        startTime
        endTime
    }
}
```

---

## Transaction Queries

### `transactions`

Returns all transactions for a household.

- **Auth required:** yes
- **Returns:** `[Transaction]!`

| Argument      | Type     | Required | Description               |
|---------------|----------|----------|---------------------------|
| `householdId` | `String` | yes      | The household's unique ID |

```graphql
query {
    transactions(householdId: "abc1234") {
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