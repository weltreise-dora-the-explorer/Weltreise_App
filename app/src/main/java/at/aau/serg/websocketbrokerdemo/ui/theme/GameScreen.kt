package at.aau.serg.websocketbrokerdemo.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.aau.serg.websocketbrokerdemo.AppViewModel
import at.aau.serg.websocketbrokerdemo.models.City
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
@Composable
fun GameScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val playersList by viewModel.playersList.collectAsState()
    val currentPlayerName by viewModel.playerName.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val diceValue by viewModel.diceValue.collectAsState()
    val currentTurnPlayerId by viewModel.currentTurnPlayerId.collectAsState()
    val ownedCities by viewModel.ownedCities.collectAsState()
    val allCities by viewModel.allCities.collectAsState()
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
    val rawMapBitmap = remember { loadRawBitmap(context, "world_map.png") }
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
            ZoomableMap(mapBitmap = mapBitmap, rawBitmap = rawMapBitmap, allCities = allCities, ownedCities = ownedCities)
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
fun ZoomableMap(
    mapBitmap: ImageBitmap,
    rawBitmap: android.graphics.Bitmap? = null,
    allCities: List<City> = emptyList(),
    ownedCities: List<City> = emptyList()
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Für jede Stadt vorausberechnen ob sie über Meer liegt
    val cityOverOcean = remember(allCities, rawBitmap) {
        if (rawBitmap == null) return@remember emptyMap<String, Boolean>()
        allCities.associate { city ->
            val px = (city.x_relativ * rawBitmap.width).toInt().coerceIn(0, rawBitmap.width - 1)
            val py = (city.y_relativ * rawBitmap.height).toInt().coerceIn(0, rawBitmap.height - 1)
            val pixel = rawBitmap.getPixel(px, py)
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)
            city.id to (b > r + 30 && b > g + 20)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Tatsächliche Kartengröße bei ContentScale.Fit berechnen
        val mapAspect = mapBitmap.width.toFloat() / mapBitmap.height.toFloat()
        val screenAspect = screenWidth / screenHeight
        val (renderedWidth, renderedHeight) = if (mapAspect > screenAspect) {
            screenWidth to screenWidth / mapAspect
        } else {
            screenHeight * mapAspect to screenHeight
        }
        val xOffset = (screenWidth - renderedWidth) / 2f
        val yOffset = (screenHeight - renderedHeight) / 2f

        Box(
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
                        val panSpeed = (newScale * 1.4f).coerceIn(2.5f, 10f)
                        scale = newScale
                        offset = if (newScale > 1f) {
                            Offset(
                                x = (offset.x + pan.x * panSpeed).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y * panSpeed).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            Offset.Zero
                        }
                    }
                }
        ) {
            Image(
                bitmap = mapBitmap,
                contentDescription = "Weltkarte",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 5f
                val cityMap = allCities.associateBy { it.id }

                // Deduplizierte Verbindungspaare sammeln
                val trainPairs = mutableSetOf<Pair<String, String>>()
                val flightPairs = mutableSetOf<Pair<String, String>>()
                allCities.forEach { city ->
                    city.trainConnections.forEach { targetId ->
                        val key = if (city.id < targetId) city.id to targetId else targetId to city.id
                        trainPairs.add(key)
                    }
                    city.flightConnections.forEach { targetId ->
                        val key = if (city.id < targetId) city.id to targetId else targetId to city.id
                        flightPairs.add(key)
                    }
                }

                // Zugverbindungen (schwarz, gerade)
                trainPairs.forEach { (idA, idB) ->
                    val a = cityMap[idA] ?: return@forEach
                    val b = cityMap[idB] ?: return@forEach
                    val ax = xOffset + a.x_relativ * renderedWidth
                    val ay = yOffset + a.y_relativ * renderedHeight
                    val bx = xOffset + b.x_relativ * renderedWidth
                    val by = yOffset + b.y_relativ * renderedHeight
                    // Leichter Versatz wenn auch Flugverbindung existiert
                    val sharedFlight = flightPairs.contains(if (idA < idB) idA to idB else idB to idA)
                    val dx = bx - ax
                    val dy = by - ay
                    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                    val perpX = if (sharedFlight) -dy / len * 2f else 0f
                    val perpY = if (sharedFlight) dx / len * 2f else 0f
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(ax + perpX, ay + perpY),
                        end = Offset(bx + perpX, by + perpY),
                        strokeWidth = 1.5f
                    )
                }

                // Flugverbindungen (rot, gebogener Bogen)
                flightPairs.forEach { (idA, idB) ->
                    val a = cityMap[idA] ?: return@forEach
                    val b = cityMap[idB] ?: return@forEach
                    val ax = xOffset + a.x_relativ * renderedWidth
                    val ay = yOffset + a.y_relativ * renderedHeight
                    val bx = xOffset + b.x_relativ * renderedWidth
                    val by = yOffset + b.y_relativ * renderedHeight

                    if (abs(a.x_relativ - b.x_relativ) > 0.5f) {
                        // Trans-Pazifik: Wrap-Around durch Pazifik (links raus, rechts rein)
                        val (leftCity, rightCity) = if (a.x_relativ < b.x_relativ) a to b else b to a
                        val lx = xOffset + leftCity.x_relativ * renderedWidth
                        val ly = yOffset + leftCity.y_relativ * renderedHeight
                        val rx = xOffset + rightCity.x_relativ * renderedWidth
                        val ry = yOffset + rightCity.y_relativ * renderedHeight
                        val virtualRx = rx - renderedWidth
                        val virtualLx = lx + renderedWidth
                        val wrapDist = sqrt((virtualRx - lx).pow(2) + (ry - ly).pow(2))
                        val curvature = (wrapDist * 0.35f).coerceAtMost(renderedHeight * 0.3f)
                        // Bogen 1: linke Stadt → linke Kartenkante (Richtung Pazifik)
                        val path1 = Path().apply {
                            moveTo(lx, ly)
                            quadraticTo((lx + virtualRx) / 2f, (ly + ry) / 2f - curvature, virtualRx, ry)
                        }
                        drawPath(path1, color = Color(0xFFE53935), style = Stroke(width = 1.5f))
                        // Bogen 2: rechte Kartenkante → rechte Stadt (aus Pazifik kommend)
                        val path2 = Path().apply {
                            moveTo(virtualLx, ly)
                            quadraticTo((virtualLx + rx) / 2f, (ly + ry) / 2f - curvature, rx, ry)
                        }
                        drawPath(path2, color = Color(0xFFE53935), style = Stroke(width = 1.5f))
                    } else {
                        val midX = (ax + bx) / 2f
                        val midY = (ay + by) / 2f
                        val dist = sqrt((bx - ax).pow(2) + (by - ay).pow(2))
                        val curvature = (dist * 0.25f).coerceAtMost(renderedHeight * 0.35f)
                        val path = Path().apply {
                            moveTo(ax, ay)
                            quadraticTo(midX, midY - curvature, bx, by)
                        }
                        drawPath(path, color = Color(0xFFE53935), style = Stroke(width = 1.5f))
                    }
                }

                // Stadtpunkte (über den Linien)
                allCities.forEach { city ->
                    val cx = xOffset + city.x_relativ * renderedWidth
                    val cy = yOffset + city.y_relativ * renderedHeight
                    val isOcean = cityOverOcean[city.id] == true

                    drawCircle(
                        color = Color(0xFFE53935),
                        radius = dotRadius,
                        center = Offset(cx, cy)
                    )

                    if (scale >= 2.5f) {
                        val labelLeft = city.x_relativ < 0.28f
                        val labelX = if (labelLeft) cx - dotRadius - 3f else cx + dotRadius + 3f

                        val paint = android.graphics.Paint().apply {
                            color = if (isOcean) android.graphics.Color.WHITE
                                    else android.graphics.Color.rgb(20, 20, 20)
                            textSize = 9f
                            isAntiAlias = true
                            textAlign = if (labelLeft) android.graphics.Paint.Align.RIGHT
                                        else android.graphics.Paint.Align.LEFT
                            setShadowLayer(1.5f, 0.5f, 0.5f,
                                if (isOcean) android.graphics.Color.BLACK
                                else android.graphics.Color.WHITE)
                        }

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(city.name, labelX, cy + 4f, paint)
                        }
                    }
                }
            }
        }
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

fun loadRawBitmap(context: Context, fileName: String): android.graphics.Bitmap? {
    return try {
        context.assets.open(fileName).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (_: Exception) {
        null
    }
}