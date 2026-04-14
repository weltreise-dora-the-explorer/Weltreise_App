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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import at.aau.serg.websocketbrokerdemo.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(viewModel: AppViewModel) {
    var gamePin by remember { mutableStateOf("") }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Clear error when user starts typing
    LaunchedEffect(gamePin) {
        viewModel.clearError()
    }

    // Hintergrund
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF001F3F), // Dunkelblau (unten/links)
                        Color(0xFFE8B86D)  // Warmes Gelb/Orange (oben/rechts)
                    )
                )
            )
    ) {
        // Bildschirm teilen
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // links -> Figur
            Box(
                modifier = Modifier
                    .weight(1f) // Nimmt 50% der Breite ein
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
               // Image(
                    // ACHTUNG: Du brauchst ein Bild deiner Freunde (ohne Hintergrund) im drawable-Ordner!
                 //   painter = painterResource(id = R.drawable.lobby_friends),
                 //   contentDescription = "Freunde warten in der Lobby",
                  //  modifier = Modifier
                  //      .fillMaxWidth(0.9f)
                 //       .padding(bottom = 16.dp),
                //    contentScale = ContentScale.Fit
            //    )
            }

            // rechts
            Column(
                modifier = Modifier
                    .weight(1f) // Nimmt die anderen 50% der Breite ein
                    .padding(end = 64.dp, start = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Überschrift
                Text(
                    text = "Join Your Friends!",
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Label über Textfeld
                Text(
                    text = "GAME PIN",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pin-Eingabe
                TextField(
                    value = gamePin,
                    onValueChange = { gamePin = it }, // Aktualisiert die Variable beim Tippen
                    placeholder = { Text("#2222", color = Color(0xFF1E56A0)) }, // Der graue Platzhalter-Text
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                        focusedIndicatorColor = Color.Transparent, // Versteckt die Standard-Unterstrich-Linie
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF1E56A0), // Die blaue Schriftfarbe wie in deinem Entwurf
                        unfocusedTextColor = Color(0xFF1E56A0)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Join Button
                Button(
                    onClick = { viewModel.joinLobby(gamePin) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF90B8D4) //hellblau
                    ),
                    enabled = !isLoading && gamePin.isNotBlank()
                ){
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Join Game",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Fehlermeldung anzeigen
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Preview disabled due to ViewModel requirement
// @Preview(showBackground = true, widthDp = 850, heightDp = 480)
// @Composable
// fun LobbyScreenPreview() {
//     MaterialTheme {
//         LobbyScreen()
//     }
// }
