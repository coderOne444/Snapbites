@file:Suppress("DEPRECATION")

package com.example.jetpackcomposenew

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import android.location.Geocoder
import android.media.MediaPlayer
import android.util.Log
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import viewmodel.LocationViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Add CartViewModel to handle the cart items
    private val cartViewModel: CartViewModel by viewModels()

    // Initialize LocationViewModel with a factory for context dependency
    private val locationViewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Enable edge-to-edge layout

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Render the UI with both cartViewModel and locationViewModel passed into AppNavigation
        setContent {
            AppNavigation(cartViewModel = cartViewModel, locationViewModel = locationViewModel)
        }
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

data class CartItem(
    val name: String,
    val priceInRs: Double,
    val quantity: Int = 1 // Default quantity is 1
)
data class Restaurant(
    val name: String,
    val imageResId: Int,
    var rating: Float,
    val distance: Float,
    val isVeg: Boolean,
    val priceInRs: Double,
    var userRating: Float = 0f, // Ensure this is mutable
    var numberOfRatings: Int = 0,
    val latitude: Double, // Add latitude
    val longitude: Double // Add longitude
)
data class FoodItemData(
    val name: String,
    val price: Double,
    @DrawableRes val imageRes: Int
)

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // New: order history of past carts
    private val _orderHistory = MutableStateFlow<List<List<CartItem>>>(emptyList())
    val orderHistory: StateFlow<List<List<CartItem>>> = _orderHistory

    private val _discountPercentage = MutableStateFlow(0)
    val discountPercentage: StateFlow<Int> = _discountPercentage

    fun addItemToCart(item: CartItem) {
        _cartItems.value = _cartItems.value + item
    }

    fun applyDiscount(discount: Int) {
        _discountPercentage.value = discount
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountPercentage.value = 0
    }

    // New: move current cart into history and then clear it
    fun placeOrder() {
        val current = _cartItems.value
        if (current.isNotEmpty()) {
            _orderHistory.value = _orderHistory.value + listOf(current)
            clearCart()
        }
    }
}


