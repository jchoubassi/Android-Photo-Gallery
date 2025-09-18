package com.example.photogallery.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

private fun permString(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
    else Manifest.permission.READ_EXTERNAL_STORAGE

/* true if we have permission to read images */
fun hasImagePermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, permString()) == PackageManager.PERMISSION_GRANTED

/* returns function to request permission; */
@Composable
fun rememberImagePermission(onGranted: () -> Unit): () -> Unit {
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        if (ok) onGranted()
    }
    return {
        if (!asked) { asked = true; launcher.launch(permString()) }
    }
}
