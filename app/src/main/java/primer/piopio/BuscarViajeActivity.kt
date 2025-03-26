///version del 18 a las 10.35 que funcioa mejor. a partir de ahi seria qeu me puse a hacer cambios hasta ahora el 21-3 a las 11 que he dicho
//hasta aqui, si antes funcionaba no me jodas. recomencemos


package primer.piopio

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
//import org.osmdroid.util.GeoPoint
import com.google.firebase.firestore.GeoPoint
import primer.piopio.ViajeAdapter // IMPORTA TU VIAJEADAPTER
import primer.piopio.Viaje // IMPORTA TU CLASE VIAJE
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.util.Locale
class BuscarViajeActivity : AppCompatActivity(), OnMapReadyCallback {

    data class Viaje(
        val id: String,
        val origen: String,
        val destino: String,
        val fecha: String,
        val publicadoPor: String,
        val origenGeoPoint: GeoPoint? = null,
        val destinoGeoPoint: GeoPoint? =null,
        val origenGeohash: String, // Nuevo campo para el geohash del origen
        val destinoGeohash: String // Nuevo campo para el geohash del destino
    )

    // -------------------- VARIABLES GLOBALES -----------------------
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private val database = FirebaseFirestore.getInstance()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViajeAdapter
    private val listaViajes = mutableListOf<Viaje>() // <<<<<<<< AQUÍ, declarada globalmente
    private val markerViajeMap = mutableMapOf<Marker, Viaje>() // Mapa para relacionar markers con viajes
    private val viajeMarkerMap = mutableMapOf<String, Marker>() // Para relación inversa
    private var isMapReady = false
    private var isViajesLoaded = false


    // ----------------------------------------------------------------

    // -------------------- ADAPTER -----------------------
    class ViajeAdapter(private val viajes: List<Viaje>, private val context: Context) :
        RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

        private var lastClickTime: Long = 0
        private var selectedViajeId: String? = null

        fun highlightViaje(viajeId: String) {
            selectedViajeId = viajeId
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViajeViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViajeViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
            val viaje = viajes[position]
            holder.viajeTextView.text = "${viaje.origen} -> ${viaje.destino} (${viaje.fecha})"

            // Cambiar color si está seleccionado
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
                    (context as BuscarViajeActivity).resaltarViajeEnMapa(viaje)
                    highlightViaje(viaje.id)
                }
                lastClickTime = currentTime
            }
        }

        override fun getItemCount(): Int = viajes.size

        class ViajeViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val viajeTextView: TextView = itemView.findViewById(android.R.id.text1)
        }
    }

    // -------------------- onCreate -----------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_buscarviaje)
        ///////lo del menu inferior
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navegar a la actividad de inicio
                    startActivity(Intent(this, BuscarViajeActivity::class.java))
                    true
                }
                R.id.nav_publish -> {
                    // Navegar a la actividad de publicación
                    startActivity(Intent(this, PostearViajeActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    // Navegar a la actividad de chat
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Navegar a la actividad de perfil
                    startActivity(Intent(this, PerfilPropioUsuarioActivity::class.java))
                    true
                }
                else -> false
            }
        }

        /////////

        ///delay para recibir datos del mapa
        android.os.Handler().postDelayed({
            if (!isMapReady) {
                Toast.makeText(this, "El mapa está tardando en cargar. Reintentando...", Toast.LENGTH_SHORT).show()
                mapView.getMapAsync(this) // Vuelves a llamar por si acaso
            }
        }, 8000) // 8 segundos


        // Mapa
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ///
//        adapter = ViajeAdapter(listaViajes, this)
//        recyclerView.adapter = adapter

        // Sesión usuario
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

        // Guardar usuario
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            with(sharedPref.edit()) {
                putString("usuario", user.displayName)
                apply()
            }
        }


        cargarViajes()
        //cargarViajesCercanos(40.4168, -3.7038)

         //Configurar el Adapter
        //adapter = ViajeAdapter(listaViajes, this, viajeMarkerMap, this)
        adapter = ViajeAdapter(listaViajes, this)
        recyclerView.adapter = adapter
    }
    ///////////////////////

    // -------------------- Cargar Viajes -----------------------

    private fun intentarCargarRutas() {
        if (isMapReady && isViajesLoaded) {
            cargarRutasDesdeFirebase()
        }
    }

