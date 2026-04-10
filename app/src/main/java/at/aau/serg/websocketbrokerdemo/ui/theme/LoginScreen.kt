package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme


@Composable
fun LoginScreen(onHostClick: () -> Unit = {}, onJoinClick: () -> Unit = {}
){
    var playerName by remember { mutableStateOf("")}
    val isJoining = remember {mutableStateOf(false)}

    //Hintergrund
    Box(modifier = Modifier
        .fillMaxSize().background(Brush.linearGradient(colors=listOf(Color(0xFF003058), Color(0xFFFDCB61))
    ))
    ){
        //Schildkröte
      // Image(painter = painterResource(id = at.aau.serg.websocketbrokerdemo.R.drawable.turtle_with_luggage_loginscreen), contentDescription = "Schildkröte mit Koffer", modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp).fillMaxHeight(0.7f), //70% der Bildschirmhöhe
    //contentScale = ContentScale.Fit)

    //Text & Eingabe
   Column(modifier = Modifier
       .align(Alignment.CenterEnd)
       .padding(end = 64.dp)
       .width(400.dp),
       horizontalAlignment = Alignment.CenterHorizontally)
   {
       //Überschrift
       Text(text = "Ready for your\nJourney?",
           style = MaterialTheme.typography.displayMedium,
           color = Color.White,
           fontWeight = FontWeight.Bold,
           lineHeight = 44.sp
       )
       Spacer(modifier = Modifier.height(48.dp))

       //Nickname Eingabefeld
       Text(text= "NICKNAME",
           color = Color.White.copy(alpha=0.8f),
           modifier = Modifier.align(Alignment.Start)
       )
       Spacer(modifier = Modifier.height(8.dp))
       OutlinedTextField(
           value = playerName,
           onValueChange = {playerName = it },
           placeholder = { Text("DoraTheExplorer")},
           modifier = Modifier.fillMaxWidth(),
           singleLine = true,
           colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White,
               unfocusedTextColor = Color.White,
           focusedBorderColor = Color.White,
               unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
               focusedContainerColor = Color.White.copy(alpha = 0.1f),
               unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
           ),
           shape = RoundedCornerShape(12.dp)

       )

       Spacer(modifier = Modifier.height(32.dp))

       //Buttons (Host Game/Join Game)
       Row(modifier = Modifier.fillMaxWidth(),
           horizontalArrangement = Arrangement.spacedBy(16.dp)){
           //Host Game Button
           Button(onClick = onHostClick,
           modifier = Modifier.weight(1f),
               shape = RoundedCornerShape(12.dp)
           ){
               Text("Host Game")

           }

           //Join Game Button
           Button(onClick = onJoinClick,
               modifier = Modifier.weight(1f),
               shape = RoundedCornerShape(12.dp)
           ){
               Text("Join Game")
           }
       }
   }}
}
@Preview(showBackground = true, widthDp = 800, heightDp = 450)
@Composable fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
