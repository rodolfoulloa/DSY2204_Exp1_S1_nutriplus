package cl.duoc.rulloa.nutriplus.data

// Todos los modelos que se sincronizan con Realtime Database necesitan un constructor sin
// argumentos (por eso los valores por defecto): es el mecanismo que usa el SDK de Firebase
// para deserializar un DataSnapshot con getValue(Clase::class.java).

/** Perfil del usuario, guardado en /users/{uid}/profile. La contraseña la maneja Firebase Auth. */
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val goal: String = "",
    val gender: String = "",
    val createdAt: Long = 0L
)

/** Receta del catálogo común, guardada en /recipes/{recipeId}. */
data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val calories: Int = 0,
    val category: String = "", // e.g., "Almuerzo", "Cena"
    val day: String = "", // día sugerido en el catálogo, e.g. "Lunes"
    val ingredients: List<String> = emptyList(),
    val preparation: String = ""
)

/** Dispositivo guardado por el usuario en /users/{uid}/devices/{deviceId}. */
data class Device(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val createdAt: Long = 0L
)

data class NutritionalRecommendation(
    val id: Int,
    val nutrient: String,
    val amount: String,
    val status: String // e.g., "Adecuado", "Bajo", "Exceso"
)

/**
 * Datos semilla. Ya no son la fuente de datos de la app: RecipeRepository sube
 * weeklyRecipes a /recipes una única vez si el nodo está vacío, y desde ahí en
 * adelante todo se lee y escribe contra Firebase (con caché local en Room).
 */
object MockData {
    val weeklyRecipes = listOf(
        Recipe(
            "r1", "Ensalada César", "Pollo a la plancha con lechuga y aderezo light.", 350, "Almuerzo", "Lunes",
            listOf("Pechuga de pollo", "Lechuga romana", "Croutons integrales", "Queso parmesano", "Yogurt natural"),
            "1. Cocinar el pollo a la plancha.\n2. Lavar y picar la lechuga.\n3. Mezclar ingredientes y aliñar con salsa de yogurt."
        ),
        Recipe(
            "r2", "Salmón al Horno", "Salmón con espárragos y papas al vapor.", 450, "Cena", "Martes",
            listOf("Filete de salmón", "Espárragos", "Papas", "Limón", "Eneldo"),
            "1. Precalentar horno a 200°C.\n2. Colocar salmón y vegetales en bandeja.\n3. Hornear por 15-20 minutos."
        ),
        Recipe(
            "r3", "Pasta Integral", "Pasta con salsa de tomate natural y albahaca.", 400, "Almuerzo", "Miércoles",
            listOf("Pasta integral", "Tomates", "Albahaca", "Ajo", "Aceite de oliva"),
            "1. Cocer la pasta al dente.\n2. Preparar salsa con tomates picados y ajo.\n3. Mezclar y decorar con albahaca."
        ),
        Recipe(
            "r4", "Tacos de Pavo", "Tortillas de maíz con pavo picado y vegetales.", 380, "Cena", "Jueves",
            listOf("Tortillas de maíz", "Pavo molido", "Cebolla", "Pimentón", "Cilantro"),
            "1. Saltear el pavo con los vegetales.\n2. Calentar las tortillas.\n3. Armar los tacos y añadir cilantro."
        ),
        Recipe(
            "r5", "Bowl de Quinoa", "Quinoa con garbanzos, palta y espinacas.", 420, "Almuerzo", "Viernes",
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
