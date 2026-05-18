package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun WeltreiseBucketList(
    drawnCities: List<String>,
    isVisible: Boolean,
    chosenCity: String?,
    visitedCities: Set<String> = emptySet(),
    onCityChosen: (String?) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val sortableList = remember(drawnCities) {
        drawnCities.toMutableStateList()
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clickable(enabled = false) { } ,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(sortableList) { index, city ->
                    CityCard(
                        name = city,
                        canMoveLeft = index > 0,
                        canMoveRight = index < sortableList.size - 1,
                        isChosen = city == chosenCity,
                        isVisited = visitedCities.contains(city),
                        onCardClick = {
                            if(!visitedCities.contains(city)) {
                                if (chosenCity == city) onCityChosen(null)
                                else onCityChosen(city)
                            }
                        },
                        toTheLeftClick = {
                            val temp = sortableList[index]
                            sortableList[index] = sortableList[index - 1]
                            sortableList[index - 1] = temp
                        },
                        toTheRightClick = {
                            val temp = sortableList[index]
                            sortableList[index] = sortableList[index + 1]
                            sortableList[index + 1] = temp
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CityCard(
    name: String,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    isChosen: Boolean,
    isVisited: Boolean,
    onCardClick: () -> Unit,
    toTheLeftClick: () -> Unit,
    toTheRightClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 160.dp)
            .background(
                color = if (isChosen) Color(0xFF6200EE) else Color(0xFFD9E2E8),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onCardClick() }
    ) {
        Text(
            text = if (isVisited) "$name ✓"
            else name.uppercase(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isVisited)
                Color.LightGray
            else
                Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Pfeile (nur anzeigen, wenn noch nicht besucht)
        if (!isVisited) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {


                // Linker Pfeil
                if (canMoveLeft) {
                    Text(
                        text = "<",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.clickable { toTheLeftClick() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Rechter Pfeil
                if (canMoveRight) {
                    Text(
                        text = ">",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.clickable { toTheRightClick() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 360)
@Composable
fun PreviewBucketList() {
    val testListe = listOf("Berlin", "Madrid", "London", "Wien", "Paris")
    var nowChosen by remember { mutableStateOf<String?>("Madrid")}
    val visited = setOf("Berlin", "Paris")

    WeltreiseBucketList(
        drawnCities = testListe,
        isVisible = true,
        chosenCity = nowChosen,
        visitedCities = visited,
        onCityChosen = { clickedCity -> nowChosen = clickedCity }
    )
}
