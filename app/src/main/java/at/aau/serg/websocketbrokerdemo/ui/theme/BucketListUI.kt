package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
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
    onCityOrderChanged: (List<String>) -> Unit,
    onDismiss: () -> Unit = {}
) {
    // Lokale Arbeitskopie der Reihenfolge: Drag & Drop schiebt hier live um,
    // ohne bei jedem Zwischenschritt den Parent neu zu komponieren (sonst würde
    // die Drag-Geste neu starten). An den Parent wird erst am Drag-Ende gemeldet.
    val localOrder = remember { mutableStateOf(drawnCities) }
    val draggingCity = remember { mutableStateOf<String?>(null) }
    val dragOffsetX = remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    // Externe (Server-)Reihenfolge übernehmen, solange gerade nicht gezogen wird.
    LaunchedEffect(drawnCities) {
        if (draggingCity.value == null) localOrder.value = drawnCities
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            val cities = localOrder.value
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clickable(enabled = false) { } ,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(cities, key = { _, city -> city }) { index, city ->
                    val isDragging = city == draggingCity.value
                    val draggable = !visitedCities.contains(city)

                    // Drag-Geste nur für noch nicht besuchte Karten (per Long-Press starten).
                    val dragModifier = if (draggable) {
                        Modifier.pointerInput(city) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingCity.value = city
                                    dragOffsetX.value = 0f
                                },
                                onDragEnd = {
                                    onCityOrderChanged(localOrder.value)
                                    draggingCity.value = null
                                    dragOffsetX.value = 0f
                                },
                                onDragCancel = {
                                    onCityOrderChanged(localOrder.value)
                                    draggingCity.value = null
                                    dragOffsetX.value = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX.value += dragAmount.x

                                    val order = localOrder.value
                                    val curr = order.indexOf(city)
                                    val info = listState.layoutInfo
                                    val dragged = info.visibleItemsInfo.firstOrNull { it.key == city }
                                    if (curr >= 0 && dragged != null) {
                                        // Mittelpunkt der gezogenen Karte (inkl. Versatz)
                                        val center = dragged.offset + dragged.size / 2f + dragOffsetX.value
                                        val target = info.visibleItemsInfo.firstOrNull {
                                            it.key != city && center >= it.offset && center <= it.offset + it.size
                                        }
                                        val targetCity = target?.key as? String
                                        val to = targetCity?.let { order.indexOf(it) } ?: -1
                                        if (target != null && to in order.indices && to != curr) {
                                            localOrder.value = order.toMutableList()
                                                .apply { add(to, removeAt(curr)) }
                                            // Karte optisch unter dem Finger halten
                                            dragOffsetX.value += dragged.offset - target.offset
                                        }
                                    }
                                }
                            )
                        }
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationX = if (isDragging) dragOffsetX.value else 0f
                            }
                            .then(dragModifier)
                    ) {
                        CityCard(
                            name = city,
                            canMoveLeft = index > 0,
                            canMoveRight = index < cities.size - 1,
                            isChosen = city == chosenCity,
                            isVisited = visitedCities.contains(city),
                            onCardClick = {
                                if (!visitedCities.contains(city)) {
                                    if (chosenCity == city) onCityChosen(null)
                                    else onCityChosen(city)
                                }
                            },
                            toTheLeftClick = {
                                val newList = cities.toMutableList()
                                val temp = newList[index]
                                newList[index] = newList[index - 1]
                                newList[index - 1] = temp
                                onCityOrderChanged(newList)
                            },
                            toTheRightClick = {
                                val newList = cities.toMutableList()
                                val temp = newList[index]
                                newList[index] = newList[index + 1]
                                newList[index + 1] = temp
                                onCityOrderChanged(newList)
                            }
                        )
                    }
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
    var testListe by remember { mutableStateOf(listOf("Berlin", "Madrid", "London", "Wien", "Paris")) }
    var nowChosen by remember { mutableStateOf<String?>("Madrid")}
    val visited = setOf("Berlin", "Paris")

    WeltreiseBucketList(
        drawnCities = testListe,
        isVisible = true,
        chosenCity = nowChosen,
        visitedCities = visited,
        onCityChosen = { clickedCity -> nowChosen = clickedCity },
        onCityOrderChanged = { newList -> testListe = newList }
    )
}
