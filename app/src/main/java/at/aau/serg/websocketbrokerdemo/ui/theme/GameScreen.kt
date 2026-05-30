package at.aau.serg.websocketbrokerdemo.ui.theme

import android.content.Context
import at.aau.serg.websocketbrokerdemo.models.Continent
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
import android.annotation.SuppressLint
import android.media.MediaPlayer
import at.aau.serg.websocketbrokerdemo.AppViewModel
import at.aau.serg.websocketbrokerdemo.models.City
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import at.aau.serg.websocketbrokerdemo.GameConstants
import at.aau.serg.websocketbrokerdemo.NewDestinationMessage

class PlayerAnimState {
    val animX = Animatable(0f)
    val animY = Animatable(0f)
    var isAnimating by mutableStateOf(false)
}

@SuppressLint("DiscouragedApi")
private fun rawId(context: Context, name: String) =
    context.resources.getIdentifier(name, "raw", context.packageName)

private fun playSound(context: Context, name: String, volume: Float = 1.0f) {
    val id = rawId(context, name)
    if (id == 0) return
    MediaPlayer.create(context, id)?.apply {
        setVolume(volume, volume)
        setOnCompletionListener { release() }
        start()
    }
}

@Composable
fun GameScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val playersList by viewModel.playersList.collectAsState()
    val currentPlayerName by viewModel.playerName.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val diceValue by viewModel.diceValue.collectAsState()
    val currentTurnPlayerId by viewModel.currentTurnPlayerId.collectAsState()
    val gamePhase by viewModel.gamePhase.collectAsState()
    val minigameWinnerPlayerId by viewModel.minigameWinnerPlayerId.collectAsState()
    val ownedCities by viewModel.ownedCities.collectAsState()
    val allCities by viewModel.allCities.collectAsState()
    val startCity by viewModel.startCity.collectAsState()
    val playerCityCounts by viewModel.playerCityCounts.collectAsState()
    val allPlayerOwnedCities by viewModel.allPlayerOwnedCities.collectAsState()
    val playerCurrentCities by viewModel.playerCurrentCities.collectAsState()
    var highlightedPlayerId by remember { mutableStateOf<String?>(null) }
    val playerVisitedBucketIds = remember { mutableStateMapOf<String, MutableSet<String>>() }
    LaunchedEffect(playerCurrentCities, allPlayerOwnedCities) {
        playerCurrentCities.forEach { (playerId, city) ->
            if (city != null) {
                val bucketIds = allPlayerOwnedCities[playerId]?.map { it.id }?.toHashSet() ?: return@forEach
                if (city.id in bucketIds) {
                    playerVisitedBucketIds.getOrPut(playerId) { mutableSetOf() }.add(city.id)
                }
            }
        }
    }
    val validMoveIds by viewModel.validMoveIds.collectAsState()
    val remainingSteps by viewModel.remainingSteps.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val freePassCount by viewModel.freePassCount.collectAsState()
    val goalReachedMessage by viewModel.goalReachedMessage.collectAsState()
    val lastConqueredCityId = remember(goalReachedMessage) {
        goalReachedMessage?.let { msg ->
            if (msg.playerName == currentPlayerName) {
                allCities.find { it.name == msg.cityName }?.id
            } else null
        }
    }
    val newDestinationMessage by viewModel.newDestinationMessage.collectAsState()
    val gameOverMessage by viewModel.gameOverMessage.collectAsState()
    val isMinigamePhase = gamePhase == GameConstants.PHASE_MINIGAME
    val isMyTurn = currentTurnPlayerId == currentPlayerName
    val myCurrentCity = playerCurrentCities[currentPlayerName]
    val isStandingOnOwnTargetCity = myCurrentCity != null && ownedCities.any {it.id == myCurrentCity.id}

    val freePassDecisionMade = remember { mutableStateOf(false) }

    val shouldShowFreePassDecision =
        freePassCount > 0 &&
                isMyTurn &&
                !isGameOver &&
                isStandingOnOwnTargetCity &&
                !freePassDecisionMade.value

    val effectiveIsMyTurn = isMyTurn && !isGameOver && !isMinigamePhase
    val canRoll = effectiveIsMyTurn && diceValue == null
    val canEndTurn = effectiveIsMyTurn && diceValue != null
    val canFinishMinigame = true
    val minigameTargetPlayer = currentTurnPlayerId ?: currentPlayerName
    val minigameLostCityName by viewModel.minigameLostCityName.collectAsState()
    val minigameNewCityName by viewModel.minigameNewCityName.collectAsState()
    val playerFreePassCounts by viewModel.playerFreePassCounts.collectAsState()


    // Hintergrundmusik – läuft solange GameScreen aktiv ist
    DisposableEffect(Unit) {
        val id = rawId(context, "backgroundmusic")
        val player = if (id != 0) MediaPlayer.create(context, id)?.apply {
            isLooping = true
            setVolume(0.1875f, 0.1875f)
            start()
        } else null
        onDispose { player?.stop(); player?.release() }
    }

    // City-reached Sound – nur für eigenen Spieler, nur bei Bucket-List-Städten
    val ownedCityIdSet = remember(ownedCities) { ownedCities.map { it.id }.toHashSet() }
    val soundCityInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(playerCurrentCities[currentPlayerName]) {
        if (!soundCityInitialized.value) { soundCityInitialized.value = true; return@LaunchedEffect }
        val arrived = playerCurrentCities[currentPlayerName]
        if (arrived != null && arrived.id in ownedCityIdSet) {
            playSound(context, "city_reached")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllCities(context)
    }

    //Bilder
    val mapBitmap = loadAssetBitmap(context, "world_map.png")
    val rawMapBitmap = remember { loadRawBitmap(context, "world_map.png") }
    val diceBitmap = loadAssetBitmap(context, "dice_icon.png")
    val bucketBitmap = loadAssetBitmap(context, "bucket_list_icon.png")
    val freePassBitmap = loadAssetBitmap(context, "freepassneu.png")

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

    val minigameTargetAvatar = avatars.getOrNull(
        playersList.indexOf(minigameTargetPlayer).takeIf {it >= 0} ?: 0
    )

    //Bucketlist offen? Default false
    val showBucketListDialog = remember { mutableStateOf(false) }
    val showFreePassDialog = remember {mutableStateOf(false)}

    LaunchedEffect(myCurrentCity?.id) {
        freePassDecisionMade.value = false
    }

    LaunchedEffect(shouldShowFreePassDecision, myCurrentCity?.id) {
        if(shouldShowFreePassDecision) {
            showFreePassDialog.value = true
        }
    }

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

    // Goal-Reached fade-out nach 4 Sekunden
    var showGoalReachedOverlay by remember { mutableStateOf(false) }
    val goalReachedAlpha = remember { Animatable(0f) }
    LaunchedEffect(goalReachedMessage) {
        if (goalReachedMessage != null) {
            showGoalReachedOverlay = true
            goalReachedAlpha.snapTo(1f)
            delay(3000)
            goalReachedAlpha.animateTo(0f, animationSpec = tween(1000))
            showGoalReachedOverlay = false
        }
    }

    //New-Destination fade-out nach 4 Sekunden
    var showNewDestinationOverlay by remember {mutableStateOf(false)}
    val newDestinationAlpha = remember {Animatable(0f)}

    LaunchedEffect(newDestinationMessage) {
        if(newDestinationMessage != null) {
            showNewDestinationOverlay = true
            newDestinationAlpha.snapTo(1f)
        }
    }

    LaunchedEffect(minigameLostCityName, minigameNewCityName) {
        if(minigameLostCityName != null && minigameNewCityName != null) {
            showNewDestinationOverlay = true
            newDestinationAlpha.snapTo(1f)
        }
    }

    var showMinigameOverlay by remember {mutableStateOf(false)}

    LaunchedEffect(gamePhase) {
        if(gamePhase == GameConstants.PHASE_MINIGAME) {
            showMinigameOverlay = false
            delay(4000)
            showMinigameOverlay = true
        } else {
            showMinigameOverlay = false
        }
    }

    // Box (Schichten-Design)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003366)) // Tiefblauer Hintergrund
    ) {

        // Weltkarte
        val highlightedCityIds = remember(highlightedPlayerId, allPlayerOwnedCities) {
            highlightedPlayerId?.let { allPlayerOwnedCities[it]?.map { c -> c.id }?.toHashSet() } ?: emptySet()
        }
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
                isMyTurn = effectiveIsMyTurn,
                myPlayerId = currentPlayerName,
                startCityId = startCity?.id,
                highlightedCityIds = highlightedCityIds,
                highlightedVisitedCityIds = playerVisitedBucketIds[highlightedPlayerId] ?: emptySet(),
                optimisticLocalCity = viewModel.optimisticPlayerCity.collectAsState().value,
                onOptimisticMove = { city -> viewModel.setOptimisticPlayerCity(city) },
                lastConqueredCityId = lastConqueredCityId,
                onCityClick = { cityId -> viewModel.onMoveToCity(cityId) }
            )
        }

        if(gamePhase == GameConstants.PHASE_MINIGAME && showMinigameOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(20f)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ){
                MinigameOverlay(
                    targetPlayerName = minigameTargetPlayer,
                    opponentPlayerNames = playersList.filter { it != minigameTargetPlayer },
                    targetCityName = playerCurrentCities[minigameTargetPlayer]?.name ?: "",
                    targetPlayerAvatar = minigameTargetAvatar,
                    opponentPlayerAvatars = playersList.filter {it != minigameTargetPlayer}.map {opponentName -> avatars.getOrNull(playersList.indexOf(opponentName))},
                    announcedWinnerPlayerId = minigameWinnerPlayerId,
                    canFinishMinigame = canFinishMinigame,
                    onAnnounceMinigameResult = { winnerPlayerId ->
                        viewModel.announceMinigameResult(winnerPlayerId)
                    },
                    onFinishMinigame = { winnerPlayerId ->
                        viewModel.finishMinigame(winnerPlayerId)
                    }
                )
            }
        }

        //Free-Pass-Overlay
        if (showFreePassDialog.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(25f)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xDD000000), RoundedCornerShape(20.dp))
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.free_pass_dialog_title),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(
                                R.string.free_pass_dialog_city_text,
                                myCurrentCity?.name ?: "your target city"
                            ),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.free_pass_dialog_question),
                            color = Color.White,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                freePassDecisionMade.value = true
                                showFreePassDialog.value = false
                                viewModel.useFreePass()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD4AF37)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.free_pass_use_button),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                freePassDecisionMade.value = true
                                showFreePassDialog.value = false

                                if (!isMinigamePhase) {
                                    viewModel.startMinigame()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8DB6CD)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.free_pass_play_minigame_button),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
            val disconnectedPlayers by viewModel.disconnectedPlayers.collectAsState()
            val mustSkipPlayers by viewModel.mustSkipPlayers.collectAsState()
            var reportingPlayer by remember { mutableStateOf<String?>(null) }
            playersList.forEachIndexed { index, playerName ->
                val avatar = avatars.getOrNull(index % avatars.size)
                val isFirstPlayer = index == 0
                val displayName = if (isFirstPlayer) "$playerName (Host)" else playerName
                val isOtherPlayer = playerName != currentPlayerName
                val isHighlighted = highlightedPlayerId == playerName
                val mustSkip = playerName in mustSkipPlayers
                val canBeReported = isOtherPlayer
                        && gamePhase != GameConstants.PHASE_LOBBY
                        && playerName == currentTurnPlayerId
                        && diceValue != null
                        && playerName !in disconnectedPlayers
                        && !mustSkip
                PlayerCard(
                    name = displayName,
                    bucketListCount = playerCityCounts[playerName] ?: 0,
                    avatar = avatar,
                    isActive = playerName == currentTurnPlayerId,
                    diceValue = if (playerName == currentTurnPlayerId) diceValue else null,
                    remainingSteps = if (playerName == currentTurnPlayerId) remainingSteps else null,
                    disconnected = playerName in disconnectedPlayers,
                    freePassCount = playerFreePassCounts[playerName] ?: 0,
                    freePassIcon = freePassBitmap,
                    isHighlighted = isHighlighted,
                    mustSkip = mustSkip,
                    canBeReported = canBeReported,
                    onReport = if (canBeReported) { { reportingPlayer = playerName } } else null,
                    onTap = if (isOtherPlayer) {
                        { highlightedPlayerId = if (isHighlighted) null else playerName }
                    } else null
                )
            }

            reportingPlayer?.let { target ->
                AlertDialog(
                    onDismissRequest = { reportingPlayer = null },
                    title = { Text("Report cheating") },
                    text = {
                        Text("Report $target for cheating? If you are wrong, you skip your next turn.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.reportCheat(target)
                            reportingPlayer = null
                        }) { Text("Report") }
                    },
                    dismissButton = {
                        TextButton(onClick = { reportingPlayer = null }) { Text("Cancel") }
                    }
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

        // Goal-Reached Popup – für alle sichtbar, verschwindet nach 4s
        if (showGoalReachedOverlay && goalReachedMessage != null) {
            val msg = goalReachedMessage!!
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(goalReachedAlpha.value)
                    .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${msg.playerName} hat ${msg.cityName} erreicht!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "(${msg.reached}/${msg.total})",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }

        //New Destination Popup -sichtbar nach verlorenem Minigame
        if(showNewDestinationOverlay && (newDestinationMessage != null || (minigameLostCityName != null && minigameNewCityName != null))) {
            val msg = newDestinationMessage ?: NewDestinationMessage(
                playerName = minigameTargetPlayer,
                lostCityName = minigameLostCityName ?: "",
                newCityName = minigameNewCityName ?: ""
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(newDestinationAlpha.value)
                    .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                    .padding(start = 24.dp, end = 32.dp, top = 20.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(350.dp)
                            .background(Color(0xDDEAF2F8), RoundedCornerShape(14.dp))
                            .padding(horizontal = 28.dp, vertical = 24.dp)
                    ) {
                        Column{
                            Text(
                                text = "VISA DENIED!",
                                color = Color(0xFF0050A8),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Attention ${msg.playerName},",
                                color = Color(0xFF0050A8),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Entry completely denied! Your mini-game skills were inspected and found to be highly insufficient.",
                                color = Color(0xFF0050A8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "You lost ${msg.lostCityName}. Since you are officially locked out of this city, your Bucket List has been changed.",
                                color = Color(0xFF0050A8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Time to pull out the map, grab a pen, and figure out a new route.",
                                color = Color(0xFF0050A8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Cordially,\nThe Department of Detours",
                                color = Color(0xFF0050A8),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NEW DESTINATION:",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(190.dp)
                                .background(Color(0xFF0050A8), RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFD4AF37),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg.newCityName.uppercase(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if(currentPlayerName == minigameTargetPlayer) {
                            Button(
                                onClick = {
                                    showNewDestinationOverlay = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8DB6CD)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .width(170.dp)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Accept",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            LaunchedEffect(showNewDestinationOverlay) {
                                if(showNewDestinationOverlay) {
                                    delay(3000)
                                    showNewDestinationOverlay = false
                                }
                            }
                        }
                    }
                }
            }
        }

        // Game-Over Popup – provisorisch, persistent, kein Schließen-Button
        if (isGameOver) {
            val winnerName = gameOverMessage?.results?.maxByOrNull { it.score }?.playerName
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(10f)
                    .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                    .padding(horizontal = 40.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (winnerName != null) {
                        Text(
                            text = "$winnerName hat gewonnen!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "Spiel beendet!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        //Actionbuttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Roll Dice – nur sichtbar wenn dran und noch nicht gewürfelt
            if (canRoll) {
                GameButton(
                    text = "ROLL DICE",
                    imageBitmap = diceBitmap,
                    enabled = true,
                    blinkBorder = true,
                    onClick = { playSound(context, "rolling_dice"); viewModel.onRollDice() }
                )

                Spacer(modifier = Modifier.height(1.dp))
            }

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
    startCityId: String? = null,
    highlightedCityIds: Set<String> = emptySet(),
    highlightedVisitedCityIds: Set<String> = emptySet(),
    optimisticLocalCity: City? = null,
    onOptimisticMove: (City) -> Unit = {},
    lastConqueredCityId: String? = null,
    onCityClick: (cityId: String) -> Unit = {}
) {
    val showValidMoves = validMoveIds.isNotEmpty() && isMyTurn
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
    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)

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

    // Grüner Haken: animiert wenn lokaler Spieler eine Zielstadt durch Minispiel oder Free Pass erobert hat
    val checkmarkProgress = remember { mutableStateMapOf<String, Animatable<Float, *>>() }

    LaunchedEffect(lastConqueredCityId) {
        if (lastConqueredCityId != null) {
            val anim = Animatable(0f)
            checkmarkProgress[lastConqueredCityId] = anim
            launch { anim.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing)) }
        }
    }

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
                .pointerInput(validMoveIdSet, isMyTurn) {
                    if (!isMyTurn) return@pointerInput
                    detectTapGestures { tapOffset ->
                        Log.d("CityTap", "tap received: $tapOffset")
                        val dbgCityWorld = allCities.firstOrNull()?.let {
                            Offset(xOffset + it.x_relativ * renderedWidth, yOffset + it.y_relativ * renderedHeight)
                        }
                        Log.d("CityTap", "tapOffset=$tapOffset  scale=$currentScale  firstCityWorld=$dbgCityWorld")
                        Log.d("CityTap", "scale=$currentScale offset=$currentOffset")
                        if (isLocalPlayerAnimating) return@detectTapGestures
                        Log.d("CityTap", "isMyTurn=$isMyTurn, validMoveIds=$validMoveIdSet")
                        val canvasX = (tapOffset.x - currentOffset.x - screenWidth / 2f) / currentScale + screenWidth / 2f
                        val canvasY = (tapOffset.y - currentOffset.y - screenHeight / 2f) / currentScale + screenHeight / 2f
                        val hitRadius = 60f / currentScale

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
                                        onOptimisticMove(targetCity)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
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
                        val (cpX, cpY) = when {
                            idA == "miami" && idB == "newyork" -> midX - curvature to midY
                            idA == "newyork" && idB == "sanfrancisco" -> midX to midY - curvature * 2.2f
                            idA == "lima" && idB == "losangeles" -> midX - curvature * 1.5f to midY
                            idA == "bangkok" && idB == "nairobi" -> midX to midY + curvature * 1.5f
                            idA == "bombay" && idB == "dubayy" -> midX to midY + curvature
                            idA == "capetown" && idB == "nairobi" -> midX to midY
                            idA == "nairobi" && idB == "paris" -> midX - curvature to midY
                            idA == "dakar" && idB == "lisboa" -> midX to midY
                            idA == "laspalmas" && idB == "madrid" -> midX to midY + curvature
                            else -> midX to midY - curvature
                        }
                        val path = Path().apply {
                            moveTo(ax, ay)
                            quadraticTo(cpX, cpY, bx, by)
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

                    if (city.id == startCityId) {
                        drawCircle(
                            color = Color(0xFF1565C0),
                            radius = dotRadius + 7f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFF1565C0),
                            radius = dotRadius + 4f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f)
                        )
                    }

                    if (city.id in highlightedCityIds) {
                        drawCircle(
                            color = Color.Black,
                            radius = dotRadius + 7f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 6f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = dotRadius + 7f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.5f)
                        )
                    }

                    drawCircle(
                        color = when {
                            isOwned -> Color(0xFFFFD600)
                            showValidMoves && !isValidMove -> Color(0xFFE53935).copy(alpha = 0.25f)
                            else -> Color(0xFFE53935)
                        },
                        radius = dotRadius,
                        center = Offset(cx, cy)
                    )

                    val importantEuropeCities = setOf("lisboa", "madrid", "palermo", "paris", "frankfurt", "wien", "london", "dublin", "roma")
                    val isMinorEuropean = city.continent == Continent.EUROPE_AFRICA
                        && city.y_relativ < 0.422f
                        && city.id !in importantEuropeCities
                    val labelThreshold = if (isMinorEuropean) 4.0f else 2.5f
                    if (scale >= labelThreshold) {
                        val labelAbove = city.id in setOf("saltlakecity", "calgary", "winnipeg", "manaus", "dakar", "lobito", "london", "kobenhaven", "berlin", "paris", "bern", "frankfurt", "hamburg", "bergen", "stockholm", "perm", "sverdlovsk", "novosibirsk", "irkutsk")
                        val labelBelow = city.id in setOf("denver", "stlouis", "bamako", "kuwait")
                        val labelBelowCenter = city.id == "wien" || city.id == "sofiya"
                        val labelLeft = !labelAbove && !labelBelow && !labelBelowCenter && (city.x_relativ < 0.28f || city.id in setOf("bordeaux", "brest", "dublin", "edinburgh", "oslo"))
                        val labelX = when {
                            labelAbove || labelBelowCenter -> cx
                            labelLeft -> cx - dotRadius - 3f
                            else -> cx + dotRadius + 3f
                        }
                        val labelY = when {
                            labelAbove -> cy - dotRadius - 3f
                            labelBelow || labelBelowCenter -> cy + 14f
                            else -> cy + 4f
                        }
                        val labelSize = when (city.id) {
                            "hamburg", "bern" -> 6f
                            "frankfurt" -> 7f
                            else -> if (isMinorEuropean) 7f else 9f
                        }

                        val paint = android.graphics.Paint().apply {
                            color = if (isOcean) android.graphics.Color.WHITE
                                    else android.graphics.Color.rgb(20, 20, 20)
                            textSize = labelSize
                            isAntiAlias = true
                            textAlign = when {
                                labelAbove || labelBelowCenter -> android.graphics.Paint.Align.CENTER
                                labelLeft -> android.graphics.Paint.Align.RIGHT
                                else -> android.graphics.Paint.Align.LEFT
                            }
                            setShadowLayer(1.5f, 0.5f, 0.5f,
                                if (isOcean) android.graphics.Color.BLACK
                                else android.graphics.Color.WHITE)
                        }

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(city.name, labelX, labelY, paint)
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
                    val current = if (name == myPlayerId && !isLocalPlayerAnimating)
                        optimisticLocalCity ?: playerCurrentCities[name] ?: return@forEachIndexed
                    else
                        playerCurrentCities[name] ?: return@forEachIndexed
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

                // Grüner Haken (oberste Ebene, über Spieler-Icons)
                allCities.forEach { city ->
                    val cx = xOffset + city.x_relativ * renderedWidth
                    val cy = yOffset + city.y_relativ * renderedHeight
                    val s = dotRadius * 1.6f
                    val checkAlpha = checkmarkProgress[city.id]?.value ?: 0f
                    val isOtherVisited = city.id in highlightedVisitedCityIds
                    if (checkAlpha > 0f || isOtherVisited) {
                        val alpha = if (checkAlpha > 0f) checkAlpha else 1f
                        val checkPath = Path().apply {
                            moveTo(cx - s, cy)
                            lineTo(cx - s * 0.15f, cy + s * 0.75f)
                            lineTo(cx + s, cy - s * 0.75f)
                        }
                        drawPath(
                            path = checkPath,
                            color = Color(0xFF2E7D32).copy(alpha = alpha),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
            } // inner graphicsLayer Box
        }
    }
}

//Hilfe damit App nicht abstürzt (bsp. derzeit noch fehlende Bilder)
@Composable
fun PlayerCard(
    name: String,
    bucketListCount: Int,
    avatar: ImageBitmap?,
    isActive: Boolean,
    diceValue: Int? = null,
    remainingSteps: Int? = null,
    disconnected: Boolean = false,
    freePassCount: Int = 0,
    freePassIcon: ImageBitmap? = null,
    isHighlighted: Boolean = false,
    mustSkip: Boolean = false,
    canBeReported: Boolean = false,
    onReport: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(50.dp)
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .zIndex(1f)
        ) {
            if (avatar != null) {
                Image(
                    bitmap = avatar,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = if (disconnected) 0.4f else 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .offset(x = (-15).dp)
                .background(
                    color = Color.White.copy(alpha = if (disconnected) 0.45f else 0.85f),
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .border(
                    width = if (isHighlighted) 2.dp else if (isActive) 3.dp else 0.dp,
                    color = when {
                        isHighlighted -> Color.White
                        isActive -> Color(0xFFD4AF37)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, fontSize = 12.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Bold)
                if(freePassCount > 0 && freePassIcon != null) {
                    Spacer(modifier = Modifier.width(5.dp))

                    repeat(minOf(freePassCount, 3)) {
                        Image(
                            bitmap = freePassIcon,
                            contentDescription = "Free Pass",
                            modifier = Modifier.size(18.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if(freePassCount > 3) {
                        Text(
                            text = "+${freePassCount - 3}",
                            fontSize = 10.sp,
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (diceValue != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val stepsLabel = if (remainingSteps != null && remainingSteps != diceValue)
                        "🎲$diceValue →$remainingSteps" else "🎲$diceValue"
                    Text(text = stepsLabel, fontSize = 11.sp, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                }
            }
            when {
                disconnected -> Text(
                    text = "(reconnecting)",
                    fontSize = 9.sp,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                mustSkip -> Text(
                    text = "skip turn",
                    fontSize = 10.sp,
                    color = Color(0xFFC0392B),
                    fontWeight = FontWeight.Bold
                )
                else -> Text(text = "Bucket List: $bucketListCount", fontSize = 10.sp, color = Color.Gray)
            }
        }

        if (canBeReported && onReport != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable { onReport() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚨", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun GameButton(text: String, imageBitmap: ImageBitmap?, onClick: () -> Unit, enabled: Boolean = true, blinkBorder: Boolean = false) {
    val bgColor = if (enabled) Color.White.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.4f)
    val infiniteTransition = rememberInfiniteTransition(label = "rollDiceBorder")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )
    val borderModifier = if (blinkBorder)
        Modifier.border(4.dp, Color(0xFF43A047).copy(alpha = borderAlpha), RoundedCornerShape(16.dp))
    else Modifier
    Box(
        modifier = Modifier
            .size(120.dp)
            .then(borderModifier)
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