package cl.duoc.rulloa.nutriplus.ui.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.nutriplus.data.ConnectivityObserver
import cl.duoc.rulloa.nutriplus.data.Device
import cl.duoc.rulloa.nutriplus.ui.common.OfflineBanner
import cl.duoc.rulloa.nutriplus.ui.common.ResourceContent
import cl.duoc.rulloa.nutriplus.ui.common.rememberIsOffline

/** BuscarDispositivo: guarda, lista, renombra y elimina los dispositivos del usuario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: DeviceViewModel,
    connectivityObserver: ConnectivityObserver
) {
    val devicesState by viewModel.devicesState.collectAsState()
    val isOffline = rememberIsOffline(connectivityObserver)
    var editingDevice by remember { mutableStateOf<Device?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        DeviceFormDialog(
            title = "Agregar dispositivo",
            initialName = "",
            initialType = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type ->
                viewModel.addDevice(name, type)
                showAddDialog = false
            }
        )
    }

    editingDevice?.let { device ->
        DeviceFormDialog(
            title = "Editar dispositivo",
            initialName = device.name,
            initialType = device.type,
            onDismiss = { editingDevice = null },
            onConfirm = { name, type ->
                viewModel.renameDevice(device.id, name, type)
                editingDevice = null
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Dispositivos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar dispositivo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isOffline) OfflineBanner()
            ResourceContent(
                resource = devicesState,
                isEmpty = { it.isEmpty() },
                emptyMessage = "Aún no has guardado ningún dispositivo",
                modifier = Modifier.fillMaxSize()
            ) { devices ->
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(devices, key = { it.id }) { device ->
                        DeviceRow(
                            device = device,
                            onEdit = { editingDevice = device },
                            onDelete = { viewModel.deleteDevice(device.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: Device, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.Bold)
                Text(device.type, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Renombrar ${device.name}")
            }
            IconButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar ${device.name}")
            }
        }
    }
}

@Composable
private fun DeviceFormDialog(
    title: String,
    initialName: String,
    initialType: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Balanza de cocina)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Tipo (ej. Balanza, Reloj)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, type) },
                enabled = name.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
