package com.example.photogallery.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.photogallery.imageloader.BitmapLoader

//full screen photo viewer with zoom
class PhotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("id")!!
        val orientation = intent.getIntExtra("orientation", 0)
        setContent {
            Surface {
                val ctx = LocalContext.current
                var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                LaunchedEffect(id) {
                    bmp = BitmapLoader.loadLarge(ctx, id, orientation, 2048)
                }
                var scale by remember { mutableStateOf(1f) }
                val state = rememberTransformableState { zoom, _, _ ->
                    scale = (scale * zoom).coerceIn(1f, 8f)
                }
                Box(Modifier.fillMaxSize()) {
                    bmp?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale)
                                .transformable(state)
                        )
                    }
                }
            }
        }
    }
}