@Composable
fun AppNavigation(
    cartViewModel: CartViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel()
) {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(false) }

    // Define the restaurant list here
    val restaurantList = listOf(
        Restaurant("Maa Kali Restaurant", R.drawable.maakali, 3.4f, 1.2f, isVeg = true, priceInRs = 250.0, latitude = 37.4220936, longitude = -122.083922),
        Restaurant("Aasha Biriyani House", R.drawable.ashabriyani, 4.5f, 2.3f, isVeg = false, priceInRs = 350.0, latitude = 37.4232000, longitude = -122.081000),
        Restaurant("Bharti Restaurant", R.drawable.bhartires, 4.0f, 1.5f, isVeg = true, priceInRs = 200.0, latitude = 37.4215000, longitude = -122.080000),
        Restaurant("Dolphin Restaurant", R.drawable.dolphinres, 2.5f, 0.9f, isVeg = false, priceInRs = 400.0, latitude = 37.4200000, longitude = -122.085000),
        Restaurant("The Nawaab Restaurant", R.drawable.nawaabres, 5.0f, 3.0f, isVeg = false, priceInRs = 500.0, latitude = 37.4190000, longitude = -122.087000),
        Restaurant("Amrita Restaurant", R.drawable.amritares, 3.7f, 1.5f, isVeg = true, priceInRs = 550.0, latitude = 37.4250000, longitude = -122.082000),
        Restaurant("Monginis Restaurant", R.drawable.monginisres, 3.9f, 0.7f, isVeg = false, priceInRs = 400.0, latitude = 37.4265000, longitude = -122.086000),
        Restaurant("Mio Amore the Cake Shop", R.drawable.mioamore, 4.3f, 1.1f, isVeg = true, priceInRs = 450.0, latitude = 37.4235000, longitude = -122.083000),
        Restaurant("Prasenjit Hotel", R.drawable.maachbhaaat, 4.4f, 2.0f, isVeg = true, priceInRs = 550.0, latitude = 37.4210000, longitude = -122.088000),
        Restaurant("MSR Cafe and Restaurant", R.drawable.msrcafe, 4.8f, 0.8f, isVeg = false, priceInRs = 600.0, latitude = 37.4222000, longitude = -122.089500),
        Restaurant("Mira Store", R.drawable.koreanbibimbaap, 4.3f, 1.4f, isVeg = true, priceInRs = 660.0, latitude = 37.4270000, longitude = -122.084200),
        Restaurant("Darjeeling Fast Food", R.drawable.darjeeling, 4.7f, 1.6f, isVeg = false, priceInRs = 650.0, latitude = 37.4288000, longitude = -122.083500),
        Restaurant("Abar Khabo Tiffin House", R.drawable.abarkhabotiffin, 1.0f, 2.2f, isVeg = false, priceInRs = 550.0, latitude = 37.423, longitude = -122.083),
        Restaurant("Spice Symphony", R.drawable.spicessymphony, 4.5f, 1.8f, isVeg = false, priceInRs = 750.0, latitude = 37.424, longitude = -122.081),
        Restaurant("Pure Veg Delights", R.drawable.paneer, 4.2f, 3.5f, isVeg = true, priceInRs = 400.0, latitude = 37.421, longitude = -122.079),
        Restaurant("Tandoori Junction", R.drawable.tandoorijunction, 4.8f, 2.0f, isVeg = false, priceInRs = 900.0, latitude = 37.419, longitude = -122.084),
        Restaurant("Biryani House", R.drawable.chickenthali, 4.6f, 2.8f, isVeg = false, priceInRs = 650.0, latitude = 37.420, longitude = -122.082),
        Restaurant("South Indian Flavors", R.drawable.southindianflavors, 4.3f, 3.0f, isVeg = true, priceInRs = 500.0, latitude = 37.417, longitude = -122.080),
        Restaurant("Dilli Chaat Bhandar", R.drawable.salad, 4.0f, 1.5f, isVeg = true, priceInRs = 350.0, latitude = 37.422, longitude = -122.086),
        Restaurant("Mughlai Darbar", R.drawable.mughlaidarbar, 4.7f, 2.5f, isVeg = false, priceInRs = 850.0, latitude = 37.418, longitude = -122.078),
        Restaurant("The Punjabi Dhaba", R.drawable.spicessymphony, 4.4f, 3.2f, isVeg = false, priceInRs = 600.0, latitude = 37.419, longitude = -122.085),
        Restaurant("Coastal Curry", R.drawable.maachbhaaat, 4.5f, 2.7f, isVeg = false, priceInRs = 720.0, latitude = 37.420, longitude = -122.081),
        Restaurant("Rajasthani Rasoi", R.drawable.rajasthanifood, 4.1f, 3.8f, isVeg = true, priceInRs = 450.0, latitude = 37.421, longitude = -122.084),
        Restaurant("The Grand Thali", R.drawable.grandthali, 4.6f, 2.1f, isVeg = true, priceInRs = 550.0, latitude = 37.423, longitude = -122.080),
        Restaurant("Hyderabadi Biryani Center", R.drawable.muttonbiriyani, 4.9f, 1.9f, isVeg = false, priceInRs = 800.0, latitude = 37.418, longitude = -122.083),
        Restaurant("Bengali Bhoj", R.drawable.chickenawab, 4.3f, 3.4f, isVeg = false, priceInRs = 580.0, latitude = 37.422, longitude = -122.082),
        Restaurant("Malabar Spices", R.drawable.chickenthali, 4.2f, 2.9f, isVeg = false, priceInRs = 620.0, latitude = 37.419, longitude = -122.080),
        Restaurant("Gujarati Swad", R.drawable.rajasthanifood, 4.0f, 3.7f, isVeg = true, priceInRs = 400.0, latitude = 37.420, longitude = -122.085),
        Restaurant("Udupi Sagar", R.drawable.taco_supreme, 4.5f, 2.3f, isVeg = true, priceInRs = 520.0, latitude = 37.421, longitude = -122.079),
        Restaurant("Chennai Dosa Corner", R.drawable.southindianflavors, 4.3f, 2.6f, isVeg = true, priceInRs = 490.0, latitude = 37.417, longitude = -122.081),
        Restaurant("Lucknowi Kebab", R.drawable.muttonbiriyani, 4.8f, 1.7f, isVeg = false, priceInRs = 770.0, latitude = 37.424, longitude = -122.080),
        Restaurant("Swad Punjab Da", R.drawable.spicessymphony, 4.5f, 3.1f, isVeg = false, priceInRs = 750.0, latitude = 37.418, longitude = -122.082),
        Restaurant("Flavors of China", R.drawable.chickenmomos, 4.2f, 5.5f, isVeg = false, priceInRs = 620.0, latitude = 37.419, longitude = -122.084),
        Restaurant("Dilli Chaat Bhandar", R.drawable.dalparatha, 4.7f, 2.0f, isVeg = true, priceInRs = 250.0, latitude = 37.421, longitude = -122.086),
        Restaurant("Ming's Dynasty", R.drawable.chickenmomos, 4.3f, 3.8f, isVeg = false, priceInRs = 680.0, latitude = 37.423, longitude = -122.083),
        Restaurant("Biryani Junction", R.drawable.chickenawab, 4.6f, 4.0f, isVeg = false, priceInRs = 900.0, latitude = 37.420, longitude = -122.078),
        Restaurant("Hakka House", R.drawable.chickenhakkanoodles, 4.1f, 6.2f, isVeg = false, priceInRs = 580.0, latitude = 37.424, longitude = -122.085),
        Restaurant("Rajdhani Thali", R.drawable.rajasthanifood, 4.8f, 1.5f, isVeg = true, priceInRs = 650.0, latitude = 37.422, longitude = -122.081),
        Restaurant("Dragon Wok", R.drawable.nawaabres, 4.0f, 5.0f, isVeg = false, priceInRs = 720.0, latitude = 37.418, longitude = -122.080),
        Restaurant("Udupi Sagar", R.drawable.southindianflavors, 4.4f, 3.3f, isVeg = true, priceInRs = 300.0, latitude = 37.417, longitude = -122.079),
        Restaurant("Golden Chopsticks", R.drawable.dalparatha, 3.9f, 4.8f, isVeg = false, priceInRs = 550.0, latitude = 37.419, longitude = -122.083),
        Restaurant("Tandoori Nights", R.drawable.tandoorijunction, 4.5f, 2.9f, isVeg = false, priceInRs = 850.0, latitude = 37.420, longitude = -122.084),
        Restaurant("Chowman Express", R.drawable.msrcafe, 4.2f, 3.7f, isVeg = false, priceInRs = 600.0, latitude = 37.423, longitude = -122.080),
        Restaurant("Bengali Rasoi", R.drawable.fishtandoori, 4.6f, 2.5f, isVeg = false, priceInRs = 500.0, latitude = 37.422, longitude = -122.079),
        Restaurant("Sichuan Delights", R.drawable.pulao, 4.0f, 6.0f, isVeg = false, priceInRs = 700.0, latitude = 37.419, longitude = -122.081),
        Restaurant("Gujju Rasoi", R.drawable.gulabjamun, 4.3f, 3.2f, isVeg = true, priceInRs = 480.0, latitude = 37.417, longitude = -122.078)
        // Add more restaurants with latitude and longitude
    )

    // Listen for route changes to toggle bottom bar visibility
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            showBottomBar = shouldShowBottomBar(backStackEntry.destination.route)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (showBottomBar) {
                AnimatedAddButton { navController.navigate("cart") }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                // 1) Read the nav backstack to know which screen is selected
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 2) Slim, transparent BottomNavigation
                BottomNavigation(
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    modifier = Modifier.height(56.dp)
                ) {
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Order") },
                        selected = currentRoute == "order",
                        onClick = {
                            navController.navigate("order") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }

    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("home") {
                HomeScreen(
                    navController = navController,
                    cartViewModel = cartViewModel,
                    locationViewModel = locationViewModel
                )
            }

            // ← all‑restaurants screen
            composable("restaurants") {
                RestaurantsScreen(
                    navController = navController,
                    restaurantList = restaurantList
                )
            }

            composable("details/{restaurantName}") { backStackEntry ->
                val restaurantName = backStackEntry.arguments
                    ?.getString("restaurantName").orEmpty()
                RestaurantDetailsScreen(
                    cartViewModel = cartViewModel,
                    restaurantName = restaurantName,
                    restaurantList = restaurantList
                )
            }
            composable("order") {
                OrderScreen(cartViewModel = cartViewModel)
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments
                    ?.getString("category").orEmpty()
                CategoryScreen(category, navController, restaurantList)
            }
            composable("signup") {
                SignUpScreen(navController = navController)
            }
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("cart") {
                CartScreen(navController = navController, cartViewModel = cartViewModel)
            }
        }
    }
}

    @Composable
