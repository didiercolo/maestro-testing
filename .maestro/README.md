# Maestro flows

End-to-end UI flows for the StackDemo Android app, written against the test cases in
`docs/TEST-CASES.md`. Selectors use visible text and content descriptions, since the app
is single-language and has no test tags.

## Prerequisites

- Maestro CLI (`brew install maestro`), Java 17+
- An emulator or device with the debug APK installed:
  `./gradlew installDebug`
- **Quit the Maestro Studio desktop app before running the CLI.** Studio holds an
  active session on the device, and the CLI then skips starting its own driver and fails
  every flow instantly with `Device server died during 'deviceInfo'`.

## Run

```bash
scripts/run-maestro.sh                 # whole suite
scripts/run-maestro.sh product-sort    # one flow
maestro test --include-tags smoke .maestro/
maestro test --format junit --output build/maestro-report.xml .maestro/
```

Screenshots and logs land in `~/.maestro/tests/<timestamp>/`.

## Flows

| Flow | Tags | Test cases |
|---|---|---|
| `product-list.yaml` | smoke, product-list | PL-01..04, PL-07, PL-08, PL-10, DV-01..03 |
| `product-sort.yaml` | product-list | PL-05, PL-06 |
| `product-detail.yaml` | smoke, product-detail | PD-01..06, EC-01, DV-04, DV-05 |
| `cart-drawer.yaml` | smoke, cart | CA-01..08, EC-06 |
| `login-locked.yaml` | login | LG-01, E2E-02 |
| `checkout-validation.yaml` | checkout | CK-01..08, LG-02 |
| `purchase-happy-path.yaml` | smoke, e2e | E2E-01, CF-01..05, CA-09, CA-10 |
| `purchase-multi-item.yaml` | e2e | E2E-03, PL-09 |

Sub-flows in `flows/` (not run on their own):

- `login.yaml` — dropdown login, `USERNAME` env (default `demouser`)
- `fill-shipping.yaml` — fills all five shipping fields with valid values
- `open-cart-and-checkout.yaml` — opens the drawer and taps CHECKOUT

## Conventions

- Every flow starts with `launchApp: clearState: true` so the in-memory cart is empty.
- Strings with `$`, `.` or `(` are single-quoted and regex-escaped, because Maestro text
  selectors are regular expressions (`'\$799\.00'`, `'25 Product\(s\) found\.'`).
- "Add to cart" uses `index:` because the grid order is fixed by `products.json`
  (index 0 is iPhone 12; under the Samsung filter, index 1 is Galaxy S21+).
- Flows were written against the Pixel Tablet AVD. On a phone, add
  `scrollUntilVisible` before `SUBMIT` on the checkout screen.

Not covered (need app changes first): PL-11 search bar, PL-12 broken-image user,
CF-06 unique order numbers, EC-04/EC-05 rotation.
