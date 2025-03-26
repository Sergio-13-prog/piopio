package primer.piopio

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PerfilPropioUsuarioActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nombreTextView: TextView
    private lateinit var perfilImageView: ImageView
    private lateinit var editarNombre: EditText
    private lateinit var guardarBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_perfilpropio)

        val btnPostearViaje = findViewById<Button>(R.id.btnPostearViaje)
        val logoutButton = findViewById<Button>(R.id.logoutButton)


        btnPostearViaje.setOnClickListener {
            startActivity(Intent(this, PostearViajeActivity::class.java))
        }
        logoutButton.setOnClickListener {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Limpiar las preferencias compartidas
            val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove("usuario")
                apply()
            }

            // Redirigir a la pantalla de inicio de sesión
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Para evitar que el usuario regrese a la actividad anterior
        }


        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        nombreTextView = findViewById(R.id.nombreUsuario)
        perfilImageView = findViewById(R.id.imagenPerfil)
        editarNombre = findViewById(R.id.editarNombreEditText)
        guardarBtn = findViewById(R.id.btnGuardarPerfil)

        // Botón para guardar cambios de nombre
        guardarBtn.setOnClickListener {
            guardarCambios()
        }

        // Cargar información del perfil
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreUsuario = document.getString("nombreUsuario")
                        val imagenUrl = document.getString("imagenUrl")

                        // Mostrar nombre si existe, de lo contrario, mostrar mensaje
                        nombreUsuario?.let {
                            nombreTextView.text = it
                            editarNombre.setText(it)
                        } ?: run {
                            nombreTextView.text = "Sin nombre de usuario"
                        }

                        // Mostrar imagen si existe
                        if (!imagenUrl.isNullOrEmpty()) {
                            Picasso.get().load(imagenUrl).into(perfilImageView)
                        }
                    } else {
                        // Si el documento no existe, mostrar mensaje
                        nombreTextView.text = "Perfil no encontrado"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = editarNombre.text.toString().trim()

        if (nuevoNombre.isNotEmpty()) {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid

                // Primero verificamos si el documento del usuario existe
                val usuarioRef = db.collection("usuarios").document(userId)

                usuarioRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Actualizar solo si el documento existe
                            usuarioRef.update("nombreUsuario", nuevoNombre)
                                .addOnSuccessListener {
                                    nombreTextView.text = nuevoNombre
                                    Toast.makeText(this, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al actualizar nombre", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Si no existe, lo creamos con el nombreUsuario
                            val nuevoUsuario = hashMapOf(
                                "nombreUsuario" to nuevoNombre
                            )

                            usuarioRef.set(nuevoUsuario)
                                .addOnSuccessListener {
                                    nombreTextView.text = nuevoNombre
                                    Toast.makeText(this, "Perfil creado con éxito", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al crear perfil", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al acceder a Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }
}
