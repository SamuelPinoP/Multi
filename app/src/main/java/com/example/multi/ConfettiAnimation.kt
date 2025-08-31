package com.example.multi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class ConfettiPiece(
    val x: Float,
    val delay: Int,
    val duration: Int,
    val emoji: String
)

@Composable
fun ConfettiAnimation(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    BoxWithConstraints(modifier) {
        val pieces = remember {
            List(40) {
                ConfettiPiece(
                    x = Random.nextFloat() * maxWidth.value,
                    delay = Random.nextInt(0, 800),
                    duration = Random.nextInt(1200, 2000),
                    emoji = if (Random.nextBoolean()) "🎉" else "⚡"
                )
            }
        }
        pieces.forEach { piece ->
            val offsetY = remember { Animatable(-20f) }
            LaunchedEffect(piece) {
                delay(piece.delay.toLong())
                offsetY.animateTo(
                    targetValue = maxHeight.value,
                    animationSpec = tween(
                        durationMillis = piece.duration,
                        easing = LinearEasing
                    )
                )
            }
            Text(
                text = piece.emoji,
                fontSize = 24.sp,
                modifier = Modifier.offset(piece.x.dp, offsetY.value.dp)
            )
        }
    }
}
