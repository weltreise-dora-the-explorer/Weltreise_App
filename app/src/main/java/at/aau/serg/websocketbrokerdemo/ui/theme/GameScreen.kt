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
import androidx.compose.runtime.*
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

@Composable
fun GameScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val playersList by viewModel.playersList.collectAsState()
    val currentPlayerName by viewModel.playerName.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()

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
            modifier = androidx.compose.ui.Modifier
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
                    bucketListCount = 8, // TODO: Später vom Server holen
                    avatar = avatar,
                    isActive = playerName == currentPlayerName
                )
            }
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
                onClick = {
                    println("WÜRFEL WURDE GEKLICKT!")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                //vertikales Scrollen
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Your Destinations:") //Beispiel STädte die für Sprint 1 nicht existieren!
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📍 Hometown: Berlin", fontWeight = FontWeight.Bold)
                    Text("Paris")
                    Text("Tokyo")
                    Text("New York")
                    Text("Sydney")
                    Text("Kapstadt")
                    Text("Rio de Janeiro")
                    Text("Moskau")
                    Text("Peking")
                    Text("Kairo")
                    Text("Rom")
                    Text("London")
                    Text("Los Angeles")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "wird später automatisch befüllt", fontSize = 12.sp, color = Color.Gray)
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

    Image(
        bitmap = mapBitmap,
        contentDescription = "Weltkarte",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)

                    if(scale > 1f) {
                        offset += pan
                    }else {
                        offset = Offset.Zero
                    }
                }
            }
    )
}

//Hilfe damit App nicht abstürzt (bsp. derzeit noch fehlende Bilder)
@Composable
fun PlayerCard(name: String, bucketListCount: Int, avatar: ImageBitmap?, isActive: Boolean) {
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
            Text(text = name, fontSize = 12.sp, color = Color(0xFF1E56A0), fontWeight = FontWeight.Bold)
            Text(text = "Bucket List: $bucketListCount", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun GameButton(text: String, imageBitmap: ImageBitmap?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
    } catch (e: Exception) {
        null
    }
}