fun RestaurantsScreen(
    navController: NavController,
    restaurantList: List<Restaurant>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "All Restaurants",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(restaurantList) { restaurant ->
                RestaurantCard(restaurant) { selected ->
                    navController.navigate("details/${selected.name}")
                }
            }
        }
    }
}

// Helper function to determine when to show the bottom bar
private fun shouldShowBottomBar(route: String?): Boolean {
    return route != "splash"
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun RestaurantImageSlider(
    restaurantImages: List<Int>,
    modifier: Modifier = Modifier,
    autoScrollDuration: Long = 3000L // 3 seconds
) {
    val pagerState = rememberPagerState()

    LaunchedEffect(key1 = pagerState.currentPage) {
        delay(autoScrollDuration)
        val nextPage = (pagerState.currentPage + 1) % restaurantImages.size
        pagerState.animateScrollToPage(nextPage)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        HorizontalPager(
            count = restaurantImages.size,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            Image(
                painter = painterResource(id = restaurantImages[page]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)
        )
    }
}
@Composable
fun FloatingNavBar(navController: NavController) {
    val items = listOf("home", "order", "profile")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .shadow(8.dp, RoundedCornerShape(50))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { screen ->
            IconButton(onClick = {
                navController.navigate(screen) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }) {
                Icon(
                    imageVector = when (screen) {
                        "home" -> Icons.Default.Home
                        "order" -> Icons.Default.ShoppingCart
                        "profile" -> Icons.Default.Person
                        else -> Icons.Default.Info
                    },
                    contentDescription = screen,
                    tint = if (currentRoute == screen) Color(0xFF7DE482) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun CategoryScreen(
    category: String,
    navController: NavHostController,
    restaurantList: List<Restaurant>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display the category title
        Text(
            text = "$category Restaurants",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // List of restaurants filtered by category
        LazyColumn {
            val filteredRestaurants = restaurantList.filter { restaurant ->
                // Implement your filtering logic based on category if needed
                true // Placeholder
            }

            items(filteredRestaurants) { restaurant ->
                RestaurantItem(
                    name = restaurant.name,
                    rating = restaurant.rating,
                    distance = "${restaurant.distance} km",
                    imageResId = restaurant.imageResId, // Use the resource ID here
                    onClick = {
                        // Navigate to the details screen when a restaurant is clicked
                        navController.navigate("details/${restaurant.name}")
                    }
                )
            }
        }
    }
}

@Composable
fun RestaurantItem(
    name: String,
    rating: Float,
    distance: String,
    imageResId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Text(text = name, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Rating: $rating ⭐", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Distance: $distance", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun OrderScreen(cartViewModel: CartViewModel) {
    val history by cartViewModel.orderHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Order History", style = MaterialTheme.typography.headlineSmall)

        if (history.isEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("No orders yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(history) { index, order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order #${index + 1}", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            order.forEach { item ->
                                Row {
                                    Text(item.name)
                                    Spacer(Modifier.weight(1f))
                                    Text("₹${item.priceInRs}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    var name by remember { mutableStateOf("Xyz User") }
    var email by remember { mutableStateOf("Xyz@example.com") }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // For storing the profile image URI
    val context = LocalContext.current

    // Set up the image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it // Update the image URI when an image is picked
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Welcome Text
            Text(
                text = "Welcome to SnapBites!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Profile Image or Avatar
            Image(
                painter = if (imageUri != null) {
                    rememberImagePainter(imageUri) // Use Coil to load the image
                } else {
                    painterResource(id = R.drawable.profile_avatar) // Default image
                },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp)) // Circular avatar
                    .background(Color.Gray)
                    .clickable {
                        imagePickerLauncher.launch("image/*") // Launch the image picker
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Changes Button
            Button(
                onClick = {
                    // Handle save logic here, e.g., save to a database or shared preferences
                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Save Changes")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = { navController.navigate("signup") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "Sign Up")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Log In Button
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "Log In")
            }
        }
    }
}

// Add this function to find a restaurant by name
fun findRestaurantByName(name: String, restaurantList: List<Restaurant>): Restaurant? {
    return restaurantList.find { it.name == name }
}

// Add these functions to handle rating persistence
fun saveRating(context: Context, restaurantName: String, rating: Float) {
    val sharedPreferences = context.getSharedPreferences("restaurant_ratings", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putFloat(restaurantName, rating)
        apply()
    }
}

fun loadRating(context: Context, restaurantName: String): Float {
    val sharedPreferences = context.getSharedPreferences("restaurant_ratings", Context.MODE_PRIVATE)
    return sharedPreferences.getFloat(restaurantName, 0f)
}

// Add this function to show a toast message
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun RestaurantDetailsScreen(
    cartViewModel: CartViewModel,
    restaurantName: String,
    restaurantList: List<Restaurant> // Pass the restaurant list as a parameter){}
) {
    val context = LocalContext.current
    val restaurant = findRestaurantByName(restaurantName, restaurantList) ?: return

    // Load the saved rating
    restaurant.userRating = loadRating(context, restaurantName)

    Column(modifier = Modifier.fillMaxSize()) {
        // Display the restaurant name passed as a parameter
        Text(text = restaurantName, style = MaterialTheme.typography.headlineSmall)

        // Display current rating
        Text(text = "Current Rating: ${restaurant.rating} ⭐")

        // Rating Bar
        RatingBar(
            currentRating = restaurant.userRating,
            onRatingChanged = { newRating ->
                restaurant.userRating = newRating
                restaurant.numberOfRatings += 1
                // Update the average rating
                restaurant.rating = (restaurant.rating * (restaurant.numberOfRatings - 1) + newRating) / restaurant.numberOfRatings

                // Save the new rating
                saveRating(context, restaurantName, newRating)

                // Show feedback
                showToast(context, "Thank you for rating!")
            }
        )

        // Burger Details
        BurgerDetails()

        // Coupon Button
        SpinningWheel { discount ->
            cartViewModel.applyDiscount(discount)
            Toast.makeText(context, "🎉 You got a $discount% discount!", Toast.LENGTH_SHORT).show()
        }

        // Food Items with the CartViewModel
        FoodItemSection(cartViewModel)
    }
}

@Composable
fun RatingBar(
    currentRating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < currentRating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRatingChanged(index + 1f) }
            )
        }
    }
}

@Composable
fun BurgerDetails() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Enjoy Your Meal", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Authentic Indian Restaurant", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(text = "4.3 ⭐️")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "1k+ Reviews")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "15min Delivery")
        }
    }
}

@Composable
fun CouponButton(onDiscountGenerated: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isSpinning) {
                isSpinning = true
                scope.launch {
                    delay(2000) // simulate wheel spinning
                    val discount = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90).random()
                    onDiscountGenerated(discount)
                    isSpinning = false
                }
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(if (isSpinning) "Spinning..." else "Click to get Discount up to 90%!")
    }
}

@Composable
fun SpinningWheel(onDiscountSelected: (Int) -> Unit) {
    val discounts = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90)
    val sweepAngle = 360f / discounts.size
    val spinAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var showWheelArea by remember { mutableStateOf(true) }
    var showConfetti by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.spin_win) }
    val scope = rememberCoroutineScope()

    // This visibility controls the entire wheel + button area
    AnimatedVisibility(
        visible = showWheelArea,
        exit = fadeOut(animationSpec = tween(durationMillis = 800))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
            // 1) Wheel Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                discounts.forEachIndexed { index, discount ->
                    val start = index * sweepAngle + spinAngle.value
                    drawArc(
                        color = Color.hsv((index * 40f) % 360, 1f, 1f),
                        startAngle = start,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    // draw discount label
                    val radians = Math.toRadians((start + sweepAngle/2).toDouble())
                    val x = size.width/2 + (size.minDimension/3) * cos(radians).toFloat()
                    val y = size.height/2 + (size.minDimension/3) * sin(radians).toFloat()
                    drawContext.canvas.nativeCanvas.drawText(
                        "${discount}%",
                        x, y,
                        android.graphics.Paint().apply {
                            textSize = 30f; color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            // 2) Spin Button (centered)
            Button(
                onClick = {
                    if (!isSpinning) {
                        isSpinning = true
                        val targetIndex = Random.nextInt(discounts.size)
                        val targetAngle = 360f*10 + sweepAngle*targetIndex
                        scope.launch {
                            spinAngle.animateTo(
                                targetValue = targetAngle,
                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                            )
                            // play sound & show confetti
                            mediaPlayer.start()
                            showConfetti = true
                            onDiscountSelected(discounts[targetIndex])
                            // wait for confetti
                            delay(2000)
                            showConfetti = false
                            // now hide the whole wheel area
                            showWheelArea = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F))
            ) {
                Text(if (isSpinning) "Spinning..." else "Spin Wheel")
            }
        }
    }

    // 3) Confetti overlay on the entire screen
    if (showConfetti) {
        ConfettiOverlay(show = true)
    }
}

@Composable
fun ConfettiOverlay(show: Boolean) {
    if (show) {
        val particles = remember { List(100) { Random.nextInt(0, 300) to Random.nextInt(0, 300) } }

        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { (x, y) ->
                drawCircle(
                    color = Color(
                        Random.nextFloat(),
                        Random.nextFloat(),
                        Random.nextFloat(),
                        1f
                    ),
                    radius = 5f,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
    }
}


@Composable
fun FoodItemSection(cartViewModel: CartViewModel) {
    val foodItems = listOf(
        FoodItemData("Chicken Kawab", 120.0, R.drawable.chickenawab),
        FoodItemData("Soya Chaap", 140.0, R.drawable.soyachaap),
        FoodItemData("Mutton Biriyani", 220.0, R.drawable.muttonbiriyani),
        FoodItemData("Pulao", 140.0, R.drawable.pulao),
        FoodItemData("Paneer", 120.0, R.drawable.paneer),
        FoodItemData("Egg Thali", 150.0, R.drawable.eggthali),
        FoodItemData("Mutton", 450.0, R.drawable.mutton),
        FoodItemData("Maach Bhaat", 250.0, R.drawable.maachbhaaat),
        FoodItemData("Chicken Thali", 350.0, R.drawable.chickenthali),
        FoodItemData("Veg Thali", 150.0, R.drawable.vegthali),
        FoodItemData("Salad", 80.0, R.drawable.salad),
        FoodItemData("Cheese Burger", 150.0, R.drawable.cheese_burger),
        FoodItemData("Veggie Burger", 100.0, R.drawable.burger),
        FoodItemData("Chicken Burger", 120.0, R.drawable.cheese_burger)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(foodItems) { foodItem ->
                FoodItem(foodItem = foodItem, cartViewModel = cartViewModel)
            }
        }
    }
}

@Composable
fun FoodItem(
    foodItem: FoodItemData,
    cartViewModel: CartViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Food Image
        Image(
            painter = painterResource(id = foodItem.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "₹${foodItem.price}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "A delicious ${foodItem.name} for you to enjoy.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Add Button
        Button(
            onClick = {
                val cartItem = CartItem(name = foodItem.name, priceInRs = foodItem.price)
                cartViewModel.addItemToCart(cartItem)
            },
            modifier = Modifier.align(Alignment.CenterVertically),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F))
        ) {
            Text(text = "ADD", color = Color.White)
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00AA4F))
    ) {
        Text(
            text = "SnapBites",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White
        )
    }
    // Navigate to home after a delay
    LaunchedEffect(Unit) {
        delay(1000) // 1-second splash
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }  // Clears backstack
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, cartViewModel: CartViewModel, locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val address by locationViewModel.address.collectAsState()
    var location by remember { mutableStateOf<Location?>(null) }
    var fetchedAddress by remember { mutableStateOf("Fetching address...") }
    val locationError by locationViewModel.locationError.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    var showCoupon by remember { mutableStateOf(true) }

    var searchText by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var minRating by remember { mutableFloatStateOf(0f) }
    var maxPrice by remember { androidx.compose.runtime.mutableFloatStateOf(Float.MAX_VALUE) }
    var onlyVeg by remember { mutableStateOf(false) }
    var within7km by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf("None") }

    // Sample restaurantList (should be moved to ViewModel/Repository)
    val restaurantList = listOf(
        Restaurant(
            "Maa Kali Restaurant",
            R.drawable.maakali,
            3.4f,
            1.2f,
            isVeg = true,
            priceInRs = 250.0,
            latitude = 37.4220936,
            longitude = -122.083922
        ),
        Restaurant(
            "Aasha Biriyani House",
            R.drawable.ashabriyani,
            4.5f,
            2.3f,
            isVeg = false,
            priceInRs = 350.0,
            latitude = 37.4232000,
            longitude = -122.081000
        ),
        Restaurant(
            "Bharti Restaurant",
            R.drawable.bhartires,
            4.0f,
            1.5f,
            isVeg = true,
            priceInRs = 200.0,
            latitude = 37.4215000,
            longitude = -122.080000
        ),
        Restaurant(
            "Dolphin Restaurant",
            R.drawable.dolphinres,
            2.5f,
            0.9f,
            isVeg = false,
            priceInRs = 400.0,
            latitude = 37.4200000,
            longitude = -122.085000
        ),
        Restaurant(
            "The Nawaab Restaurant",
            R.drawable.nawaabres,
            5.0f,
            3.0f,
            isVeg = false,
            priceInRs = 500.0,
            latitude = 37.4190000,
            longitude = -122.087000
        ),
        Restaurant(
            "Amrita Restaurant",
            R.drawable.amritares,
            3.7f,
            1.5f,
            isVeg = true,
            priceInRs = 550.0,
            latitude = 37.4250000,
            longitude = -122.082000
        ),
        Restaurant(
            "Monginis Restaurant",
            R.drawable.monginisres,
            3.9f,
            0.7f,
            isVeg = false,
            priceInRs = 400.0,
            latitude = 37.4265000,
            longitude = -122.086000
        ),
        Restaurant(
            "Mio Amore the Cake Shop",
            R.drawable.mioamore,
            4.3f,
            1.1f,
            isVeg = true,
            priceInRs = 450.0,
            latitude = 37.4235000,
            longitude = -122.083000
        ),
        Restaurant(
            "Prasenjit Hotel",
            R.drawable.maachbhaaat,
            4.4f,
            2.0f,
            isVeg = true,
            priceInRs = 550.0,
            latitude = 37.4210000,
            longitude = -122.088000
        ),
        Restaurant(
            "MSR Cafe and Restaurant",
            R.drawable.msrcafe,
            4.8f,
            0.8f,
            isVeg = false,
            priceInRs = 600.0,
            latitude = 37.4222000,
            longitude = -122.089500
        ),
        Restaurant(
            "Mira Store",
            R.drawable.koreanbibimbaap,
            4.3f,
            1.4f,
            isVeg = true,
            priceInRs = 660.0,
            latitude = 37.4270000,
            longitude = -122.084200
        ),
        Restaurant(
            "Darjeeling Fast Food",
            R.drawable.darjeeling,
            4.7f,
            1.6f,
            isVeg = false,
            priceInRs = 650.0,
            latitude = 37.4288000,
            longitude = -122.083500
        ),
        Restaurant(
            "Abar Khabo Tiffin House",
            R.drawable.abarkhabotiffin,
            1.0f,
            2.2f,
            isVeg = false,
            priceInRs = 550.0,
            latitude = 37.423,
            longitude = -122.083
        ),
        Restaurant(
            "Spice Symphony",
            R.drawable.spicessymphony,
            4.5f,
            1.8f,
            isVeg = false,
            priceInRs = 750.0,
            latitude = 37.424,
            longitude = -122.081
        ),
        Restaurant(
            "Pure Veg Delights",
            R.drawable.paneer,
            4.2f,
            3.5f,
            isVeg = true,
            priceInRs = 400.0,
            latitude = 37.421,
            longitude = -122.079
        ),
        Restaurant(
            "Tandoori Junction",
            R.drawable.tandoorijunction,
            4.8f,
            2.0f,
            isVeg = false,
            priceInRs = 900.0,
            latitude = 37.419,
            longitude = -122.084
        ),
        Restaurant(
            "Biryani House",
            R.drawable.chickenthali,
            4.6f,
            2.8f,
            isVeg = false,
            priceInRs = 650.0,
            latitude = 37.420,
            longitude = -122.082
        ),
        Restaurant(
            "South Indian Flavors",
            R.drawable.southindianflavors,
            4.3f,
            3.0f,
            isVeg = true,
            priceInRs = 500.0,
            latitude = 37.417,
            longitude = -122.080
        ),
        Restaurant(
            "Dilli Chaat Bhandar",
            R.drawable.salad,
            4.0f,
            1.5f,
            isVeg = true,
            priceInRs = 350.0,
            latitude = 37.422,
            longitude = -122.086
        ),
        Restaurant(
            "Mughlai Darbar",
            R.drawable.mughlaidarbar,
            4.7f,
            2.5f,
            isVeg = false,
            priceInRs = 850.0,
            latitude = 37.418,
            longitude = -122.078
        ),
        Restaurant(
            "The Punjabi Dhaba",
            R.drawable.spicessymphony,
            4.4f,
            3.2f,
            isVeg = false,
            priceInRs = 600.0,
            latitude = 37.419,
            longitude = -122.085
        ),
        Restaurant(
            "Coastal Curry",
            R.drawable.maachbhaaat,
            4.5f,
            2.7f,
            isVeg = false,
            priceInRs = 720.0,
            latitude = 37.420,
            longitude = -122.081
        ),
        Restaurant(
            "Rajasthani Rasoi",
            R.drawable.rajasthanifood,
            4.1f,
            3.8f,
            isVeg = true,
            priceInRs = 450.0,
            latitude = 37.421,
            longitude = -122.084
        ),
        Restaurant(
            "The Grand Thali",
            R.drawable.grandthali,
            4.6f,
            2.1f,
            isVeg = true,
            priceInRs = 550.0,
            latitude = 37.423,
            longitude = -122.080
        ),
        Restaurant(
            "Hyderabadi Biryani Center",
            R.drawable.muttonbiriyani,
            4.9f,
            1.9f,
            isVeg = false,
            priceInRs = 800.0,
            latitude = 37.418,
            longitude = -122.083
        ),
        Restaurant(
            "Bengali Bhoj",
            R.drawable.chickenawab,
            4.3f,
            3.4f,
            isVeg = false,
            priceInRs = 580.0,
            latitude = 37.422,
            longitude = -122.082
        ),
        Restaurant(
            "Malabar Spices",
            R.drawable.chickenthali,
            4.2f,
            2.9f,
            isVeg = false,
            priceInRs = 620.0,
            latitude = 37.419,
            longitude = -122.080
        ),
        Restaurant(
            "Gujarati Swad",
            R.drawable.rajasthanifood,
            4.0f,
            3.7f,
            isVeg = true,
            priceInRs = 400.0,
            latitude = 37.420,
            longitude = -122.085
        ),
        Restaurant(
            "Udupi Sagar",
            R.drawable.taco_supreme,
            4.5f,
            2.3f,
            isVeg = true,
            priceInRs = 520.0,
            latitude = 37.421,
            longitude = -122.079
        ),
        Restaurant(
            "Chennai Dosa Corner",
            R.drawable.southindianflavors,
            4.3f,
            2.6f,
            isVeg = true,
            priceInRs = 490.0,
            latitude = 37.417,
            longitude = -122.081
        ),
        Restaurant(
            "Lucknowi Kebab",
            R.drawable.muttonbiriyani,
            4.8f,
            1.7f,
            isVeg = false,
            priceInRs = 770.0,
            latitude = 37.424,
            longitude = -122.080
        ),
        Restaurant(
            "Swad Punjab Da",
            R.drawable.spicessymphony,
            4.5f,
            3.1f,
            isVeg = false,
            priceInRs = 750.0,
            latitude = 37.418,
            longitude = -122.082
        ),
        Restaurant(
            "Flavors of China",
            R.drawable.chickenmomos,
            4.2f,
            5.5f,
            isVeg = false,
            priceInRs = 620.0,
            latitude = 37.419,
            longitude = -122.084
        ),
        Restaurant(
            "Dilli Chaat Bhandar",
            R.drawable.dalparatha,
            4.7f,
            2.0f,
            isVeg = true,
            priceInRs = 250.0,
            latitude = 37.421,
            longitude = -122.086
        ),
        Restaurant(
            "Ming's Dynasty",
            R.drawable.chickenmomos,
            4.3f,
            3.8f,
            isVeg = false,
            priceInRs = 680.0,
            latitude = 37.423,
            longitude = -122.083
        ),
        Restaurant(
            "Biryani Junction",
            R.drawable.chickenawab,
            4.6f,
            4.0f,
            isVeg = false,
            priceInRs = 900.0,
            latitude = 37.420,
            longitude = -122.078
        ),
        Restaurant(
            "Hakka House",
            R.drawable.chickenhakkanoodles,
            4.1f,
            6.2f,
            isVeg = false,
            priceInRs = 580.0,
            latitude = 37.424,
            longitude = -122.085
        ),
        Restaurant(
            "Rajdhani Thali",
            R.drawable.rajasthanifood,
            4.8f,
            1.5f,
            isVeg = true,
            priceInRs = 650.0,
            latitude = 37.422,
            longitude = -122.081
        ),
        Restaurant(
            "Dragon Wok",
            R.drawable.nawaabres,
            4.0f,
            5.0f,
            isVeg = false,
            priceInRs = 720.0,
            latitude = 37.418,
            longitude = -122.080
        ),
        Restaurant(
            "Udupi Sagar",
            R.drawable.southindianflavors,
            4.4f,
            3.3f,
            isVeg = true,
            priceInRs = 300.0,
            latitude = 37.417,
            longitude = -122.079
        ),
        Restaurant(
            "Golden Chopsticks",
            R.drawable.dalparatha,
            3.9f,
            4.8f,
            isVeg = false,
            priceInRs = 550.0,
            latitude = 37.419,
            longitude = -122.083
        ),
        Restaurant(
            "Tandoori Nights",
            R.drawable.tandoorijunction,
            4.5f,
            2.9f,
            isVeg = false,
            priceInRs = 850.0,
            latitude = 37.420,
            longitude = -122.084
        ),
        Restaurant(
            "Chowman Express",
            R.drawable.msrcafe,
            4.2f,
            3.7f,
            isVeg = false,
            priceInRs = 600.0,
            latitude = 37.423,
            longitude = -122.080
        ),
        Restaurant(
            "Bengali Rasoi",
            R.drawable.fishtandoori,
            4.6f,
            2.5f,
            isVeg = false,
            priceInRs = 500.0,
            latitude = 37.422,
            longitude = -122.079
        ),
        Restaurant(
            "Sichuan Delights",
            R.drawable.pulao,
            4.0f,
            6.0f,
            isVeg = false,
            priceInRs = 700.0,
            latitude = 37.419,
            longitude = -122.081
        ),
        Restaurant(
            "Gujju Rasoi",
            R.drawable.gulabjamun,
            4.3f,
            3.2f,
            isVeg = true,
            priceInRs = 480.0,
            latitude = 37.417,
            longitude = -122.078
        )
        // Add more restaurants with latitude and longitude
    )

    LaunchedEffect(Unit) {
        getCurrentLocation(context) { loc -> userLocation = loc }
    }

    val filteredRestaurantsByLocation =
        filterRestaurantsByLocation(userLocation, restaurantList, maxDistance = 7.0f)

    val filteredAndSortedRestaurantList by remember(
        searchText, minRating, maxPrice, onlyVeg, within7km, sortOption, userLocation
    ) {
        mutableStateOf(
            filteredRestaurantsByLocation
                .filter {
                    it.name.contains(searchText, ignoreCase = true) &&
                            it.rating >= minRating &&
                            it.priceInRs <= maxPrice &&
                            (!onlyVeg || it.isVeg) &&
                            (!within7km || it.distance <= 7.0f)
                }
                .sortedWith(compareBy<Restaurant> {
                    when (sortOption) {
                        "Distance High to Low" -> -it.distance
                        "Delivery Time Low to High" -> it.distance
                        "Best Offers" -> -it.rating
                        else -> 0
                    }
                })
        )
    }

    RequestLocationPermission(
        fusedLocationClient = fusedLocationClient,
        onLocationReceived = { loc -> location = loc }
    )

    LaunchedEffect(location) {
        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            fetchedAddress = try {
                withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                }?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
            } catch (e: Exception) {
                "Error fetching address"
            }
        }
    }

    if (locationError != null) {
        LocationErrorState(error = locationError!!) {
            locationViewModel.fetchLocation()
        }
    } else {
        Scaffold(
            content = { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE8F5E9))
                            .padding(paddingValues)
                    ) {

                        item {
                            DeliveryHeader(
                                address = address,
                                userLocation = userLocation,
                                onAutomaticFetch = { locationViewModel.fetchLocation() },
                                onCartClick = { navController.navigate("cart") } // pass the cart action here
                            )
                        }

                        item {
                            if (showCoupon) {
                                CouponBanner(onOrderNowClick = {
                                    showCoupon = false
                                    navController.navigate("restaurants")
                                })
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        tint = Color(0xFF4CAF50) // Green
                                    )
                                },
                                placeholder = { Text("Search food or restaurants") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = Color.White,
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color(0xFF4CAF50),
                                    cursorColor = Color(0xFF4CAF50)
                                )
                            )
                        }


                        item {
                            SortMenu(
                                sortOption = sortOption,
                                onSortOptionSelected = { sortOption = it }
                            )
                        }

                        item { FoodCategories(navController) }
                        item { HighestRatingSection(searchText, navController, cartViewModel) }

                        items(filteredAndSortedRestaurantList) { restaurant ->
                            RestaurantCard(restaurant) {
                                navController.navigate("details/${restaurant.name}")
                            }
                        }
                    }

                    if (showManualDialog) {
                        AlertDialog(
                            onDismissRequest = { showManualDialog = false },
                            title = { Text("Enter Address Manually") },
                            text = {
                                ManualAddressInput { manualAddress ->
                                    locationViewModel.setManualAddress(manualAddress)
                                    showManualDialog = false
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = {
                                    showManualDialog = false
                                }) { Text("Cancel") }
                            }
                        )
                    }

                    if (showFilterDialog) {
                        FilterDialog(
                            minRating = minRating,
                            maxPrice = maxPrice,
                            onlyVeg = onlyVeg,
                            within7km = within7km,
                            sortOption = sortOption,
                            onMinRatingChange = { minRating = it },
                            onMaxPriceChange = { maxPrice = it },
                            onOnlyVegChange = { onlyVeg = it },
                            onWithin7kmChange = { within7km = it },
                            onSortOptionChange = { sortOption = it },
                            onDismiss = { showFilterDialog = false }
                        )
                    }
                }
            }
        )
    }
}
        // Function to get the current location
