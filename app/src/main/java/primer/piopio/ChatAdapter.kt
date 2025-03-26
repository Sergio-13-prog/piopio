package primer.piopio

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ChatAdapter(private val context: Context, private val mensajes: List<Mensaje>) : BaseAdapter() {

    override fun getCount(): Int = mensajes.size

    override fun getItem(position: Int): Mensaje = mensajes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vista = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val mensajeTexto = vista.findViewById<TextView>(android.R.id.text1)
        mensajeTexto.text = mensajes[position].texto
        return vista
    }
}

data class Mensaje(val texto: String, val usuarioId: String)
