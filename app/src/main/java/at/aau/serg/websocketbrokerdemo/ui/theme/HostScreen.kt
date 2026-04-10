package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HostScreen(onStartClick: () -> Unit = {}) {
    // SpielModus
    var selectedTour by remember { mutableStateOf("Grand Tour") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF001F3F), // Dunkelblau oben
                        Color(0xFF5D9BBF), // Hellblau mitte
                        Color(0xFFE8B86D)  // Gelblich unten
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lobby-Code
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GAME PIN:    ",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "#2222",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Bildschirm auf zwei aufgeteilt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // linke spalt -> Einstellungen
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "STOPS ON TOUR",
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Die 3 Tour-Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TourButton(
                            text = "City Hopper\n(6)",
                            isSelected = selectedTour == "City Hopper",
                            onClick = { selectedTour = "City Hopper" }
                        )
                        TourButton(
                            text = "Grand Tour\n(12)",
                            isSelected = selectedTour == "Grand Tour",
                            onClick = { selectedTour = "Grand Tour" }
                        )
                        TourButton(
                            text = "Epic Voyage\n(18)",
                            isSelected = selectedTour == "Epic Voyage",
                            onClick = { selectedTour = "Epic Voyage" }
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Start Button
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8DB6CD))
                    ) {
                        Text(
                            text = "Start The Journey",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                //rechte Spalte -> Mitspieler
                Column(
                    modifier = Modifier.weight(1f).padding(start = 32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "TRAVELLERS",
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 64.dp) // Leicht eingerückt wegen den Avataren
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hier werden die Platzhalter-Bilder aufgerufen (Namen später anpassen!)
                    //TravellerCard("DoraTheExplorer (Host)", R.drawable.avatar_turtle)
                    //Spacer(modifier = Modifier.height(12.dp))

                    //TravellerCard("JetLagJerry", R.drawable.avatar_duck)
                    //Spacer(modifier = Modifier.height(12.dp))

                    //TravellerCard("CaptainCooked", R.drawable.avatar_bear)
                   // Spacer(modifier = Modifier.height(12.dp))

                   // TravellerCard("LostInPacific", R.drawable.avatar_pig)
                }
            }
        }
    }
}

// Helfer-Funktion für die Tour-Buttons (ändert Farbe bei Klick)
@Composable
fun TourButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFFE8C872) else Color(0xFFB0C4DE).copy(alpha = 0.8f)
    val textColor = if (isSelected) Color(0xFF001F3F) else Color(0xFF1E56A0)

    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// Helfer-Funktion für die Spieler-Karten rechts
@Composable
fun TravellerCard(name: String, avatarResId: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar Bild
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = "Avatar",
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Namensschild
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            color = Color.White.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = name,
                    color = Color(0xFF1E56A0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 850, heightDp = 480)
@Composable
fun HostScreenPreview() {
    MaterialTheme {
        HostScreen()
    }
}
