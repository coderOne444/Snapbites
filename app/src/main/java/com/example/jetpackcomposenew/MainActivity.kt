@file:Suppress("DEPRECATION")

package com.example.jetpackcomposenew

import android.Manifest
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Add CartViewModel to handle the cart items
    private val cartViewModel: CartViewModel by this.viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Enable edge-to-edge layout

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Render the UI with cartViewModel passed into AppNavigation
        setContent {
            AppNavigation(cartViewModel)
        }
    }

    // Function to fetch the user's last known location
    private fun getLastKnownLocation(onLocationResult: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Pass location to callback
                    onLocationResult(location)
                }
        }
    }
}

data class CartItem(
    val name: String,
    val price: Double,
    val quantity: Int = 1 // Default quantity is 1
)

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun addItemToCart(item: CartItem) {
        _cartItems.value += item
    }

    fun removeItemFromCart(item: CartItem) {
        _cartItems.value -= item
    }
}


@Composable
fun AppNavigation(cartViewModel: CartViewModel) {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(false) }

    // Listen for route changes to toggle bottom bar visibility
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            showBottomBar = backStackEntry.destination.route != "splash"
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
                HomeScreen(navController, cartViewModel) // Pass cartViewModel to HomeScreen
            }
            composable("details/{restaurantName}") { backStackEntry ->
                val restaurantName = backStackEntry.arguments?.getString("restaurantName")
                RestaurantDetailsScreen(restaurantName ?: "Unknown Restaurant")
            }
            composable("order") {
                OrderScreen()
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                if (category != null) {
                    CategoryScreen(category, navController)
                }
            }
            composable("signup") {
                SignUpScreen(navController = navController)
            }
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("cart") {
                CartScreen(navController = navController, cartViewModel = cartViewModel) // Pass cartViewModel to CartScreen
            }
        }
    }
}


@Composable
fun BottomNavBar(navController: NavController) {
    val currentDestination = navController.currentDestination
    val currentRoute = currentDestination?.route  // Extract the current route safely

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",  // Check the route safely
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Order") },
            label = { Text("Order") },
            selected = currentRoute == "order",  // Check the route safely
            onClick = {
                navController.navigate("order") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",  // Check the route safely
            onClick = {
                navController.navigate("profile") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun CategoryScreen(category: String, navController: NavController) {
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

        // List of dummy restaurants for the given category
        LazyColumn {
            items(5) { index ->
                RestaurantItem(
                    name = "$category Restaurant $index",
                    onClick = {
                        // Navigate to the details screen when a restaurant is clicked
                        navController.navigate("details/$category Restaurant $index")
                    }
                )
            }
        }
    }
}


@Composable
fun OrderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No Orders", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    var name by remember { mutableStateOf("Jonny Kumar") }
    var email by remember { mutableStateOf("jonnykumar@example.com") }
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



@Composable
fun RestaurantDetailsScreen(restaurantName: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Display the restaurant name passed as a parameter
        Text(text = restaurantName, style = MaterialTheme.typography.headlineSmall)

        // Burger Details
        BurgerDetails()

        // Coupon Button
        CouponButton()

        // Food Items
        FoodItemSection()
    }
}


@Composable
fun BurgerDetails() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Burger Anzay", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Indian, Fast food, Burger", style = MaterialTheme.typography.titleSmall)
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
fun FoodItemSection() {
    var cartItems by remember { mutableStateOf(listOf<String>()) }

    // List of items (You can replace this with a more complex data model)
    val foodItems = listOf("Cheese Burger", "Veggie Burger", "Chicken Burger")

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(foodItems) { item ->
            val context = LocalContext.current
            // Pass the onAddToCart function to FoodItem with specific item name
            FoodItem(foodName = item, onAddToCart = {
                // Add item to cart and update the cart state
                cartItems = cartItems + item // Add the specific item
                Toast.makeText(context, "$item added to cart!", Toast.LENGTH_SHORT).show()
            })
        }
    }

    // Display the cart contents
    Text(
        text = "Cart contains: ${cartItems.size} items",
        modifier = Modifier.padding(16.dp)
    )
    // You can also display the list of items in the cart, if desired
    cartItems.forEach { item ->
        Text(text = item, modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun FoodItem(foodName: String, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = R.drawable.burger), // Example image
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = foodName, style = MaterialTheme.typography.titleMedium)
                Text(text = "$15", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button
                Button(
                    onClick = onAddToCart, // Call the passed function when clicked
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00AA4F))
                ) {
                    Text(text = "Add to Cart")
                }
            }
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
fun HomeScreen(navController: NavController, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("Fetching location...") }
    var location by remember { mutableStateOf<Location?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var isManualEntry by remember { mutableStateOf(false) }

    // Get cart item count from CartViewModel
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Trigger location fetching
    if (isFetchingLocation) {
        RequestLocationPermission(
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context),
            onLocationReceived = { loc ->
                location = loc
                isFetchingLocation = false
            },
            onPermissionDenied = {
                locationError = "Location permission denied"
                isFetchingLocation = false
            }
        )
    }

    LaunchedEffect(location) {
        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            address = try {
                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                }
                if (!addressList.isNullOrEmpty()) {
                    addressList[0].getAddressLine(0)
                } else {
                    "Address not found"
                }
            } catch (e: Exception) {
                "Error fetching address"
            }
        }
    }

    if (locationError != null) {
        LocationErrorState(error = locationError!!) {
            locationError = null
            location = null
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Snapbites") },
                    actions = {
                        IconButton(onClick = {
                            // Navigate to Cart Screen
                            navController.navigate("cart")
                        }) {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge { Text(cartItems.size.toString()) }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    DeliveryHeader(
                        address = address,
                        onManualAddress = {
                            isManualEntry = true
                        },
                        onAutomaticFetch = {
                            isFetchingLocation = true
                        }
                    )

                    if (isManualEntry) {
                        ManualAddressInput { enteredAddress ->
                            address = enteredAddress
                            isManualEntry = false
                        }
                    }

                    CouponBanner()

                    var searchText by remember { mutableStateOf("") }

                    // Search Bar
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChanged = { newText -> searchText = newText }
                    )

                    // Food Categories and Highest Rating Section
                    FoodCategories(navController)
                    HighestRatingSection(searchText = searchText, navController = navController, cartViewModel = cartViewModel)
                    // Example: Button to Add Item to Cart
                    Button(onClick = {
                        // Add item to cart using ViewModel
                        cartViewModel.addItemToCart(CartItem("Sample Item", 12.99))
                    }) {
                        Text("Add to Cart")
                    }
                }
            }
        )
    }
}

