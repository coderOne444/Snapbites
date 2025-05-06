package viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.Geocoder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * ViewModel for managing location and address state.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    // Holds the current resolved address (automatic or manual)
    private val _address = MutableStateFlow("Fetching location...")
    val address: StateFlow<String> = _address.asStateFlow()

    // Holds any location permission or fetch errors
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation.asStateFlow()

    /**
     * Updates the current address manually entered by the user.
     */
    fun setManualAddress(manualAddress: String) {
        _address.value = manualAddress
    }

    /**
     * Updates the selected location.
     */
    fun setSelectedLocation(latLng: LatLng) {
        _selectedLocation.value = latLng
        val location = Location("").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
        _location.value = location

        // Fix: Launch coroutine to call suspend function
        viewModelScope.launch {
            getAddressFromLocation(location)
        }
    }


    /**
     * Call to retry automatic location fetching.
     * Implement permission checks and location API calls here.
     */
    fun fetchLocation() {
        val context = getApplication<Application>()

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    handleLocation(location)
                } else {
                    // Request fresh location
                    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 1000
                        numUpdates = 1
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedLocationClient.removeLocationUpdates(this)
                                val freshLocation = result.lastLocation
                                if (freshLocation != null) {
                                    handleLocation(freshLocation)
                                } else {
                                    _locationError.value = "Unable to get location"
                                }
                            }
                        },
                        Looper.getMainLooper()
                    )
                }
            }.addOnFailureListener { e ->
                _locationError.value = "Error fetching location: ${e.message}"
            }
        } else {
            _locationError.value = "Location permission not granted"
        }
    }

    private fun handleLocation(location: Location) {
        _location.value = location
        _selectedLocation.value = LatLng(location.latitude, location.longitude)

        viewModelScope.launch {
            getAddressFromLocation(location)
        }
    }
    private suspend fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        try {
            val addressList = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            val addressObj = addressList?.firstOrNull()
            val area = addressObj?.subLocality
            val building = addressObj?.thoroughfare
            val city = addressObj?.locality

            val formattedAddress = listOfNotNull(building, area, city).joinToString(", ")

            _address.value = formattedAddress.ifEmpty { "Address not found" }
        } catch (e: Exception) {
            _address.value = "Error fetching address"
            _locationError.value = e.message
        }
    }


    fun calculateDistance(restaurantLatLng: LatLng): Float {
        val userLocation = _location.value ?: return 0f
        val restaurantLocation = Location("").apply {
            latitude = restaurantLatLng.latitude
            longitude = restaurantLatLng.longitude
        }
        return userLocation.distanceTo(restaurantLocation) / 1000 // Convert to kilometers
    }
}

class LocationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
