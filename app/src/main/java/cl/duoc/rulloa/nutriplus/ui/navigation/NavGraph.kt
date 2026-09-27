package cl.duoc.rulloa.nutriplus.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import cl.duoc.rulloa.nutriplus.data.ServiceLocator
import cl.duoc.rulloa.nutriplus.ui.auth.AuthViewModel
import cl.duoc.rulloa.nutriplus.ui.auth.LoginScreen
import cl.duoc.rulloa.nutriplus.ui.auth.RecoverPasswordScreen
import cl.duoc.rulloa.nutriplus.ui.auth.RegisterScreen
import cl.duoc.rulloa.nutriplus.ui.common.ViewModelFactory
import cl.duoc.rulloa.nutriplus.ui.detail.RecipeDetailScreen
import cl.duoc.rulloa.nutriplus.ui.detail.RecipeDetailViewModel
import cl.duoc.rulloa.nutriplus.ui.devices.DeviceViewModel
import cl.duoc.rulloa.nutriplus.ui.devices.DevicesScreen
import cl.duoc.rulloa.nutriplus.ui.minuta.MinutaScreen
import cl.duoc.rulloa.nutriplus.ui.minuta.MinutaViewModel
import cl.duoc.rulloa.nutriplus.ui.profile.ProfileScreen
import cl.duoc.rulloa.nutriplus.ui.profile.ProfileViewModel
import cl.duoc.rulloa.nutriplus.ui.recipes.RecipeCatalogScreen
import cl.duoc.rulloa.nutriplus.ui.recipes.RecipeViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RecoverPassword : Screen("recover_password")
    object Minuta : Screen("minuta")
    object Recipes : Screen("recipes")
    object Devices : Screen("devices")
    object Profile : Screen("profile")
    object RecipeDetail : Screen("recipe/{recipeId}") {
        fun buildRoute(recipeId: String) = "recipe/$recipeId"
    }
}

private data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Minuta, "Minuta", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Recipes, "Recetas", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Screen.Devices, "Dispositivos", Icons.Default.Devices),
    BottomNavItem(Screen.Profile, "Perfil", Icons.Default.Person)
)

/**
 * Grafo de navegación de NutriPlus.
 *
 * Sin sesión solo se puede llegar a Login/Registro/Recuperar (rutas protegidas por defecto:
 * ningún botón de la app lleva a Minuta/Recetas/Dispositivos/Perfil sin pasar por un login
 * o registro exitoso, y si la sesión termina mientras el usuario está en una de esas
 * pantallas, lo devolvemos a Login).
 *
 * [deepLinkRecipeId] es el id de receta que trae el Widget (ver MainActivity, que lo extrae
 * de la Uri `nutriplus://recipe/{id}` del PendingIntent). Se maneja a mano en vez de con un
 * navDeepLink automático de Navigation-Compose para poder aplicar la regla "sin sesión,
 * primero Login y después la receta".
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    deepLinkRecipeId: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val authRepository = ServiceLocator.authRepository
    val userRepository = ServiceLocator.userRepository
    val recipeRepository = ServiceLocator.recipeRepository
    val minutaRepository = ServiceLocator.minutaRepository
    val deviceRepository = ServiceLocator.deviceRepository
    val connectivityObserver = ServiceLocator.connectivityObserver

    // El destino inicial nunca es el detalle: Navigation no extrae argumentos de una ruta de
    // inicio ya rellenada ("recipe/r4"), y además "Volver" quedaría sin nada debajo. El deep
    // link se abre encima de la Minuta desde el LaunchedEffect de más abajo.
    val startDestination = remember {
        if (authRepository.currentUser != null) Screen.Minuta.route else Screen.Login.route
    }

    val currentUser by authRepository.observeAuthState().collectAsState(initial = authRepository.currentUser)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // La sesión terminó (logout o cuenta eliminada) estando en una pantalla protegida.
    LaunchedEffect(currentUser) {
        val protectedRoutes = setOf(Screen.Minuta.route, Screen.Recipes.route, Screen.Devices.route, Screen.Profile.route)
        val current = navController.currentDestination?.route
        if (currentUser == null && current != null && (current in protectedRoutes || current == Screen.RecipeDetail.route)) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Abre la receta del deep link apenas hay sesión y el usuario ya salió de las pantallas de
    // autenticación. Depende de la ruta actual para no competir con la navegación del login:
    // tras iniciar sesión, onLoginSuccess lleva a la Minuta y recién ahí se abre la receta.
    LaunchedEffect(deepLinkRecipeId, currentUser, currentRoute) {
        val authRoutes = setOf(Screen.Login.route, Screen.Register.route, Screen.RecoverPassword.route)
        if (deepLinkRecipeId != null && currentUser != null && currentRoute != null && currentRoute !in authRoutes) {
            navController.navigate(Screen.RecipeDetail.buildRoute(deepLinkRecipeId))
            onDeepLinkConsumed()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = ViewModelFactory { AuthViewModel(authRepository, userRepository) }
            )
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Minuta.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onRecoverPasswordClick = { navController.navigate(Screen.RecoverPassword.route) }
            )
        }
        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = ViewModelFactory { AuthViewModel(authRepository, userRepository) }
            )
            RegisterScreen(
                viewModel = authViewModel,
                // Firebase deja la sesión iniciada apenas se crea la cuenta, así que
                // seguimos directo a la Minuta (no tendría sentido pedir login de nuevo).
                onRegisterSuccess = {
                    navController.navigate(Screen.Minuta.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.RecoverPassword.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = ViewModelFactory { AuthViewModel(authRepository, userRepository) }
            )
            RecoverPasswordScreen(
                viewModel = authViewModel,
                onEmailSent = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Minuta.route) {
            MainScaffold(navController) {
                val viewModel: MinutaViewModel = viewModel(
                    factory = ViewModelFactory { MinutaViewModel(minutaRepository, recipeRepository, authRepository) }
                )
                MinutaScreen(
                    viewModel = viewModel,
                    connectivityObserver = connectivityObserver,
                    onRecipeClick = { recipeId -> navController.navigate(Screen.RecipeDetail.buildRoute(recipeId)) }
                )
            }
        }
        composable(Screen.Recipes.route) {
            MainScaffold(navController) {
                val viewModel: RecipeViewModel = viewModel(
                    factory = ViewModelFactory { RecipeViewModel(recipeRepository, authRepository) }
                )
                RecipeCatalogScreen(
                    viewModel = viewModel,
                    connectivityObserver = connectivityObserver,
                    onRecipeClick = { recipeId -> navController.navigate(Screen.RecipeDetail.buildRoute(recipeId)) }
                )
            }
        }
        composable(Screen.Devices.route) {
            MainScaffold(navController) {
                val viewModel: DeviceViewModel = viewModel(
                    factory = ViewModelFactory { DeviceViewModel(deviceRepository, authRepository) }
                )
                DevicesScreen(viewModel = viewModel, connectivityObserver = connectivityObserver)
            }
        }
        composable(Screen.Profile.route) {
            MainScaffold(navController) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = ViewModelFactory { ProfileViewModel(userRepository, authRepository) }
                )
                ProfileScreen(
                    viewModel = viewModel,
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId").orEmpty()
            val viewModel: RecipeDetailViewModel = viewModel(
                key = "recipe_detail_$recipeId",
                factory = ViewModelFactory { RecipeDetailViewModel(recipeId, recipeRepository, authRepository) }
            )
            RecipeDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController, content: @Composable () -> Unit) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        Box(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            content()
        }
    }
}
