package at.aau.serg.websocketbrokerdemo.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.AppViewModel

@Composable
fun GameScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val playersList by viewModel.playersList.collectAsState()
    val currentPlayerName by viewModel.playerName.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val diceValue by viewModel.diceValue.collectAsState()
    val currentTurnPlayerId by viewModel.currentTurnPlayerId.collectAsState()
    val ownedCities by viewModel.ownedCities.collectAsState()
    val startCity by viewModel.startCity.collectAsState()
    val playerCityCounts by viewModel.playerCityCounts.collectAsState()
    val isMyTurn = currentTurnPlayerId == currentPlayerName
    val canRoll = isMyTurn && diceValue == null
    val canEndTurn = isMyTurn && diceValue != null



    //Bilder
    val mapBitmap = loadAssetBitmap(context, "world_map_klein.png")
    val diceBitmap = loadAssetBitmap(context, "dice_icon.png")
    val bucketBitmap = loadAssetBitmap(context, "bucket_list_icon.png")

    // Avatar-Liste für verschiedene Spieler
    val avatars = listOf(
        loadAssetBitmap(context, "turtle_with_luggage_loginscreen.png"),
        loadAssetBitmap(context, "avatar_duck.png"),
        loadAssetBitmap(context, "avatar_bear.png"),
        loadAssetBitmap(context, "avatar_pig.png")
    )

    //Bucketlist offen? Default false
    var showBucketListDialog by remember { mutableStateOf(false) }

    // Würfelergebnis fade-out nach 5 Sekunden
    var showDiceOverlay by remember { mutableStateOf(false) }
    val diceAlpha = remember { Animatable(0f) }
    LaunchedEffect(diceValue) {
        if (diceValue != null) {
            showDiceOverlay = true
            diceAlpha.snapTo(1f)
            delay(2000)
            diceAlpha.animateTo(0f, animationSpec = tween(1000))
            showDiceOverlay = false
        } else {
            showDiceOverlay = false
            diceAlpha.snapTo(0f)
        }
    }

    // Box (Schichten-Design)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003366)) // Tiefblauer Hintergrund
    ) {

        // Weltkarte
        if (mapBitmap != null) {
            Image(
                bitmap = mapBitmap,
                contentDescription = "Weltkarte",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Game Mode Badge oben rechts
        Text(
            text = gameMode.uppercase(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp)
                .background(Color(0x88000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        //Mitspieler - dynamisch aus dem ViewModel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            playersList.forEachIndexed { index, playerName ->
                val avatar = avatars.getOrNull(index % avatars.size)
                val isFirstPlayer = index == 0
                val displayName = if (isFirstPlayer) "$playerName (Host)" else playerName
                PlayerCard(
                    name = displayName,
                    bucketListCount = playerCityCounts[playerName] ?: 0,
                    avatar = avatar,
                    isActive = playerName == currentTurnPlayerId,
                    diceValue = if (playerName == currentTurnPlayerId) diceValue else null
                )
            }
        }

        // Würfelergebnis – für alle sichtbar in der Mitte, verschwindet nach 5s
        if (showDiceOverlay && diceValue != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(diceAlpha.value)
                    .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$diceValue",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isMyTurn) "Dein Wurf!" else "$currentTurnPlayerId würfelt",
                        fontSize = 14.sp,
                        color = Color(0xFFD4AF37)
                    )
                }
            }
        }

        // Hinweis wessen Zug es ist
        if (currentTurnPlayerId != null) {
            Text(
                text = if (isMyTurn) "Du bist dran!" else "Warte auf $currentTurnPlayerId ...",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        //Actionbuttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            //Roll Dice
            GameButton(
                text = "ROLL DICE",
                imageBitmap = diceBitmap,
                enabled = canRoll,
                onClick = { viewModel.onRollDice() }
            )

            Spacer(modifier = Modifier.height(1.dp))

            // Zug beenden – nur sichtbar wenn gewürfelt und dran
            if (canEndTurn) {
                GameButton(
                    text = "ZUG ENDE",
                    imageBitmap = null,
                    enabled = true,
                    onClick = { viewModel.onEndTurn() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Bucket List
            GameButton(
                text = "BUCKET LIST",
                imageBitmap = bucketBitmap,
                onClick = {
                    showBucketListDialog = true
                }
            )
        }
    }

    //Pop Up Bucket List
    if (showBucketListDialog) {
        AlertDialog(
            onDismissRequest = { showBucketListDialog = false },
            title = {
                Text(text = "Bucket List", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (startCity != null) {
                        Text(
                            text = "🏠 Startstadt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E56A0)
                        )
                        Text(
                            text = "${startCity!!.name}  •  ${startCity!!.continent.name.replace("_", " ")}",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (ownedCities.isEmpty()) {
                        Text(
                            text = "Noch keine Zielstädte zugewiesen.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "📍 Zielstädte (${ownedCities.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E56A0)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ownedCities.forEach { city ->
                            Text(
                                text = "${city.name}  •  ${city.continent.name.replace("_", " ")}",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBucketListDialog = false }) {
                    Text("close")
                }
            }
        )
    }
}


//Hilfe damit App nicht abstürzt (bsp. derzeit noch fehlende Bilder)

@Composable
fun PlayerCard(name: String, bucketListCount: Int, avatar: ImageBitmap?, isActive: Boolean, diceValue: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .zIndex(1f)
        ) {
            if (avatar != null) {
                Image(bitmap = avatar, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }

        Column(
            modifier = Modifier
                .offset(x = (-15).dp)
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .border(
                    width = if (isActive) 3.dp else 0.dp,
                    color = if (isActive) Color(0xFFD4AF37) else Color.Transparent,
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, fontSize = 12.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Bold)
                if (diceValue != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🎲 $diceValue", fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                }
            }
            Text(text = "Bucket List: $bucketListCount", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun GameButton(text: String, imageBitmap: ImageBitmap?, onClick: () -> Unit, enabled: Boolean = true) {
    val bgColor = if (enabled) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(bgColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = text,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(modifier = Modifier.size(60.dp).background(Color.Gray))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

fun loadAssetBitmap(context: Context, fileName: String): ImageBitmap? {
    return try {
        context.assets.open(fileName).use { inputStream ->
            BitmapFactory.decodeStream(inputStream).asImageBitmap()
        }
    } catch (_: Exception) {
        null
    }
}