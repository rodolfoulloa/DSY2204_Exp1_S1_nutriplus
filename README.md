# NutriPlus

App de minuta nutricional semanal (Kotlin + Jetpack Compose), pensada para personas con poca
experiencia usando apps. Proyecto de la asignatura DSY2204 (Duoc UC).

## Semana 7 — Back end con Firebase

Esta entrega reemplaza los datos en memoria (`MockData`) por Firebase como back end real:

1. **Firebase y arquitectura de datos.** Firebase Auth (Email/Contraseña) y Realtime Database,
   con persistencia local activada. Capa de repositorios (`AuthRepository`, `UserRepository`,
   `RecipeRepository`, `MinutaRepository`, `DeviceRepository`) que usan coroutines y Flow
   (`callbackFlow` para los listeners en tiempo real, `await()` para operaciones puntuales). Los
   ViewModels solo hablan con los repositorios, nunca con Firebase directamente.
2. **Autenticación y CRUD de usuario.** Login, registro y recuperar contraseña contra Firebase
   Auth, con mensajes de error en español simples. La sesión persiste entre aperturas de la app
   (si ya iniciaste sesión, se entra directo a la Minuta). Perfil editable (nombre), cerrar
   sesión y eliminar cuenta (borra también todos los datos del usuario). La navegación está
   protegida: sin sesión solo se puede ver Login, Registro y Recuperar contraseña.
3. **CRUD de las vistas.** Minuta (asignar/cambiar/quitar recetas por día), Recetas (catálogo +
   favoritas) y **Mis Dispositivos** — antes "BuscarDispositivo" no existía en el código; se
   implementó como un CRUD manual simple (nombre + tipo, sin escaneo real ni permisos nuevos,
   para no agregar componentes Android solo para demostrar).
4. **Reglas de seguridad** en `database.rules.json` (ver más abajo).
5. **Consolidación pedida por el docente:**
   - `RecipeContentProvider` dejó de hacer CRUD sobre una lista en memoria: ahora lee y escribe
     una caché local en **Room**, sincronizada desde `RecipeRepository`/`MinutaRepository`. Las
     escrituras del Provider también se intentan propagar a Firebase (ver la nota sobre reglas
     más abajo).
   - El detalle de receta dejó de ser un `Fragment` dentro de un overlay manual: ahora es
     `RecipeDetailScreen`, una pantalla Compose normal en la ruta `recipe/{recipeId}`.
   - El Widget abre la receta del día directo en `RecipeDetailScreen` (deep link
     `nutriplus://recipe/{id}`, `PendingIntent` inmutable) y se refresca cuando cambia la minuta.

## Estructura de la base de datos (Realtime Database)

```
/recipes/{recipeId}
    id, title, description, calories, category, day, ingredients, preparation
    # Catálogo común. Semilla subida una sola vez desde MockData si el nodo está vacío.

/users/{uid}/profile
    name, email, goal, gender, createdAt

/users/{uid}/minuta/{dia}/{recipeId} = fecha de asignación (timestamp)
    # dia ∈ {Lunes..Domingo}. Puede haber varias recetas por día (ej. almuerzo y cena).

/users/{uid}/favorites/{recipeId} = true

/users/{uid}/devices/{deviceId}
    id, name, type, createdAt
```

Caché local (Room, `app/schemas/`): tablas `recipes` y `minuta_assignments`, sincronizadas en
segundo plano mientras la app está abierta. Solo las usan `RecipeContentProvider` y el Widget,
que necesitan responder de forma síncrona (sin esperar a la red).

## Reglas de seguridad

Definidas en [`database.rules.json`](database.rules.json):

- `recipes`: lectura solo para usuarios autenticados; **escritura siempre denegada** desde el
  cliente (es un catálogo común, no lo edita nadie desde la app).
- `users/{uid}`: cada usuario solo puede leer y escribir su propio nodo (`auth.uid === $uid`).
- Validaciones básicas de campos requeridos y tipos (`.validate`) en perfil, minuta, favoritas
  y dispositivos.

> **Importante — orden de despliegue:** como `recipes` queda de solo lectura, la carga inicial
> del catálogo (`RecipeRepository.seedCatalogIfEmpty`, que sube `MockData` la primera vez que la
> app corre) **deja de funcionar apenas se publican estas reglas**. Para dejar el proyecto listo:
> 1. Corre la app una vez con las reglas por defecto de un proyecto nuevo (o las del emulador de
>    Firebase) para que se suba el catálogo semilla, **o** pega el contenido de
>    `MockData.weeklyRecipes` directo en la consola (Realtime Database → pestaña Datos → ⋮ →
>    Importar JSON).
> 2. Recién después publica `database.rules.json`.
>
> Para publicarlas:
> - **Consola:** Firebase Console → Realtime Database → Reglas → pega el contenido de
>   `database.rules.json` → Publicar.
> - **CLI:** con la [Firebase CLI](https://firebase.google.com/docs/cli) instalada y logueada
>   (`firebase login`), desde la raíz del repo:
>   ```
>   firebase deploy --only database
>   ```
>   El proyecto (`nutriplus-b362b`) ya está configurado en `.firebaserc`.

## Configurar Firebase

1. En la [consola de Firebase](https://console.firebase.google.com/), crea o abre el proyecto y
   agrega una app Android con el paquete **`cl.duoc.rulloa.nutriplus`**.
2. Descarga el `google-services.json` generado y colócalo en `app/google-services.json`
   (ya está incluido en este repo para el proyecto del curso; si usas tu propio proyecto de
   Firebase, reemplázalo).
3. Habilita **Authentication → Sign-in method → Correo electrónico/contraseña**.
4. Habilita **Realtime Database** (modo de prueba para desarrollar, y publica
   `database.rules.json` antes de entregar/mostrar la app en producción — ver sección anterior).

## Cómo probar la app

- **Compilar:** `./gradlew assembleDebug` (o `Run` desde Android Studio).
- **Registro/Login:** crea una cuenta desde "Regístrate aquí"; Firebase deja la sesión iniciada
  de inmediato y entra a la Minuta. Cerrar y volver a abrir la app debería mantener la sesión.
- **Minuta:** en cada día usa el botón **+** para agregar una receta del catálogo, y la ❌ junto a
  una receta asignada para quitarla.
- **Recetas:** busca por nombre/día/categoría y marca/desmarca el corazón para favoritas.
- **Mis Dispositivos:** agrega, edita (lápiz) y elimina (basurero) dispositivos con el botón +.
- **Perfil:** edita el nombre, cierra sesión y prueba "Eliminar mi cuenta" (borra todo).
- **Sin conexión:** activa modo avión con la app abierta; las pantallas deben seguir mostrando
  los últimos datos (gracias a `setPersistenceEnabled(true)`) con el aviso "Sin conexión a
  internet" arriba.
- **Widget:** agrega el widget de NutriPlus a la pantalla de inicio, asigna una receta al día de
  hoy en la Minuta y tócalo: debería abrir NutriPlus directo en el detalle de esa receta (si no
  hay sesión iniciada, abre primero el Login).
