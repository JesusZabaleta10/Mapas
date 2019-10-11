package com.example.mapas

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    // ------------- Ubicación actual -----------------------------
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }

        // ----------------- Buscar ubicación ---------------------------
        /*if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }*/
        // ----------------- Buscar ubicación ---------------------------
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    // ------------- Ubicación actual -----------------------------

    // ----------------- Buscar ubicación ---------------------------
    /*private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }*/
    // ----------------- Buscar ubicación ---------------------------

    // ----------------- Mover el marcador -----------------------------
    override fun onMarkerDragEnd(marker: Marker?) {
        Log.d("latitudEnd", marker?.position?.latitude.toString())
        Log.d("longiutdEnd", marker?.position?.longitude.toString())

        val posicion = LatLng(marker?.position?.latitude!!, marker?.position?.longitude!! )
        placeMarkerOnMap(posicion)
    }

    override fun onMarkerDragStart(marker: Marker?) {
        /*Log.d("latitudStart", marker?.position?.latitude.toString())
        Log.d("longiutdStart", marker?.position?.longitude.toString())*/
    }

    override fun onMarkerDrag(marker: Marker?) {
        /*Log.d("latitudDrag", marker?.position?.latitude.toString())
        Log.d("longiutdDrag", marker?.position?.longitude.toString())*/
    }
    // ----------------- Mover el marcador -----------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // ------------------- Ubicación actual ---------------------------
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        // ------------------- Ubicación actual---------------------------

        // --------- Buscar unicación --------------------------------
        // val fab = findViewById <FloatingActionButton> (R.id.fab)
        /*bt_ir.setOnClickListener {
            loadPlacePicker()
        }*/
        // --------- Buscar unicación --------------------------------

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this)

        createLocationRequest()
    }

    //---------------------- Menú Tipos de Mapas --------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.tipos_mapas, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        when(item!!.itemId){
            R.id.normal -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.satellite -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
            R.id.hybrid -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.terrain -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //--------------------------------------------------------------------------

    // ------------ Dibujar un nuevo marcador -----------------------------
    private fun placeMarkerOnMap(location: LatLng) {
        val titleStr = getAddress(location)
        val markerOptions = MarkerOptions().position(location).title(titleStr).draggable(true)
        ubicacion.text = titleStr
        mMap.addMarker(markerOptions)
    }
    // ------------ Dibujar un nuevo marcador -----------------------------

    // ------------- Obtener la dirección del marcador -------------------------
    private fun getAddress(latLng: LatLng): String {

        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        var title = ""

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        title = addresses[0].getAddressLine(0)

        mMap.clear()

        return title
    }
    // ------------- Obtener la dirección del marcador -------------------------

    // ------- Inicializar el mapa ---------------------------------------
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)

        val unal = LatLng(6.2612371,-75.5771919)
        //mMap.addMarker(MarkerOptions().position(unal).title("UNAL"))
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.school)).position(unal).title("UNAL"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(unal,15f))

        val upb = LatLng(6.2420357,-75.5893456)
        //mMap.addMarker(MarkerOptions().position(upb).title("UPB"))
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.school)).position(upb).title("UPB"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(upb,15f))

        val udem = LatLng(6.2312566,-75.6109789)
        //mMap.addMarker(MarkerOptions().position(udem).title("UDEM"))
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.school)).position(udem).title("UDEM"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(udem,15f))

        val eafit = LatLng(6.199319,-75.5790089)
        //mMap.addMarker(MarkerOptions().position(eafit).title("EAFIT"))
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.school)).position(eafit).title("EAFIT"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eafit,15f))

        val udea = LatLng(6.2673124,-75.5686624)
        //mMap.addMarker(MarkerOptions().position(udea).title("UdeA"))
        mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.school)).position(udea).title("UDEA"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(udea,15f))

        // ----------------- Buscar una dirección -------------------
        bt_ir.setOnClickListener {
            var geocoder = Geocoder(this)
            var ubicacion = et_ir.text.toString()

            lateinit var list : MutableList<Address>

            try {
                list = geocoder.getFromLocationName(ubicacion, 1)
            }catch (e: IOException){

            }
            if (list.size > 0) {

                var address: Address = list.get(0)
                var position = LatLng(address.latitude, address.longitude)
                placeMarkerOnMap(position)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15F))
            } else
                Toast.makeText(this, "Direccion no encontrada", Toast.LENGTH_SHORT).show()
        }
        // ----------------- Buscar una dirección -------------------

        setUpMap()

        mMap.setOnMarkerDragListener(this)
    }
    // ------- Inicializar el mapa ---------------------------------------

    // ------------- Permisos --------------------------------------------
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        // ------------- Permisos --------------------------------------------

        // ----------- Ubicacuión actual --------------------------------
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
        // ----------- Ubicacuión actual --------------------------------
    }
}
