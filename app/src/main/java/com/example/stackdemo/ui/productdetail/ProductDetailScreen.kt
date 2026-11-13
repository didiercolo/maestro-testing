package com.example.stackdemo.ui.productdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.launch
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
    val vm: ProductDetailViewModel = viewModel(
        key = "product_detail_$productId",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = ProductRepository(JsonDataSource(context.applicationContext))
                @Suppress("UNCHECKED_CAST")
                return ProductDetailViewModel(repo, productId) as T
            }
        }
    )
    val state     by vm.uiState.collectAsStateWithLifecycle()
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbar  = remember { SnackbarHostState() }
    val scope     = rememberCoroutineScope()
    var isFavourite by remember { mutableStateOf(false) }
    var addedFlash  by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick, modifier = Modifier.padding(end = 8.dp)) {
                        BadgedBox(
                            badge = {
                                if (cartState.itemCount > 0) Badge { Text(cartState.itemCount.toString()) }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val product = state.product
        if (state.isLoading || product == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator() else Text("Product not found.")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                contentScale       = ContentScale.Fit,
                modifier           = Modifier.fillMaxWidth().height(280.dp).padding(16.dp)
            )

            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isFavourite = !isFavourite }) {
                        Icon(
                            imageVector        = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint               = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                AssistChip(onClick = {}, label = { Text(product.vendor) }, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    "$" + String.format(Locale.US, "%.2f", product.price),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "or ${product.installments} x $" + String.format(Locale.US, "%.2f", product.installmentPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(12.dp))
                Text(product.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        cartViewModel.addProduct(product)
                        addedFlash = true
                        scope.launch {
                            snackbar.showSnackbar("Added to cart")
                            addedFlash = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BStackYellow, contentColor = BStackDark)
                ) {
                    Text(if (addedFlash) "ADDED ✓" else "ADD TO CART")
                }
            }
        }
    }
}
