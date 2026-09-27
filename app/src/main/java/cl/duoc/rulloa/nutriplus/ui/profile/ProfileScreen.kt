package cl.duoc.rulloa.nutriplus.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.nutriplus.ui.common.ResourceContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!actionState.isLoading) showDeleteConfirm = false },
            title = { Text("Eliminar cuenta") },
            text = {
                Column {
                    Text("Se borrará tu cuenta y todos tus datos (minuta, favoritas y dispositivos). Esta acción no se puede deshacer.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Para confirmar, escribe tu contraseña:")
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it; viewModel.clearError() },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = actionState.error != null,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    // El error se muestra dentro del diálogo, que queda abierto hasta que funcione.
                    if (actionState.error != null) {
                        Text(
                            actionState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(deletePassword) { result ->
                            if (result.isSuccess) {
                                showDeleteConfirm = false
                                onLoggedOut()
                            }
                        }
                    },
                    enabled = !actionState.isLoading,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    if (actionState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    enabled = !actionState.isLoading,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Cancelar") }
            }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Mi Perfil") }) }) { padding ->
        // isEmpty = false: aunque falte el perfil en la base de datos, se muestran igual el
        // correo de la sesión y los botones de cerrar sesión / eliminar cuenta.
        ResourceContent(
            resource = profileState,
            isEmpty = { false },
            emptyMessage = "",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { profile ->
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text("Correo electrónico", style = MaterialTheme.typography.labelLarge)
                Text(
                    profile?.email ?: viewModel.sessionEmail.orEmpty(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Nombre", style = MaterialTheme.typography.labelLarge)
                if (profile == null) {
                    Text(
                        "No encontramos los datos de tu perfil.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isEditingName) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Button(
                            onClick = {
                                viewModel.updateName(nameDraft)
                                // Con un nombre vacío se queda en edición para que se vea el error.
                                if (nameDraft.isNotBlank()) isEditingName = false
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Guardar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { isEditingName = false },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Cancelar") }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                viewModel.clearError()
                                nameDraft = profile.name
                                isEditingName = true
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text("Editar") }
                    }
                }

                if (actionState.error != null && !showDeleteConfirm) {
                    Text(
                        actionState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Cerrar sesión") }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.clearError()
                        deletePassword = ""
                        showDeleteConfirm = true
                    },
                    enabled = !actionState.isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Eliminar mi cuenta") }
            }
        }
    }
}
