package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

//Button oben rechts in der Lobby, der das Regel Popup öffnet
@Composable
fun GameRulesButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rulesIcon = loadAssetBitmap(context, "rules_icon.png")
    var showRules by remember {mutableStateOf(false)}
    var selectedLanguage by remember { mutableStateOf("DE")}

    Box(
        modifier = modifier
            .padding(top = 24.dp, end = 32.dp)
            .size(64.dp)
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable {showRules = true},
        contentAlignment = Alignment.Center
    ) {
        if(rulesIcon != null) {
            Image(
                bitmap = rulesIcon,
                contentDescription = "Spielregeln",
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
        }else{
            Text("?", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    //Dialog mit den Spielregeln
    if(showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false},
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = if(selectedLanguage == "DE") "Spielregeln" else "Game Rules",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = "\uD83C\uDDE9\uD83C\uDDEA",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .background(
                                    color = if (selectedLanguage == "DE") Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {selectedLanguage = "DE"}
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "\uD83C\uDDEC\uD83C\uDDE7",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .background(
                                    color = if (selectedLanguage == "EN") Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLanguage = "EN" }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
//                Text(
//                    text = "Spielregeln",
//                    fontWeight = FontWeight.Bold
//                )
            },
            text = {
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(scrollState)
                    ) {
                        if (selectedLanguage == "DE") {
                            //Aufklappbare Regeln
                            ExpandableRule(
                                title = "\uD83C\uDFAF Spielziel",
                                content = "Ziel des Spiels ist es, alle eigenen Zielstädte zu erreichen und anschließend wieder zur eigenen Startstadt zurückzukehren."
                            )

                            ExpandableRule(
                                title = "\uD83D\uDE80 Start des Spiels",
                                content = "Zu Beginn erhält jeder Spieler eine eigene Startstadt und je nach gewähltem Spielmodus 6, 12 oder 18 Zielstädte. Jeder Spieler startet an einer unterschiedlichen Stadt."
                            )

                            ExpandableRule(
                                title = "\uD83C\uDFB2 Bewegung",
                                content = "In jeder Runde wird gewürfelt. Die gewürfelte Zahl bestimmt, wie viele Bewegungspunkte zur Verfügung stehen. Zugrouten kosten 1 Punkt, Flugrouten kosten 2 Punkte. Der Zug kann auch frühzeitig beendet werden."
                            )

                            ExpandableRule(
                                title = "⚠\uFE0F Bewegungsregeln",
                                content = "Eine Stadt darf nicht betreten werden, wenn dort bereits ein anderer Spieler steht. Außerdem darf man innerhalb eines Zuges nicht direkt dieselbe Strecke zurückreisen. In der nächsten Runde ist das aber wieder erlaubt."
                            )

                            ExpandableRule(
                                title = "\uD83E\uDDE9 Zielstädte und Minispiele",
                                content = "In jeder eigenen Zielstadt wartet ein Minispiel. Wird das Minispiel gewonnen, gilt die Zielstadt als abgeschlossen. Wird es verloren, wird diese Zielstadt verworfen und eine neue Zielstadt zugeteilt."
                            )

                            ExpandableRule(
                                title = "\uD83C\uDFC1 Spielende",
                                content = "Sobald ein Spieler alle Zielstädte abgeschlossen hat, muss er zu seiner Startstadt zurückkehren. Wer zuerst alle Zielstädte erledigt hat und wieder auf der Startstadt steht, gewinnt das Spiel."
                            )
                        } else {
                            ExpandableRule(
                                title = "🎯 Goal of the Game",
                                content = "The goal of the game is to reach all of your own destination cities and then return to your own starting city."
                            )

                            ExpandableRule(
                                title = "🚀 Start of the Game",
                                content = "At the beginning, each player receives their own starting city and, depending on the selected game mode, 6, 12 or 18 destination cities. Each player starts in a different city."
                            )

                            ExpandableRule(
                                title = "🎲 Movement",
                                content = "In each round, the player rolls the dice. The rolled number determines how many movement points are available. Train routes cost 1 point, flight routes cost 2 points. The turn can also be ended early."
                            )

                            ExpandableRule(
                                title = "⚠️ Movement Rules",
                                content = "A city may not be entered if another player is already standing there. In addition, a player may not directly travel back along the same route within one turn. In the next round, this is allowed again."
                            )

                            ExpandableRule(
                                title = "🧩 Destination Cities and Minigames",
                                content = "Each own destination city contains a minigame. If the minigame is won, the destination city is completed. If it is lost, this destination city is discarded and a new destination city is assigned."
                            )

                            ExpandableRule(
                                title = "🏁 End of the Game",
                                content = "Once a player has completed all destination cities, they must return to their starting city. The first player to complete all destination cities and stand on their starting city wins the game."
                            )
                        }
                    }
                    if(scrollState.maxValue > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        BoxWithConstraints(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(4.dp)
                                .fillMaxHeight()
                        ){
                            val scrollbarHeight = 40.dp
                            val maxOffset = maxHeight - scrollbarHeight

                            val scrollProgress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()

                            Box(
                                modifier = Modifier
                                    .offset(y = maxOffset * scrollProgress)
                                    .width(4.dp)
                                    .height(scrollbarHeight)
                                    .background(
                                        color = Color.DarkGray.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {showRules = false},
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000)
                    )
                ){
                    Text(
                        text = if(selectedLanguage == "DE" ) "Schließen" else "Close",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

//Einzelner aufklappbarer Regelpunkt
@Composable
fun ExpandableRule(title: String, content: String) {
    var expanded by remember {mutableStateOf(false)}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp))
            .clickable {expanded = !expanded}
            .padding(12.dp)
    ){
        Text(
            text = if (expanded) "▲ $title" else "▼ $title",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E56A0)
        )

        if(expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Start
            )
        }
    }
}