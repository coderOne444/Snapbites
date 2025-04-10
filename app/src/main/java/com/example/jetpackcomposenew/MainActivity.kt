@file:Suppress("DEPRECATION")

package com.example.jetpackcomposenew

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import android.location.Geocoder
import android.util.Log
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    private val _orderDetails = MutableStateFlow<List<CartItem>?>(null)
    val orderDetails: StateFlow<List<CartItem>?> = _orderDetails

    fun addItemToCart(item: CartItem) {
        _cartItems.value += item
        Log.d("CartViewModel", "Added item: ${item.name}. Current cart: ${_cartItems.value}")
    }

    // Function to clear all items from the cart
    fun clearCart() {
        _cartItems.value = emptyList()
        Log.d("CartViewModel", "Cart cleared. Current cart: ${_cartItems.value}")
    }

    // Function to place an order
    fun placeOrder() {
        _orderDetails.value = _cartItems.value
        clearCart()  // Clear the cart after placing the order
        Log.d("CartViewModel", "Order placed. Order details: ${_orderDetails.value}")
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
        Restaurant("Maa Kali Restaurant", R.drawable.maakali, 3.4f, 1.2f, isVeg = true, priceInRs = 250.0, latitude = 22.5726, longitude = 88.3639),
        Restaurant("Aasha Biriyani House", R.drawable.ashabriyani, 4.5f, 2.3f, isVeg = false, priceInRs = 350.0, latitude = 22.5726, longitude = 88.3639),
        // Add more restaurants with latitude and longitude
    )

    // Listen for route changes to toggle bottom bar visibility
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            showBottomBar = shouldShowBottomBar(backStackEntry.destination.route)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
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
                HomeScreen(navController = navController, cartViewModel = cartViewModel, locationViewModel = locationViewModel)
            }
            composable("details/{restaurantName}") { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName").orEmpty()
                RestaurantDetailsScreen(
                    cartViewModel = cartViewModel,
                    restaurantName = restaurantName,
                    restaurantList = restaurantList
                ) // Pass the restaurant list here
            }
            composable("order") {
                OrderScreen(cartViewModel = cartViewModel)
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category").orEmpty()
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
// Helper function to determine when to show the bottom bar
private fun shouldShowBottomBar(route: String?): Boolean {
    return route != "splash"
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
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp) // Rounded edges for the entire navigation bar
            )
            .shadow(8.dp, RoundedCornerShape(16.dp)) // Optional shadow for elevation effect
    ) {
        NavigationBar(
            modifier = Modifier
                .height(90.dp) // Adjust the height for compactness
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier
                            .size(24.dp) // Smaller icon size
                            .padding(top = 4.dp) // Shift icon downwards
                    )
                },
                label = {
                    Text(
                        "Home",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 2.dp) // Shift text downwards
                    )
                },
                selected = currentRoute == "home",
                onClick = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    indicatorColor = Color(0xFF4CAF50) // Green background when selected
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Order",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 4.dp) // Shift icon downwards
                    )
                },
                label = {
                    Text(
                        "Order",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 2.dp) // Shift text downwards
                    )
                },
                selected = currentRoute == "order",
                onClick = {
                    navController.navigate("order") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    indicatorColor = Color(0xFF4CAF50)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 4.dp) // Shift icon downwards
                    )
                },
                label = {
                    Text(
                        "Profile",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 2.dp) // Shift text downwards
                    )
                },
                selected = currentRoute == "profile",
                onClick = {
                    navController.navigate("profile") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.Black,
                    unselectedTextColor = Color.Black,
                    indicatorColor = Color(0xFF4CAF50)
                )
            )
        }
    }
}

