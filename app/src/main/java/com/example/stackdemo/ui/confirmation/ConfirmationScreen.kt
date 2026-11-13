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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(title = { Text("StackDemo", fontWeight = FontWeight.Bold) })
        }
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

            OrderSummaryCard(
                items    = cartState.items,
                total    = cartState.subtotal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
