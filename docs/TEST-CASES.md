# BStackDemo Android App — Test Cases

**App:** StackDemo Android  
**Version:** 1.0  
**Date:** 2026-11-12  
**Data files:** `assets/products.json`, `assets/users.json`

---

## Conventions

- **User:** Unless stated, use `demouser` / `testingisfun99`
- **Precondition "Fresh launch":** App just opened, cart is empty, user is not logged in
- **TC-ID format:** `[SCREEN]-[NUMBER]`

---

## 1. Product List Screen

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| PL-01 | Products load from JSON on launch | Fresh launch | 1. Open app | Product grid displays 25 products with names, prices, and images |
| PL-02 | Filter by Apple vendor | Fresh launch | 1. Tap "Apple" chip | Grid shows only Apple products (iPhone models); count label updates |
| PL-03 | Filter by Samsung vendor | Fresh launch | 1. Tap "Samsung" chip | Grid shows only Samsung products |
| PL-04 | Deselect vendor filter shows all | Apple filter active | 1. Tap "Apple" chip again | All 25 products are shown again |
| PL-05 | Sort Price Low to High | Fresh launch | 1. Open sort dropdown  2. Select "Price: Low → High" | Products reorder with lowest price first |
| PL-06 | Sort Price High to Low | Fresh launch | 1. Open sort dropdown  2. Select "Price: High → Low" | Products reorder with highest price first |
| PL-07 | Cart badge shows 0 on fresh launch | Fresh launch | 1. Open app  2. Check cart icon | Cart icon shows no badge (or "0") |
| PL-08 | Add to Cart from list updates badge | Fresh launch | 1. Tap "Add to Cart" on any product | Cart icon badge increments to 1 |
| PL-09 | Add multiple different products to cart | Fresh launch | 1. Tap "Add to Cart" on 3 different products | Cart badge shows 3 |
| PL-10 | Tapping product card navigates to detail | Fresh launch | 1. Tap the body of a product card (not the button) | Product Detail screen opens for that product |
| PL-11 | Search bar is present | Fresh launch | 1. Open app | Search bar visible in top area |
| PL-12 | image_not_loading_user shows placeholder images | Log in as `image_not_loading_user` before browsing | 1. Log in  2. Navigate to Product List | Product cards show placeholder/broken image icons instead of product photos |

---

## 2. Product Detail Screen

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| PD-01 | Product name and price match list | Fresh launch | 1. Tap iPhone 12 card | Detail screen shows "iPhone 12" and "$799.00" |
| PD-02 | Installment line is correct | Fresh launch | 1. Tap iPhone 12 card | Shows "or 9 x $88.78" |
| PD-03 | Vendor chip matches JSON vendor | Fresh launch | 1. Tap any Samsung product | Vendor chip shows "Samsung" |
| PD-04 | Add to Cart from detail updates badge | Fresh launch | 1. Tap iPhone 12  2. Tap "ADD TO CART" | Cart badge increments to 1; Snackbar "Added to cart" appears |
| PD-05 | Back arrow returns to product list | On Product Detail | 1. Tap back arrow | Returns to Product List; scroll position preserved |
| PD-06 | Adding same product twice increments quantity | 1 unit of iPhone 12 in cart | 1. Tap iPhone 12  2. Tap "ADD TO CART" again | Cart contains 1 item (iPhone 12) with quantity 2 |

---

## 3. Cart Drawer

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| CA-01 | Empty cart shows placeholder message | Fresh launch | 1. Tap cart icon | Drawer opens showing "Add some products in the bag :)" |
| CA-02 | Added product appears in cart | 1 product added to cart | 1. Tap cart icon | Product shows with thumbnail, name, vendor, qty=1, price |
| CA-03 | Increase quantity with + button | Cart has 1x iPhone 12 | 1. Open cart  2. Tap "+" next to iPhone 12 | Quantity becomes 2; subtotal doubles |
| CA-04 | Decrease quantity with - button | Cart has 2x iPhone 12 | 1. Open cart  2. Tap "−" next to iPhone 12 | Quantity becomes 1; subtotal halves |
| CA-05 | Decreasing quantity to 0 removes item | Cart has 1x iPhone 12 | 1. Open cart  2. Tap "−" | iPhone 12 removed; empty state shown |
| CA-06 | Remove button (×) removes item | Cart has 2 products | 1. Open cart  2. Tap × on first product | That product removed; second product remains |
| CA-07 | Subtotal reflects all items | Cart has iPhone 12 ($799) + Galaxy S20+ ($999) | 1. Open cart | Subtotal shows $1,798.00 |
| CA-08 | CHECKOUT button navigates to Login | Cart has 1 product | 1. Open cart  2. Tap "CHECKOUT" | Cart closes; Login screen appears |
| CA-09 | Cart persists when navigating between screens | 1 product in cart | 1. Add product  2. Navigate to Product Detail  3. Navigate back  4. Open cart | Cart still contains the product |
| CA-10 | Cart clears after successful order | Completed full checkout flow | 1. Tap "CONTINUE SHOPPING" on Confirmation | Product List appears with cart badge at 0 |

