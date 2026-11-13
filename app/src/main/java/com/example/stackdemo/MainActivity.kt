package com.example.stackdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.stackdemo.navigation.AppNavGraph
import com.example.stackdemo.navigation.Routes
import com.example.stackdemo.ui.cart.CartDrawer
import com.example.stackdemo.ui.cart.CartViewModel
import com.example.stackdemo.ui.theme.StackDemoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackDemoTheme {
                val navController = rememberNavController()
                val drawerState   = rememberDrawerState(DrawerValue.Closed)
                val scope         = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState     = drawerState,
                    gesturesEnabled = drawerState.isOpen,
                    drawerContent   = {
                        CartDrawer(
                            cartViewModel = cartViewModel,
                            onCheckout = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Routes.LOGIN)
                            },
                            onClose = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    AppNavGraph(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        onOpenCart    = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}
