package io.github.janmalch.woroboro.ui.routine.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Media
import io.github.janmalch.woroboro.models.Routine
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.absoluteValue


@Composable
fun RoutinePagerMode(
    routine: Routine,
) {
    var isPaused by remember {
        // TODO
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) {
        val pagerState = rememberPagerState(pageCount = {
            routine.exercises.size
        })
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 0.dp, // no page spacing here, but we get it thanks to scaling animation
            modifier = Modifier.weight(1F),
            userScrollEnabled = isPaused,
        ) { page ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // We animate the alpha, between 50% and 100%
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        // We animate the scale, between 90% and 100%
                        scaleX = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                ExerciseCard(exercise = routine.exercises[page])
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoutineControls(
            isPaused = isPaused,
            onPauseChange = { isPaused = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun RoutineControls(
    isPaused: Boolean,
    onPauseChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: free mode vs fixed timer mode, but free mode with timer also
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(0.8f),
    ) {
        FloatingActionButton(onClick = { onPauseChange(!isPaused) }) {
            if (isPaused) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            } else {
                Icon(Icons.Rounded.Pause, contentDescription = null)
            }
        }
    }
}

@Composable
fun ExerciseMediaPager(
    media: ImmutableList<Media>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = modifier.background(Color.Black.copy(alpha = 0.6f)),
    ) {
        items(media, key = { it.id }) {
            AsyncImage(
                model = it.source,
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {

            ExerciseMediaPager(
                media = exercise.media,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(24.dp))
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {

                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = exercise.description)
            }
        }

    }
    /*Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {

        AsyncImage(
            model = exercise.media.firstOrNull()?.source,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.2f, renderEffect = BlurEffect(20f, 20f))
        )

        AsyncImage(
            model = exercise.media.firstOrNull()?.source,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = exercise.name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(Color.Black, Offset(1f, 1f))
                ),
            )
        }
    }*/
}
