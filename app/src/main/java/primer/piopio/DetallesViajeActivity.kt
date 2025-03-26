package primer.piopio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DetallesViajeActivity : AppCompatActivity() {

    private lateinit var viajeDetallesTextView: TextView
    private lateinit var perfilUsuarioButton: Button
    private lateinit var contactarButton: Button
    private lateinit var estadoSesionTextView: TextView
    private lateinit var nombreUsuarioTextView: TextView  // Aquí se añadirá el TextView para el nombre del usuario
    private lateinit var db: FirebaseFirestore
    private var viajeId: String? = null
    private var publicadoPor: String? = null
    private var idCreador: String? = null  // ID del creador del viaje

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detallesviaje)

        // Inicializa las vistas
        viajeDetallesTextView = findViewById(R.id.viajeDetallesTextView)
        perfilUsuarioButton = findViewById(R.id.btnPerfilUsuario)
        contactarButton = findViewById(R.id.btnContactar)
        estadoSesionTextView = findViewById(R.id.estadoSesionTextView)
        nombreUsuarioTextView = findViewById(R.id.btnPerfilUsuario)  // Asegúrate de que este ID esté en tu XML
        db = FirebaseFirestore.getInstance()

        viajeId = intent.getStringExtra("viaje_id")
        publicadoPor = intent.getStringExtra("publicado_por") ?: "Desconocido"
        idCreador = intent.getStringExtra("ID_CREADOR") ?: ""

        // Configuración de sesión
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

        // Cargar detalles del viaje
        if (viajeId != null) {
            db.collection("viajes").document(viajeId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val detalles = "${document.getString("origen")} -> ${document.getString("destino")} (${document.getString("fecha")})"
                        viajeDetallesTextView.text = detalles
                    } else {
                        viajeDetallesTextView.text = "Detalles no disponibles"
                    }
                }
                .addOnFailureListener {
                    viajeDetallesTextView.text = "Error al cargar detalles"
                }
        }

        // Mostrar el nombre del usuario que publicó el viaje
        if (idCreador?.isNotEmpty() == true) {
            obtenerNombreUsuario(idCreador!!)
        } else {
            nombreUsuarioTextView.text = "Anónimo"
        }

        perfilUsuarioButton.text = "Viaje publicado por: $publicadoPor"
        perfilUsuarioButton.setOnClickListener {
            val intent = Intent(this, PerfilOtroUsuarioActivity::class.java)
            intent.putExtra("usuario_id", publicadoPor)
            startActivity(intent)
        }

        // Configurar el botón de contactar
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        if (usuarioActual != null) {
            contactarButton.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("usuarioDestino", publicadoPor)
                startActivity(intent)
            }
        } else {
            contactarButton.isEnabled = false
            contactarButton.text = "Inicia sesión para contactar"
        }
    }

    // Función para obtener el nombre del usuario que creó el viaje
    private fun obtenerNombreUsuario(userId: String) {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreUsuario = document.getString("Usuario")
                    if (!nombreUsuario.isNullOrEmpty()) {
                        nombreUsuarioTextView.text = nombreUsuario
                    } else {
                        nombreUsuarioTextView.text = "Usuario desconocido"
                    }
                } else {
                    nombreUsuarioTextView.text = "Usuario no encontrado"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show()
            }
    }
}