fun getCurrentLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationReceived(location)
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    } else {
        // Handle the case where permission is not granted
        onLocationReceived(null)
    }
}
// Function to calculate distance between two locations
fun calculateDistance(location1: Location, location2: Location): Float {
    return location1.distanceTo(location2) / 1000 // Convert meters to kilometers
}

// Function to filter restaurants by location
fun filterRestaurantsByLocation(userLocation: Location?, restaurantList: List<Restaurant>, maxDistance: Float): List<Restaurant> {
    if (userLocation == null) return emptyList()
    return restaurantList.filter { restaurant ->
        val restaurantLocation = Location("").apply {
            latitude = restaurant.latitude
            longitude = restaurant.longitude
        }
        calculateDistance(userLocation, restaurantLocation) <= maxDistance
    }
}

@Composable
fun SortMenu(sortOption: String, onSortOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                .background(Color(0xFFD0F0C0), shape = RoundedCornerShape(8.dp)) // light green
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = "Sort by: $sortOption", color = Color.Black)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Distance High to Low") },
                onClick = {
                    onSortOptionSelected("Distance High to Low")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Delivery Time Low to High") },
                onClick = {
                    onSortOptionSelected("Delivery Time Low to High")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Best Offers") },
                onClick = {
                    onSortOptionSelected("Best Offers")
                    expanded = false
                }
            )
        }
    }
}


