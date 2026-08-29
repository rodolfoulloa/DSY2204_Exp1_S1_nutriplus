package com.example.myapplication.data

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String
)

data class Recipe(
    val id: Int,
    val title: String,
    val description: String,
    val calories: Int,
    val category: String, // e.g., "Almuerzo", "Cena"
    val day: String // e.g., "Lunes", "Martes"
)

data class NutritionalRecommendation(
    val id: Int,
    val nutrient: String,
    val amount: String,
    val status: String // e.g., "Adecuado", "Bajo", "Exceso"
)

object MockData {
    // Requerimiento Semana 2/3: Lista de 5 usuarios registrados
    val registeredUsers = listOf(
        User(1, "admin@nutriplus.com", "admin123", "Administrador"),
        User(2, "usuario1@test.com", "pass123", "Juan Pérez"),
        User(3, "ayuda@nutriplus.org", "nutri2026", "Soporte Nutricional"),
        User(4, "estudiante@duoc.cl", "duoc123", "Alumno Nutrición"),
        User(5, "maria@correo.cl", "maria456", "María González")
    )

    // Datos para el entorno de Minuta Nutricional
    val weeklyRecipes = listOf(
        Recipe(1, "Ensalada César", "Pollo a la plancha con lechuga y aderezo light.", 350, "Almuerzo", "Lunes"),
        Recipe(2, "Salmón al Horno", "Salmón con espárragos y papas al vapor.", 450, "Cena", "Martes"),
        Recipe(3, "Pasta Integral", "Pasta con salsa de tomate natural y albahaca.", 400, "Almuerzo", "Miércoles"),
        Recipe(4, "Tacos de Pavo", "Tortillas de maíz con pavo picado y vegetales.", 380, "Cena", "Jueves"),
        Recipe(5, "Bowl de Quinoa", "Quinoa con garbanzos, palta y espinacas.", 420, "Almuerzo", "Viernes")
    )

    val nutritionalRecommendations = listOf(
        NutritionalRecommendation(1, "Proteínas", "80g", "Adecuado"),
        NutritionalRecommendation(2, "Carbohidratos", "200g", "Exceso"),
        NutritionalRecommendation(3, "Grasas", "50g", "Bajo"),
        NutritionalRecommendation(4, "Fibra", "30g", "Adecuado"),
        NutritionalRecommendation(5, "Hierro", "15mg", "Adecuado")
    )
}
