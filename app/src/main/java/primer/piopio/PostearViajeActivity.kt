package primer.piopio

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import android.location.Geocoder
import android.location.Address
import android.util.Log
import com.google.firebase.firestore.GeoPoint


class PostearViajeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_postearviaje)

        val origenEditText = findViewById<EditText>(R.id.origenEditText)
        val destinoEditText = findViewById<EditText>(R.id.etDestino)
        val fechaEditText = findViewById<EditText>(R.id.fechaEditText)
        val btnPostear = findViewById<Button>(R.id.btnPostear)

        ///boton login
        val estadoSesionTextView = findViewById<TextView>(R.id.estadoSesionTextView)
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            estadoSesionTextView.text = user.displayName ?: "Perfil"
            estadoSesionTextView.setOnClickListener {
                startActivity(Intent(this, PerfilPropioUsuarioActivity::class.java))
            }
        } else {
            estadoSesionTextView.text = "Log in"
            estadoSesionTextView.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }


        // Selección de fecha con DatePickerDialog
        fechaEditText.setOnClickListener {
            val calendario = Calendar.getInstance()
            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val fechaSeleccionada = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                fechaEditText.setText(fechaSeleccionada)
            }, year, month, day)

            datePicker.show()
        }

        // Manejar el clic en "Publicar viaje"
        btnPostear.setOnClickListener {
            //confirmar que esta logeado y si no entrar?
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Si el usuario está autenticado, continuar con la acción de postear viaje
                startActivity(Intent(this, PostearViajeActivity::class.java))
            } else {
                // Si no está autenticado, redirigir al login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            //...
            val origen = origenEditText.text.toString().trim()
            val destino = destinoEditText.text.toString().trim()
            val fecha = fechaEditText.text.toString().trim()

            if (origen.isEmpty() || destino.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                //////
                // Primero obtenemos las coordenadas del origen
                obtenerCoordenadas(origen) { origenGeo ->
                    if (origenGeo == null) {
                        Toast.makeText(this, "Dirección de origen no válida", Toast.LENGTH_SHORT)
                            .show()
                        return@obtenerCoordenadas
                    }
                    // Luego las del destino
                    obtenerCoordenadas(destino) { destinoGeo ->
                        if (destinoGeo == null) {
                            Toast.makeText(
                                this,
                                "Dirección de destino no válida",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@obtenerCoordenadas
                        }

                        /////
                        val db = FirebaseFirestore.getInstance()
                        val viaje = hashMapOf(
                            "origen" to origen,
                            "destino" to destino,
                            "fecha" to fecha,
                            "usuario" to FirebaseAuth.getInstance().currentUser?.displayName,
                            "origenGeoPoint" to origenGeo,
                            "destinoGeoPoint" to destinoGeo
                        )

                        db.collection("viajes")
                            .add(viaje)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Viaje publicado exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Limpiar campos o redirigir
                                origenEditText.text.clear()
                                destinoEditText.text.clear()
                                fechaEditText.text.clear()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al publicar viaje", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
            }


        }
    }
    // Función para convertir dirección en coordenadas
    private fun obtenerCoordenadas(direccion: String, callback: (GeoPoint?) -> Unit) {
        val geocoder = Geocoder(this)
        try {
            val addresses: List<Address> =
                geocoder.getFromLocationName(direccion, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val location = addresses[0]
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                callback(geoPoint)
            } else {
                Log.e("Geocoder", "No se encontró la dirección: $direccion")
                callback(null)
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error al obtener coordenadas: ${e.message}")
            callback(null)
        }
    }
}