@Composable
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            label = { Text("Search") },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)), // Rounded corners
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(24.dp) // Rounded corners applied here
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onFilterClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
        }
    }
}


@Composable
fun FilterDialog(
    minRating: Float,
    maxPrice: Float,
    onlyVeg: Boolean,
    within7km: Boolean,
    sortOption: String,
    onMinRatingChange: (Float) -> Unit,
    onMaxPriceChange: (Float) -> Unit,
    onOnlyVegChange: (Boolean) -> Unit,
    onWithin7kmChange: (Boolean) -> Unit,
    onSortOptionChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filters") },
        text = {
            Column {
                Text("Minimum Rating: ${minRating.toInt()}+")
                Slider(
                    value = minRating,
                    onValueChange = onMinRatingChange,
                    valueRange = 0f..5f,
                    steps = 4
                )

                Text("Max Price: ₹${maxPrice.toInt()}")
                Slider(
                    value = maxPrice,
                    onValueChange = onMaxPriceChange,
                    valueRange = 100f..500f,
                    steps = 4
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = onlyVeg,
                        onCheckedChange = onOnlyVegChange
                    )
                    Text("Only Veg")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = within7km,
                        onCheckedChange = onWithin7kmChange
                    )
                    Text("Within 7km")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Sort by:")
                Box {
                    Button(onClick = { sortMenuExpanded = true }) {
                        Text(sortOption)
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Distance High to Low") },
                            onClick = {
                                onSortOptionChange("Distance High to Low")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delivery Time Low to High") },
                            onClick = {
                                onSortOptionChange("Delivery Time Low to High")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Best Offers") },
                            onClick = {
                                onSortOptionChange("Best Offers")
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Apply") }
        }
    )
}


@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: (Restaurant) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(restaurant) })
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF))
    ) {
        Box {
            // 1) Hero image with gradient overlay
            Image(
                painter = painterResource(restaurant.imageResId),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            )
            Box(Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                        startY = 0f, endY = 400f
                    )
                )
            )
            // 2) Text overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(restaurant.name, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("${restaurant.rating} ★   ${restaurant.distance} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White)
            }
        }
    }
}
@Composable
fun AnimatedAddButton(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.9f else 1f, label = "scaleAnim")

    // This effect runs when `pressed` becomes true
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(50)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
    }
}


