package com.example.stackdemo.ui.cart

import androidx.compose.foundation.clickable
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Add some products in the bag :)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SUBTOTAL", style = MaterialTheme.typography.labelSmall)
            Text(
                "$${String.format(Locale.US, "%.2f", state.subtotal)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick  = onCheckout,
            enabled  = state.items.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = BStackDark,
                contentColor   = BStackWhite
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(
                    context.resources.getIdentifier(
                        item.product.image, "drawable", context.packageName
                    ).takeIf { it != 0 } ?: R.drawable.ic_product_placeholder
                )
                .error(R.drawable.ic_product_placeholder)
                .build(),
            contentDescription = item.product.name,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.product.name,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
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
            Text(
                "$${String.format(Locale.US, "%.2f", item.subtotal)}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
