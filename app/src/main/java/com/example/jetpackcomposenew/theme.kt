import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackcomposenew.ui.theme.Typography

// Theme.kt
@Composable
fun SnapBitesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF1DB954),
            onPrimary = Color.White,
            secondary = Color(0xFF191414),
            onSecondary = Color.White,
            background = Color(0xFF121212),
            onBackground = Color(0xFFEEEEEE)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF4CAF50),
            onPrimary = Color.White,
            secondary = Color(0xFFE8F5E9),
            onSecondary = Color.Black,
            background = Color(0xFFF1F8E9),
            onBackground = Color(0xFF212121)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            displayLarge = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold),
            headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            bodyMedium    = TextStyle(fontSize = 16.sp),
            labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        ),
        shapes = Shapes(
            small  = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(16.dp),
            large  = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
