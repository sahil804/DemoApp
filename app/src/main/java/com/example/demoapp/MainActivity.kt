package com.example.demoapp

import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    lateinit var mImageView: ImageView
    lateinit var handler: Handler
    val mInterval = 5 * 1000L
    var currentColor: Colors = Colors.RED
    var currentShape: Shape = Shape.TRIANGLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handler = Handler()

        mImageView = findViewById(R.id.ivVectorImage)
        startTask()
        //mImageView.setColorFilter(Colors.RED.ordinal)
    }

    fun loadBitmap() {

        val imageKey = "${currentColor.ordinal}_${currentShape.ordinal}"
        Log.d("Sahil", " key :::" + imageKey)

        CachedImage.getBitmapFromMemCache(imageKey)?.also {
            mImageView.setImageBitmap(it)
            mImageView.setColorFilter(Color.parseColor(currentColor.rgb))
        } ?: run {
            mImageView.setImageResource(Shape.getVectorDrawbale(currentShape))
            mImageView.setColorFilter(Color.parseColor(currentColor.rgb))

            val bm = getBitMap(mImageView.drawable as VectorDrawable)
            CachedImage.addImageToMemCache("${currentColor.ordinal}_${currentShape.ordinal}", bm)
        }
        currentColor = currentColor.next()
        currentShape = currentShape.next()
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

        companion object {
//            fun valueOf(value: Int) = values().find { it.value == value }
//            fun getColorCode(c: Colors):Int {
//                Log.d("Sahil"," getColorCode "+c.rgb)
//                return c.rgb
//            }
        }

        fun next(): Colors {
            return values()[(ordinal + 1) % values().size]
        }
    }

    enum class Shape(val value: Int) {
        TRIANGLE(0),
        RECTANGLE(1),
        CIRCLE(2);

        companion object {
            fun getVectorDrawbale(s: Shape): Int {
                return when (s) {
                    TRIANGLE -> R.drawable.ic_arrow_triangle_24dp
                    RECTANGLE -> R.drawable.ic_rectangle_24dp
                    CIRCLE -> R.drawable.ic_circle_24dp
                }
            }

            //fun valueOf(value: Int) = values().find { it.value == value }
        }

        fun next(): Shape {
            return values()[(ordinal + 1) % values().size]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTask()
    }

}