---

## 4. Login Screen

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| LG-01 | Login screen shows username and password dropdowns | Navigate to Login | 1. Open Login screen | Two dropdown fields labeled "Select Username" and "Select Password" are visible |
| LG-02 | Username dropdown lists all 5 users | On Login screen | 1. Tap "Select Username" | Dropdown shows: demouser, image_not_loading_user, existing_orders_user, fav_user, locked_user |
| LG-03 | Password dropdown shows single option | On Login screen | 1. Tap "Select Password" | Dropdown shows: testingisfun99 |
| LG-04 | LOG IN button disabled until both fields selected | On Login screen | 1. Select only username  2. Check LOG IN button | LOG IN button is disabled / not clickable |
| LG-05 | demouser logs in successfully | Both fields selected with demouser | 1. Select demouser  2. Select testingisfun99  3. Tap LOG IN | Navigates to Checkout screen |
| LG-06 | locked_user shows error message | Both fields selected with locked_user | 1. Select locked_user  2. Select testingisfun99  3. Tap LOG IN | Error message shown: "Your account has been locked." — does not navigate |
| LG-07 | fav_user logs in successfully | Both fields selected | 1. Select fav_user  2. Select testingisfun99  3. Tap LOG IN | Navigates to Checkout screen |
| LG-08 | existing_orders_user logs in successfully | Both fields selected | 1. Select existing_orders_user  2. Select testingisfun99  3. Tap LOG IN | Navigates to Checkout screen |
| LG-09 | image_not_loading_user logs in successfully | Both fields selected | 1. Select image_not_loading_user  2. Select testingisfun99  3. Tap LOG IN | Navigates to Checkout screen |

---

## 5. Checkout Screen

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| CK-01 | Checkout shows correct order summary | 1 product in cart, logged in | 1. Reach Checkout | Order Summary card lists cart items with correct names, quantities, prices, and total |
| CK-02 | SUBMIT with all fields filled navigates to Confirmation | On Checkout, all fields filled | 1. Enter First Name: "John"  2. Last Name: "Doe"  3. Address: "123 Main St"  4. State: "California"  5. Postal: "90210"  6. Tap SUBMIT | Navigates to Confirmation screen |
| CK-03 | SUBMIT with empty First Name shows error | On Checkout | 1. Leave First Name empty  2. Fill all other fields  3. Tap SUBMIT | Error shown under First Name: "Required" — does not navigate |
| CK-04 | SUBMIT with empty Last Name shows error | On Checkout | 1. Leave Last Name empty  2. Fill all other fields  3. Tap SUBMIT | Error shown under Last Name |
| CK-05 | SUBMIT with empty Address shows error | On Checkout | 1. Leave Address empty  2. Fill all other fields  3. Tap SUBMIT | Error shown under Address |
| CK-06 | SUBMIT with invalid postal code shows error | On Checkout | 1. Enter Postal Code: "ABC"  2. Fill all other fields  3. Tap SUBMIT | Error shown under Postal Code: "Enter a valid postal code" |
| CK-07 | Multiple empty fields show multiple errors simultaneously | On Checkout | 1. Tap SUBMIT with no fields filled | All 5 fields show error messages at once |
| CK-08 | Total in Order Summary matches cart subtotal | Cart has iPhone 12 ($799) | 1. Reach Checkout | Total shows $799.00 |

---

## 6. Confirmation Screen

| ID | Title | Preconditions | Steps | Expected Result |
|---|---|---|---|---|
| CF-01 | Success message is displayed | Complete full checkout | 1. Submit valid checkout form | "Your Order has been successfully placed." message shown |
| CF-02 | Order number is present | Complete full checkout | 1. Submit valid checkout form | "Your order number is XXXXX." is shown with a numeric value |
| CF-03 | Order summary matches what was checked out | Complete flow with iPhone 12 | 1. Submit checkout | Confirmation Order Summary shows iPhone 12 at $799.00 |
| CF-04 | Download order receipt shows Snackbar | On Confirmation screen | 1. Tap "Download order receipt" link | Snackbar appears: "Receipt downloaded" |
| CF-05 | CONTINUE SHOPPING clears cart | On Confirmation screen | 1. Tap "CONTINUE SHOPPING »" | Navigates to Product List; cart badge is 0; cart drawer is empty |
| CF-06 | Order numbers are unique across two orders | Complete two full checkout flows | 1. Complete first order (note number)  2. Complete second order (note number) | The two order numbers are different |