//    private fun cargarViajes() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val documents = database.collection("viajes").get().await()
//                val viajesTemp = mutableListOf<Viaje>()
//                for (document in documents) {
////                    val origenGeoPoint = document.getGeoPoint("origenGeoPoint") ?: GeoPoint(0.0, 0.0)
////                    val destinoGeoPoint = document.getGeoPoint("destinoGeoPoint") ?: GeoPoint(0.0, 0.0)
//                    val origenGeohash = document.getString("origenGeohash") ?: ""
//                    val destinoGeohash = document.getString("destinoGeohash") ?: ""
//                    val origenNombre = document.getString("origen") ?: "Desconocido"
//                    val destinoNombre = document.getString("destino") ?: "Desconocido"
//
////                    // Obtener coordenadas desde Google API
////                    val origenGeoPoint = obtenerCoordenadas(origenNombre) ?: GeoPoint(0.0, 0.0)
////                    val destinoGeoPoint = obtenerCoordenadas(destinoNombre) ?: GeoPoint(0.0, 0.0)
//
//
//                    val viaje = Viaje(
//                        id = document.id,
////                        origen = document.getString("origen") ?: "Desconocido",
////                        destino = document.getString("destino") ?: "Desconocido",
//                        origen = origenNombre,
//                        destino = destinoNombre,
//                        fecha = document.getString("fecha") ?: "Sin fecha",
//                        publicadoPor = document.getString("publicadoPor") ?: "Anónimo",
////                        origenGeoPoint = origenGeoPoint,
////                        destinoGeoPoint = destinoGeoPoint,
////                        origenGeohash = origenGeohash,
////                        destinoGeohash = destinoGeohash
//                        origenGeohash = document.getString("origenGeohash") ?: "",
//                        destinoGeohash = document.getString("destinoGeohash") ?: ""
//                    )
//                    viajesTemp.add(viaje)
//                }
//
//                withContext(Dispatchers.Main) {
//                    listaViajes.clear()
//                    listaViajes.addAll(viajesTemp)
//                    adapter.notifyDataSetChanged()
//                    isViajesLoaded = true
//                    intentarCargarRutas()
//                    Toast.makeText(this@BuscarViajeActivity, "Viajes cargados exitosamente", Toast.LENGTH_SHORT).show()
//                }
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@BuscarViajeActivity, "Error al cargar los viajes", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
private fun cargarViajes() {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val documents = database.collection("viajes").get().await()
            val viajesTemp = mutableListOf<Viaje>()

            for (document in documents) {
                val origenGeoPoint = document.getGeoPoint("origenGeoPoint")
                val destinoGeoPoint = document.getGeoPoint("destinoGeoPoint")
                val origenNombre = document.getString("origen") ?: "Desconocido"
                val destinoNombre = document.getString("destino") ?: "Desconocido"
                val origenGeohash = document.getString("origenGeohash") ?: ""
                val destinoGeohash = document.getString("destinoGeohash") ?: ""

                if (origenGeoPoint != null && destinoGeoPoint != null) {
                    val viaje = Viaje(
                        id = document.id,
                        origen = origenNombre,
                        destino = destinoNombre,
                        fecha = document.getString("fecha") ?: "Sin fecha",
                        publicadoPor = document.getString("publicadoPor") ?: "Anónimo",
                        origenGeoPoint = origenGeoPoint,
                        destinoGeoPoint = destinoGeoPoint,
                        origenGeohash = origenGeohash,   // ← Se añade aquí
                        destinoGeohash = destinoGeohash  // ← Se añade aquí
                    )
                    viajesTemp.add(viaje)
                } else {
                    Log.e("Firestore", "Viaje con coordenadas nulas: ${document.id}")
                }
            }

            withContext(Dispatchers.Main) {
                listaViajes.clear()
                listaViajes.addAll(viajesTemp)
                adapter.notifyDataSetChanged()
                isViajesLoaded = true
                intentarCargarRutas()
                Toast.makeText(this@BuscarViajeActivity, "Viajes cargados exitosamente", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@BuscarViajeActivity, "Error al cargar los viajes", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al obtener viajes", e)
            }
        }
    }
}


