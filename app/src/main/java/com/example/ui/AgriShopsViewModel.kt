package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgriShopItem
import com.example.data.repository.AgriShopsRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ShopViewMode {
    LIST, MAP
}

class AgriShopsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgriShopsRepository()

    // Default test location preset: Surepalle Village, Musunuru Mandal, Eluru District, AP
    private val defaultTestLat = 16.9205
    private val defaultTestLng = 80.9508
    private val defaultTestName = "Surepalle, Musunuru, Eluru, AP"

    private val _currentLat = MutableStateFlow(defaultTestLat)
    val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLng = MutableStateFlow(defaultTestLng)
    val currentLng: StateFlow<Double> = _currentLng.asStateFlow()

    private val _locationName = MutableStateFlow(defaultTestName)
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow(10) // 5, 10, 25, 50
    val searchRadiusKm: StateFlow<Int> = _searchRadiusKm.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _viewMode = MutableStateFlow(ShopViewMode.LIST)
    val viewMode: StateFlow<ShopViewMode> = _viewMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shopsList = MutableStateFlow<List<AgriShopItem>>(emptyList())
    val shopsList: StateFlow<List<AgriShopItem>> = _shopsList.asStateFlow()

    private val _selectedMapShop = MutableStateFlow<AgriShopItem?>(null)
    val selectedMapShop: StateFlow<AgriShopItem?> = _selectedMapShop.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkPermissions()
        fetchShops()
    }

    fun checkPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _isPermissionGranted.value = fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun requestGpsLocation(context: Context) {
        checkPermissions()
        if (!_isPermissionGranted.value) {
            _errorMessage.value = "Location permission is required for live GPS. You can also search manually."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _currentLat.value = location.latitude
                    _currentLng.value = location.longitude
                    _isGpsActive.value = true

                    viewModelScope.launch {
                        val placeName = repository.getPlaceNameFromCoordinates(location.latitude, location.longitude)
                        _locationName.value = placeName
                        fetchShops()
                    }
                } else {
                    // Fallback to LocationManager if fused client last location is null
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (gpsLoc != null) {
                        _currentLat.value = gpsLoc.latitude
                        _currentLng.value = gpsLoc.longitude
                        _isGpsActive.value = true

                        viewModelScope.launch {
                            val placeName = repository.getPlaceNameFromCoordinates(gpsLoc.latitude, gpsLoc.longitude)
                            _locationName.value = placeName
                            fetchShops()
                        }
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = "Could not retrieve live GPS signal. Showing shops around selected location."
                        fetchShops()
                    }
                }
            }.addOnFailureListener {
                _isLoading.value = false
                _errorMessage.value = "GPS request failed. Search manually or use test location."
                fetchShops()
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "GPS Error: ${e.message}"
            fetchShops()
        }
    }

    fun setTestLocationSurepalle() {
        _currentLat.value = defaultTestLat
        _currentLng.value = defaultTestLng
        _locationName.value = defaultTestName
        _isGpsActive.value = false
        _searchQuery.value = "Surepalle, Musunuru, Eluru"
        fetchShops()
    }

    fun searchManualLocation(query: String) {
        if (query.isBlank()) return
        _searchQuery.value = query
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val coords = repository.searchLocationCoordinates(query)
            if (coords != null) {
                _currentLat.value = coords.first
                _currentLng.value = coords.second
                _locationName.value = query
                _isGpsActive.value = false
                fetchShops()
            } else {
                _isLoading.value = false
                _errorMessage.value = "Could not find village/town: '$query'. Try 'Eluru', 'Musunuru', or 'Surepalle'."
            }
        }
    }

    fun fetchShops() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val shops = repository.getNearbyShops(
                    centerLat = _currentLat.value,
                    centerLng = _currentLng.value,
                    radiusKm = _searchRadiusKm.value,
                    categoryFilter = _selectedCategory.value
                )
                _shopsList.value = shops
                if (shops.isNotEmpty() && _selectedMapShop.value == null) {
                    _selectedMapShop.value = shops.first()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching nearby shops: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setRadius(radiusKm: Int) {
        _searchRadiusKm.value = radiusKm
        fetchShops()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        fetchShops()
    }

    fun setViewMode(mode: ShopViewMode) {
        _viewMode.value = mode
    }

    fun setSearchQueryText(text: String) {
        _searchQuery.value = text
    }

    fun selectMapShop(shop: AgriShopItem?) {
        _selectedMapShop.value = shop
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
