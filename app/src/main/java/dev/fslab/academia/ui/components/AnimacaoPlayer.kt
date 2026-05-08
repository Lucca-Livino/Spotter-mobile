package dev.fslab.academia.ui.components

import android.graphics.drawable.AnimatedImageDrawable
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import dev.fslab.academia.ui.theme.LocalAcademiaColors

@Composable
fun AnimacaoPlayer(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val colors = LocalAcademiaColors.current
    var pausado by rememberSaveable(url) { mutableStateOf(false) }
    val isWebm = url.lowercase().endsWith(".webm")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isWebm) {
            WebmPlayerView(
                url = url,
                isPlaying = !pausado,
                onTogglePause = { pausado = !pausado },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AnimatedMediaView(
                url = url,
                contentDescription = contentDescription,
                isPlaying = !pausado,
                onTogglePause = { pausado = !pausado },
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.background.copy(alpha = 0.7f))
                .clickable { pausado = !pausado },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (pausado) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (pausado) "Reproduzir" else "Pausar",
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun WebmPlayerView(
    url: String,
    isPlaying: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }
    LaunchedEffect(isPlaying) { exoPlayer.playWhenReady = isPlaying }
    DisposableEffect(url) { onDispose { exoPlayer.release() } }
    AndroidView(
        factory = { ctx -> TextureView(ctx).apply { exoPlayer.setVideoTextureView(this) } },
        update = { view -> view.setOnClickListener { onTogglePause() } },
        modifier = modifier
    )
}

@Composable
private fun AnimatedMediaView(
    url: String,
    contentDescription: String,
    isPlaying: Boolean,
    onTogglePause: () -> Unit,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawableRef = remember { mutableStateOf<AnimatedImageDrawable?>(null) }
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(ImageDecoderDecoder.Factory()) }
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
        imageLoader = imageLoader,
        onSuccess = { state ->
            val d = state.result.drawable as? AnimatedImageDrawable
            d?.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
            drawableRef.value = d
            if (isPlaying) d?.start() else d?.stop()
        }
    )
    val currentDrawable = drawableRef.value
    SideEffect {
        val d = currentDrawable ?: return@SideEffect
        if (isPlaying) d.start() else d.stop()
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.clickable(onClick = onTogglePause)
    )
}
