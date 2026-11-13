package com.example.stackdemo.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stackdemo.data.model.CartItem
import com.example.stackdemo.data.model.Order
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.common.ResponsiveTwoPane
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
                    "Shipping Address",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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
                FormField("Postal Code", formState.postalCode, formState.errors["postalCode"], isLast = true) {
                    vm.update(postalCode = it)
                }

                Button(
                    onClick = {
                        if (vm.validate()) {
                            val order = cartViewModel.snapshotOrder()
                            onOrderPlaced(order)
                        }
                    },
                    enabled  = cartState.items.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = BStackDark,
                        contentColor   = BStackWhite
                    )
                ) {
                    Text("SUBMIT")
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

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String?,
    isLast: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        singleLine    = true,
        keyboardOptions = KeyboardOptions(imeAction = if (isLast) ImeAction.Done else ImeAction.Next),
        isError       = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
fun OrderSummaryCard(
    items: List<CartItem>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Order Summary",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            Text(
                "${items.sumOf { it.quantity }} item(s)",
                style = MaterialTheme.typography.bodyMedium
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, style = MaterialTheme.typography.bodyMedium)
                        Text(item.product.vendor, style = MaterialTheme.typography.labelSmall)
                        Text("Qty: ${item.quantity}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        "$${String.format(Locale.US, "%.0f", item.subtotal)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total (USD)", fontWeight = FontWeight.Bold)
                Text(
                    "$${String.format(Locale.US, "%.2f", total)}",
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