@Composable
fun OrderScreen(cartViewModel: CartViewModel) {
    val orderDetails by cartViewModel.orderDetails.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (orderDetails.isNullOrEmpty()) {
            Text(text = "No Orders", style = MaterialTheme.typography.headlineSmall)
        } else {
            Column {
                Text(text = "Order Details", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                orderDetails!!.forEach { orderItem ->
                    Text(text = "${orderItem.name} - $${orderItem.priceInRs}", modifier = Modifier.padding(4.dp))
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
        CouponButton()

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
fun CouponButton() {
    Button(
        onClick = { /* Handle coupon claim */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F)),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Claim Free Cheese!")
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
fun HomeScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val address by locationViewModel.address.collectAsState()
    var location by remember { mutableStateOf<Location?>(null) }
    var fetchedAddress by remember { mutableStateOf("Fetching address...") }
    val locationError by locationViewModel.locationError.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var minRating by remember { mutableStateOf(0f) }
    var maxPrice by remember { mutableStateOf(Float.MAX_VALUE) }
    var onlyVeg by remember { mutableStateOf(false) }
    var within7km by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf("None") }

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
    )

    // Get the user's current location
    LaunchedEffect(Unit) {
        getCurrentLocation(context) { loc ->
            userLocation = loc
        }
    }

    // Filter restaurants by location
    val filteredRestaurantsByLocation = filterRestaurantsByLocation(userLocation, restaurantList, maxDistance = 7.0f)

    // Combine location-based filtering with existing filters
    val filteredAndSortedRestaurantList by remember(searchText, minRating, maxPrice, onlyVeg, within7km, sortOption, userLocation) {
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
        onLocationReceived = { loc ->
            location = loc
        }
    )

    LaunchedEffect(location) {
        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                }
                fetchedAddress = addressList?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
            } catch (e: Exception) {
                fetchedAddress = "Error fetching address"
            }
        }
    }

    if (locationError != null) {
        LocationErrorState(error = locationError!!) {
            locationViewModel.fetchLocation()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Snapbites") },
                    actions = {
                        IconButton(onClick = { navController.navigate("cart") }) {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) Badge {
                                        Text(cartItems.size.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart"
                                )
                            }
                        }
                    }
                )
            },
            content = { paddingValues ->
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
                            onManualAddress = { navController.navigate("manual_address") },
                            onAutomaticFetch = { locationViewModel.fetchLocation() }
                        )

                    }

                    item {
                        CouponBanner(onOrderNowClick = { navController.navigate("restaurants") })
                    }

                    item {
                        SearchBar(
                            searchText = searchText,
                            onSearchTextChanged = { searchText = it },
                            onFilterClick = { showFilterDialog = true }
                        )
                    }

                    item {
                        SortMenu(
                            sortOption = sortOption,
                            onSortOptionSelected = { selectedOption ->
                                sortOption = selectedOption
                            }
                        )
                    }

                    item { FoodCategories(navController) }
                    item { HighestRatingSection(searchText, navController, cartViewModel) }

                    items(filteredAndSortedRestaurantList) { restaurant ->
                        RestaurantCard(restaurant) { selectedRestaurant ->
                            navController.navigate("details/${selectedRestaurant.name}")
                        }
                    }
                }
            }
        )

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
            .padding(8.dp)
            .clickable { onClick(restaurant) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFC8E6C9)) // Light green background for the card
        ) {
            Column {
                // Display restaurant image using the drawable resource ID
                Image(
                    painter = painterResource(id = restaurant.imageResId),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50)) // Dark green background for text section
                        .padding(8.dp) // Padding for spacing
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White // White text for contrast
                    )
                    Text(
                        text = "Rating: ${restaurant.rating}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Distance: ${restaurant.distance} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel) {
    // Get cart items from CartViewModel
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Calculate total price
    val totalPrice = cartItems.sumOf { it.priceInRs }

    // Access the context for Toast
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Cart", style = MaterialTheme.typography.headlineSmall)

        if (cartItems.isEmpty()) {
            Text("Your cart is empty")
            // Button to navigate back to Home
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
                        Text("$${item.priceInRs}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display total price
            Text(
                text = "Total: $${"%.2f".format(totalPrice)}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Place Order Button
            Button(
                onClick = {
                    // Handle place order action
                    Toast.makeText(context, "Order placed successfully", Toast.LENGTH_SHORT).show()
                    // Clear cart after placing the order
                    cartViewModel.clearCart()
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
                painter = painterResource(id = R.drawable.freedeliverycoupon),
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
                    text = "2x free delivery coupon!",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
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
    userLocation: Location?, // Ensure this parameter is included
    onManualAddress: () -> Unit,
    onAutomaticFetch: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "User",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(text = "Deliver To", style = MaterialTheme.typography.labelSmall)

            Box(
                modifier = Modifier
                    .border(BorderStroke(2.dp, Color.Green), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(text = address, style = MaterialTheme.typography.titleMedium)
            }

            // Display user location if available
            userLocation?.let {
                Text(
                    text = "Your Location: ${it.latitude}, ${it.longitude}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDialog) {
        AddressOptionDialog(
            onDismiss = { showDialog = false },
            onManualAddress = {
                showDialog = false
                onManualAddress()
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
    onLocationReceived: (Location?) -> Unit = {} // Callback when location is received
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    // Create a launcher to request location permission
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

    // Check permission and request if not granted
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

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _address = MutableStateFlow("Fetching location...")
    val address: StateFlow<String> = _address

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError

    fun fetchLocation(context: Context) {
        // Check for permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            _locationError.value = "Location permission not granted"
            // Optionally, inform the UI to request permissions
        }
    }
    fun fetchLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _location.value = location
                    viewModelScope.launch {
                        _address.value = getAddressFromLocation(location)
                    }
                } else {
                    _locationError.value = "Location not available"
                }
            }.addOnFailureListener { exception ->
                _locationError.value = "Location error: ${exception.message}"
            }
        } catch (e: SecurityException) {
            _locationError.value = "Location permission required"
        }
    }

    private suspend fun getAddressFromLocation(location: Location): String {
        val geocoder = Geocoder(getApplication(), Locale.getDefault())
        return try {
            val addressList = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            addressList?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
        } catch (e: Exception) {
            "Error fetching address"
        }
    }
}
