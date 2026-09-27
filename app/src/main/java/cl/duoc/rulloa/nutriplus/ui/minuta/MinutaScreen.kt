package cl.duoc.rulloa.nutriplus.ui.minuta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.rulloa.nutriplus.data.ConnectivityObserver
import cl.duoc.rulloa.nutriplus.data.MockData
import cl.duoc.rulloa.nutriplus.data.NutritionalRecommendation
import cl.duoc.rulloa.nutriplus.data.Recipe
import cl.duoc.rulloa.nutriplus.data.Resource
import cl.duoc.rulloa.nutriplus.ui.common.OfflineBanner
import cl.duoc.rulloa.nutriplus.ui.common.ResourceContent
import cl.duoc.rulloa.nutriplus.ui.common.rememberIsOffline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinutaScreen(
    viewModel: MinutaViewModel,
    connectivityObserver: ConnectivityObserver,
    onRecipeClick: (String) -> Unit
) {
    val minutaState by viewModel.minutaByDayState.collectAsState()
    val catalogState by viewModel.catalogState.collectAsState()
    val isOffline = rememberIsOffline(connectivityObserver)
    var showInfoDialog by remember { mutableStateOf(false) }
    var dayForPicker by remember { mutableStateOf<String?>(null) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") }
            },
            title = { Text("Informe Detallado Nutricional") },
            text = {
                Text("Este informe contiene el análisis completo de tu ingesta semanal, comparado con los objetivos establecidos por tu nutricionista.")
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }

    dayForPicker?.let { day ->
        val assignedIds = (minutaState as? Resource.Success)?.data
            ?.firstOrNull { it.day == day }?.recipes?.map { it.id }.orEmpty().toSet()
        val available = (catalogState as? Resource.Success)?.data.orEmpty().filter { it.id !in assignedIds }
        RecipePickerDialog(
            title = "Agregar receta a $day",
            recipes = available,
            onDismiss = { dayForPicker = null },
            onSelected = { recipe ->
                viewModel.assign(day, recipe.id)
                dayForPicker = null
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Minuta Semanal") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isOffline) OfflineBanner()
            ResourceContent(
                resource = minutaState,
                isEmpty = { false },
                emptyMessage = "Aún no tienes recetas en tu minuta",
                modifier = Modifier.fillMaxSize()
            ) { days ->
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(days) { minutaDay ->
                        DaySection(
                            minutaDay = minutaDay,
                            onRecipeClick = onRecipeClick,
                            onRemove = { recipeId -> viewModel.remove(minutaDay.day, recipeId) },
                            onAddClick = { dayForPicker = minutaDay.day }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recomendaciones Nutricionales",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        NutritionalTable(MockData.nutritionalRecommendations)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Ver informe detallado completo", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySection(
    minutaDay: MinutaDay,
    onRecipeClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(minutaDay.day, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onAddClick, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar receta a ${minutaDay.day}")
                }
            }
            if (minutaDay.recipes.isEmpty()) {
                Text(
                    "Sin recetas asignadas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                minutaDay.recipes.forEach { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { onRecipeClick(recipe.id) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.title, fontWeight = FontWeight.Medium)
                            Text("${recipe.category} · ${recipe.calories} kcal", fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = { onRemove(recipe.id) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar ${recipe.title} de ${minutaDay.day}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipePickerDialog(
    title: String,
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onSelected: (Recipe) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(title) },
        text = {
            if (recipes.isEmpty()) {
                Text("No hay más recetas disponibles para agregar.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(recipes) { recipe ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { onSelected(recipe) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(recipe.title, fontWeight = FontWeight.Medium)
                                Text("${recipe.day} · ${recipe.category}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun NutritionalTable(recommendations: List<NutritionalRecommendation>) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
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
            recommendations.forEach { rec ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(rec.nutrient, modifier = Modifier.weight(1f))
                    Text(rec.amount, modifier = Modifier.weight(1f))
                    Text(
                        text = rec.status,
                        modifier = Modifier.weight(1f),
                        color = when (rec.status) {
                            "Exceso" -> Color.Red
                            "Bajo" -> Color(0xFFFFA500)
                            else -> Color(0xFF008000)
                        }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
