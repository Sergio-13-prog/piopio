package primer.piopio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, CrearCuentaActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Si el usuario no tiene un nombre, asignarle uno por defecto
                        if (user.displayName == null) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(email) // Usa el email como nombre temporal
                                .build()
                            user.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    guardarNombreUsuario(user.displayName)
                                }
                            }
                        } else {
                            guardarNombreUsuario(user.displayName)
                        }
                    }

                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val estadoSesionTextView = findViewById<TextView>(R.id.estadoSesionTextView)
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {
            estadoSesionTextView.text = usuario.displayName ?: "Usuario"
        } else {
            estadoSesionTextView.text = "Log in"
        }

        // Detectar cambios en la sesión
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                estadoSesionTextView.text = user.displayName ?: "Usuario"
            } else {
                estadoSesionTextView.text = "Log in"
            }
        }
    }

    private fun guardarNombreUsuario(nombre: String?) {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("usuario", nombre ?: "Usuario")
            apply()
        }
    }
}
