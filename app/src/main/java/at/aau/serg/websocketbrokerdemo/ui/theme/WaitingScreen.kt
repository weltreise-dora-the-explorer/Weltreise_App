package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaitingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF002244), // Dunkelblau oben
                        Color(0xFF4A90E2), // Mittelblau
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

            Spacer(modifier = Modifier.height(16.dp))

            //Kopfzeile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GAME PIN:",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = "#2222",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TRAVELMODE: GRAND TOUR (12 STOPS)",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "TRAVELLERS",
                fontSize = 16.sp,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            //Spieler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Spieler 1 (Host)
                TravellerItem("DoraTheExplorer\n(Host)")
                // Spieler 2
                TravellerItem("JetLagJerry")
                // Spieler 3
                TravellerItem("CaptainCooked")
                // Spieler 4
                TravellerItem("LostInPacific")
            }

            Spacer(modifier = Modifier.weight(1f)) // Schiebt den Button nach unten

            //Warten - Anzeige
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "waiting for your journey\nto start ...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Eine kleine Hilfs-Funktion für die Spieler-Avatare (KI)
@Composable
fun TravellerItem(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 850, heightDp = 480)
@Composable
fun WaitingScreenPreview() {
    MaterialTheme {
        WaitingScreen()
    }
}