@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel) {
    // Get cart items from CartViewModel
    val cartItems by cartViewModel.cartItems.collectAsState()

    // Calculate total price
    val totalPrice = cartItems.sumOf { it.price }

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
                        Text("$${item.price}", style = MaterialTheme.typography.bodyLarge)
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
                    //cartViewModel.clearCart()  // Optionally clear the cart after placing the order
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
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        label = { Text(text = "Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Search Icon")
        },
    )
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
    cartViewModel: CartViewModel,  // Pass CartViewModel as a parameter
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(end = 16.dp)
            .clickable { onClick() }
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = itemName, style = MaterialTheme.typography.titleMedium)
                Text(text = "4.3 ⭐️ 156+ reviews", style = MaterialTheme.typography.labelSmall)
                Text(text = "1.5km • 15min", style = MaterialTheme.typography.labelSmall)

                // Add to Cart Button
                Button(onClick = {
                    val item = CartItem(name = itemName, price = 9.99) // Replace with the actual price if available
                    cartViewModel.addItemToCart(item)
                }) {
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
fun CouponBanner() {
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
                    text = "You have 2x free delivery coupon!",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = { /* Handle click */ },
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
fun RestaurantItem(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "4.1 ⭐️", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DeliveryHeader(
    address: String,
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
            imageVector = Icons.Default.Person,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Choose Address Option") },
        text = { Text(text = "How would you like to enter your address?") },
        confirmButton = {
            Button(onClick = onManualAddress) {
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

            // Fetch last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    Log.d("RequestLocationPermission", "Last known location is null")
                    onLocationReceived(null)
                }
            }.addOnFailureListener { e ->
                Log.e("RequestLocationPermission", "Error retrieving location", e)
            }

        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            onPermissionDenied()
        }
    }

    // Request location permission
    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                permissionGranted = true
                onPermissionGranted()

                // Fetch last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    onLocationReceived(location)
                }.addOnFailureListener { e ->
                    Log.e("RequestLocationPermission", "Error retrieving location", e)
                }
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
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



