package cl.duoc.rulloa.nutriplus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.duoc.rulloa.nutriplus.ui.navigation.SetupNavGraph
import cl.duoc.rulloa.nutriplus.ui.theme.NutriPlusTheme

// singleTask: si el Widget vuelve a abrir la app con una receta distinta, reutilizamos la
// misma instancia (onNewIntent) en vez de apilar una MainActivity nueva sobre la anterior.
class MainActivity : ComponentActivity() {

    private var deepLinkRecipeId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkRecipeId = extractRecipeId(intent)
        setContent {
            NutriPlusTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SetupNavGraph(
                            navController = navController,
                            deepLinkRecipeId = deepLinkRecipeId,
                            onDeepLinkConsumed = { deepLinkRecipeId = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractRecipeId(intent)?.let { deepLinkRecipeId = it }
    }

    /** Uri esperada: nutriplus://recipe/{recipeId} (ver widget/WidgetRefresher y el manifest). */
    private fun extractRecipeId(intent: Intent?): String? {
        val data = intent?.data ?: return null
        return if (data.scheme == "nutriplus" && data.host == "recipe") data.lastPathSegment else null
    }
}
