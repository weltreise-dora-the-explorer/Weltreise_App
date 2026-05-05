package at.aau.serg.websocketbrokerdemo.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.withSave
import at.aau.serg.websocketbrokerdemo.AppViewModel
import at.aau.serg.websocketbrokerdemo.models.City
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PlayerAnimState {
    val animX = Animatable(0f)
    val animY = Animatable(0f)
    var isAnimating by mutableStateOf(false)
}

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
    val playerCurrentCities by viewModel.playerCurrentCities.collectAsState()
    val validMoveIds by viewModel.validMoveIds.collectAsState()
    val remainingSteps by viewModel.remainingSteps.collectAsState()
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
    val rawAvatars = remember {
        listOf(
            loadRawBitmap(context, "turtle_with_luggage_loginscreen.png"),
            loadRawBitmap(context, "avatar_duck.png"),
            loadRawBitmap(context, "avatar_bear.png"),
            loadRawBitmap(context, "avatar_pig.png")
        )
    }

    //Bucketlist offen? Default false
    val showBucketListDialog = remember { mutableStateOf(false) }

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
            ZoomableMap(
                mapBitmap = mapBitmap,
                rawBitmap = rawMapBitmap,
                allCities = allCities,
                ownedCities = ownedCities,
                playersList = playersList,
                playerCurrentCities = playerCurrentCities,
                rawAvatars = rawAvatars,
                validMoveIds = validMoveIds,
                isMyTurn = isMyTurn,
                myPlayerId = currentPlayerName,
                onCityClick = { cityId -> viewModel.onMoveToCity(cityId) }
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
                    diceValue = if (playerName == currentTurnPlayerId) diceValue else null,
                    remainingSteps = if (playerName == currentTurnPlayerId) remainingSteps else null
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
                    showBucketListDialog.value = true
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
    if (showBucketListDialog.value) {
        AlertDialog(
            onDismissRequest = { showBucketListDialog.value = false },
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
                TextButton(onClick = { showBucketListDialog.value = false }) {
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
    ownedCities: List<City> = emptyList(),
    playersList: List<String> = emptyList(),
    playerCurrentCities: Map<String, City?> = emptyMap(),
    rawAvatars: List<android.graphics.Bitmap?> = emptyList(),
    validMoveIds: List<String> = emptyList(),
    isMyTurn: Boolean = false,
    myPlayerId: String = "",
    onCityClick: (cityId: String) -> Unit = {}
) {
    val showValidMoves = validMoveIds.isNotEmpty()
    val validMoveIdSet = remember(validMoveIds) { validMoveIds.toHashSet() }
    val ownedCityIdSet = remember(ownedCities) { ownedCities.map { it.id }.toHashSet() }

    val infiniteTransition = rememberInfiniteTransition(label = "validMoves")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable<Float>(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val playerIconRadius = 16f

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Icon-Radius schrumpft mit Zoom, damit das Icon in Screen-Pixeln ~konstant bleibt
    val effectiveIconRadius = (playerIconRadius / scale).coerceIn(4f, playerIconRadius)

    // Bitmaps einmalig auf Maxgröße skalieren; beim Zeichnen per RectF dynamisch verkleinern
    val scaledAvatars = remember(rawAvatars) {
        val size = (playerIconRadius * 2).toInt()
        rawAvatars.map { bmp ->
            bmp?.scale(size, size)
        }
    }

    // Animationszustand für den lokalen Spieler (Pfadbewegung)
    val coroutineScope = rememberCoroutineScope()
    var isLocalPlayerAnimating by remember { mutableStateOf(false) }
    val localPlayerAnimX = remember { Animatable(0f) }
    val localPlayerAnimY = remember { Animatable(0f) }

    // Animationszustand für remote Spieler
    val remotePlayerAnims = remember { mutableStateMapOf<String, PlayerAnimState>() }
    val prevPlayerCities = remember { mutableStateOf<Map<String, City?>>(emptyMap()) }

    LaunchedEffect(playersList) {
        val current = playersList.toSet()
        val existing = remotePlayerAnims.keys.toSet()
        (current - existing).forEach { remotePlayerAnims[it] = PlayerAnimState() }
        (existing - current).forEach { remotePlayerAnims.remove(it) }
    }

    // Für jede Stadt vorausberechnen ob sie über Meer liegt
    val cityOverOcean = remember(allCities, rawBitmap) {
        if (rawBitmap == null) return@remember emptyMap<String, Boolean>()
        allCities.associate { city ->
            val px = (city.x_relativ * rawBitmap.width).toInt().coerceIn(0, rawBitmap.width - 1)
            val py = (city.y_relativ * rawBitmap.height).toInt().coerceIn(0, rawBitmap.height - 1)
            val pixel = rawBitmap[px, py]
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

        // Remote Spieler animieren wenn sich deren Position ändert
        LaunchedEffect(playerCurrentCities) {
            val prev = prevPlayerCities.value
            prevPlayerCities.value = playerCurrentCities
            val cityMap = allCities.associateBy { it.id }

            playerCurrentCities.forEach { (playerId, newCity) ->
                if (playerId == myPlayerId) return@forEach
                val oldCity = prev[playerId]
                if (newCity == null || oldCity == null || newCity.id == oldCity.id) return@forEach
                val path = findShortestPath(oldCity.id, newCity.id, cityMap)
                if (path.size <= 1) return@forEach
                val animState = remotePlayerAnims[playerId] ?: return@forEach
                launch {
                    try {
                        animState.isAnimating = true
                        val startC = cityMap[path.first()]
                        if (startC != null) {
                            animState.animX.snapTo(xOffset + startC.x_relativ * renderedWidth)
                            animState.animY.snapTo(yOffset + startC.y_relativ * renderedHeight)
                        }
                        for (stepId in path.drop(1)) {
                            val step = cityMap[stepId] ?: continue
                            val tx = xOffset + step.x_relativ * renderedWidth
                            val ty = yOffset + step.y_relativ * renderedHeight
                            val spec = tween<Float>(380, easing = FastOutSlowInEasing)
                            val jx = launch { animState.animX.animateTo(tx, spec) }
                            animState.animY.animateTo(ty, spec)
                            jx.join()
                        }
                    } finally {
                        animState.isAnimating = false
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
                .pointerInput(validMoveIdSet, isMyTurn) {
                    if (!isMyTurn) return@pointerInput
                    detectTapGestures { tapOffset ->
                        Log.d("CityTap", "tap received: $tapOffset")
                        if (isLocalPlayerAnimating) return@detectTapGestures
                        Log.d("CityTap", "isMyTurn=$isMyTurn, validMoveIds=$validMoveIdSet")
                        val canvasX = (tapOffset.x - offset.x - screenWidth / 2f) / scale + screenWidth / 2f
                        val canvasY = (tapOffset.y - offset.y - screenHeight / 2f) / scale + screenHeight / 2f
                        val hitRadius = 50f / scale

                        var closestCity: City? = null
                        var closestDist = Float.MAX_VALUE
                        allCities.filter { it.id in validMoveIdSet }.forEach { city ->
                            val cx = xOffset + city.x_relativ * renderedWidth
                            val cy = yOffset + city.y_relativ * renderedHeight
                            val dist = sqrt((canvasX - cx).pow(2) + (canvasY - cy).pow(2))
                            if (dist < closestDist) { closestDist = dist; closestCity = city }
                        }

                        Log.d("CityTap", "closestCity=${closestCity?.id}, dist=$closestDist, hitRadius=$hitRadius")
                        if (closestDist <= hitRadius) closestCity?.let { targetCity ->
                            Log.d("CityTap", "calling onCityClick: ${targetCity.id}")
                            coroutineScope.launch {
                                val cityMap = allCities.associateBy { it.id }
                                val fromId = playerCurrentCities[myPlayerId]?.id
                                val path = if (fromId != null && fromId != targetCity.id)
                                    findShortestPath(fromId, targetCity.id, cityMap)
                                else listOf(targetCity.id)

                                if (path.size > 1) {
                                    try {
                                        isLocalPlayerAnimating = true
                                        val startCity = cityMap[path.first()]
                                        if (startCity != null) {
                                            localPlayerAnimX.snapTo(xOffset + startCity.x_relativ * renderedWidth)
                                            localPlayerAnimY.snapTo(yOffset + startCity.y_relativ * renderedHeight)
                                        }
                                        for (stepId in path.drop(1)) {
                                            val step = cityMap[stepId] ?: continue
                                            val tx = xOffset + step.x_relativ * renderedWidth
                                            val ty = yOffset + step.y_relativ * renderedHeight
                                            val spec = tween<Float>(380, easing = FastOutSlowInEasing)
                                            val jx = launch { localPlayerAnimX.animateTo(tx, spec) }
                                            localPlayerAnimY.animateTo(ty, spec)
                                            jx.join()
                                        }
                                    } finally {
                                        isLocalPlayerAnimating = false
                                    }
                                }
                                onCityClick(targetCity.id)
                            }
                        }
                    }
                }
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
                    val isValidMove = showValidMoves && city.id in validMoveIdSet
                    val isOwned = city.id in ownedCityIdSet

                    // Blink-Effekt für erreichbare Städte
                    if (isValidMove) {
                        // Äußerer goldener Glow
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = blinkAlpha * 0.45f),
                            radius = dotRadius * 3.8f,
                            center = Offset(cx, cy)
                        )
                        // Goldener Rand
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = blinkAlpha),
                            radius = dotRadius + 5f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5f)
                        )
                    }

                    drawCircle(
                        color = when {
                            isOwned -> Color(0xFF4CAF50)
                            showValidMoves && !isValidMove -> Color(0xFFE53935).copy(alpha = 0.25f)
                            else -> Color(0xFFE53935)
                        },
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

                // Spieler-Icons auf der Karte
                val cityById = allCities.associateBy { it.id }
                val cityByName = allCities.associateBy { it.name }
                val myPlayerIndex = playersList.indexOf(myPlayerId)

                // Spieler nach Stadt gruppieren; animierende Spieler werden separat gezeichnet
                val cityGroups = mutableMapOf<String, MutableList<Int>>()
                playersList.forEachIndexed { index, name ->
                    if (isLocalPlayerAnimating && index == myPlayerIndex) return@forEachIndexed
                    if (remotePlayerAnims[name]?.isAnimating == true) return@forEachIndexed
                    val current = playerCurrentCities[name] ?: return@forEachIndexed
                    val key = current.id.ifEmpty { current.name }
                    cityGroups.getOrPut(key) { mutableListOf() }.add(index)
                }

                fun drawPlayerIcon(nativeCanvas: android.graphics.Canvas, cx: Float, cy: Float, playerIndex: Int) {
                    val avatarBmp = scaledAvatars.getOrNull(playerIndex % scaledAvatars.size)
                    val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; isAntiAlias = true }
                    nativeCanvas.drawCircle(cx, cy, effectiveIconRadius + 2f, bgPaint)
                    nativeCanvas.withSave {
                        val clip = android.graphics.Path()
                        clip.addCircle(cx, cy, effectiveIconRadius, android.graphics.Path.Direction.CW)
                        clipPath(clip)
                        if (avatarBmp != null) {
                            val dstRect = android.graphics.RectF(cx - effectiveIconRadius, cy - effectiveIconRadius, cx + effectiveIconRadius, cy + effectiveIconRadius)
                            drawBitmap(avatarBmp, null, dstRect, null)
                        } else {
                            val fp = android.graphics.Paint().apply { color = android.graphics.Color.rgb(100, 100, 200); isAntiAlias = true }
                            drawCircle(cx, cy, effectiveIconRadius, fp)
                        }
                    }
                    val borderPaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; style = android.graphics.Paint.Style.STROKE; strokeWidth = 2.5f; isAntiAlias = true }
                    nativeCanvas.drawCircle(cx, cy, effectiveIconRadius, borderPaint)
                }

                cityGroups.forEach { (cityKey, indices) ->
                    val cityData = cityById[cityKey] ?: cityByName[cityKey] ?: return@forEach
                    val baseCx = xOffset + cityData.x_relativ * renderedWidth
                    val baseCy = yOffset + cityData.y_relativ * renderedHeight
                    val step = effectiveIconRadius * 2.4f
                    val totalWidth = step * (indices.size - 1)
                    indices.forEachIndexed { pos, playerIndex ->
                        val iconCx = baseCx - totalWidth / 2f + pos * step
                        val iconCy = baseCy - effectiveIconRadius - dotRadius - 4f
                        drawIntoCanvas { canvas -> drawPlayerIcon(canvas.nativeCanvas, iconCx, iconCy, playerIndex) }
                    }
                }

                // Animierender lokaler Spieler wird über alle anderen gezeichnet
                if (isLocalPlayerAnimating && myPlayerIndex >= 0) {
                    val animX = localPlayerAnimX.value
                    val animY = localPlayerAnimY.value - effectiveIconRadius - dotRadius - 4f
                    drawIntoCanvas { canvas -> drawPlayerIcon(canvas.nativeCanvas, animX, animY, myPlayerIndex) }
                }

                // Animierende remote Spieler werden ebenfalls über alle anderen gezeichnet
                remotePlayerAnims.forEach { (playerId, animState) ->
                    if (!animState.isAnimating) return@forEach
                    val playerIndex = playersList.indexOf(playerId)
                    if (playerIndex < 0) return@forEach
                    val animX = animState.animX.value
                    val animY = animState.animY.value - effectiveIconRadius - dotRadius - 4f
                    drawIntoCanvas { canvas -> drawPlayerIcon(canvas.nativeCanvas, animX, animY, playerIndex) }
                }
            }
        }
    }
}

//Hilfe damit App nicht abstürzt (bsp. derzeit noch fehlende Bilder)
@Composable
fun PlayerCard(name: String, bucketListCount: Int, avatar: ImageBitmap?, isActive: Boolean, diceValue: Int? = null, remainingSteps: Int? = null) {
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
                    val stepsLabel = if (remainingSteps != null && remainingSteps != diceValue)
                        "🎲$diceValue →$remainingSteps" else "🎲$diceValue"
                    Text(text = stepsLabel, fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
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

/** BFS: kürzester Pfad von [fromId] nach [toId] durch Zug- und Flugverbindungen. */
fun findShortestPath(fromId: String, toId: String, cityMap: Map<String, City>): List<String> {
    if (fromId == toId) return listOf(fromId)
    val queue = ArrayDeque<List<String>>()
    queue.add(listOf(fromId))
    val visited = mutableSetOf(fromId)
    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val current = cityMap[path.last()] ?: continue
        for (neighborId in current.trainConnections + current.flightConnections) {
            if (neighborId !in visited) {
                val newPath = path + neighborId
                if (neighborId == toId) return newPath
                visited.add(neighborId)
                queue.add(newPath)
            }
        }
    }
    return listOf(fromId, toId)
}