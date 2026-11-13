# Android BStackDemo App — Design Spec

**Date:** 2026-11-12  
**Author:** Didier Corrales  
**Status:** Approved

---

## 1. Overview

Build a native Android application that simulates the full e-commerce flow of [bstackdemo.com](https://bstackdemo.com/). The app serves as a realistic demo/testing target — it mirrors the site's 6-screen flow with all product and login data stored in local JSON files so the app works fully offline and is easy to extend.

### Goals
- Replicate all 6 screens of bstackdemo.com in a native Android app
- Store product catalog and user credentials in JSON assets (not hardcoded)
- Support 5 user types with distinct behaviors (normal, locked, broken images, etc.)
- Include a dedicated Product Detail screen (enhancement over the website)
- Produce comprehensive test cases for QA automation

---

## 2. Technology Stack

| Concern | Choice | Reason |
|---|---|---|
| Language | Kotlin | Modern Android standard |
| UI | Jetpack Compose | Declarative, less boilerplate, future-proof |
| Architecture | MVVM + Repository | Clean separation, testable, Android-recommended |
| Navigation | Single Activity + Compose NavHost | Idiomatic Compose navigation |
| Data | Local JSON in `assets/` | Offline, fast, easy to edit |
| State | StateFlow + collectAsState() | Reactive UI, works well with Compose |
| DI | Manual (no Hilt/Dagger) | Keeps scope small; can be added later |
| Min SDK | API 26 (Android 8.0) | Broad device coverage |

---

## 3. Project Structure

```
app/
├── src/main/
│   ├── assets/
│   │   ├── products.json
│   │   └── users.json
│   ├── java/com.example.stackdemo/
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   ├── Product.kt
│   │   │   │   ├── User.kt
│   │   │   │   ├── CartItem.kt
│   │   │   │   └── Order.kt
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.kt
│   │   │   │   └── UserRepository.kt
│   │   │   └── local/
│   │   │       └── JsonDataSource.kt
│   │   ├── ui/
│   │   │   ├── productlist/
│   │   │   │   ├── ProductListScreen.kt
│   │   │   │   └── ProductListViewModel.kt
│   │   │   ├── productdetail/
│   │   │   │   ├── ProductDetailScreen.kt
│   │   │   │   └── ProductDetailViewModel.kt
│   │   │   ├── cart/
│   │   │   │   ├── CartDrawer.kt
│   │   │   │   └── CartViewModel.kt
│   │   │   ├── login/
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   └── LoginViewModel.kt
│   │   │   ├── checkout/
│   │   │   │   ├── CheckoutScreen.kt
│   │   │   │   └── CheckoutViewModel.kt
│   │   │   ├── confirmation/
│   │   │   │   └── ConfirmationScreen.kt
│   │   │   └── theme/
│   │   │       ├── Color.kt
│   │   │       ├── Type.kt
│   │   │       └── Theme.kt
│   │   ├── navigation/
│   │   │   └── AppNavGraph.kt
│   │   └── MainActivity.kt
│   └── res/
│       └── drawable/
│           └── (25 product images as PNG/WebP)
```

---

## 4. Data Files

### 4.1 `assets/products.json`

```json
{
  "products": [
    {
      "id": 1,
      "name": "iPhone 12",
      "vendor": "Apple",
      "price": 799.00,
      "installments": 9,
      "image": "iphone_12",
      "description": "6.1-inch Super Retina XDR display, A14 Bionic chip, 5G capable."
    }
  ]
}
```

**Fields:**
- `id` — unique int, used as navigation argument and cart key
- `name` — display name
- `vendor` — one of: `Apple`, `Samsung`, `Google`, `OnePlus`
- `price` — USD double
- `installments` — number of monthly installments (installment price = price / installments)
- `image` — drawable resource name (no extension)
- `description` — shown on Product Detail screen

All 25 products from the live site are included.

### 4.2 `assets/users.json`

```json
{
  "users": [
    { "username": "demouser",               "password": "testingisfun99", "type": "normal" },
    { "username": "image_not_loading_user", "password": "testingisfun99", "type": "broken_images" },
    { "username": "existing_orders_user",   "password": "testingisfun99", "type": "existing_orders" },
    { "username": "fav_user",               "password": "testingisfun99", "type": "favourites" },
    { "username": "locked_user",            "password": "testingisfun99", "type": "locked" }
  ]
}
```

**User type behaviors:**
| Type | Behavior |
|---|---|
| `normal` | Full flow, no restrictions |
| `broken_images` | Product images fail to load (placeholder shown) |
| `existing_orders` | Pre-populated orders list (future screen) |
| `favourites` | Pre-populated favourites |
| `locked` | Login shows error: "Your account has been locked." |

---

## 5. Navigation Graph

```
ProductList ──(tap product)──► ProductDetail
     │                              │
     └──────(Add to Cart)───────────┘
                   │
            [Cart Drawer overlay — accessible from any screen via top bar icon]
                   │
            (CHECKOUT tapped)
                   │
                Login ──(LOG IN success)──► Checkout ──(SUBMIT)──► Confirmation
                                                                        │
                                                              (CONTINUE SHOPPING)
                                                                        │
                                                                  ProductList
```

**Routes:**
```kotlin
sealed class Screen(val route: String) {
    object ProductList   : Screen("product_list")
    object ProductDetail : Screen("product_detail/{productId}")
    object Login         : Screen("login")
    object Checkout      : Screen("checkout")
    object Confirmation  : Screen("confirmation")
}
```

The Cart is a `ModalDrawerSheet` composable hosted in `MainActivity` — it overlays any screen and is not a nav destination.

---

## 6. Screen Specifications

### 6.1 Product List Screen

**ViewModel state:**
```kotlin
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val selectedVendor: String? = null,   // null = show all
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val isLoading: Boolean = true
)
```

**UI elements:**
- `TopAppBar`: app logo text, cart icon + badge, "Sign In" text button
- Vendor filter row: `FilterChip` for Apple, Samsung, Google, OnePlus
- Sort dropdown: `ExposedDropdownMenuBox` with Default / Price: Low→High / Price: High→Low
- `LazyVerticalGrid(columns = GridCells.Fixed(2))` of `ProductCard` composables
- Each `ProductCard`: product image (coil/AsyncImage), name, price, installment line, heart toggle, "Add to Cart" button

**Behavior:**
- Tapping a product card body → navigate to ProductDetail
- Tapping "Add to Cart" → add to CartViewModel, show brief Snackbar confirmation
- Tapping cart icon → open CartDrawer
- Vendor filter is single-select (tap same chip to deselect = show all)

---

### 6.2 Product Detail Screen

**ViewModel state:**
```kotlin
data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true
)
```

**UI elements:**
- Back arrow in TopAppBar
- Large product image (full width, aspect ratio 1:1)
- Product name (`titleLarge`)
- Vendor chip (small outlined chip)
- Price (`headlineMedium`, bold)
- Installment line: "or N x $X.XX"
- Product description paragraph
- Heart icon (favourites toggle)
- "ADD TO CART" button (full width, yellow, bottom of screen)

**Behavior:**
- Receives `productId: Int` as nav argument
- "ADD TO CART" → adds to CartViewModel, shows Snackbar, button changes to "ADDED ✓" briefly

---

### 6.3 Cart Drawer

**ViewModel:** `CartViewModel` — scoped to the Activity so it persists across nav destinations.

**State:**
```kotlin
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0
)
```

**UI elements:**
- Drawer header: cart icon + "Bag" title + item count + X close button
- Empty state: "Add some products in the bag :)"
- `LazyColumn` of cart rows: thumbnail, name, vendor, quantity −/+ row, item price, × remove button
- Subtotal section
- "CHECKOUT" button (full width, dark)

**Behavior:**
- Quantity −/+ updates item count; at 0 the item is removed
- "CHECKOUT" → close drawer, navigate to Login (passing `checkout=true` context)

---

### 6.4 Login Screen

**ViewModel state:**
```kotlin
data class LoginUiState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val selectedPassword: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)
```

**UI elements:**
- Centered card layout
- BrowserStack logo + "Demo" subtitle
- Username `ExposedDropdownMenuBox` — options loaded from `users.json`
- Password `ExposedDropdownMenuBox` — single option: `testingisfun99`
- "LOG IN" button
- Error text in red below button (for locked_user)

**Behavior:**
- Both dropdowns must be selected before LOG IN is enabled
- `locked_user` → show error "Your account has been locked.", do not navigate
- All other users → navigate to Checkout

---

### 6.5 Checkout Screen

**ViewModel state:**
```kotlin
data class CheckoutUiState(
    val firstName: String = "",
    val lastName: String = "",
    val address: String = "",
    val state: String = "",
    val postalCode: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val errors: Map<String, String> = emptyMap()   // field name → error message
)
```

**UI elements:**
- Header: "StackDemo" text
- Two-column layout (phone: stacked; tablet: form left, summary right)
- Shipping Address section with 5 `OutlinedTextField` fields
- Order Summary card: item list (thumbnail, name, vendor, qty, price) + Total
- "SUBMIT" button

**Validation rules:**
- All fields required (non-empty)
- Postal code: digits only, min 5 chars
- Error shown inline below each field

**Behavior:**
- Valid submit → generate random order number (10000–99999), navigate to Confirmation

---

### 6.6 Confirmation Screen

**UI elements:**
- "Your Order has been successfully placed." (`titleLarge`, bold)
- "Your order number is **XXXXX**."
- "Download order receipt" link → shows Snackbar: "Receipt downloaded" (no actual file)
- "CONTINUE SHOPPING »" outlined button
- Order Summary card (same as Checkout, read from CartViewModel snapshot)

**Behavior:**
- "CONTINUE SHOPPING" → clear cart, navigate to ProductList, pop entire back stack

---

## 7. Theme

Match bstackdemo.com visual identity:

| Token | Value |
|---|---|
| Primary (yellow) | `#F5A623` |
| Background | `#FFFFFF` |
| Surface dark (cart/nav) | `#1A1A1A` |
| Text primary | `#212121` |
| Text secondary | `#757575` |
| Error | `#D32F2F` |
| Font | System default (Roboto) |

---

## 8. Key Dependencies (`build.gradle`)

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.09.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.8.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

// Image loading
implementation("io.coil-kt:coil-compose:2.7.0")

// JSON parsing
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

// Testing
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## 9. Out of Scope (v1)

- Real network calls / API integration
- Persistent cart (Room database)
- Actual file download for order receipt
- Orders history screen (data exists in JSON for `existing_orders_user`)
- Accessibility (a11y) audit
- iOS / tablet-specific layouts (phone portrait is primary target)
- Push notifications
- Payment processing

---

## 10. Success Criteria

- [ ] App launches and shows product grid loaded from `products.json`
- [ ] Vendor filter and sort work correctly
- [ ] Tapping a product navigates to Detail screen
- [ ] Add to Cart updates cart badge
- [ ] Cart drawer opens, shows items, quantity controls work
- [ ] Checkout navigates to Login
- [ ] `demouser` / `testingisfun99` logs in successfully
- [ ] `locked_user` shows error and does not navigate
- [ ] Checkout form validates all fields
- [ ] Confirmation shows order number
- [ ] "Continue Shopping" clears cart and returns to Product List
- [ ] All 25 products appear with correct name, vendor, price
- [ ] `image_not_loading_user` triggers placeholder images