@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val discountPercentage by cartViewModel.discountPercentage.collectAsState()
    val context = LocalContext.current

    val originalTotal = cartItems.sumOf { it.priceInRs }
    val discountAmount = (originalTotal * discountPercentage) / 100
    val discountedTotal = originalTotal - discountAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Cart", style = MaterialTheme.typography.headlineSmall)

        if (cartItems.isEmpty()) {
            Text("Your cart is empty")
            Button(onClick = { navController.navigate("home") }) {
                Text("Go to Home")
            }
        } else {
            LazyColumn {
                items(cartItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("₹${item.priceInRs}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Original: ₹${"%.2f".format(originalTotal)}")
            if (discountPercentage > 0) {
                Text("Discount: $discountPercentage% (-₹${"%.2f".format(discountAmount)})")
            }

            Text(
                text = "Total: ₹${"%.2f".format(discountedTotal)}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    cartViewModel.placeOrder()              // ← save into history + clear cart
                    Toast.makeText(context, "Order placed!", Toast.LENGTH_SHORT).show()
                    navController.navigate("order")       // ← go to your Order screen
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F))
            ) {
                Text("Place Order")
            }
        }
    }
}

@Composable
fun HighestRatingSection(
    searchText: String,
    navController: NavController,
    cartViewModel: CartViewModel // Pass cartViewModel as a parameter
) {
    val items = listOf(
        "Snap Pizza" to R.drawable.snap_pizza,
        "Taco Supreme" to R.drawable.taco_supreme,
        "Deluxe Burger" to R.drawable.burger,
        "Cheese Burger" to R.drawable.cheese_burger
    )

    val filteredItems = items.filter { it.first.contains(searchText, ignoreCase = true) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Highest rating in town", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow {
            items(filteredItems) { item ->
                HighestRatedItem(
                    itemName = item.first,
                    imageRes = item.second, // Ensure this matches the type expected by HighestRatedItem
                    cartViewModel = cartViewModel
                ) {
                    // On click, navigate to RestaurantDetailsScreen
                    navController.navigate("details/${item.first}")
                }
            }
        }
    }
}

