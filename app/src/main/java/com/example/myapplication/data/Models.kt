package com.example.myapplication.data

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val goal: String,
    val gender: String
)

data class Recipe(
    val id: Int,
    val title: String,
    val description: String,
    val calories: Int,
    val category: String, // e.g., "Almuerzo", "Cena"
    val day: String, // e.g., "Lunes", "Martes"
    val ingredients: List<String>,
    val preparation: String
)

data class NutritionalRecommendation(
    val id: Int,
    val nutrient: String,
    val amount: String,
    val status: String // e.g., "Adecuado", "Bajo", "Exceso"
)

object MockData {
    // Requerimiento Semana 4: Lista de usuarios registrados (mutable)
    val registeredUsers = mutableListOf(
        User(1, "admin@nutriplus.com", "admin123", "Administrador", "Mantenerse", "Otro"),
        User(2, "usuario1@test.com", "pass123", "Juan Pérez", "Ganar músculo", "Masculino"),
        User(3, "ayuda@nutriplus.org", "nutri2026", "Soporte Nutricional", "Mantenerse", "Otro"),
        User(4, "estudiante@duoc.cl", "duoc123", "Alumno Nutrición", "Perder peso", "Femenino"),
        User(5, "maria@correo.cl", "maria456", "María González", "Perder peso", "Femenino")
    )

    // Datos para el entorno de Minuta Nutricional (mutable: RecipeContentProvider hace CRUD sobre esta lista)
    val weeklyRecipes = mutableListOf(
        Recipe(
            1, "Ensalada César", "Pollo a la plancha con lechuga y aderezo light.", 350, "Almuerzo", "Lunes",
            listOf("Pechuga de pollo", "Lechuga romana", "Croutons integrales", "Queso parmesano", "Yogurt natural"),
            "1. Cocinar el pollo a la plancha.\n2. Lavar y picar la lechuga.\n3. Mezclar ingredientes y aliñar con salsa de yogurt."
        ),
        Recipe(
            2, "Salmón al Horno", "Salmón con espárragos y papas al vapor.", 450, "Cena", "Martes",
            listOf("Filete de salmón", "Espárragos", "Papas", "Limón", "Eneldo"),
            "1. Precalentar horno a 200°C.\n2. Colocar salmón y vegetales en bandeja.\n3. Hornear por 15-20 minutos."
        ),
        Recipe(
            3, "Pasta Integral", "Pasta con salsa de tomate natural y albahaca.", 400, "Almuerzo", "Miércoles",
            listOf("Pasta integral", "Tomates", "Albahaca", "Ajo", "Aceite de oliva"),
            "1. Cocer la pasta al dente.\n2. Preparar salsa con tomates picados y ajo.\n3. Mezclar y decorar con albahaca."
        ),
        Recipe(
            4, "Tacos de Pavo", "Tortillas de maíz con pavo picado y vegetales.", 380, "Cena", "Jueves",
            listOf("Tortillas de maíz", "Pavo molido", "Cebolla", "Pimentón", "Cilantro"),
            "1. Saltear el pavo con los vegetales.\n2. Calentar las tortillas.\n3. Armar los tacos y añadir cilantro."
        ),
        Recipe(
            5, "Bowl de Quinoa", "Quinoa con garbanzos, palta y espinacas.", 420, "Almuerzo", "Viernes",
            listOf("Quinoa", "Garbanzos", "Palta", "Espinaca", "Semillas de sésamo"),
            "1. Cocer la quinoa.\n2. Mezclar en un bowl con los garbanzos y espinacas.\n3. Añadir palta en láminas encima."
        )
    )

    val nutritionalRecommendations = listOf(
        NutritionalRecommendation(1, "Proteínas", "80g", "Adecuado"),
        NutritionalRecommendation(2, "Carbohidratos", "200g", "Exceso"),
        NutritionalRecommendation(3, "Grasas", "50g", "Bajo"),
        NutritionalRecommendation(4, "Fibra", "30g", "Adecuado"),
        NutritionalRecommendation(5, "Hierro", "15mg", "Adecuado")
    )
}
