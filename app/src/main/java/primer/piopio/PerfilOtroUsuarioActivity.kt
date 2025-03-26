package primer.piopio

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilOtroUsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_perfilotro)

        val usuarioId = intent.getStringExtra("usuario_id") ?: return
        val perfilTextView = findViewById<TextView>(R.id.perfilDetallesTextView)

        val db = FirebaseFirestore.getInstance()
        /////
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


        db.collection("usuarios").document(usuarioId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Desconocido"
                    val email = document.getString("email") ?: "No disponible"
                    perfilTextView.text = "Nombre: $nombre\nEmail: $email"
                } else {
                    perfilTextView.text = "Perfil no encontrado"
                }
            }
            .addOnFailureListener {
                perfilTextView.text = "Error al cargar el perfil"
            }
    }
}
