package com.example.stackdemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
    NavHost(navController = navController, startDestination = Routes.PRODUCT_LIST) {

        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                cartViewModel  = cartViewModel,
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                onCartClick    = onOpenCart,
                onSignInClick  = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStack ->
            val productId = backStack.arguments?.getInt("productId") ?: return@composable
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
                cartViewModel = cartViewModel,
                onOrderPlaced = { order ->
                    navController.navigate(Routes.confirmation(order.orderNumber)) {
                        popUpTo(Routes.PRODUCT_LIST) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.CONFIRMATION,
            arguments = listOf(navArgument("orderNumber") { type = NavType.IntType })
        ) { backStack ->
            val orderNumber = backStack.arguments?.getInt("orderNumber") ?: 0
            ConfirmationScreen(
                orderNumber        = orderNumber,
                cartViewModel      = cartViewModel,
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
