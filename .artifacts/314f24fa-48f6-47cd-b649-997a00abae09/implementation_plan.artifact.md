# Plan de Implementación: Sistema de Autenticación y Pantalla Principal de Nutrición

Este plan detalla la implementación de un flujo de autenticación completo (Login, Registro, Recuperación) y una pantalla principal funcional para la aplicación "NutriPlus", utilizando Jetpack Compose y Material Design 3.

## User Review Required

> [!IMPORTANT]
> La implementación utilizará datos locales simulados (mock) para las recetas y el proceso de autenticación, ya que no se ha especificado un backend (Firebase, API, etc.).
> Se añadirán dependencias de Navegación y ViewModel a los archivos Gradle.

## Proposed Changes

### [Dependencias]

#### [MODIFY] [libs.versions.toml](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/gradle/libs.versions.toml)
Se añadirán las versiones y definiciones para:
- `androidx.navigation:navigation-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`

#### [MODIFY] [build.gradle.kts](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/build.gradle.kts)
Se incluirán las nuevas dependencias en el bloque `dependencies`.

---

### [Navegación y Modelos]

#### [NEW] [NavGraph.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/ui/navigation/NavGraph.kt)
Define las rutas de la aplicación: `Login`, `Register`, `RecoverPassword` y `Main`.

#### [NEW] [Models.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/data/Models.kt)
Clases de datos para `Recipe` y `NutritionalRecommendation`.

---

### [Pantallas de Autenticación]

#### [NEW] [LoginScreen.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/ui/auth/LoginScreen.kt)
- **Inputs**: Email y Password.
- **Botones**: "Iniciar Sesión".
- **Vínculos**: "Registrarse" y "¿Olvidaste tu contraseña?".

#### [NEW] [RegisterScreen.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/ui/auth/RegisterScreen.kt)
- **Inputs**: Nombre, Email, Password.
- **Combo box (Dropdown)**: Objetivo nutricional (Ganar músculo, Perder peso, Mantenerse).
- **Radio buttons**: Género.
- **Check list**: Aceptación de términos y condiciones.

#### [NEW] [RecoverPasswordScreen.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/ui/auth/RecoverPasswordScreen.kt)
- **Inputs**: Email.
- **Botones**: "Enviar instrucciones".

---

### [Pantalla Principal (Minuta)]

#### [NEW] [MainScreen.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/ui/main/MainScreen.kt)
- **Grilla (Grid)**: Mostrará las 5 recetas semanales.
- **Tablas/Listas**: Recomendaciones nutricionales detalladas.
- Diseño adaptativo y simplificado para accesibilidad.

---

### [Integración]

#### [MODIFY] [MainActivity.kt](file:///C:/proyectos/DSY2204_Exp1_S1_nutriplus/app/src/main/java/com/example/myapplication/MainActivity.kt)
Se reemplazará el contenido de `setContent` para inicializar el `NavHost`.

## Verification Plan

### Manual Verification
1. Ejecutar la app y verificar que inicie en la pantalla de Login.
2. Navegar a Registro y Recuperación de Contraseña usando los links.
3. Simular un login exitoso y verificar la transición a la pantalla de Minuta.
4. Validar que la pantalla de Minuta muestre correctamente la grilla de recetas y las recomendaciones.
5. Probar la adaptabilidad rotando el dispositivo (si aplica).
