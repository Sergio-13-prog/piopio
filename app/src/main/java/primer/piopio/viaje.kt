package primer.piopio

import com.google.firebase.firestore.GeoPoint

data class Viaje(
    val id: String,
    val origen: String,
    val destino: String,
    val fecha: String,
    val publicadoPor: String,
    val origenGeoPoint: GeoPoint? = null,
    val destinoGeoPoint: GeoPoint? = null,
    val origenGeohash: String,
    val destinoGeohash: String
)
