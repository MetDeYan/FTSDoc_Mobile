package com.ftsdoc.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ftsdoc.mobile.ui.LocalAppContainer
import com.ftsdoc.mobile.ui.login.LoginScreen

@Composable
fun FtsDocRoot() {
    val container = LocalAppContainer.current
    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(container.authRepository),
    )
    val authorized by sessionViewModel.isAuthorized.collectAsStateWithLifecycle()

    if (authorized == null) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else {
        AppNavHost(loggedIn = authorized == true)
    }
}

@Composable
private fun AppNavHost(loggedIn: Boolean) {
    val navController = rememberNavController()
    val startDestination = remember { if (loggedIn) Routes.MAIN else Routes.LOGIN }

    LaunchedEffect(loggedIn) {
        if (loggedIn) return@LaunchedEffect
        val route = navController.currentBackStackEntry?.destination?.route
        if (route == null || route == Routes.LOGIN) return@LaunchedEffect
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.MAIN) {
            MainScaffold()
        }
    }
}
