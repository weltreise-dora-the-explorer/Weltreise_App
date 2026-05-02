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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
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



    LaunchedEffect(Unit) {
        viewModel.loadAllCities(context)
    }

    //Bilder
    val mapBitmap = loadAssetBitmap(context, "world_map.png")
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
            ZoomableMap(mapBitmap = mapBitmap)
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
                        text = if (isMyTurn) "Your roll!" else "$currentTurnPlayerId is rolling",
                        fontSize = 14.sp,
                        color = Color(0xFFD4AF37)
                    )
                }
            }
        }

        // Hinweis wessen Zug es ist
        if (currentTurnPlayerId != null) {
            Text(
                text = if (isMyTurn) "It is your turn!" else "Waiting for $currentTurnPlayerId ...",
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
                .padding(start = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Roll Dice
            GameButton(
                text = "ROLL DICE",
                imageBitmap = diceBitmap,
                enabled = canRoll,
                onClick = { viewModel.onRollDice() }
            )

            Spacer(modifier = Modifier.height(1.dp))

            //Bucket List
            GameButton(
                text = "BUCKET LIST",
                imageBitmap = bucketBitmap,
                onClick = {
                    showBucketListDialog = true
                }
            )

            // Zug beenden – nur sichtbar wenn gewürfelt und dran, unter Bucket List
            if (canEndTurn) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.onEndTurn() },
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8DB6CD)
                    )
                ) {
                    Text(
                        text = "End Turn",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
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
                            text = "No target cities assigned yet.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "📍 Target Cities (${ownedCities.size})",
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


//Weltkarte mit Zoom und Verschiebe Funktion
@Composable
fun ZoomableMap(mapBitmap: ImageBitmap) {
    var scale by remember {mutableStateOf(1f)}
    var offset by remember {mutableStateOf(Offset.Zero)}

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()


        Image(
            bitmap = mapBitmap,
            contentDescription = "Weltkarte",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
                .pointerInput(screenWidth, screenHeight) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val adjustedZoom = if (zoom < 1f) {
                            zoom.toDouble().pow(2.2).toFloat()
                        } else {
                            zoom.toDouble().pow(1.1).toFloat()
                        }

                        val newScale = (scale * adjustedZoom).coerceIn(1f, 8f)

                        val maxOffsetX = ((screenWidth * newScale) - screenWidth) / 2f
                        val maxOffsetY = ((screenHeight * newScale) - screenHeight) / 2f

                        // Je stärker hineingezoomt ist, desto schneller soll die Karte verschiebbar sein.
                        val panSpeed = (newScale * 1.4f).coerceIn(2.5f, 10f)

                        scale = newScale

                        offset = if (newScale > 1f) {
                            Offset(
                                x = (offset.x + pan.x * panSpeed)
                                    .coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y * panSpeed)
                                    .coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            Offset.Zero
                        }
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