@Composable
fun HighestRatedItem(
    itemName: String,
    imageRes: Int,
    cartViewModel: CartViewModel, // Pass CartViewModel as a parameter
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(end = 16.dp)
            .clickable { onClick() }, // Click action for the card
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)) // Medium-light green background
    ) {
        Column {
            // Image for the restaurant item
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null, // Use a meaningful description in production
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
            Column(modifier = Modifier.padding(8.dp)) {
                // Item name
                Text(text = itemName, style = MaterialTheme.typography.titleMedium, color = Color.Black)

                // Additional info about the item
                Text(text = "4.4 ⭐ 156+ reviews", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                Text(text = "1.5km • 15min", style = MaterialTheme.typography.labelSmall, color = Color.Black)

                // Add to Cart Button
                Button(
                    onClick = {
                        val item = CartItem(name = itemName, priceInRs = 9.99) // Replace with the actual price if available
                        cartViewModel.addItemToCart(item)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Green button
                        contentColor = Color.White // White text color
                    )
                ) {
                    Text("Add to Cart")
                }
            }
        }
    }
}

@Composable
fun FoodCategories(navController: NavController) {
    val categoryIcons = mapOf(
        "Promo" to R.drawable.promocode1,
        "Taco" to R.drawable.taco_icon,
        "Drinks" to R.drawable.drinks_icon,
        "Meat" to R.drawable.meat_icon,
        "Sushi" to R.drawable.sushi_icon,
        "Pizza" to R.drawable.pizza
    )
    val defaultIcon = R.drawable.default_icon

    LazyRow(modifier = Modifier.padding(40.dp)) {
        items(categoryIcons.keys.toList()) { category ->
            CategoryItem(category, categoryIcons[category] ?: defaultIcon) {
                navController.navigate("category/$category")
            }
        }
    }
}

