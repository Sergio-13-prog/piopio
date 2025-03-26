package primer.piopio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var chatListView: ListView
    private lateinit var mensajeEditText: EditText
    private lateinit var btnEnviarMensaje: Button
    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: ChatAdapter
    private val db = FirebaseFirestore.getInstance()
    private val usuarioActual = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_chat)

        chatListView = findViewById(R.id.chatListView)
        mensajeEditText = findViewById(R.id.mensajeEditText)
        btnEnviarMensaje = findViewById(R.id.btnEnviarMensaje)

        adapter = ChatAdapter(this, mensajes)
        chatListView.adapter = adapter

        btnEnviarMensaje.setOnClickListener {
            val mensajeTexto = mensajeEditText.text.toString()
            if (mensajeTexto.isNotEmpty() && usuarioActual != null) {
                val mensaje = Mensaje(mensajeTexto, usuarioActual.uid)
                db.collection("chats").add(mensaje)
                mensajeEditText.setText("")
            }
        }

        // Cargar mensajes desde Firebase (sin auto-actualización)
        db.collection("chats").get().addOnSuccessListener { documents ->
            for (document in documents) {
                mensajes.add(document.toObject(Mensaje::class.java))
            }
            adapter.notifyDataSetChanged()
        }
    }
}
