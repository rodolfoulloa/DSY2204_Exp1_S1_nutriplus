package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Mantenerse") }
    var gender by remember { mutableStateOf("Otro") }
    var termsAccepted by remember { mutableStateOf(false) }
    
    var expanded by remember { mutableStateOf(false) }
    val goals = listOf("Ganar músculo", "Perder peso", "Mantenerse")
    val genders = listOf("Femenino", "Masculino", "Otro")

    val isEmailValid = viewModel.isEmailValid(email)
    val isPasswordValid = viewModel.isPasswordValid(password)
    val isFormValid = isEmailValid && isPasswordValid && name.isNotBlank() && termsAccepted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotEmpty() && !isEmailValid,
            supportingText = {
                if (email.isNotEmpty() && !isEmailValid) {
                    Text("Formato de correo inválido")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mín. 6 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && !isPasswordValid
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dropdown (Combo box)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = goal,
                onValueChange = {},
                readOnly = true,
                label = { Text("Objetivo Nutricional") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                goals.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            goal = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Radio Buttons (Género)
        Text("Género:", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Medium)
        genders.forEach { text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == gender),
                    onClick = { gender = text }
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checkbox (Términos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Text(
                text = "Acepto los términos y condiciones",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (viewModel.register(name, email, password)) {
                    onRegisterSuccess()
                }
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Ya tengo cuenta. Iniciar sesión")
        }
    }
}