@Composable
fun CategoryItem(category: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = category,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CouponBanner(onOrderNowClick: () -> Unit) {
    val isVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(visible = isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.foodbanner),
                contentDescription = "Coupon Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Epic Deals Upto 40% Off",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = onOrderNowClick, // Use the callback here
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(text = "Order Now")
                }
            }
        }
    }
}

@Composable
fun DeliveryHeader(
    address: String,
    userLocation: Location?, // Nullable user location
    onAutomaticFetch: () -> Unit,
    onCartClick: () -> Unit // Add this to handle cart navigation
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { showDialog = true }
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Location",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = "Deliver To", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = address,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier
                        .border(BorderStroke(1.dp, Color(0xFF4CAF50)), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                )
                Text(
                    text = userLocation?.let {
                        "Location: %.4f, %.4f".format(it.latitude, it.longitude)
                    } ?: "Fetching your location...",
                    fontSize = 12.sp,
                    color = if (userLocation != null) Color.Black else Color.Gray
                )
            }
        }

        IconButton(onClick = { onCartClick() }) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.Black
            )
        }
    }

    if (showDialog) {
        AddressOptionDialog(
            onDismiss = { showDialog = false },
            onManualAddress = {
                showDialog = false
                val intent = android.content.Intent(context, MapsActivity::class.java)
                context.startActivity(intent)
            },
            onAutomaticFetch = {
                showDialog = false
                onAutomaticFetch()
            }
        )
    }
}


@Composable
fun AddressOptionDialog(
    onDismiss: () -> Unit,
    onManualAddress: () -> Unit,
    onAutomaticFetch: () -> Unit
) {
    var showManualInput by remember { mutableStateOf(false) }

    if (showManualInput) {
        ManualAddressInput(onSubmit = { address ->
            // Handle the submitted address
            onManualAddress()
            showManualInput = false
        })
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Choose Address Option") },
            text = { Text(text = "How would you like to enter your address?") },
            confirmButton = {
                Button(onClick = {
                    showManualInput = true
                }) {
                    Text(text = "Manual Address")
                }
            },
            dismissButton = {
                Button(onClick = onAutomaticFetch) {
                    Text(text = "Fetch Automatically")
                }
            }
        )
    }
}

@Composable
fun ManualAddressInput(onSubmit: (String) -> Unit) {
    var manualAddress by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = manualAddress,
            onValueChange = { manualAddress = it },
            label = { Text("Enter Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSubmit(manualAddress) }) {
            Text("Submit")
        }
    }
}

@Composable
fun RequestLocationPermission(
    fusedLocationClient: FusedLocationProviderClient,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    onLocationReceived: (Location?) -> Unit = {}
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
            onPermissionGranted()
            fetchLastKnownLocation(fusedLocationClient, onLocationReceived, context)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                permissionGranted = true
                onPermissionGranted()
                fetchLastKnownLocation(fusedLocationClient, onLocationReceived, context)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

private fun fetchLastKnownLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit,
    context: Context // Pass context as a parameter
) {
    // Check permission again before attempting to fetch the location
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location)
            } else {
                Log.d("RequestLocationPermission", "Last known location is null")
                onLocationReceived(null)
            }
        }.addOnFailureListener { e ->
            Log.e("RequestLocationPermission", "Error retrieving location", e)
            onLocationReceived(null) // Optionally pass null to the callback on failure
        }
    } else {
        Log.e("RequestLocationPermission", "Location permission required")
        onLocationReceived(null) // Optionally handle lack of permission
    }
}


@Composable
fun LocationErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error, color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.padding(16.dp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.padding(16.dp),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = {
                if (isValidEmail(email)) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                                navController.navigate("profile")
                            } else {
                                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                                Toast.makeText(context, "Sign Up Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Sign Up")
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            modifier = Modifier.padding(16.dp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            modifier = Modifier.padding(16.dp),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = {
                if (isValidEmail(email)) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Log In Successful", Toast.LENGTH_SHORT).show()
                                navController.navigate("profile")
                            } else {
                                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                                Toast.makeText(context, "Log In Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Log In")
        }
    }
}
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

