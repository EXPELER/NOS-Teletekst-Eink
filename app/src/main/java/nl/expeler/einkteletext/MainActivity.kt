package nl.expeler.einkteletext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.expeler.einkteletext.ui.screens.TeletextScreen
import nl.expeler.einkteletext.ui.theme.EinkTeletextTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EinkTeletextTheme {
                TeletextScreen()
            }
        }
    }
}
