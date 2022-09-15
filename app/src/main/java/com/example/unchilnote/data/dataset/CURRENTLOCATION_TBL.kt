package com.example.unchilnote.data.dataset

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "CURRENTLOCATION_TBL")
data class CURRENTLOCATION_TBL(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var dt: Long,
    var latitude: Float,
    var longitude: Float,
    var altitude: Float
)

fun CURRENTLOCATION_TBL.toLatLng(): LatLng {
    return LatLng(this.latitude.toDouble(), this.longitude.toDouble())
}
