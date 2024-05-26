package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size

/**
 * @author
 *   https://github.com/DanielMartinus/Konfetti/blob/90e6479e3f02cde424c12a05f67bf47d16349549/samples/shared/src/main/java/nl/dionsegijn/samples/shared/Presets.kt
 */
private val festive = run {
    val party =
        Party(
            speed = 30f,
            maxSpeed = 50f,
            damping = 0.9f,
            angle = Angle.TOP,
            spread = 45,
            size = listOf(Size.SMALL, Size.LARGE),
            timeToLive = 3000L,
            rotation = Rotation(),
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(30),
            position = Position.Relative(0.5, 1.0)
        )

    listOf(
        party,
        party.copy(
            speed = 55f,
            maxSpeed = 65f,
            spread = 10,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
        ),
        party.copy(
            speed = 50f,
            maxSpeed = 60f,
            spread = 120,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(40),
        ),
        party.copy(
            speed = 65f,
            maxSpeed = 80f,
            spread = 10,
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
        )
    )
}

@Composable
fun DoneCelebration(
    modifier: Modifier = Modifier,
    onFinished: (() -> Unit)? = null,
) {
    KonfettiView(
        modifier = modifier,
        parties = festive,
        updateListener =
            onFinished?.let { cb ->
                object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) cb()
                    }
                }
            }
    )
}
