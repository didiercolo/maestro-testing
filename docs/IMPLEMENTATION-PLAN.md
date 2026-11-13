# BStackDemo Android App — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Android app that simulates the full e-commerce flow of bstackdemo.com — product list, product detail, cart, login, checkout, and confirmation — with all data sourced from local JSON files.

**Architecture:** Single `MainActivity` hosts a Jetpack Compose `NavHost` with 5 screen destinations. `CartViewModel` is scoped to the Activity and shared across all screens. Each screen has its own ViewModel. A `Repository` layer reads JSON assets once and caches in-memory.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Navigation Compose, ViewModel + StateFlow, kotlinx.serialization for JSON, Coil for images, JUnit4 + ComposeTestRule for tests.

---

## File Map

| File | Responsibility |
|---|---|
| `app/build.gradle.kts` | Dependencies and build config |
| `assets/products.json` | 25 product records |
| `assets/users.json` | 5 user records |
| `data/model/Product.kt` | Product data class |
| `data/model/User.kt` | User + UserType data classes |
| `data/model/CartItem.kt` | CartItem (product + quantity) |
| `data/model/Order.kt` | Completed order snapshot |
| `data/local/JsonDataSource.kt` | Reads + deserializes JSON assets |
| `data/repository/ProductRepository.kt` | Product list + detail access |
| `data/repository/UserRepository.kt` | User list + login validation |
| `ui/theme/Color.kt` | BStack color palette |
| `ui/theme/Type.kt` | Typography |
| `ui/theme/Theme.kt` | MaterialTheme wrapper |
| `ui/cart/CartViewModel.kt` | Cart state (Activity-scoped) |
| `navigation/AppNavGraph.kt` | NavHost + all routes |
| `ui/productlist/ProductListViewModel.kt` | Filter/sort state |
| `ui/productlist/ProductListScreen.kt` | Product grid composable |
| `ui/productdetail/ProductDetailViewModel.kt` | Single product state |
| `ui/productdetail/ProductDetailScreen.kt` | Detail composable |
| `ui/cart/CartDrawer.kt` | Drawer overlay composable |
| `ui/login/LoginViewModel.kt` | Login state + validation |
| `ui/login/LoginScreen.kt` | Login composable |
| `ui/checkout/CheckoutViewModel.kt` | Form state + validation |
| `ui/checkout/CheckoutScreen.kt` | Checkout composable |
| `ui/confirmation/ConfirmationScreen.kt` | Confirmation composable |
| `MainActivity.kt` | Entry point, CartViewModel host |

---

## Task 1: Project Setup

**Files:**
- Create: `StackDemoAndroid/settings.gradle.kts`
- Create: `StackDemoAndroid/build.gradle.kts`
- Create: `StackDemoAndroid/app/build.gradle.kts`
- Create: `StackDemoAndroid/app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create project via Android Studio**

  Open Android Studio → New Project → Empty Activity → configure:
  - Name: `StackDemo`
  - Package: `com.example.stackdemo`
  - Save location: `~/StackDemoAndroid`
  - Language: Kotlin
  - Min SDK: API 26

- [ ] **Step 2: Replace `app/build.gradle.kts` with the full dependency set**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.example.stackdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stackdemo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 3: Sync Gradle**

  In Android Studio: File → Sync Project with Gradle Files.  
  Expected: BUILD SUCCESSFUL in the Build output panel.

- [ ] **Step 4: Create the package directory tree**

```bash
cd ~/StackDemoAndroid/app/src/main/java/com/example/stackdemo
mkdir -p data/model data/local data/repository
mkdir -p ui/theme ui/productlist ui/productdetail ui/cart ui/login ui/checkout ui/confirmation
mkdir -p navigation
mkdir -p ../assets
```

- [ ] **Step 5: Commit skeleton**

```bash
cd ~/StackDemoAndroid
git add .
git commit -m "chore: project setup with Compose + serialization dependencies"
```

---

## Task 2: Data Models

**Files:**
- Create: `data/model/Product.kt`
- Create: `data/model/User.kt`
- Create: `data/model/CartItem.kt`
- Create: `data/model/Order.kt`
- Test: `src/test/java/com/example/stackdemo/data/model/CartItemTest.kt`

- [ ] **Step 1: Write failing test for CartItem subtotal**

Create `src/test/java/com/example/stackdemo/data/model/CartItemTest.kt`:

```kotlin
package com.example.stackdemo.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CartItemTest {

    private val testProduct = Product(
        id = 1,
        name = "iPhone 12",
        vendor = "Apple",
        price = 799.0,
        installments = 9,
        image = "iphone_12",
        description = "Test product"
    )

    @Test
    fun `subtotal equals price times quantity`() {
        val item = CartItem(product = testProduct, quantity = 3)
        assertEquals(2397.0, item.subtotal, 0.01)
    }

    @Test
    fun `installmentPrice equals price divided by installments`() {
        val item = CartItem(product = testProduct, quantity = 1)
        assertEquals(88.78, item.product.installmentPrice, 0.01)
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
./gradlew test --tests "com.example.stackdemo.data.model.CartItemTest" 
```
Expected: FAILED — `Product`, `CartItem` classes not found.

- [ ] **Step 3: Create `Product.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/model/Product.kt
package com.example.stackdemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val vendor: String,
    val price: Double,
    val installments: Int,
    val image: String,
    val description: String
) {
    val installmentPrice: Double get() = price / installments
}
```

- [ ] **Step 4: Create `User.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/model/User.kt
package com.example.stackdemo.data.model

import kotlinx.serialization.Serializable

enum class UserType {
    NORMAL, BROKEN_IMAGES, EXISTING_ORDERS, FAVOURITES, LOCKED;

    companion object {
        fun from(value: String): UserType = when (value) {
            "broken_images"   -> BROKEN_IMAGES
            "existing_orders" -> EXISTING_ORDERS
            "favourites"      -> FAVOURITES
            "locked"          -> LOCKED
            else              -> NORMAL
        }
    }
}

@Serializable
data class User(
    val username: String,
    val password: String,
    val type: String
) {
    val userType: UserType get() = UserType.from(type)
    val isLocked: Boolean get() = userType == UserType.LOCKED
    val hasBrokenImages: Boolean get() = userType == UserType.BROKEN_IMAGES
}
```

- [ ] **Step 5: Create `CartItem.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/model/CartItem.kt
package com.example.stackdemo.data.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}
```

- [ ] **Step 6: Create `Order.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/model/Order.kt
package com.example.stackdemo.data.model

data class Order(
    val orderNumber: Int,
    val items: List<CartItem>,
    val total: Double
)
```

- [ ] **Step 7: Run tests — confirm they pass**

```bash
./gradlew test --tests "com.example.stackdemo.data.model.CartItemTest"
```
Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/data/model/ \
        app/src/test/java/com/example/stackdemo/data/model/
git commit -m "feat: data models — Product, User, CartItem, Order"
```

---

## Task 3: JSON Asset Files

**Files:**
- Create: `app/src/main/assets/products.json`
- Create: `app/src/main/assets/users.json`

- [ ] **Step 1: Create `users.json`**

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

- [ ] **Step 2: Create `products.json` (all 25 products)**

