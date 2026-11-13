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
    onSignInClick: () -> Unit,
    viewModel: ProductListViewModel = viewModel(
        factory = ProductListViewModelFactory(LocalContext.current)
    )
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    var sortExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BrowserStack", fontWeight = FontWeight.Bold)
                        Text("Demo", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    TextButton(onClick = onSignInClick) { Text("Sign In") }
                    BadgedBox(
                        badge = {
                            if (cartState.itemCount > 0) Badge { Text(cartState.itemCount.toString()) }
                        },
                        modifier = Modifier.padding(end = 16.dp).clickable { onCartClick() }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${uiState.products.size} Product(s) found.", style = MaterialTheme.typography.bodyMedium)
                Box {
                    OutlinedButton(onClick = { sortExpanded = true }) {
                        Text(
                            when (uiState.sortOrder) {
                                SortOrder.DEFAULT        -> "Order by"
                                SortOrder.PRICE_LOW_HIGH -> "Price: Low → High"
                                SortOrder.PRICE_HIGH_LOW -> "Price: High → Low"
                            }
                        )
                    }
                    DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Price: Low → High") },
                            onClick = { viewModel.setSortOrder(SortOrder.PRICE_LOW_HIGH); sortExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Price: High → Low") },
                            onClick = { viewModel.setSortOrder(SortOrder.PRICE_HIGH_LOW); sortExpanded = false }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductCard(
                            product     = product,
                            onCardClick = { onProductClick(product.id) },
                            onAddToCart = { cartViewModel.addProduct(product) }
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
    val context = LocalContext.current
    var isFavourite by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(
                            context.resources.getIdentifier(product.image, "drawable", context.packageName)
                                .takeIf { it != 0 } ?: R.drawable.ic_product_placeholder
                        )
                        .error(R.drawable.ic_product_placeholder)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxWidth().aspectRatio(1f)
                )
                IconButton(onClick = { isFavourite = !isFavourite }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        imageVector        = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint               = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    product.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                )
                Text("$" + String.format(Locale.US, "%.2f", product.price), style = MaterialTheme.typography.bodyMedium)
                Text(
                    "or ${product.installments} x $" + String.format(Locale.US, "%.2f", product.installmentPrice),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick  = onAddToCart,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = BStackYellow, contentColor = BStackDark)
            ) {
                Text("Add to cart")
            }
        }
    }
}
