# StackDemo Android

A native Android app that mirrors the e-commerce flow of [bstackdemo.com](https://bstackdemo.com/): product list, product detail, cart, login, checkout, and order confirmation. All product and user data lives in local JSON assets, so the app runs fully offline and is a stable target for UI automation (Maestro, Espresso, Compose tests).

## Stack

- Kotlin 2.0, Jetpack Compose + Material 3
- MVVM with `ViewModel` + `StateFlow`, repository layer over JSON assets
- Single `MainActivity`, Navigation Compose, cart as a `ModalNavigationDrawer`
- Min SDK 26, target SDK 35

## Build and run

```bash
./gradlew assembleDebug                 # APK at app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17+ and the Android SDK (set `sdk.dir` in `local.properties`).

## Tests

```bash
./gradlew testDebugUnitTest             # 45 unit tests (models, repositories, ViewModels)
./gradlew connectedDebugAndroidTest     # 12 instrumented tests, needs a running emulator/device
```

Instrumented coverage: JSON loading, product list, vendor filter, cart drawer, locked-user login error, full happy-path purchase, checkout validation.

## Test data

Edit `app/src/main/assets/products.json` (25 products, 4 vendors) and `app/src/main/assets/users.json` to change the catalog or accounts without touching code.

| Username | Password | Behaviour |
|---|---|---|
| `demouser` | `testingisfun99` | Normal flow |
| `locked_user` | `testingisfun99` | Login shows "Your account has been locked." |
| `image_not_loading_user` | `testingisfun99` | Reserved for broken-image scenario |
| `existing_orders_user` | `testingisfun99` | Reserved for order-history scenario |
| `fav_user` | `testingisfun99` | Reserved for favourites scenario |

## Flow

1. **Product list** — grid of 25 products, vendor filter chips, price sort, tap a card for detail, "Add to cart" on each card.
2. **Product detail** — image, price, installments, description, "ADD TO CART".
3. **Cart drawer** — opens from the cart icon; quantity controls, subtotal, "CHECKOUT".
4. **Login** — dropdown username and password, matching the website.
5. **Checkout** — shipping form (all fields required, postal code numeric) plus order summary; "SUBMIT".
6. **Confirmation** — order number, receipt link, "CONTINUE SHOPPING »" clears the cart.

## Maestro E2E flows

`.maestro/` holds eight Maestro flows mapped to the test cases, plus reusable sub-flows.
Quit the Maestro Studio desktop app, then run `scripts/run-maestro.sh` (whole suite) or
`scripts/run-maestro.sh purchase-happy-path` (one flow). See `.maestro/README.md`.

## Docs

- `docs/DESIGN-SPEC.md` — screen-by-screen design and data model
- `docs/IMPLEMENTATION-PLAN.md` — task breakdown used to build the app
- `docs/TEST-CASES.md` — manual QA test cases (ids, steps, expected results) ready to turn into Maestro flows