```json
{
  "products": [
    { "id": 1,  "name": "iPhone 12",           "vendor": "Apple",   "price": 799.00,  "installments": 9, "image": "iphone_12",           "description": "6.1-inch Super Retina XDR display, A14 Bionic chip, 5G capable." },
    { "id": 2,  "name": "iPhone 12 Mini",      "vendor": "Apple",   "price": 699.00,  "installments": 9, "image": "iphone_12_mini",      "description": "5.4-inch compact design, A14 Bionic chip, 5G capable." },
    { "id": 3,  "name": "iPhone 12 Pro",       "vendor": "Apple",   "price": 999.00,  "installments": 5, "image": "iphone_12_pro",       "description": "6.1-inch Pro display with LiDAR Scanner and ProRAW." },
    { "id": 4,  "name": "iPhone 12 Pro Max",   "vendor": "Apple",   "price": 1099.00, "installments": 5, "image": "iphone_12_pro_max",   "description": "6.7-inch Pro Max display, largest battery in iPhone history." },
    { "id": 5,  "name": "iPhone 11",           "vendor": "Apple",   "price": 599.00,  "installments": 9, "image": "iphone_11",           "description": "6.1-inch Liquid Retina display, A13 Bionic chip, dual camera." },
    { "id": 6,  "name": "iPhone 11 Pro",       "vendor": "Apple",   "price": 799.00,  "installments": 9, "image": "iphone_11_pro",       "description": "5.8-inch Super Retina XDR, triple camera system, A13 Bionic." },
    { "id": 7,  "name": "iPhone XR",           "vendor": "Apple",   "price": 499.00,  "installments": 9, "image": "iphone_xr",           "description": "6.1-inch Liquid Retina display, A12 Bionic chip." },
    { "id": 8,  "name": "iPhone SE",           "vendor": "Apple",   "price": 399.00,  "installments": 9, "image": "iphone_se",           "description": "4.7-inch Retina HD display, A13 Bionic chip, most affordable iPhone." },
    { "id": 9,  "name": "Samsung Galaxy S21",  "vendor": "Samsung", "price": 799.00,  "installments": 9, "image": "samsung_s21",         "description": "6.2-inch Dynamic AMOLED 2X, Exynos 2100, 8K video." },
    { "id": 10, "name": "Samsung Galaxy S21+", "vendor": "Samsung", "price": 999.00,  "installments": 5, "image": "samsung_s21_plus",    "description": "6.7-inch Dynamic AMOLED 2X, 4800mAh battery, 5G." },
    { "id": 11, "name": "Samsung Galaxy S21 Ultra", "vendor": "Samsung", "price": 1199.00, "installments": 5, "image": "samsung_s21_ultra", "description": "6.8-inch, 100x Space Zoom camera, built-in S Pen support." },
    { "id": 12, "name": "Samsung Galaxy S20+", "vendor": "Samsung", "price": 899.00,  "installments": 9, "image": "samsung_s20_plus",    "description": "6.7-inch Infinity-O display, 64MP camera, 5G." },
    { "id": 13, "name": "Samsung Galaxy Note 20", "vendor": "Samsung", "price": 999.00, "installments": 5, "image": "samsung_note_20",   "description": "6.7-inch Super AMOLED Plus, S Pen, 64MP triple camera." },
    { "id": 14, "name": "Samsung Galaxy A52",  "vendor": "Samsung", "price": 499.00,  "installments": 9, "image": "samsung_a52",         "description": "6.5-inch Super AMOLED, 64MP quad camera, 5000mAh." },
    { "id": 15, "name": "Google Pixel 5",      "vendor": "Google",  "price": 699.00,  "installments": 9, "image": "pixel_5",             "description": "6-inch OLED display, Snapdragon 765G, pure Android 11." },
    { "id": 16, "name": "Google Pixel 4a",     "vendor": "Google",  "price": 349.00,  "installments": 9, "image": "pixel_4a",            "description": "5.81-inch OLED, 12.2MP camera with Night Sight, Android 11." },
    { "id": 17, "name": "Google Pixel 4a 5G",  "vendor": "Google",  "price": 499.00,  "installments": 9, "image": "pixel_4a_5g",         "description": "6.2-inch OLED, Snapdragon 765G, 5G capable." },
    { "id": 18, "name": "Google Pixel 4 XL",   "vendor": "Google",  "price": 599.00,  "installments": 9, "image": "pixel_4_xl",          "description": "6.3-inch QHD+ OLED, Snapdragon 855, face unlock." },
    { "id": 19, "name": "Google Pixel 3a",     "vendor": "Google",  "price": 299.00,  "installments": 9, "image": "pixel_3a",            "description": "5.6-inch Full HD+ OLED, 12.2MP camera, headphone jack." },
    { "id": 20, "name": "OnePlus 9 Pro",       "vendor": "OnePlus", "price": 969.00,  "installments": 5, "image": "oneplus_9_pro",       "description": "6.7-inch Fluid AMOLED, 120Hz, Hasselblad quad camera." },
    { "id": 21, "name": "OnePlus 9",           "vendor": "OnePlus", "price": 729.00,  "installments": 9, "image": "oneplus_9",           "description": "6.55-inch Fluid AMOLED, 120Hz, Snapdragon 888, 65W fast charge." },
    { "id": 22, "name": "OnePlus 8 Pro",       "vendor": "OnePlus", "price": 799.00,  "installments": 9, "image": "oneplus_8_pro",       "description": "6.78-inch Fluid AMOLED, 120Hz, Snapdragon 865, IP68." },
    { "id": 23, "name": "OnePlus Nord",        "vendor": "OnePlus", "price": 399.00,  "installments": 9, "image": "oneplus_nord",        "description": "6.44-inch Fluid AMOLED, Snapdragon 765G, 5G, dual selfie cameras." },
    { "id": 24, "name": "OnePlus 8T",          "vendor": "OnePlus", "price": 749.00,  "installments": 9, "image": "oneplus_8t",          "description": "6.55-inch Fluid AMOLED, 120Hz, 65W Warp Charge, quad camera." },
    { "id": 25, "name": "OnePlus 7T",          "vendor": "OnePlus", "price": 599.00,  "installments": 9, "image": "oneplus_7t",          "description": "6.55-inch Fluid AMOLED, 90Hz, Snapdragon 855+, triple camera." }
  ]
}
```

- [ ] **Step 3: Add a placeholder drawable for missing images**

Create `app/src/main/res/drawable/ic_product_placeholder.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportWidth="200"
    android:viewportHeight="200">
  <path android:fillColor="#EEEEEE"
        android:pathData="M0,0 L200,0 L200,200 L0,200 Z"/>
  <path android:fillColor="#BDBDBD"
        android:pathData="M80,60 L120,60 L120,100 L80,100 Z M60,110 L140,110 L140,150 L60,150 Z"/>
</vector>
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/assets/ app/src/main/res/drawable/ic_product_placeholder.xml
git commit -m "feat: add products.json, users.json assets and placeholder drawable"
```

---

## Task 4: JSON Data Source

**Files:**
- Create: `data/local/JsonDataSource.kt`
- Test: `src/test/java/com/example/stackdemo/data/local/JsonDataSourceTest.kt`

> This runs on Android (needs `Context` to open assets), so the test is an **instrumented** test (`androidTest`).

- [ ] **Step 1: Write failing instrumented test**

Create `src/androidTest/java/com/example/stackdemo/data/local/JsonDataSourceTest.kt`:

```kotlin
package com.example.stackdemo.data.local

import androidx.test.platform.app.InstrumentationRegistry
import com.example.stackdemo.data.model.UserType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsonDataSourceTest {

    private lateinit var source: JsonDataSource

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        source = JsonDataSource(context)
    }

    @Test
    fun loadProducts_returns25Items() {
        val products = source.loadProducts()
        assertEquals(25, products.size)
    }

    @Test
    fun loadProducts_firstProductIsIPhone12() {
        val first = source.loadProducts().first()
        assertEquals("iPhone 12", first.name)
        assertEquals("Apple", first.vendor)
        assertEquals(799.0, first.price, 0.01)
    }

    @Test
    fun loadUsers_returns5Users() {
        val users = source.loadUsers()
        assertEquals(5, users.size)
    }

    @Test
    fun loadUsers_lockedUserHasCorrectType() {
        val locked = source.loadUsers().first { it.username == "locked_user" }
        assertEquals(UserType.LOCKED, locked.userType)
        assertTrue(locked.isLocked)
    }

    @Test
    fun loadUsers_brokenImagesUserHasCorrectType() {
        val broken = source.loadUsers().first { it.username == "image_not_loading_user" }
        assertTrue(broken.hasBrokenImages)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew connectedAndroidTest --tests "com.example.stackdemo.data.local.JsonDataSourceTest"
```
Expected: FAILED — `JsonDataSource` class not found.

