package cl.duoc.rulloa.nutriplus.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.rulloa.nutriplus.data.ConnectivityObserver
import cl.duoc.rulloa.nutriplus.data.Resource

/** Aviso compacto de "sin conexión", pensado para no asustar: los datos guardados se siguen viendo. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = "Sin conexión a internet. Mostrando lo último guardado.",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Envuelve un [Resource]: muestra carga, error o el contenido normal (con un caso especial
 * para "sin datos"). Evita repetir la misma lógica de carga/vacío/error en cada pantalla.
 */
@Composable
fun <T> ResourceContent(
    resource: Resource<T>,
    isEmpty: (T) -> Boolean,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (resource) {
        is Resource.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is Resource.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudOff, contentDescription = null)
                Text(resource.message, modifier = Modifier.padding(top = 8.dp))
            }
        }

        is Resource.Success -> if (isEmpty(resource.data)) {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            content(resource.data)
        }
    }
}

/** true si el observador de conectividad reporta que no hay red en este momento. */
@Composable
fun rememberIsOffline(connectivityObserver: ConnectivityObserver): Boolean {
    val isConnected by produceState(initialValue = true) {
        connectivityObserver.observe().collect { value = it }
    }
    return !isConnected
}
