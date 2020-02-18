package com.example.demoapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    lateinit var handler: Handler
    val mInterval = 5 * 1000L
    var currentColor: Colors = Colors.RED
    var currentShape: Shape = Shape.TRIANGLE
    private val imagesList = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageArray = intArrayOf(R.id.ivVectorImage_1)
        // Can intialize mutiple images in an array, so mutiple images will be displayed
        //val imageArray = intArrayOf(R.id.ivVectorImage_1, R.id.ivVectorImage_2)
        handler = Handler()
        for (id in imageArray) {
            val imageView: ImageView = findViewById(id)
            imageView.visibility = View.VISIBLE
            imagesList.add(imageView)
        }
        startTask()
    }

    fun loadBitmap() {
        imagesList.forEach { imageView ->
            val imageKey = "${currentColor.ordinal}_${currentShape.ordinal}"
            CachedImage.getBitmapFromMemCache(imageKey)?.also {
                imageView.setImageBitmap(it)
                imageView.setColorFilter(Color.parseColor(currentColor.rgb))
            } ?: run {
                imageView.setImageResource(Shape.getVectorDrawable(currentShape))
                imageView.setColorFilter(Color.parseColor(currentColor.rgb))

                val bm = getBitMap(imageView.drawable as VectorDrawable)
                CachedImage.addImageToMemCache(
                    "${currentColor.ordinal}_${currentShape.ordinal}",
                    bm
                )
            }
            currentColor = currentColor.next()
            currentShape = currentShape.next()
        }
    }

    private fun getBitMap(drawable: VectorDrawable): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    var mRepeatTask: Runnable = object : Runnable {
        override fun run() {
            try {
                loadBitmap()
            } finally {
                handler.postDelayed(this, mInterval)
            }
        }
    }

    fun startTask() {
        handler.post(mRepeatTask)
    }

    fun stopTask() {
        handler.removeCallbacksAndMessages(null)
    }

    enum class Colors(val value: Int, val rgb: String) {
        RED(0, "#c94747"),
        GREEN(1, "#516c5a"),
        YELLOW(2, "#cd823f");

        fun next(): Colors {
            return values()[(ordinal + 1) % values().size]
        }
    }

    enum class Shape(val value: Int) {
        TRIANGLE(0),
        RECTANGLE(1),
        CIRCLE(2);

        companion object {
            fun getVectorDrawable(s: Shape): Int {
                return when (s) {
                    TRIANGLE -> R.drawable.ic_arrow_triangle_24dp
                    RECTANGLE -> R.drawable.ic_rectangle_24dp
                    CIRCLE -> R.drawable.ic_circle_24dp
                }
            }
        }

        fun next(): Shape {
            return values()[(ordinal + 1) % values().size]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CachedImage.clearCache()
        stopTask()
    }

}