- [ ] **Step 3: Implement `JsonDataSource.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/local/JsonDataSource.kt
package com.example.stackdemo.data.local

import android.content.Context
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ProductsWrapper(val products: List<Product>)

@Serializable
private data class UsersWrapper(val users: List<User>)

class JsonDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadProducts(): List<Product> {
        val raw = context.assets.open("products.json").bufferedReader().readText()
        return json.decodeFromString<ProductsWrapper>(raw).products
    }

    fun loadUsers(): List<User> {
        val raw = context.assets.open("users.json").bufferedReader().readText()
        return json.decodeFromString<UsersWrapper>(raw).users
    }
}
```

- [ ] **Step 4: Run tests — confirm they pass**

```bash
./gradlew connectedAndroidTest --tests "com.example.stackdemo.data.local.JsonDataSourceTest"
```
Expected: 5 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/data/local/ \
        app/src/androidTest/java/com/example/stackdemo/data/local/
git commit -m "feat: JsonDataSource reads products and users from assets"
```

---

## Task 5: Repositories

**Files:**
- Create: `data/repository/ProductRepository.kt`
- Create: `data/repository/UserRepository.kt`
- Test: `src/test/java/com/example/stackdemo/data/repository/ProductRepositoryTest.kt`
- Test: `src/test/java/com/example/stackdemo/data/repository/UserRepositoryTest.kt`

- [ ] **Step 1: Write failing unit tests**

Create `src/test/java/com/example/stackdemo/data/repository/ProductRepositoryTest.kt`:

```kotlin
package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductRepositoryTest {

    private val mockSource: JsonDataSource = mock()
    private lateinit var repo: ProductRepository

    private val sampleProducts = listOf(
        Product(1, "iPhone 12", "Apple", 799.0, 9, "iphone_12", "Desc"),
        Product(2, "Galaxy S21", "Samsung", 799.0, 9, "samsung_s21", "Desc"),
        Product(3, "Pixel 5", "Google", 699.0, 9, "pixel_5", "Desc")
    )

    @Before
    fun setup() {
        whenever(mockSource.loadProducts()).thenReturn(sampleProducts)
        repo = ProductRepository(mockSource)
    }

    @Test
    fun `getAll returns all products`() {
        assertEquals(3, repo.getAll().size)
    }

    @Test
    fun `getByVendor filters correctly`() {
        val appleProducts = repo.getByVendor("Apple")
        assertEquals(1, appleProducts.size)
        assertEquals("iPhone 12", appleProducts.first().name)
    }

    @Test
    fun `getById returns correct product`() {
        val product = repo.getById(2)
        assertNotNull(product)
        assertEquals("Galaxy S21", product!!.name)
    }

    @Test
    fun `getById returns null for missing id`() {
        assertNull(repo.getById(99))
    }

    @Test
    fun `getVendors returns unique vendor list`() {
        val vendors = repo.getVendors()
        assertEquals(listOf("Apple", "Samsung", "Google"), vendors)
    }
}
```

Create `src/test/java/com/example/stackdemo/data/repository/UserRepositoryTest.kt`:

```kotlin
package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserRepositoryTest {

    private val mockSource: JsonDataSource = mock()
    private lateinit var repo: UserRepository

    private val sampleUsers = listOf(
        User("demouser", "testingisfun99", "normal"),
        User("locked_user", "testingisfun99", "locked")
    )

    @Before
    fun setup() {
        whenever(mockSource.loadUsers()).thenReturn(sampleUsers)
        repo = UserRepository(mockSource)
    }

    @Test
    fun `getAll returns all users`() {
        assertEquals(2, repo.getAll().size)
    }

    @Test
    fun `getPassword returns the shared password`() {
        assertEquals("testingisfun99", repo.getPassword())
    }

    @Test
    fun `findByUsername returns correct user`() {
        val user = repo.findByUsername("locked_user")
        assertNotNull(user)
        assertTrue(user!!.isLocked)
    }

    @Test
    fun `findByUsername returns null for unknown user`() {
        assertNull(repo.findByUsername("ghost"))
    }
}
```

- [ ] **Step 2: Add mockito-kotlin to `build.gradle.kts`**

In the `dependencies` block add:

```kotlin
testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
testImplementation("org.mockito:mockito-core:5.12.0")
```

Sync Gradle.

- [ ] **Step 3: Run tests to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.data.repository.*"
```
Expected: FAILED — classes not found.

- [ ] **Step 4: Implement `ProductRepository.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/repository/ProductRepository.kt
package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.Product

class ProductRepository(private val source: JsonDataSource) {

    private val cache: List<Product> by lazy { source.loadProducts() }

    fun getAll(): List<Product> = cache

    fun getByVendor(vendor: String): List<Product> =
        cache.filter { it.vendor == vendor }

    fun getById(id: Int): Product? = cache.firstOrNull { it.id == id }

    fun getVendors(): List<String> = cache.map { it.vendor }.distinct()
}
```

- [ ] **Step 5: Implement `UserRepository.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/data/repository/UserRepository.kt
package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.User

class UserRepository(private val source: JsonDataSource) {

    private val cache: List<User> by lazy { source.loadUsers() }

    fun getAll(): List<User> = cache

    fun getPassword(): String = cache.firstOrNull()?.password ?: "testingisfun99"

    fun findByUsername(username: String): User? =
        cache.firstOrNull { it.username == username }
}
```

- [ ] **Step 6: Run tests — confirm they pass**

```bash
./gradlew test --tests "com.example.stackdemo.data.repository.*"
```
Expected: 8 tests pass.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/data/repository/ \
        app/src/test/java/com/example/stackdemo/data/repository/ \
        app/build.gradle.kts
git commit -m "feat: ProductRepository and UserRepository with unit tests"
```

---

## Task 6: Theme

**Files:**
- Create: `ui/theme/Color.kt`
- Create: `ui/theme/Type.kt`
- Create: `ui/theme/Theme.kt`

No tests needed for pure theme files.

- [ ] **Step 1: Create `Color.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/theme/Color.kt
package com.example.stackdemo.ui.theme

import androidx.compose.ui.graphics.Color

val BStackYellow    = Color(0xFFF5A623)
val BStackDark      = Color(0xFF1A1A1A)
val BStackDarkGray  = Color(0xFF212121)
val BStackGray      = Color(0xFF757575)
val BStackLightGray = Color(0xFFEEEEEE)
val BStackError     = Color(0xFFD32F2F)
val BStackWhite     = Color(0xFFFFFFFF)
val BStackSurface   = Color(0xFFF5F5F5)
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/theme/Type.kt
package com.example.stackdemo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        color = BStackGray
    )
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/theme/Theme.kt
package com.example.stackdemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary          = BStackYellow,
    onPrimary        = BStackDark,
    secondary        = BStackDark,
    onSecondary      = BStackWhite,
    background       = BStackWhite,
    surface          = BStackSurface,
    onBackground     = BStackDarkGray,
    onSurface        = BStackDarkGray,
    error            = BStackError
)

@Composable
fun StackDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content
    )
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/theme/
git commit -m "feat: BStack theme — colors, typography, MaterialTheme wrapper"
```

---

## Task 7: CartViewModel

**Files:**
- Create: `ui/cart/CartViewModel.kt`
- Test: `src/test/java/com/example/stackdemo/ui/cart/CartViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
// src/test/java/com/example/stackdemo/ui/cart/CartViewModelTest.kt
package com.example.stackdemo.ui.cart

