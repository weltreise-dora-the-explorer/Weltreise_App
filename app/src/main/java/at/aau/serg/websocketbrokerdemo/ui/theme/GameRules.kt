package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

//Button oben rechts in der Lobby, der das Regel Popup öffnet
@Composable
fun GameRulesButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rulesIcon = loadAssetBitmap(context, "rules_icon.png")
    var showRules by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(top = 24.dp, end = 32.dp)
            .size(64.dp)
            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable { showRules = true },
        contentAlignment = Alignment.Center
    ) {
        if (rulesIcon != null) {
            Image(
                bitmap = rulesIcon,
                contentDescription = "Spielregeln",
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("?", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    //Dialog mit den Spielregeln
    if (showRules) {
        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showRules = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = stringResource(R.string.game_rules_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
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
                            .padding(end = 12.dp)
                    ) {
                        //Aufklappbare Regeln
                        ExpandableRule(
                            title = stringResource(R.string.rule_goal_title),
                            content = stringResource(R.string.rule_goal_content)
                        )

                        ExpandableRule(
                            title = stringResource(R.string.rule_start_title),
                            content = stringResource(R.string.rule_start_content)
                        )

                        ExpandableRule(
                            title = stringResource(R.string.rule_movement_title),
                            content = stringResource(R.string.rule_movement_content)
                        )

                        ExpandableRule(
                            title = stringResource(R.string.rule_movement_rules_title),
                            content = stringResource(R.string.rule_movement_rules_content)
                        )

                        ExpandableRule(
                            title = stringResource(R.string.rule_minigame_title),
                            content = stringResource(R.string.rule_minigame_content)
                        )

                        ExpandableRule(
                            title = stringResource(R.string.rule_end_title),
                            content = stringResource(R.string.rule_end_content)
                        )
                    }

                    if (scrollState.maxValue > 0) {
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
                        ) {
                            val scrollbarHeight = 40.dp
                            val maxOffset = maxHeight - scrollbarHeight

                            val scrollProgress =
                                scrollState.value.toFloat() / scrollState.maxValue.toFloat()

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
                    onClick = { showRules = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.game_rules_close),
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
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Text(
            text = if (expanded) "▲ $title" else "▼ $title",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E56A0)
        )

        if (expanded) {
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