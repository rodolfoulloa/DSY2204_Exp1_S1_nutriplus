package com.example.myapplication.ui.main

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.palette.graphics.Palette
import com.example.myapplication.data.MockData
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.NutritionalRecommendation
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.detail.RecipeDetailFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    var selectedRecipeId by remember { mutableStateOf<Int?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    val filteredRecipes by viewModel.filteredRecipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Informe Detallado Nutricional") },
            text = {
                Text("Este informe contiene el análisis completo de tu ingesta semanal, comparado con los objetivos establecidos por tu nutricionista.")
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }

    // El overlay del detalle se dibuja dentro del mismo Box que el Scaffold (no en un Dialog/Window
    // aparte), porque el FragmentManager de la Activity solo puede resolver el id del
    // FragmentContainerView si este vive en el árbol de vistas de la propia Activity.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Minuta Semanal") },
                    actions = {
                        TextButton(onClick = onLogout) {
                            Text("Salir", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Barra de Búsqueda (Mejora Semana 4)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    label = { Text("Buscar recetas (día, plato...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Recetas Sugeridas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Grid de Recetas
                    Box(modifier = Modifier.height(350.dp)) {
                        if (filteredRecipes.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No se encontraron recetas", color = Color.Gray)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRecipes) { recipe ->
                                    RecipeCard(recipe) {
                                        selectedRecipeId = recipe.id
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Recomendaciones Nutricionales",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // "Tabla" de recomendaciones
                    NutritionalTable(MockData.nutritionalRecommendations)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Link ACTIVO
                    TextButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ver informe detallado completo", color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        selectedRecipeId?.let { recipeId ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { selectedRecipeId = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* consume el click para no cerrar el overlay */ },
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Contenido de detalle delegado a un Fragment (interop Compose + Fragments)
                        RecipeDetailFragmentHost(
                            recipeId = recipeId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                        )
                        TextButton(
                            onClick = { selectedRecipeId = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Entendido")
                        }
                    }
                }
            }
        }
    }
}

// Patrón oficial de interop Compose + Fragments: FragmentContainerView alojado vía AndroidView,
// con la transacción manejada por el FragmentManager de la Activity (ver developer.android.com/develop/ui/compose/migrate/interoperability-apis/fragments-in-compose)
private tailrec fun Context.findFragmentActivity(): FragmentActivity = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> error("No se encontró una FragmentActivity en el contexto")
}

@Composable
fun RecipeDetailFragmentHost(recipeId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fragmentManager = remember(context) { context.findFragmentActivity().supportFragmentManager }
    key(recipeId) {
        AndroidView(
            modifier = modifier,
            factory = { FragmentContainerView(it).apply { id = View.generateViewId() } },
            update = { container ->
                if (fragmentManager.findFragmentById(container.id) == null) {
                    fragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(container.id, RecipeDetailFragment.newInstance(recipeId), "recipe_detail_$recipeId")
                    }
                }
            },
            onRelease = { container ->
                fragmentManager.findFragmentById(container.id)?.let { fragment ->
                    fragmentManager.commit { remove(fragment) }
                }
            }
        )
    }
}

// No hay fotos reales por receta en MockData, así que generamos un bitmap
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
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
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
            Text(text = recipe.day, fontSize = 12.sp, fontWeight = FontWeight.Light)
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

@Composable
fun NutritionalTable(recommendations: List<NutritionalRecommendation>) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text("Nutriente", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Cantidad", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Estado", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
            
            // Rows
            recommendations.forEach { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(rec.nutrient, modifier = Modifier.weight(1f))
                    Text(rec.amount, modifier = Modifier.weight(1f))
                    Text(
                        text = rec.status,
                        modifier = Modifier.weight(1f),
                        color = when(rec.status) {
                            "Exceso" -> Color.Red
                            "Bajo" -> Color(0xFFFFA500) // Orange
                            else -> Color(0xFF008000) // Green
                        }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