import com.example.stackdemo.data.model.CartItem
import com.example.stackdemo.data.model.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartViewModelTest {

    private lateinit var vm: CartViewModel
    private val iphone = Product(1, "iPhone 12", "Apple", 799.0, 9, "iphone_12", "Desc")
    private val pixel  = Product(2, "Pixel 5",   "Google", 699.0, 9, "pixel_5",  "Desc")

    @Before fun setup() { vm = CartViewModel() }

    @Test
    fun `initial state is empty`() {
        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(0.0, vm.uiState.value.subtotal, 0.01)
        assertEquals(0, vm.uiState.value.itemCount)
    }

    @Test
    fun `addProduct adds new item with quantity 1`() {
        vm.addProduct(iphone)
        val items = vm.uiState.value.items
        assertEquals(1, items.size)
        assertEquals(1, items.first().quantity)
    }

    @Test
    fun `addProduct twice increments quantity`() {
        vm.addProduct(iphone)
        vm.addProduct(iphone)
        val items = vm.uiState.value.items
        assertEquals(1, items.size)
        assertEquals(2, items.first().quantity)
    }

    @Test
    fun `increaseQuantity increments quantity`() {
        vm.addProduct(iphone)
        vm.increaseQuantity(iphone.id)
        assertEquals(2, vm.uiState.value.items.first().quantity)
    }

    @Test
    fun `decreaseQuantity removes item when quantity reaches 0`() {
        vm.addProduct(iphone)
        vm.decreaseQuantity(iphone.id)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `removeProduct removes item`() {
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        vm.removeProduct(iphone.id)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Pixel 5", vm.uiState.value.items.first().product.name)
    }

    @Test
    fun `subtotal reflects all items`() {
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        assertEquals(1498.0, vm.uiState.value.subtotal, 0.01)
    }

    @Test
    fun `itemCount is sum of all quantities`() {
        vm.addProduct(iphone)
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        assertEquals(3, vm.uiState.value.itemCount)
    }

    @Test
    fun `clear empties the cart`() {
        vm.addProduct(iphone)
        vm.clear()
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `snapshotOrder returns Order with correct total and random number`() {
        vm.addProduct(iphone)
        val order = vm.snapshotOrder()
        assertEquals(1, order.items.size)
        assertEquals(799.0, order.total, 0.01)
        assertTrue(order.orderNumber in 10000..99999)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.ui.cart.CartViewModelTest"
```
Expected: FAILED — `CartViewModel` not found.

- [ ] **Step 3: Implement `CartViewModel.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/cart/CartViewModel.kt
package com.example.stackdemo.ui.cart

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.CartItem
import com.example.stackdemo.data.model.Order
import com.example.stackdemo.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartUiState(
    val items: List<CartItem> = emptyList()
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val itemCount: Int   get() = items.sumOf { it.quantity }
}

class CartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        _uiState.update { state ->
            val existing = state.items.firstOrNull { it.product.id == product.id }
            val newItems = if (existing != null) {
                state.items.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.items + CartItem(product, 1)
            }
            state.copy(items = newItems)
        }
    }

    fun increaseQuantity(productId: Int) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
            })
        }
    }

    fun decreaseQuantity(productId: Int) {
        _uiState.update { state ->
            val updated = state.items.mapNotNull {
                if (it.product.id == productId) {
                    if (it.quantity <= 1) null else it.copy(quantity = it.quantity - 1)
                } else it
            }
            state.copy(items = updated)
        }
    }

    fun removeProduct(productId: Int) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.product.id != productId })
        }
    }

    fun clear() {
        _uiState.value = CartUiState()
    }

    fun snapshotOrder(): Order {
        val state = _uiState.value
        return Order(
            orderNumber = (10000..99999).random(),
            items       = state.items.toList(),
            total       = state.subtotal
        )
    }
}
```

- [ ] **Step 4: Run tests — confirm they pass**

```bash
./gradlew test --tests "com.example.stackdemo.ui.cart.CartViewModelTest"
```
Expected: 10 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/cart/CartViewModel.kt \
        app/src/test/java/com/example/stackdemo/ui/cart/CartViewModelTest.kt
git commit -m "feat: CartViewModel with add/remove/quantity/clear/snapshot"
```

---

## Task 8: Navigation Graph

**Files:**
- Create: `navigation/AppNavGraph.kt`

No unit tests — navigation is verified in Compose UI tests.

- [ ] **Step 1: Create `AppNavGraph.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/navigation/AppNavGraph.kt
package com.example.stackdemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.checkout.CheckoutScreen
import com.example.stackdemo.ui.confirmation.ConfirmationScreen
import com.example.stackdemo.ui.login.LoginScreen
import com.example.stackdemo.ui.productdetail.ProductDetailScreen
import com.example.stackdemo.ui.productlist.ProductListScreen

object Routes {
    const val PRODUCT_LIST   = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val LOGIN          = "login"
    const val CHECKOUT       = "checkout"
    const val CONFIRMATION   = "confirmation/{orderNumber}"

    fun productDetail(id: Int) = "product_detail/$id"
    fun confirmation(orderNumber: Int) = "confirmation/$orderNumber"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    cartViewModel: CartViewModel,
    onOpenCart: () -> Unit
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.PRODUCT_LIST
    ) {
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                cartViewModel = cartViewModel,
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                onCartClick    = onOpenCart
            )
        }

        composable(Routes.PRODUCT_DETAIL) { backStack ->
            val productId = backStack.arguments?.getString("productId")?.toIntOrNull() ?: return@composable
            ProductDetailScreen(
                productId     = productId,
                cartViewModel = cartViewModel,
                onBack        = { navController.popBackStack() },
                onCartClick   = onOpenCart
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.CHECKOUT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                cartViewModel  = cartViewModel,
                onOrderPlaced  = { order ->
                    navController.navigate(Routes.confirmation(order.orderNumber)) {
                        popUpTo(Routes.PRODUCT_LIST) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.CONFIRMATION) { backStack ->
            val orderNumber = backStack.arguments?.getString("orderNumber")?.toIntOrNull() ?: 0
            ConfirmationScreen(
                orderNumber      = orderNumber,
                cartViewModel    = cartViewModel,
                onContinueShopping = {
                    cartViewModel.clear()
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/navigation/
git commit -m "feat: AppNavGraph with all 5 screen routes"
```

---

## Task 9: ProductListViewModel

