package com.example.stackdemo.ui.confirmation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.checkout.OrderSummaryCard
import com.example.stackdemo.ui.common.ResponsiveTwoPane
import kotlinx.coroutines.launch

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

    // Back from the confirmation screen behaves like "Continue Shopping": the order is placed,
    // so the cart must be cleared rather than leaving the user with already-ordered items.
    BackHandler(onBack = onContinueShopping)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(title = { Text("StackDemo", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        ResponsiveTwoPane(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            first = { paneModifier ->
            Column(
                modifier = paneModifier,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Your Order has been successfully placed.",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Your order number is $orderNumber.",
                    style = MaterialTheme.typography.bodyMedium
                )

                TextButton(onClick = {
                    scope.launch { snackbarState.showSnackbar("Receipt downloaded") }
                }) {
                    Text(
                        "Download order receipt",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                HorizontalDivider()

                OutlinedButton(onClick = onContinueShopping) {
                    Text("CONTINUE SHOPPING »")
                }
            }
            },
            second = { paneModifier ->
            OrderSummaryCard(
                items    = cartState.items,
                total    = cartState.subtotal,
                modifier = paneModifier
            )
            }
        )
    }
}
