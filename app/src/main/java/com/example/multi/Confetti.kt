package com.example.multi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class ConfettiPiece(
    val x: Float,
    val size: Float,
    val color: Color,
    val anim: Animatable<Float, AnimationVector1D>
)

@Composable
fun ConfettiCelebration(onFinished: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val pieces = remember {
            List(40) {
                ConfettiPiece(
                    x = Random.nextFloat() * maxWidth.value,
                    size = Random.nextFloat() * 6f + 4f,
                    color = listOf(
                        Color.Red,
                        Color.Green,
                        Color.Blue,
                        Color.Magenta,
                        Color.Yellow
                    ).random(),
                    anim = Animatable(-10f)
                )
            }
        }

        LaunchedEffect(Unit) {
            pieces.forEach { piece ->
                launch {
                    piece.anim.animateTo(
                        targetValue = maxHeight.value + 10f,
                        animationSpec = tween(durationMillis = 1500)
                    )
                }
            }
            delay(1600)
            onFinished()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            pieces.forEach { piece ->
                Box(
                    modifier = Modifier
                        .offset(x = piece.x.dp, y = piece.anim.value.dp)
                        .size(piece.size.dp)
                        .background(piece.color)
                )
            }
        }
    }
}
