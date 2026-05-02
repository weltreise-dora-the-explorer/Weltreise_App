package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import at.aau.serg.websocketbrokerdemo.AppViewModel

@Composable
fun HostScreen(viewModel: AppViewModel) {
    val selectedTour by viewModel.gameMode.collectAsState()
    val playersList by viewModel.playersList.collectAsState()
    val context = LocalContext.current
    val avatars = listOf(
        loadAssetBitmap(context, "turtle_with_luggage_loginscreen.png"),
        loadAssetBitmap(context, "avatar_duck.png"),
        loadAssetBitmap(context, "avatar_bear.png"),
        loadAssetBitmap(context, "avatar_pig.png")
    )

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
        Button(
            onClick = { viewModel.leaveLobby() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
        ) {
            Text("Leave Lobby", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        GameRulesButton(
            modifier = Modifier.align(Alignment.TopEnd)
        )

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
                val pin by viewModel.lobbyId.collectAsState()
                Text(
                    text = "GAME PIN:    ",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "#$pin",
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
                            onClick = { viewModel.setGameMode("City Hopper") }
                        )
                        TourButton(
                            text = "Grand Tour\n(9)",
                            isSelected = selectedTour == "Grand Tour",
                            onClick = { viewModel.setGameMode("Grand Tour") }
                        )
                        TourButton(
                            text = "Epic Voyage\n(12)",
                            isSelected = selectedTour == "Epic Voyage",
                            onClick = { viewModel.setGameMode("Epic Voyage") }
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Start Button
                    Button(
                        onClick = { viewModel.startGame() },
                        enabled = playersList.size >= 2,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8DB6CD),
                            disabledContainerColor = Color(0x1F000000),
                            disabledContentColor = Color(0x1F000000)
                        )
                    ) {
                        Text(
                            text = if (playersList.size >= 2) "Start The Journey" else "Not Enough Players",
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

                    // Dynamically render players
                    playersList.forEachIndexed { index, playerName ->
                        TravellerCard(playerName, avatars.getOrNull(index % avatars.size))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
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
fun TravellerCard(name: String, avatar: ImageBitmap? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (avatar != null) {
                Image(
                    bitmap = avatar,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

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

// @Preview(showBackground = true, widthDp = 850, heightDp = 480)
// @Composable
// fun HostScreenPreview() {
// }
