package primer.piopio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


/////////////////////////
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

///////////////////////////////
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantallaprimera)
        supportActionBar?.hide()

        val btnBuscarViaje = findViewById<Button>(R.id.btnBuscarViaje)
        val btnPostearViaje = findViewById<Button>(R.id.btnPostearViaje)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

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


        FirebaseApp.initializeApp(this) // Initialize Firebase if not already done
        val db = Firebase.firestore // Get a Firestore instance

        btnBuscarViaje.setOnClickListener {
            startActivity(Intent(this, BuscarViajeActivity::class.java))
        }

        btnPostearViaje.setOnClickListener {
            startActivity(Intent(this, PostearViajeActivity::class.java))
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, CrearCuentaActivity::class.java))
        }

        val estadoSesionTextView = findViewById<TextView>(R.id.estadoSesionTextView)
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val nombreUsuario = sharedPref.getString("usuario", null)

        if (nombreUsuario != null) {
            estadoSesionTextView.text = nombreUsuario
            estadoSesionTextView.setOnClickListener {
                startActivity(Intent(this, PerfilPropioUsuarioActivity::class.java))
            }
        } else {
            estadoSesionTextView.text = "Log in"
            estadoSesionTextView.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

    }




}
