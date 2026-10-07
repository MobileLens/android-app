package com.mobilelens.mobilelens.core.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobilelens.mobilelens.core.navigation.Screen
import com.mobilelens.mobilelens.core.navigation.TOP_LEVEL_ROUTES

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
            val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true
            val label = stringResource(topLevelRoute.labelRes)

            NavigationBarItem(
                // The label is already read by accessibility services, so the icon needs no description
                icon = { Icon(topLevelRoute.icon, contentDescription = null) },
                label = { Text(label) },
                selected = isSelected,
                onClick = {
                    val startDestinationId = navController.graph.findStartDestination().id
                    if (topLevelRoute.route == Screen.Home) {
                        navController.popBackStack(startDestinationId, inclusive = false)
                    } else {
                        navController.navigate(topLevelRoute.route) {
                            popUpTo(startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    MaterialTheme {
        BottomNavigationBar(navController = rememberNavController())
    }
}
