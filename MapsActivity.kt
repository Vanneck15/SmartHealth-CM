package com.smarthealth.cm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.smarthealth.cm.R

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Solution de localisation des hôpitaux (Offline-ready markers)
        // Voici une liste d'hôpitaux clés au Cameroun pré-enregistrés
        val hospitals = listOf(
            HospitalLocation("Hôpital Central de Yaoundé", 3.8667, 11.5167),
            HospitalLocation("Hôpital Général de Douala", 4.0500, 9.7000),
            HospitalLocation("Hôpital Laquintinie", 4.0433, 9.7067),
            HospitalLocation("Centre Pasteur", 3.8680, 11.5220),
            HospitalLocation("Hôpital Régional de Bafoussam", 5.4777, 10.4176)
        )

        for (hospital in hospitals) {
            val pos = LatLng(hospital.lat, hospital.lng)
            mMap.addMarker(MarkerOptions().position(pos).title(hospital.name))
        }

        // Centrer sur le Cameroun
        val cameroon = LatLng(3.848, 11.502)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameroon, 6f))
    }

    data class HospitalLocation(val name: String, val lat: Double, val lng: Double)
}
