package com.example.myapplication.ui.detail

import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.myapplication.data.Recipe
import com.example.myapplication.data.RecipeContentProvider
import com.example.myapplication.data.toRecipe
import com.example.myapplication.ui.theme.MyApplicationTheme

class RecipeDetailFragment : Fragment() {

    companion object {
        private const val ARG_RECIPE_ID = "arg_recipe_id"

        fun newInstance(recipeId: Int): RecipeDetailFragment = RecipeDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_RECIPE_ID, recipeId) }
        }

        private fun queryRecipeById(context: Context, id: Int): Recipe? {
            val uri = ContentUris.withAppendedId(RecipeContentProvider.CONTENT_URI, id.toLong())
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) return cursor.toRecipe()
            }
            return null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recipeId = arguments?.getInt(ARG_RECIPE_ID) ?: -1
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyApplicationTheme {
                    val recipe = remember(recipeId) { queryRecipeById(requireContext(), recipeId) }
                    RecipeDetailContent(recipe)
                }
            }
        }
    }
}

@Composable
fun RecipeDetailContent(recipe: Recipe?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = recipe?.title ?: "Receta no encontrada",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
        if (recipe != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Calorías: ${recipe.calories} kcal", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ingredientes:", fontWeight = FontWeight.SemiBold)
            recipe.ingredients.forEach { ingredient ->
                Text("- $ingredient")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Preparación:", fontWeight = FontWeight.SemiBold)
            Text(recipe.preparation)
        }
    }
}