**Files:**
- Create: `ui/productlist/ProductListViewModel.kt`
- Test: `src/test/java/com/example/stackdemo/ui/productlist/ProductListViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
// src/test/java/com/example/stackdemo/ui/productlist/ProductListViewModelTest.kt
package com.example.stackdemo.ui.productlist

import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductListViewModelTest {

    private val mockRepo: ProductRepository = mock()
    private lateinit var vm: ProductListViewModel

    private val products = listOf(
        Product(1, "iPhone 12",   "Apple",   799.0,  9, "iphone_12",  "Desc"),
        Product(2, "Galaxy S21",  "Samsung", 799.0,  9, "samsung_s21", "Desc"),
        Product(3, "Pixel 5",     "Google",  699.0,  9, "pixel_5",     "Desc"),
        Product(4, "OnePlus 9",   "OnePlus", 729.0,  9, "oneplus_9",   "Desc"),
        Product(5, "iPhone SE",   "Apple",   399.0,  9, "iphone_se",   "Desc")
    )

    @Before
    fun setup() {
        whenever(mockRepo.getAll()).thenReturn(products)
        whenever(mockRepo.getVendors()).thenReturn(listOf("Apple", "Samsung", "Google", "OnePlus"))
        whenever(mockRepo.getByVendor("Apple")).thenReturn(products.filter { it.vendor == "Apple" })
        vm = ProductListViewModel(mockRepo)
    }

    @Test
    fun `initial state loads all products`() {
        assertEquals(5, vm.uiState.value.products.size)
    }

    @Test
    fun `initial state has no vendor filter`() {
        assertNull(vm.uiState.value.selectedVendor)
    }

    @Test
    fun `selectVendor filters products`() {
        vm.selectVendor("Apple")
        assertEquals(2, vm.uiState.value.products.size)
        assertTrue(vm.uiState.value.products.all { it.vendor == "Apple" })
    }

    @Test
    fun `selectVendor same vendor clears filter`() {
        vm.selectVendor("Apple")
        vm.selectVendor("Apple")
        assertNull(vm.uiState.value.selectedVendor)
        assertEquals(5, vm.uiState.value.products.size)
    }

    @Test
    fun `sortOrder LOW_HIGH sorts ascending`() {
        vm.setSortOrder(SortOrder.PRICE_LOW_HIGH)
        val prices = vm.uiState.value.products.map { it.price }
        assertEquals(prices.sorted(), prices)
    }

    @Test
    fun `sortOrder HIGH_LOW sorts descending`() {
        vm.setSortOrder(SortOrder.PRICE_HIGH_LOW)
        val prices = vm.uiState.value.products.map { it.price }
        assertEquals(prices.sortedDescending(), prices)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.ui.productlist.ProductListViewModelTest"
```
Expected: FAILED — classes not found.

- [ ] **Step 3: Implement `ProductListViewModel.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/productlist/ProductListViewModel.kt
package com.example.stackdemo.ui.productlist

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SortOrder { DEFAULT, PRICE_LOW_HIGH, PRICE_HIGH_LOW }

data class ProductListUiState(
    val products: List<Product>   = emptyList(),
    val vendors: List<String>     = emptyList(),
    val selectedVendor: String?   = null,
    val sortOrder: SortOrder      = SortOrder.DEFAULT,
    val isLoading: Boolean        = true
)

class ProductListViewModel(private val repo: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        _uiState.update { it.copy(
            products  = repo.getAll(),
            vendors   = repo.getVendors(),
            isLoading = false
        ) }
    }

    fun selectVendor(vendor: String) {
        _uiState.update { state ->
            val newVendor = if (state.selectedVendor == vendor) null else vendor
            val base      = if (newVendor == null) repo.getAll() else repo.getByVendor(newVendor)
            state.copy(
                selectedVendor = newVendor,
                products       = applySortOrder(base, state.sortOrder)
            )
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { state ->
            state.copy(
                sortOrder = order,
                products  = applySortOrder(state.products, order)
            )
        }
    }

    private fun applySortOrder(list: List<Product>, order: SortOrder): List<Product> =
        when (order) {
            SortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOrder.DEFAULT        -> list
        }
}
```

- [ ] **Step 4: Run tests — confirm they pass**

```bash
./gradlew test --tests "com.example.stackdemo.ui.productlist.ProductListViewModelTest"
```
Expected: 6 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/productlist/ProductListViewModel.kt \
        app/src/test/java/com/example/stackdemo/ui/productlist/ProductListViewModelTest.kt