//    override fun onMapReady(map: GoogleMap) {
//                googleMap = map
//        val espana = LatLng(40.4168, -3.7038)
//
//        espana?.let {
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 5f))
//        }
//
//        googleMap.setOnMarkerClickListener { marker ->
//            markerViajeMap[marker]?.let { viaje ->
//                resaltarViajeEnLista(viaje.id)
//            }
//            false
//        }
//        isMapReady = true
//        intentarCargarRutas()
//    }
override fun onMapReady(map: GoogleMap) {
    googleMap = map

    // Habilitar controles de zoom y gestos
    googleMap.uiSettings.isZoomControlsEnabled = true
    googleMap.uiSettings.isZoomGesturesEnabled = true
    googleMap.uiSettings.isMapToolbarEnabled = true

    // Ajustar vista del mapa automáticamente según los viajes
    ajustarVistaMapa()

    // Si no hay viajes, centrar en España con un zoom más alejado
    if (listaViajes.isEmpty()) {
        val espana = LatLng(40.4168, -3.7038)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 5f))
    }

    // Manejar clics en los marcadores
    googleMap.setOnMarkerClickListener { marker ->
        markerViajeMap[marker]?.let { viaje ->
            resaltarViajeEnLista(viaje.id)
        }
        false
    }

    isMapReady = true
    intentarCargarRutas()
}


    //private fun cargarRutasDesdeFirebase() {
