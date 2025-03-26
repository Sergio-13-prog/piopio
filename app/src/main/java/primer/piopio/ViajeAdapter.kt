package primer.piopio

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.Marker
import primer.piopio.DetallesViajeActivity

//class ViajeAdapter(
//    private val viajes: List<Viaje>,
//    private val context: Context,
//    private val markerMap: Map<String, Marker>,
//    private val clickListener: OnViajeClickListener
//) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {
//
//    private var selectedViajeId: String? = null
//
//    interface OnViajeClickListener {
//        fun onViajeSeleccionado(viaje: Viaje)
//    }
//
//    inner class ViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val viajeTextView: TextView = itemView.findViewById(android.R.id.text1)
//    }
//
//    fun highlightViaje(viajeId: String) {
//        selectedViajeId = viajeId
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(android.R.layout.simple_list_item_1, parent, false)
//        return ViajeViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
//        val viaje = viajes[position]
//        holder.viajeTextView.text = "${viaje.origen} -> ${viaje.destino} (${viaje.fecha})"
//
//        if (viaje.id == selectedViajeId) {
//            holder.viajeTextView.setBackgroundColor(Color.LTGRAY)
//        } else {
//            holder.viajeTextView.setBackgroundColor(Color.TRANSPARENT)
//        }
//
//        holder.itemView.setOnClickListener {
//            clickListener.onViajeSeleccionado(viaje)
//        }
//    }
//
//    override fun getItemCount(): Int = viajes.size
//}

// -------------------- ADAPTER -----------------------
class ViajeAdapter(
    private val viajes: List<Viaje>,
    private val context: Context,
    private val markerMap: Map<String, Marker>,
    private val clickListener: OnViajeClickListener
) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    private var lastClickTime: Long = 0
    private var selectedViajeId: String? = null

    interface OnViajeClickListener {
        fun onViajeSeleccionado(viaje: Viaje)
    }

    //        inner class ViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val origenTextView: TextView = itemView.findViewById(R.id.origenTextView)
//            val destinoTextView: TextView = itemView.findViewById(R.id.destinoTextView)
//        }
    class ViajeViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val viajeTextView: TextView = itemView.findViewById(android.R.id.text1)
    }


    fun highlightViaje(viajeId: String) {
        selectedViajeId = viajeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViajeViewHolder(view)
    }
    //        override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
    //        val viaje = viajes[position]
//        holder.viajeTextView.text = "${viaje.origen} -> ${viaje.destino} (${viaje.fecha})"
//
//        // Cambiar color si está seleccionado
//        if (viaje.id == selectedViajeId) {
//            holder.viajeTextView.setBackgroundColor(Color.LTGRAY)
//        } else {
//            holder.viajeTextView.setBackgroundColor(Color.TRANSPARENT)
//        }
//
    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = viajes[position]
        holder.viajeTextView.text = "${viaje.origen} -> ${viaje.destino} (${viaje.fecha})"

        if (viaje.id == selectedViajeId) {
            holder.viajeTextView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.viajeTextView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 500) {
                val intent = Intent(context, DetallesViajeActivity::class.java)
                intent.putExtra("viaje_id", viaje.id)
                context.startActivity(intent)
            } else {
                clickListener.onViajeSeleccionado(viaje) // Llama a la Activity
                highlightViaje(viaje.id)
            }
            lastClickTime = currentTime
        }

    }


    override fun getItemCount(): Int = viajes.size
}
