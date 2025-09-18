package com.example.photogallery.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

//query photos from media store
object MediaStoreRepo {
    private val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    fun queryAllPhotosNewestFirst(context: Context): List<Photo> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sort = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val result = mutableListOf<Photo>()
        context.contentResolver.query(contentUri, projection, null, null, sort)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val oriCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)
            val wCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val hCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            while (c.moveToNext()) {
                result += Photo(
                    id = c.getLong(idCol).toString(),
                    orientation = c.getInt(oriCol),
                    width = c.getInt(wCol),
                    height = c.getInt(hCol)
                )
            }
        }
        return result
    }

    fun uriFor(id: String): Uri =
        ContentUris.withAppendedId(contentUri, id.toLong())
}
