package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Zentriertes Modal das angezeigt wird wenn der gerade-an-der-Reihe Spieler
 * disconnected ist. Zeigt den Namen und einen Live-Countdown an.
 *
 * Das Modal blockiert UI-Interaktionen, damit niemand handeln kann waehrend
 * auf den disconnecteten Spieler gewartet wird.
 */
@Composable
fun PlayerDisconnectedDialog(
    playerName: String,
    secondsLeft: Int,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /* Modal nicht schliessbar - wartet auf Reconnect oder Timeout */ },
        confirmButton = { },
        dismissButton = null,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        modifier = modifier,
        title = {
            Text(
                text = "Player Disconnected",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFFB71C1C),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = Color(0xFF1E56A0),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "$playerName lost connection.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Waiting for reconnect...",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = formatCountdown(secondsLeft),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E56A0),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

private fun formatCountdown(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val mm = safe / 60
    val ss = safe % 60
    return "%02d:%02d".format(mm, ss)
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun PlayerDisconnectedDialogPreview() {
    PlayerDisconnectedDialog(
        playerName = "Marco Polo",
        secondsLeft = 52
    )
}
