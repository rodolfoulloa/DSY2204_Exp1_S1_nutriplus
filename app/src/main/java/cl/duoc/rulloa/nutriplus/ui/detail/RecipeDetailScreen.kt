package cl.duoc.rulloa.nutriplus.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.ui.common.ResourceContent

/**
 * Pantalla Compose pura del detalle de una receta (reemplaza el antiguo overlay con
 * RecipeDetailFragment + AndroidView). Vive en su propia ruta "recipe/{recipeId}", por lo
 * que también es el destino del deep link que abre el Widget.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la receta") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val detail = (state as? Resource.Success)?.data
                    if (detail?.recipe != null) {
                        IconButton(onClick = viewModel::toggleFavorite, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(
                                imageVector = if (detail.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (detail.isFavorite) "Quitar de favoritas" else "Marcar como favorita"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        ResourceContent(
            resource = state,
            isEmpty = { it.recipe == null },
            emptyMessage = "No encontramos esta receta",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { detail ->
            val recipe = detail.recipe ?: return@ResourceContent
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Día sugerido: ${recipe.day} · ${recipe.category}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Calorías: ${recipe.calories} kcal", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ingredientes:", fontWeight = FontWeight.SemiBold)
                recipe.ingredients.forEach { ingredient ->
                    Text("- $ingredient")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preparación:", fontWeight = FontWeight.SemiBold)
                Text(recipe.preparation)
            }
        }
    }
}
