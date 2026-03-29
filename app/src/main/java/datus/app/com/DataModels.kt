package datus.app.com

import androidx.compose.ui.graphics.vector.ImageVector

data class Plan(
    val name: String,
    val description: String,
    val price: String,
    val ussdCode: String
)

data class QuickQuery(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val ussdCode: String
)

data class Utility(
    val icon: ImageVector,
    val title: String,
    val description: String
)