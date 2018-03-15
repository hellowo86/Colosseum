package com.hellowo.colosseum.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

import java.io.IOException

fun makeProfileBitmapFromFile(filePath: String): Bitmap? {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.RGB_565
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    val widthScale = (options.outWidth / 128).toFloat()
    val heightScale = (options.outHeight / 128).toFloat()
    val scale = if (widthScale > heightScale) widthScale else heightScale

    /*if(scale >= 16) {
        options.inSampleSize = 16;
    }else if(scale >= 14) {
        options.inSampleSize = 14;
    }else if(scale >= 12) {
        options.inSampleSize = 12;
    }else if(scale >= 10) {
        options.inSampleSize = 10;
    }else */if (scale >= 8) {
        options.inSampleSize = 8
    } else if (scale >= 4) {
        options.inSampleSize = 4
    } else if (scale >= 2) {
        options.inSampleSize = 2
    } else {
        options.inSampleSize = 1
    }

    options.inJustDecodeBounds = false

    val degree = GetExifOrientation(filePath)
    return GetRotatedBitmap(BitmapFactory.decodeFile(filePath, options), degree)
}

@Throws(Exception::class)
fun makePhotoBitmapFromFile(filePath: String): Bitmap? {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.RGB_565
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    val widthScale = (options.outWidth / 512).toFloat()
    val heightScale = (options.outHeight / 512).toFloat()
    val scale = if (widthScale > heightScale) widthScale else heightScale

    /*if(scale >= 16) {
        options.inSampleSize = 16;
    }else if(scale >= 14) {
        options.inSampleSize = 14;
    }else if(scale >= 12) {
        options.inSampleSize = 12;
    }else if(scale >= 10) {
        options.inSampleSize = 10;
    }else */if (scale >= 8) {
        options.inSampleSize = 8
    } else if (scale >= 4) {
        options.inSampleSize = 4
    } else if (scale >= 2) {
        options.inSampleSize = 2
    } else {
        options.inSampleSize = 1
    }

    options.inJustDecodeBounds = false

    val degree = GetExifOrientation(filePath)
    return GetRotatedBitmap(BitmapFactory.decodeFile(filePath, options), degree)
}

@Synchronized
fun GetExifOrientation(filepath: String): Int {
    var degree = 0
    var exif: ExifInterface? = null

    try {
        exif = ExifInterface(filepath)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    if (exif != null) {
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)

        if (orientation != -1) {
            // We only recognize a subset of orientation tag values.
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90

                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180

                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }

        }
    }

    return degree
}

@Synchronized
fun GetRotatedBitmap(bitmap: Bitmap?, degrees: Int): Bitmap? {
    var bitmap = bitmap
    if (degrees != 0 && bitmap != null) {
        val m = Matrix()
        m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        try {
            val b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            if (bitmap != b2) {
                bitmap.recycle()
                bitmap = b2
            }
        } catch (ex: OutOfMemoryError) {
            // We have no memory to rotate. Return the original bitmap.
        }

    }

    return bitmap
}