git commit -m "feat: ProductListViewModel with vendor filter and sort order"
```

---

## Task 10: ProductListScreen

**Files:**
- Create: `ui/productlist/ProductListScreen.kt`

- [ ] **Step 1: Implement `ProductListScreen.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/productlist/ProductListScreen.kt
package com.example.stackdemo.ui.productlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stackdemo.R
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.theme.BStackDark
import com.example.stackdemo.ui.theme.BStackYellow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    cartViewModel: CartViewModel,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(LocalContext.current)
    )
) {
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val cartState  by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var sortExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BrowserStack", fontWeight = FontWeight.Bold)
                        Text("Demo", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    Text("Sign In", modifier = Modifier.padding(end = 8.dp))
                    BadgedBox(
                        badge = {
                            if (cartState.itemCount > 0)
                                Badge { Text(cartState.itemCount.toString()) }
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onCartClick() }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Vendor filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.vendors.forEach { vendor ->
                    FilterChip(
                        selected = uiState.selectedVendor == vendor,
                        onClick  = { viewModel.selectVendor(vendor) },
                        label    = { Text(vendor) }
                    )
                }
            }

            // Sort row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${uiState.products.size} Product(s) found.",
                    style = MaterialTheme.typography.bodyMedium)
                Box {
                    OutlinedButton(onClick = { sortExpanded = true }) {
                        Text(when (uiState.sortOrder) {
                            SortOrder.DEFAULT       -> "Order by: Select"
                            SortOrder.PRICE_LOW_HIGH -> "Price: Low → High"
                            SortOrder.PRICE_HIGH_LOW -> "Price: High → Low"
                        })
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        DropdownMenuItem(text = { Text("Price: Low → High") }, onClick = {
                            viewModel.setSortOrder(SortOrder.PRICE_LOW_HIGH); sortExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Price: High → Low") }, onClick = {
                            viewModel.setSortOrder(SortOrder.PRICE_HIGH_LOW); sortExpanded = false
                        })
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(
                            product      = product,
                            onCardClick  = { onProductClick(product.id) },
                            onAddToCart  = {
                                cartViewModel.addProduct(product)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    var isFavourite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(drawableIdForName(LocalContext.current, product.image))
                        .error(R.drawable.ic_product_placeholder)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxWidth().aspectRatio(1f)
                )
                IconButton(
                    onClick  = { isFavourite = !isFavourite },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("$${String.format(Locale.US, "%.2f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium)
                Text("or ${product.installments} x $${"%.2f".format(product.installmentPrice)}",
                    style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BStackYellow, contentColor = BStackDark)
            ) {
                Text("Add to cart")
            }
        }
    }
}

private fun drawableIdForName(context: android.content.Context, name: String): Int =
    context.resources.getIdentifier(name, "drawable", context.packageName)
        .takeIf { it != 0 } ?: R.drawable.ic_product_placeholder
```

- [ ] **Step 2: Create `ProductListViewModelFactory.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/productlist/ProductListViewModelFactory.kt
package com.example.stackdemo.ui.productlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.repository.ProductRepository

class ProductListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val source = JsonDataSource(context.applicationContext)
        val repo   = ProductRepository(source)
        @Suppress("UNCHECKED_CAST")
        return ProductListViewModel(repo) as T
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/productlist/
git commit -m "feat: ProductListScreen with grid, filters, sort, and cart integration"
```

---

## Task 11: ProductDetailViewModel + Screen

**Files:**
- Create: `ui/productdetail/ProductDetailViewModel.kt`
- Create: `ui/productdetail/ProductDetailScreen.kt`
- Test: `src/test/java/com/example/stackdemo/ui/productdetail/ProductDetailViewModelTest.kt`

- [ ] **Step 1: Write failing test**

```kotlin
// src/test/java/com/example/stackdemo/ui/productdetail/ProductDetailViewModelTest.kt
package com.example.stackdemo.ui.productdetail

import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductDetailViewModelTest {

    private val mockRepo: ProductRepository = mock()
    private val product = Product(1, "iPhone 12", "Apple", 799.0, 9, "iphone_12", "Great phone")

    @Test
    fun `loadProduct puts product in state`() {
        whenever(mockRepo.getById(1)).thenReturn(product)
        val vm = ProductDetailViewModel(mockRepo, productId = 1)
        assertEquals("iPhone 12", vm.uiState.value.product?.name)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadProduct with invalid id leaves product null`() {
        whenever(mockRepo.getById(99)).thenReturn(null)
        val vm = ProductDetailViewModel(mockRepo, productId = 99)
        assertNull(vm.uiState.value.product)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.ui.productdetail.ProductDetailViewModelTest"
```
Expected: FAILED.

- [ ] **Step 3: Implement `ProductDetailViewModel.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/productdetail/ProductDetailViewModel.kt
package com.example.stackdemo.ui.productdetail

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true
)

class ProductDetailViewModel(
    private val repo: ProductRepository,
    productId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(product = repo.getById(productId), isLoading = false) }
    }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew test --tests "com.example.stackdemo.ui.productdetail.ProductDetailViewModelTest"
```
Expected: 2 tests pass.

- [ ] **Step 5: Implement `ProductDetailScreen.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/productdetail/ProductDetailScreen.kt
package com.example.stackdemo.ui.productdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stackdemo.R
import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.repository.ProductRepository
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.theme.BStackDark
import com.example.stackdemo.ui.theme.BStackYellow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val context = LocalContext.current
    val vm: ProductDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T {
            val repo = ProductRepository(JsonDataSource(context.applicationContext))
            @Suppress("UNCHECKED_CAST")
            return ProductDetailViewModel(repo, productId) as T
        }
    })
    val state       by vm.uiState.collectAsStateWithLifecycle()
    val cartState   by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbar    = remember { SnackbarHostState() }
    var isFavourite by remember { mutableStateOf(false) }
    var addedFlash  by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (cartState.itemCount > 0)
                                Badge { Text(cartState.itemCount.toString()) }
                        },
                        modifier = Modifier.padding(end = 16.dp).also { Modifier.clickable { onCartClick() } }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart",
                            modifier = Modifier.clickable { onCartClick() })
                    }
                }
            )
        }
    ) { padding ->
        val product = state.product
        if (state.isLoading || product == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator()
                else Text("Product not found.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(context.resources.getIdentifier(product.image, "drawable", context.packageName))
                    .error(R.drawable.ic_product_placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxWidth().aspectRatio(1f)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    IconButton(onClick = { isFavourite = !isFavourite }) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                AssistChip(onClick = {}, label = { Text(product.vendor) },
                    modifier = Modifier.padding(vertical = 4.dp))

                Text("$${String.format(Locale.US, "%.2f", product.price)}",
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("or ${product.installments} x $${"%.2f".format(product.installmentPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(Modifier.height(12.dp))
                Text(product.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        cartViewModel.addProduct(product)
                        addedFlash = true
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BStackYellow, contentColor = BStackDark)
                ) {
                    Text(if (addedFlash) "ADDED ✓" else "ADD TO CART")
                }
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Modifier.clickable(noinline onClick: () -> Unit) =
    this.then(androidx.compose.foundation.clickable { onClick() })
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/productdetail/ \
        app/src/test/java/com/example/stackdemo/ui/productdetail/
git commit -m "feat: ProductDetailScreen and ProductDetailViewModel"
```

---

## Task 12: CartDrawer

**Files:**
- Create: `ui/cart/CartDrawer.kt`

- [ ] **Step 1: Implement `CartDrawer.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/cart/CartDrawer.kt
package com.example.stackdemo.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stackdemo.R
import com.example.stackdemo.data.model.CartItem
import com.example.stackdemo.ui.theme.BStackDark
import com.example.stackdemo.ui.theme.BStackWhite
import java.util.Locale

@Composable
fun CartDrawer(
    cartViewModel: CartViewModel,
    onCheckout: () -> Unit,
    onClose: () -> Unit
) {
    val state by cartViewModel.uiState.collectAsStateWithLifecycle()

    ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bag", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge { Text(state.itemCount.toString()) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close cart")
                }
            }
        }

        HorizontalDivider()

        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Add some products in the bag :)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                items(state.items, key = { it.product.id }) { item ->
                    CartItemRow(
                        item       = item,
                        onIncrease = { cartViewModel.increaseQuantity(item.product.id) },
                        onDecrease = { cartViewModel.decreaseQuantity(item.product.id) },
                        onRemove   = { cartViewModel.removeProduct(item.product.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // Subtotal + Checkout
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SUBTOTAL", style = MaterialTheme.typography.labelSmall)
            Text("$${String.format(Locale.US, "%.2f", state.subtotal)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick  = onCheckout,
            enabled  = state.items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = BStackDark, contentColor = BStackWhite)
        ) {
            Text("CHECKOUT")
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(context.resources.getIdentifier(item.product.image, "drawable", context.packageName))
                .error(R.drawable.ic_product_placeholder)
                .build(),
            contentDescription = item.product.name,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(item.product.vendor, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
            Text("$${String.format(Locale.US, "%.2f", item.subtotal)}", fontWeight = FontWeight.SemiBold)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/cart/CartDrawer.kt
git commit -m "feat: CartDrawer with item list, quantity controls, and checkout CTA"
```

---

## Task 13: LoginViewModel + Screen

**Files:**
- Create: `ui/login/LoginViewModel.kt`
- Create: `ui/login/LoginScreen.kt`
- Test: `src/test/java/com/example/stackdemo/ui/login/LoginViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
// src/test/java/com/example/stackdemo/ui/login/LoginViewModelTest.kt
package com.example.stackdemo.ui.login

import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginViewModelTest {

    private val mockRepo: UserRepository = mock()
    private lateinit var vm: LoginViewModel

    private val users = listOf(
        User("demouser",    "testingisfun99", "normal"),
        User("locked_user", "testingisfun99", "locked")
    )

    @Before
    fun setup() {
        whenever(mockRepo.getAll()).thenReturn(users)
        whenever(mockRepo.getPassword()).thenReturn("testingisfun99")
        vm = LoginViewModel(mockRepo)
    }

    @Test
    fun `initial state has no selection`() {
        assertNull(vm.uiState.value.selectedUser)
        assertNull(vm.uiState.value.selectedPassword)
        assertFalse(vm.uiState.value.canLogin)
    }

    @Test
    fun `selecting user and password enables login`() {
        vm.selectUser(users[0])
        vm.selectPassword("testingisfun99")
        assertTrue(vm.uiState.value.canLogin)
    }

    @Test
    fun `login with demouser succeeds`() {
        whenever(mockRepo.findByUsername("demouser")).thenReturn(users[0])
        vm.selectUser(users[0])
        vm.selectPassword("testingisfun99")
        val result = vm.login()
        assertTrue(result)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `login with locked_user sets error`() {
        whenever(mockRepo.findByUsername("locked_user")).thenReturn(users[1])
        vm.selectUser(users[1])
        vm.selectPassword("testingisfun99")
        val result = vm.login()
        assertFalse(result)
        assertEquals("Your account has been locked.", vm.uiState.value.error)
    }

    @Test
    fun `users list loaded from repository`() {
        assertEquals(2, vm.uiState.value.users.size)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.ui.login.LoginViewModelTest"
```
Expected: FAILED.

- [ ] **Step 3: Implement `LoginViewModel.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/login/LoginViewModel.kt
package com.example.stackdemo.ui.login

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val users: List<User>          = emptyList(),
    val password: String           = "",
    val selectedUser: User?        = null,
    val selectedPassword: String?  = null,
    val error: String?             = null
) {
    val canLogin: Boolean get() = selectedUser != null && selectedPassword != null
}

class LoginViewModel(private val repo: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(users = repo.getAll(), password = repo.getPassword()) }
    }

    fun selectUser(user: User) {
        _uiState.update { it.copy(selectedUser = user, error = null) }
    }

    fun selectPassword(password: String) {
        _uiState.update { it.copy(selectedPassword = password, error = null) }
    }

    fun login(): Boolean {
        val user = _uiState.value.selectedUser ?: return false
        return if (user.isLocked) {
            _uiState.update { it.copy(error = "Your account has been locked.") }
            false
        } else {
            true
        }
    }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew test --tests "com.example.stackdemo.ui.login.LoginViewModelTest"
```
Expected: 5 tests pass.

- [ ] **Step 5: Implement `LoginScreen.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/login/LoginScreen.kt
package com.example.stackdemo.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val vm: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T {
            val repo = UserRepository(JsonDataSource(context.applicationContext))
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repo) as T
        }
    })
    val state by vm.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.width(320.dp).padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("BrowserStack", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Demo", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                Spacer(Modifier.height(8.dp))

                // Username dropdown
                DropdownSelector(
                    label    = "Select Username",
                    options  = state.users,
                    selected = state.selectedUser?.username,
                    display  = { it.username },
                    onSelect = { vm.selectUser(it) }
                )

                // Password dropdown
                DropdownSelector(
                    label    = "Select Password",
                    options  = listOf(state.password),
                    selected = state.selectedPassword,
                    display  = { it },
                    onSelect = { vm.selectPassword(it) }
                )

                state.error?.let { errorMsg ->
                    Text(errorMsg, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    onClick  = { if (vm.login()) onLoginSuccess() },
                    enabled  = state.canLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = com.example.stackdemo.ui.theme.BStackDark,
                        contentColor   = com.example.stackdemo.ui.theme.BStackWhite
                    )
                ) {
                    Text("LOG IN")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSelector(
    label: String,
    options: List<T>,
    selected: String?,
    display: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value         = selected ?: "",
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text    = { Text(display(option)) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/login/ \
        app/src/test/java/com/example/stackdemo/ui/login/
git commit -m "feat: LoginScreen + LoginViewModel with locked_user error handling"
```

---

## Task 14: CheckoutViewModel + Screen

**Files:**
- Create: `ui/checkout/CheckoutViewModel.kt`
- Create: `ui/checkout/CheckoutScreen.kt`
- Test: `src/test/java/com/example/stackdemo/ui/checkout/CheckoutViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
// src/test/java/com/example/stackdemo/ui/checkout/CheckoutViewModelTest.kt
package com.example.stackdemo.ui.checkout

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CheckoutViewModelTest {

    private lateinit var vm: CheckoutViewModel

    @Before fun setup() { vm = CheckoutViewModel() }

    @Test
    fun `initial state has empty form and no errors`() {
        val s = vm.uiState.value
        assertTrue(s.firstName.isEmpty())
        assertTrue(s.errors.isEmpty())
    }

    @Test
    fun `validate returns false when all fields empty`() {
        assertFalse(vm.validate())
        assertEquals(5, vm.uiState.value.errors.size)
    }

    @Test
    fun `validate returns true when all fields filled`() {
        vm.update(firstName = "John", lastName = "Doe", address = "123 Main St",
                  state = "CA", postalCode = "90210")
        assertTrue(vm.validate())
        assertTrue(vm.uiState.value.errors.isEmpty())
    }

    @Test
    fun `invalid postal code sets error`() {
        vm.update(firstName = "John", lastName = "Doe", address = "123 Main St",
                  state = "CA", postalCode = "ABC")
        vm.validate()
        assertNotNull(vm.uiState.value.errors["postalCode"])
    }

    @Test
    fun `missing lastName sets error`() {
        vm.update(firstName = "John", lastName = "", address = "123 Main St",
                  state = "CA", postalCode = "90210")
        vm.validate()
        assertNotNull(vm.uiState.value.errors["lastName"])
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "com.example.stackdemo.ui.checkout.CheckoutViewModelTest"
```
Expected: FAILED.

- [ ] **Step 3: Implement `CheckoutViewModel.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/checkout/CheckoutViewModel.kt
package com.example.stackdemo.ui.checkout

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CheckoutUiState(
    val firstName:  String = "",
    val lastName:   String = "",
    val address:    String = "",
    val state:      String = "",
    val postalCode: String = "",
    val errors: Map<String, String> = emptyMap()
)

class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun update(
        firstName:  String = _uiState.value.firstName,
        lastName:   String = _uiState.value.lastName,
        address:    String = _uiState.value.address,
        state:      String = _uiState.value.state,
        postalCode: String = _uiState.value.postalCode
    ) {
        _uiState.update { it.copy(
            firstName  = firstName,
            lastName   = lastName,
            address    = address,
            state      = state,
            postalCode = postalCode,
            errors     = emptyMap()
        ) }
    }

    fun validate(): Boolean {
        val s = _uiState.value
        val errors = buildMap<String, String> {
            if (s.firstName.isBlank())  put("firstName",  "Required")
            if (s.lastName.isBlank())   put("lastName",   "Required")
            if (s.address.isBlank())    put("address",    "Required")
            if (s.state.isBlank())      put("state",      "Required")
            if (s.postalCode.isBlank()) put("postalCode", "Required")
            else if (!s.postalCode.matches(Regex("\\d{5,}")))
                put("postalCode", "Enter a valid postal code")
        }
        _uiState.update { it.copy(errors = errors) }
        return errors.isEmpty()
    }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew test --tests "com.example.stackdemo.ui.checkout.CheckoutViewModelTest"
```
Expected: 5 tests pass.

- [ ] **Step 5: Implement `CheckoutScreen.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/checkout/CheckoutScreen.kt
package com.example.stackdemo.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stackdemo.data.model.Order
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.theme.BStackDark
import com.example.stackdemo.ui.theme.BStackWhite
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onOrderPlaced: (Order) -> Unit,
    vm: CheckoutViewModel = viewModel()
) {
    val formState by vm.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("StackDemo", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Shipping form
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Shipping Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                FormField("First Name", formState.firstName, formState.errors["firstName"]) {
                    vm.update(firstName = it)
                }
                FormField("Last Name", formState.lastName, formState.errors["lastName"]) {
                    vm.update(lastName = it)
                }
                FormField("Address", formState.address, formState.errors["address"]) {
                    vm.update(address = it)
                }
                FormField("State/Province", formState.state, formState.errors["state"]) {
                    vm.update(state = it)
                }
                FormField("Postal Code", formState.postalCode, formState.errors["postalCode"]) {
                    vm.update(postalCode = it)
                }

                Button(
                    onClick = {
                        if (vm.validate()) {
                            val order = cartViewModel.snapshotOrder()
                            onOrderPlaced(order)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BStackDark, contentColor = BStackWhite)
                ) {
                    Text("SUBMIT")
                }
            }

            // Order summary
            OrderSummaryCard(
                items = cartState.items,
                total = cartState.subtotal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FormField(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
fun OrderSummaryCard(
    items: List<com.example.stackdemo.data.model.CartItem>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Text("${items.sumOf { it.quantity }} item(s)", style = MaterialTheme.typography.bodyMedium)
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, style = MaterialTheme.typography.bodyMedium)
                        Text(item.product.vendor, style = MaterialTheme.typography.labelSmall)
                        Text("Qty: ${item.quantity}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("$${String.format(Locale.US, "%.0f", item.subtotal)}", fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total (USD)", fontWeight = FontWeight.Bold)
                Text("$${String.format(Locale.US, "%.2f", total)}", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/checkout/ \
        app/src/test/java/com/example/stackdemo/ui/checkout/
git commit -m "feat: CheckoutScreen + CheckoutViewModel with field validation"
```

---

## Task 15: ConfirmationScreen

**Files:**
- Create: `ui/confirmation/ConfirmationScreen.kt`

- [ ] **Step 1: Implement `ConfirmationScreen.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/ui/confirmation/ConfirmationScreen.kt
package com.example.stackdemo.ui.confirmation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.checkout.OrderSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    orderNumber: Int,
    cartViewModel: CartViewModel,
    onContinueShopping: () -> Unit
) {
    val cartState     by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = { TopAppBar(title = { Text("StackDemo", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Your Order has been successfully placed.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Your order number is $orderNumber.",
                    style = MaterialTheme.typography.bodyMedium)

                TextButton(onClick = {
                    scope.launch { snackbarState.showSnackbar("Receipt downloaded") }
                }) {
                    Text("Download order receipt",
                        color = MaterialTheme.colorScheme.tertiary)
                }

                HorizontalDivider()

                OutlinedButton(onClick = onContinueShopping) {
                    Text("CONTINUE SHOPPING »")
                }
            }

            OrderSummaryCard(
                items    = cartState.items,
                total    = cartState.subtotal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/ui/confirmation/
git commit -m "feat: ConfirmationScreen with order number, receipt snackbar, continue shopping"
```

---

## Task 16: MainActivity — Wire Everything Together

**Files:**
- Modify: `MainActivity.kt`

- [ ] **Step 1: Implement `MainActivity.kt`**

```kotlin
// app/src/main/java/com/example/stackdemo/MainActivity.kt
package com.example.stackdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.stackdemo.navigation.Routes
import com.example.stackdemo.ui.cart.CartDrawer
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.theme.StackDemoTheme
import com.example.stackdemo.navigation.AppNavGraph
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackDemoTheme {
                val navController = rememberNavController()
                val drawerState   = rememberDrawerState(DrawerValue.Closed)
                val scope         = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState   = drawerState,
                    gesturesEnabled = drawerState.isOpen,
                    drawerContent = {
                        CartDrawer(
                            cartViewModel = cartViewModel,
                            onCheckout = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Routes.LOGIN)
                            },
                            onClose = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    AppNavGraph(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        onOpenCart    = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Update `AndroidManifest.xml` internet permission (optional, for future network use)**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="true"
        android:label="StackDemo"
        android:theme="@style/Theme.StackDemo">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 3: Build the app**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL, `app-debug.apk` generated in `app/build/outputs/apk/debug/`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/stackdemo/MainActivity.kt \
        app/src/main/AndroidManifest.xml
git commit -m "feat: MainActivity wires NavHost, CartViewModel, and CartDrawer"
```

---

## Task 17: Compose UI Tests — End-to-End

**Files:**
- Create: `src/androidTest/java/com/example/stackdemo/E2EFlowTest.kt`
- Create: `src/androidTest/java/com/example/stackdemo/LoginScreenTest.kt`

- [ ] **Step 1: Implement `LoginScreenTest.kt`**

```kotlin
// src/androidTest/java/com/example/stackdemo/LoginScreenTest.kt
package com.example.stackdemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository
import com.example.stackdemo.ui.login.LoginScreen
import com.example.stackdemo.ui.login.LoginViewModel
import com.example.stackdemo.ui.theme.StackDemoTheme
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginScreenTest {

    @get:Rule val rule = createComposeRule()

    private val users = listOf(
        User("demouser",    "testingisfun99", "normal"),
        User("locked_user", "testingisfun99", "locked")
    )

    @Test
    fun loginButton_disabledWithNoSelection() {
        rule.setContent {
            StackDemoTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }
        rule.onNodeWithText("LOG IN").assertIsNotEnabled()
    }

    @Test
    fun lockedUser_showsErrorMessage() {
        rule.setContent {
            StackDemoTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }
        rule.onNodeWithText("Select Username").performClick()
        rule.onNodeWithText("locked_user").performClick()
        rule.onNodeWithText("Select Password").performClick()
        rule.onNodeWithText("testingisfun99").performClick()
        rule.onNodeWithText("LOG IN").performClick()
        rule.onNodeWithText("Your account has been locked.").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Implement `E2EFlowTest.kt`**

```kotlin
// src/androidTest/java/com/example/stackdemo/E2EFlowTest.kt
package com.example.stackdemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class E2EFlowTest {

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun happyPath_addToCart_login_checkout_confirmation() {
        // 1. Product list is shown
        rule.onNodeWithText("BrowserStack").assertIsDisplayed()

        // 2. Add iPhone 12 to cart
        rule.onNodeWithText("Add to cart").onFirst().performClick()
        // Cart badge appears
        rule.onNodeWithText("1").assertIsDisplayed()

        // 3. Open cart
        rule.onContentDescriptionMatching("Cart").onFirst().performClick()
        rule.onNodeWithText("CHECKOUT").assertIsDisplayed()

        // 4. Go to checkout → login
        rule.onNodeWithText("CHECKOUT").performClick()
        rule.onNodeWithText("LOG IN").assertIsDisplayed()

        // 5. Select demouser
        rule.onNodeWithText("Select Username").performClick()
        rule.onNodeWithText("demouser").performClick()
        rule.onNodeWithText("Select Password").performClick()
        rule.onNodeWithText("testingisfun99").performClick()
        rule.onNodeWithText("LOG IN").performClick()

        // 6. Checkout form
        rule.onNodeWithText("Shipping Address").assertIsDisplayed()
        rule.onNodeWithText("First Name").performTextInput("John")
        rule.onNodeWithText("Last Name").performTextInput("Doe")
        rule.onNodeWithText("Address").performTextInput("123 Main St")
        rule.onNodeWithText("State/Province").performTextInput("California")
        rule.onNodeWithText("Postal Code").performTextInput("90210")
        rule.onNodeWithText("SUBMIT").performClick()

        // 7. Confirmation screen
        rule.onNodeWithText("Your Order has been successfully placed.").assertIsDisplayed()
        rule.onNodeWithText("CONTINUE SHOPPING »").assertIsDisplayed()
    }

    @Test
    fun lockedUser_cannotProceedToCheckout() {
        rule.onNodeWithText("Add to cart").onFirst().performClick()
        rule.onContentDescriptionMatching("Cart").onFirst().performClick()
        rule.onNodeWithText("CHECKOUT").performClick()

        rule.onNodeWithText("Select Username").performClick()
        rule.onNodeWithText("locked_user").performClick()
        rule.onNodeWithText("Select Password").performClick()
        rule.onNodeWithText("testingisfun99").performClick()
        rule.onNodeWithText("LOG IN").performClick()

        rule.onNodeWithText("Your account has been locked.").assertIsDisplayed()
        rule.onNodeWithText("Shipping Address").assertDoesNotExist()
    }

    @Test
    fun vendorFilter_showsOnlyAppleProducts() {
        rule.onNodeWithText("Apple").performClick()
        rule.onNodeWithText("Samsung").assertDoesNotExist()
    }

    @Test
    fun cartDrawer_emptyState_showsPlaceholder() {
        rule.onContentDescriptionMatching("Cart").onFirst().performClick()
        rule.onNodeWithText("Add some products in the bag :)").assertIsDisplayed()
    }
}
```

- [ ] **Step 3: Run all unit tests**

```bash
./gradlew test
```
Expected: All unit tests pass (CartViewModel, ProductListViewModel, etc.).

- [ ] **Step 4: Run instrumented tests on a connected device/emulator**

```bash
./gradlew connectedAndroidTest
```
Expected: All tests pass. If E2E tests flake on slow emulators, increase timeout in `createAndroidComposeRule`.

- [ ] **Step 5: Final commit**

```bash
git add app/src/androidTest/java/com/example/stackdemo/
git commit -m "test: add Compose UI tests — login screen, happy path E2E, locked user flow"
```

---

## Final Verification Checklist

Before calling this done, verify:

- [ ] `./gradlew test` — all unit tests green
- [ ] `./gradlew connectedAndroidTest` — all instrumented tests green
- [ ] `./gradlew assembleDebug` — APK builds without warnings
- [ ] App launches on emulator, shows 25 products
- [ ] Apple filter shows only Apple products
- [ ] Add to cart updates badge
- [ ] Cart drawer opens, shows items, quantity controls work
- [ ] CHECKOUT → Login → `demouser` logs in → Checkout form → SUBMIT → Confirmation
- [ ] `locked_user` shows error on Login screen
- [ ] "CONTINUE SHOPPING" clears cart (badge = 0) and returns to Product List
- [ ] `image_not_loading_user` shows placeholder images in product grid