---

## 7. End-to-End Flows

### E2E-01: Happy Path — Full Purchase Flow

**User:** `demouser` / `testingisfun99`  
**Precondition:** Fresh app launch

| Step | Action | Expected |
|---|---|---|
| 1 | Open app | Product List shows 25 products |
| 2 | Tap "Apple" filter | Grid filters to Apple products only |
| 3 | Tap iPhone 12 card | Product Detail screen opens |
| 4 | Tap "ADD TO CART" | Cart badge shows 1; Snackbar confirms |
| 5 | Tap back arrow | Returns to filtered Product List |
| 6 | Tap cart icon | Cart Drawer shows iPhone 12, qty 1, $799.00 |
| 7 | Tap "CHECKOUT" | Login screen appears |
| 8 | Select `demouser` | Username field shows demouser |
| 9 | Select `testingisfun99` | Password field shows testingisfun99 |
| 10 | Tap "LOG IN" | Checkout screen appears with order summary |
| 11 | Fill all shipping fields | Form fills successfully |
| 12 | Tap "SUBMIT" | Confirmation screen appears |
| 13 | Verify success message | "Your Order has been successfully placed." visible |
| 14 | Verify order number | A numeric order number is displayed |
| 15 | Tap "CONTINUE SHOPPING »" | Product List reappears; cart is empty |

---

### E2E-02: Locked User Cannot Complete Purchase

**User:** `locked_user` / `testingisfun99`  
**Precondition:** 1 product in cart

| Step | Action | Expected |
|---|---|---|
| 1 | Add any product to cart | Cart badge = 1 |
| 2 | Open cart, tap CHECKOUT | Login screen appears |
| 3 | Select `locked_user` | Username field shows locked_user |
| 4 | Select `testingisfun99` | Password field shows testingisfun99 |
| 5 | Tap "LOG IN" | Error shown: "Your account has been locked." |
| 6 | Confirm no navigation | Still on Login screen; Checkout not reached |

---

### E2E-03: Multi-Item Cart Purchase

**User:** `demouser`  
**Precondition:** Fresh launch

| Step | Action | Expected |
|---|---|---|
| 1 | Add iPhone 12 to cart | Badge = 1 |
| 2 | Add Samsung Galaxy S20+ to cart | Badge = 2 |
| 3 | Open cart | 2 items shown; subtotal = $1,798.00 |
| 4 | Increase iPhone 12 qty to 2 | Subtotal = $2,597.00 |
| 5 | Proceed through login → checkout → submit | Confirmation shows 2 line items + correct total |

---

## 8. Data Verification

| ID | Title | Steps | Expected |
|---|---|---|---|
| DV-01 | All 25 products loaded | Open app, count grid items | Exactly 25 product cards visible (or scroll to confirm) |
| DV-02 | Apple filter returns correct count | Tap Apple chip | Expected ~8 Apple products (iPhone models) |
| DV-03 | Samsung filter returns correct count | Tap Samsung chip | Expected Samsung products |
| DV-04 | Product prices match source site | Check iPhone 12 price | $799.00 displayed |
| DV-05 | Installment calculation is accurate | Check iPhone 12 detail | $799 / 9 = $88.78 displayed |

---

## 9. Edge Cases

| ID | Title | Steps | Expected |
|---|---|---|---|
| EC-01 | Add same product from list and detail | Add iPhone 12 from list, then open detail and add again | Cart shows iPhone 12 with quantity 2 |
| EC-02 | Navigate back from Login without logging in | Reach Login, press back | Returns to Product List; cart is preserved |
| EC-03 | Navigate back from Checkout | Reach Checkout, press back | Returns to Login or Product List |
| EC-04 | Rotate screen on Product List | Open app, rotate to landscape | Products still visible, layout adapts |
| EC-05 | Rotate screen on Checkout | Fill first name, rotate | First name text is preserved |
| EC-06 | Cart badge correct after removing one of two products | Add 2 products, open cart, remove 1 | Badge shows 1 |
