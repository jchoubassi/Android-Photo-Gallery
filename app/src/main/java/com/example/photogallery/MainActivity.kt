@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.photogallery

import android.content.ContentResolver
import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.photogallery.imageloader.BitmapLoader
import com.example.photogallery.media.MediaStoreRepo
import com.example.photogallery.media.Photo
import com.example.photogallery.ui.hasImagePermission
import com.example.photogallery.ui.rememberImagePermission
import com.example.photogallery.viewer.PhotoActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


/*
main screen w title and refresh button
* if no photos, show empty state and will ask for permission
* */
class MainActivity : ComponentActivity() {
    private var observer: ContentObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GalleryApp(::registerObserver, ::unregisterObserver) }
    }

    private fun registerObserver(onChange: () -> Unit) {
        val cr: ContentResolver = contentResolver
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = onChange()
        }
        cr.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )
    }

    private fun unregisterObserver(@Suppress("UNUSED_PARAMETER") ignored: () -> Unit = {}) {
        observer?.let { contentResolver.unregisterContentObserver(it) }
        observer = null
    }
}

@Composable
fun GalleryApp(
    registerObserver: ((() -> Unit) -> Unit),
    unregisterObserver: (() -> Unit) -> Unit
) {
    val ctx = LocalContext.current

    var photos by remember { mutableStateOf<List<Photo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPerm by remember { mutableStateOf(hasImagePermission(ctx)) }

    // one place to load/reload
    val reload: () -> Unit = {
        isLoading = true
        photos = MediaStoreRepo.queryAllPhotosNewestFirst(ctx)
        isLoading = false
    }

    //ask for permission; then load
    val requestPermission = rememberImagePermission {
        hasPerm = true
        reload()
    }
    // observe resume/pause
    LifecycleResumeEffect(hasPerm) {
        if (!hasPerm) requestPermission()
        onPauseOrDispose { }
    }

    //initial load + observe
    DisposableEffect(hasPerm) {
        if (!hasPerm) return@DisposableEffect onDispose { }
        registerObserver { reload() }
        reload()
        onDispose { unregisterObserver {} }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                actions = {
                    IconButton(onClick = { reload() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !hasPerm -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Allow access to your photos to show the gallery.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { requestPermission() }) { Text("Grant Photos Permission") }
                    }
                }
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                photos.isEmpty() -> {
                    Text(
                        "No photos found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos, key = { it.id }) { p ->
                            var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                            LaunchedEffect(p.id) {
                                bmp = BitmapLoader.loadThumb(
                                    ctx, p.id, p.orientation, Size(300, 300)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        ctx.startActivity(
                                            Intent(ctx, PhotoActivity::class.java)
                                                .putExtra("id", p.id)
                                                .putExtra("orientation", p.orientation)
                                        )
                                    }
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    CircularProgressIndicator(Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
