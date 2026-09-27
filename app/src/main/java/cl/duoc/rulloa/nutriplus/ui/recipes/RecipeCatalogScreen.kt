package cl.duoc.rulloa.nutriplus.ui.recipes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import cl.duoc.rulloa.nutriplus.data.ConnectivityObserver
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.ui.common.OfflineBanner
import cl.duoc.rulloa.nutriplus.ui.common.ResourceContent
import cl.duoc.rulloa.nutriplus.ui.common.rememberIsOffline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCatalogScreen(
    viewModel: RecipeViewModel,
    connectivityObserver: ConnectivityObserver,
    onRecipeClick: (String) -> Unit
) {
    val listState by viewModel.recipeListState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isOffline = rememberIsOffline(connectivityObserver)

    Scaffold(topBar = { TopAppBar(title = { Text("Catálogo de Recetas") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (isOffline) OfflineBanner(modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Buscar recetas (día, plato...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            ResourceContent(
                resource = listState,
                isEmpty = { it.isEmpty() },
                emptyMessage = "No se encontraron recetas",
                modifier = Modifier.fillMaxSize()
            ) { items ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items, key = { it.recipe.id }) { item ->
                        RecipeCard(
                            recipe = item.recipe,
                            isFavorite = item.isFavorite,
                            onClick = { onRecipeClick(item.recipe.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.recipe.id, !item.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

// No hay fotos reales por receta en el catálogo, así que generamos un bitmap
// determinístico por receta y le aplicamos Palette para extraer un color dominante/vibrante.
private fun generateRecipePlaceholderBitmap(recipe: Recipe): Bitmap {
    val seed = recipe.title.hashCode()
    val hueA = ((seed % 360) + 360) % 360
    val hueB = (hueA + 120) % 360
    val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawRect(0f, 0f, 48f, 24f, Paint().apply {
        color = android.graphics.Color.HSVToColor(floatArrayOf(hueA.toFloat(), 0.55f, 0.85f))
    })
    canvas.drawRect(0f, 24f, 48f, 48f, Paint().apply {
        color = android.graphics.Color.HSVToColor(floatArrayOf(hueB.toFloat(), 0.65f, 0.6f))
    })
    return bitmap
}

private fun extractDominantColor(recipe: Recipe): Color? {
    val palette = Palette.from(generateRecipePlaceholderBitmap(recipe)).generate()
    val swatch = palette.vibrantSwatch ?: palette.dominantSwatch ?: return null
    return Color(swatch.rgb)
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val dominantColor = remember(recipe.id) { extractDominantColor(recipe) }
    val containerColor = dominantColor ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = when {
        dominantColor == null -> MaterialTheme.colorScheme.onSurfaceVariant
        dominantColor.luminance() > 0.5f -> Color.Black
        else -> Color.White
    }
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = recipe.day, fontSize = 12.sp, fontWeight = FontWeight.Light)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) {
                            "Quitar ${recipe.title} de favoritas"
                        } else {
                            "Marcar ${recipe.title} como favorita"
                        },
                        tint = contentColor
                    )
                }
            }
            Text(text = recipe.title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(text = recipe.category, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${recipe.calories} kcal", fontSize = 12.sp)
            }
        }
    }
}
