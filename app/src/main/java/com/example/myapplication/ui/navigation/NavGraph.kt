package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.RegisterScreen
import com.example.myapplication.ui.auth.RecoverPasswordScreen
import com.example.myapplication.ui.main.MainScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RecoverPassword : Screen("recover_password")
    object Main : Screen("main")
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Screen.Main.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onRecoverPasswordClick = { navController.navigate(Screen.RecoverPassword.route) }
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.navigate(Screen.Login.route) },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(route = Screen.RecoverPassword.route) {
            RecoverPasswordScreen(
                viewModel = authViewModel,
                onEmailSent = { navController.navigate(Screen.Login.route) },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Main.route) {
            MainScreen(
                onLogout = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