//    googleMap.clear()
//    markerViajeMap.clear()
//    viajeMarkerMap.clear()
//
//    Log.d("CargarViajes", "Total de viajes cargados: ${listaViajes.size}")
//
//    for (viaje in listaViajes) {
//        val origenGeo = viaje.origenGeoPoint
//        val destinoGeo = viaje.destinoGeoPoint
//
//        if (origenGeo != null && destinoGeo != null) {
//            val origen = LatLng(origenGeo.latitude, origenGeo.longitude)
//            val destino = LatLng(destinoGeo.latitude, destinoGeo.longitude)
//
//            googleMap.addPolyline(
//                PolylineOptions()
//                    .add(origen, destino)
//                    .width(5f)
//                    .color(Color.RED)
//            )
//
//            val marker = googleMap.addMarker(
//                MarkerOptions()
//                    .position(origen)
//                    .title("${viaje.origen} -> ${viaje.destino}")
//            )
//
//            marker?.let {
//                markerViajeMap[it] = viaje
//                viajeMarkerMap[viaje.id] = it
//            }
//        } else {
//            Log.e("CargarViajes", "Viaje sin coordenadas: ${viaje.id}")
//        }
//    }
//}
private fun ajustarVistaMapa() {
    if (!::googleMap.isInitialized || listaViajes.isEmpty()) return

    val builder = LatLngBounds.Builder()

    for (viaje in listaViajes) {
        viaje.origenGeoPoint?.let { builder.include(LatLng(it.latitude, it.longitude)) }
        viaje.destinoGeoPoint?.let { builder.include(LatLng(it.latitude, it.longitude)) }
    }

    val bounds = builder.build()
    val padding = 100 // Espaciado en píxeles alrededor de los límites

    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
}

    private fun cargarRutasDesdeFirebase() {
    if (!::googleMap.isInitialized) {
        Log.e("CargarRutas", "Mapa no inicializado")
        return
    }

    googleMap.clear()
    markerViajeMap.clear()
    viajeMarkerMap.clear()

    Log.d("CargarViajes", "Total de viajes cargados: ${listaViajes.size}")

    for (viaje in listaViajes) {
        val origenGeo = viaje.origenGeoPoint
        val destinoGeo = viaje.destinoGeoPoint

        if (origenGeo != null && destinoGeo != null) {
            val origen = LatLng(origenGeo.latitude, origenGeo.longitude)
            val destino = LatLng(destinoGeo.latitude, destinoGeo.longitude)

            // Dibujar la línea entre origen y destino
            googleMap.addPolyline(
                PolylineOptions()
                    .add(origen, destino)
                    .width(5f)
                    .color(Color.RED)
            )

            // Agregar marcador en origen
            val markerOrigen = googleMap.addMarker(
                MarkerOptions()
                    .position(origen)
                    .title("Origen: ${viaje.origen}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            // Agregar marcador en destino
            val markerDestino = googleMap.addMarker(
                MarkerOptions()
                    .position(destino)
                    .title("Destino: ${viaje.destino}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            markerOrigen?.let { markerViajeMap[it] = viaje }
            markerDestino?.let { markerViajeMap[it] = viaje }

            markerOrigen?.let { viajeMarkerMap[viaje.id] = it }

        } else {
            Log.e("CargarViajes", "Viaje sin coordenadas: ${viaje.id}")
        }
    }
}


    // adapter = ViajeAdapter(listaViajes, this, viajeMarkerMap, this)
        ///recyclerView.adapter = adapter

    private fun distanciaEntre(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
                val R = 6371 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
        //Log.e("distancias", "distancia calculada: ${a}")

    }
        private fun cargarViajesCercanos(latitud: Double, longitud: Double, radioKm: Int = 1000000) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val viajesCercanos = mutableListOf<Viaje>()
                val documents = database.collection("viajes").get().await()

                for (document in documents) {
                    val origenGeoPoint = document.getGeoPoint("origenGeoPoint")
                    if (origenGeoPoint != null) {
                        val distancia = distanciaEntre(
                            latitud,
                            longitud,
                            origenGeoPoint.latitude,
                            origenGeoPoint.longitude
                        )
                        if (distancia <= radioKm) {
                            val viaje = Viaje(
                                id = document.id,
                                origen = document.getString("origen") ?: "Desconocido",
                                destino = document.getString("destino") ?: "Desconocido",
                                fecha = document.getString("fecha") ?: "Sin fecha",
                                publicadoPor = document.getString("publicadoPor") ?: "Anónimo",
                                origenGeoPoint = origenGeoPoint,
                                destinoGeoPoint = document.getGeoPoint("destinoGeoPoint") ?: GeoPoint(0.0, 0.0),
                                origenGeohash = document.getString("origenGeohash") ?: "",
                                destinoGeohash = document.getString("destinoGeohash") ?: ""
                            )
                            viajesCercanos.add(viaje)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    listaViajes.clear()
                    listaViajes.addAll(viajesCercanos)
                    isViajesLoaded = true
                    intentarCargarRutas()
                    ajustarVistaMapa() // Llamar después de cargar los viajes

                    Toast.makeText(this@BuscarViajeActivity, "Viajes cercanos cargados", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BuscarViajeActivity, "Error al cargar viajes cercanos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resaltarViajeEnMapa(viaje: Viaje) {
        val marker = viajeMarkerMap[viaje.id]
        marker?.showInfoWindow()

        marker?.position?.let {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(it))
        }
    }

    private fun resaltarViajeEnLista(viajeId: String) {
        val index = listaViajes.indexOfFirst { it.id == viajeId }
        if (index != -1) {
            recyclerView.smoothScrollToPosition(index)
        }
    }
//    override fun onViajeSeleccionado(viaje: Viaje) {
//        //resaltarViajeEnMapa(viaje)
//        // Resalta el marcador en el mapa
//        viajeMarkerMap[viaje.id]?.let {
//            it.showInfoWindow()
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 10f))
//        }
//    }



    ///////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////ultimo codigo
    // -------------------- Ciclo de vida MapView -----------------------
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
