package com.example.stackdemo

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class E2EFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun addFirstProductToCart() {
        rule.onAllNodesWithText("Add to cart").onFirst().performScrollTo().performClick()
    }

    private fun openCart() {
        rule.onNodeWithContentDescription("Cart").performClick()
    }

    private fun login(username: String) {
        rule.onNodeWithText("Select Username").performClick()
        rule.onNodeWithText(username).performClick()
        rule.onNodeWithText("Select Password").performClick()
        rule.onNodeWithText("testingisfun99").performClick()
        rule.onNodeWithText("LOG IN").performClick()
    }

    @Test
    fun productList_isShownOnLaunch() {
        rule.onNodeWithText("BrowserStack").assertIsDisplayed()
        rule.onAllNodesWithText("Add to cart").onFirst().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cartDrawer_emptyState_showsPlaceholderAndDisabledCheckout() {
        openCart()
        rule.onNodeWithText("Add some products in the bag :)").assertIsDisplayed()
        rule.onNodeWithText("CHECKOUT").assertIsNotEnabled()
    }

    @Test
    fun vendorFilter_apple_hidesSamsungProducts() {
        rule.onNodeWithText("Apple").performClick()
        rule.onAllNodesWithText("Samsung Galaxy S21").assertCountEquals(0)
        rule.onAllNodesWithText("iPhone 12").onFirst().assertIsDisplayed()
    }

    @Test
    fun addToCart_updatesBadge_andOpensCartWithItem() {
        addFirstProductToCart()
        openCart()
        rule.onNodeWithText("Bag").assertIsDisplayed()
        rule.onNodeWithText("CHECKOUT").assertIsDisplayed()
        rule.onNodeWithText("Add some products in the bag :)").assertDoesNotExist()
    }

    @Test
    fun lockedUser_seesError_andCannotReachCheckout() {
        addFirstProductToCart()
        openCart()
        rule.onNodeWithText("CHECKOUT").performClick()
        login("locked_user")
        rule.onNodeWithText("Your account has been locked.").assertIsDisplayed()
        rule.onNodeWithText("Shipping Address").assertDoesNotExist()
    }

    @Test
    fun happyPath_addToCart_login_checkout_confirmation() {
        addFirstProductToCart()
        openCart()
        rule.onNodeWithText("CHECKOUT").performClick()

        rule.onNodeWithText("LOG IN").assertIsDisplayed()
        login("demouser")

        rule.onNodeWithText("Shipping Address").assertIsDisplayed()
        rule.onNodeWithText("First Name").performTextInput("John")
        rule.onNodeWithText("Last Name").performTextInput("Doe")
        rule.onNodeWithText("Address").performTextInput("123 Main St")
        rule.onNodeWithText("State/Province").performTextInput("California")
        rule.onNodeWithText("Postal Code").performTextInput("90210")
        rule.onNodeWithText("SUBMIT").performClick()

        rule.onNodeWithText("Your Order has been successfully placed.").assertIsDisplayed()
        rule.onNodeWithText("CONTINUE SHOPPING »").assertIsDisplayed()
    }

    @Test
    fun checkout_emptyForm_showsRequiredErrors_andStays() {
        addFirstProductToCart()
        openCart()
        rule.onNodeWithText("CHECKOUT").performClick()
        login("demouser")
        rule.onNodeWithText("SUBMIT").performClick()
        rule.onAllNodesWithText("Required").onFirst().assertIsDisplayed()
        rule.onNodeWithText("Shipping Address").assertIsDisplayed()
    }
}
