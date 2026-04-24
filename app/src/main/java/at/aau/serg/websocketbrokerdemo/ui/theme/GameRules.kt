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
import androidx.compose.material3.TextButton
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

@Composable
fun GameRulesButton() {
    val context = LocalContext.current
    val rulesIcon = loadAssetBitmap(context, "rules.png")
    var showRules by remember {mutableStateOf(false)}

    Box(
        modifier = Modifier
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

    if(showRules) {
        AlertDialog(
            onDismissRequest = {showRules = false},
            title = {
                Text(
                    text = "Spielregeln",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ){
                    ExpandableRule(
                        title = "Spielziel",
                        content = "Ziel des Spiels ist es, alle eigenen Zielstädte zu erreichen und anschließend wieder zur eigenen Startstadt zurückzukehren."
                    )

                    ExpandableRule(
                        title = "Start des Spiels",
                        content = "Zu Beginn erhält jeder Spieler eine eigene Startstadt und je nach gewähltem Spielmodus 6, 12 oder 18 Zielstädte. Jeder Spieler startet an einer unterschiedlichen Stadt."
                    )

                    ExpandableRule(
                        title = "Bewegung",
                        content = "In jeder Runde wird gewürfelt. Die gewürfelte Zahl bestimmt, wie viele Bewegungspunkte zur Verfügung stehen. Zugrouten kosten 1 Punkt, Flugrouten kosten 2 Punkte. Der Zug kann auch frühzeitig beendet werden."
                    )

                    ExpandableRule(
                        title = "Bewegungsregeln",
                        content = "Eine Stadt darf nicht betreten werden, wenn dort bereits ein anderer Spieler steht. Außerdem darf man innerhalb eines Zuges nicht direkt dieselbe Strecke zurückreisen. In der nächsten Runde ist das aber wieder erlaubt."
                    )

                    ExpandableRule(
                        title = "Zielstädte und Minispiele",
                        content = "In jeder eigenen Zielstadt wartet ein Minispiel. Wird das Minispiel gewonnen, gilt die Zielstadt als abgeschlossen. Wird es verloren, wird diese Zielstadt verworfen und eine neue Zielstadt zugeteilt."
                    )

                    ExpandableRule(
                        title = "Spielende",
                        content = "Sobald ein Spieler alle Zielstädte abgeschlossen hat, muss er zu seiner Startstadt zurückkehren. Wer zuerst alle Zielstädte erledigt hat und wieder auf der Startstadt steht, gewinnt das Spiel."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRules = false}) {
                    Text("Schließen")
                }
            }
        )
    }
}

@Composable
fun ExpandableRule(title: String, content: String) {
    var expanded by remember {mutableStateOf(false)}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFFEAF2F8), RoundedCornerShape(10.dp))
            .clickable {expanded = !expanded}
            .padding(12.dp)
    ){
        Text(
            text = if (expanded) "⌃ $title" else "⌄ $title",
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