package com.example.myapplication.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MockData
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.NutritionalRecommendation
import com.example.myapplication.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
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

    if (selectedRecipe != null) {
        AlertDialog(
            onDismissRequest = { selectedRecipe = null },
            confirmButton = {
                TextButton(onClick = { selectedRecipe = null }) {
                    Text("Entendido")
                }
            },
            title = { Text(selectedRecipe?.title ?: "") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Calorías: ${selectedRecipe?.calories} kcal", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ingredientes:", fontWeight = FontWeight.SemiBold)
                    selectedRecipe?.ingredients?.forEach { ingredient ->
                        Text("- $ingredient")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preparación:", fontWeight = FontWeight.SemiBold)
                    Text(selectedRecipe?.preparation ?: "")
                }
            }
        )
    }

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
                                    selectedRecipe = recipe
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
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
