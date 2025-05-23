package com.example.firebaseconcloud

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.firebaseconcloud.ui.theme.FirebaseConCloudTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private lateinit var fcmReceiver: BroadcastReceiver
    private lateinit var mensajeGlobal: MutableState<String>
    private lateinit var tokenFCM: MutableState<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mensajeGlobal = mutableStateOf("Esperando notificación...")
        tokenFCM = mutableStateOf("Obteniendo token...")

        // Pedir permiso de notificaciones en tiempo de ejecucion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show()
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Obtener token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { tokenFCM.value = it }
            .addOnFailureListener { tokenFCM.value = "Error al obtener token" }

        // Receiver local para mensajes en tiempo real
        fcmReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val title = intent?.getStringExtra("title") ?: "Sin título"
                val body = intent?.getStringExtra("body") ?: "Sin mensaje"
                val mensaje = "$title: $body"
                mensajeGlobal.value = mensaje
                Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_LONG).show()
            }
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(fcmReceiver, IntentFilter("FCM_MESSAGE"))

        // Cargar toda la interfaz
        setContent {
            FirebaseConCloudTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaPrincipal(mensajeGlobal.value, tokenFCM.value)
                }
            }
        }

        // Procesar datos del intent si viene desde una notificación
        procesarIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        procesarIntent(intent)
    }

    private fun procesarIntent(intent: Intent?) {
        intent?.extras?.let {
            val titleExtra = it.getString("title")
            val bodyExtra = it.getString("body")
            if (!titleExtra.isNullOrBlank() || !bodyExtra.isNullOrBlank()) {
                val mensaje = "${titleExtra ?: ""}: ${bodyExtra ?: ""}"
                Log.d("La notificacion we", mensaje)
                mensajeGlobal.value = mensaje
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver)
        super.onDestroy()
    }
}

@Composable
fun PantallaPrincipal(mensaje: String, token: String) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Token FCM:", style = MaterialTheme.typography.titleMedium)

        Text(
            text = token,
            modifier = Modifier.clickable {
                clipboard.setText(AnnotatedString(token))
                Toast.makeText(context, "Token copiado al portapapeles", Toast.LENGTH_SHORT).show()
            },
            style = MaterialTheme.typography.bodySmall
        )

        Divider()

        Text("Último mensaje recibido:", style = MaterialTheme.typography.titleMedium)
        Text(mensaje, style = MaterialTheme.typography.bodyLarge)
    }
}