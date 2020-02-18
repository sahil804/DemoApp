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
    val ITEM_INTERVAL = 5 * 1000L
    var menuitem: MENUITEM = MENUITEM.PIZZA
    var topping: INGREDIENTS = INGREDIENTS.RED_CHILLI
    private val menuItemImages = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageArray = intArrayOf(R.id.iv_menu_item_image_1)
        // Can intialize mutiple images in an array, so mutiple images will be displayed
        //val imageArray = intArrayOf(R.id.iv_menu_item_image_1, R.id.iv_menu_item_image_2)
        handler = Handler()
        for (id in imageArray) {
            val imageView: ImageView = findViewById(id)
            imageView.visibility = View.VISIBLE
            menuItemImages.add(imageView)
        }
        startTask()
    }

    fun loadBitmap() {
        menuItemImages.forEach { menuItemView ->
            val imageKey = "${menuitem.ordinal}_${topping.ordinal}"
            CachedImage.getBitmapFromMemCache(imageKey)?.also {
                menuItemView.setImageBitmap(it)
                menuItemView.setColorFilter(Color.parseColor(topping.rgb))
            } ?: run {
                menuItemView.setImageResource(MENUITEM.getVectorDrawable(menuitem))
                menuItemView.setColorFilter(Color.parseColor(topping.rgb))

                val bm = getBitMap(menuItemView.drawable as VectorDrawable)
                CachedImage.addImageToMemCache(
                    "${menuitem.ordinal}_${topping.ordinal}",
                    bm
                )
            }
            menuitem = menuitem.next()
            topping = topping.next()
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

    var mRepeatMenuItem: Runnable = object : Runnable {
        override fun run() {
            try {
                loadBitmap()
            } finally {
                handler.postDelayed(this, ITEM_INTERVAL)
            }
        }
    }

    fun startTask() {
        handler.post(mRepeatMenuItem)
    }

    fun stopTask() {
        handler.removeCallbacksAndMessages(null)
    }

    enum class INGREDIENTS(val value: Int, val rgb: String) {
        RED_CHILLI(0, "#c94747"),
        GREEN_CHILLI(1, "#516c5a"),
        YELLOW_CAPSICUM(2, "#cd823f");

        fun next(): INGREDIENTS {
            return values()[(ordinal + 1) % values().size]
        }
    }

    enum class MENUITEM(val value: Int) {
        PIZZA(0),
        HOTDOG(1),
        CHAPATI(2);

        companion object {
            fun getVectorDrawable(s: MENUITEM): Int {
                return when (s) {
                    PIZZA -> R.drawable.ic_arrow_pizza_24dp
                    HOTDOG -> R.drawable.ic_hotdog_24dp
                    CHAPATI -> R.drawable.ic_chapati_24dp
                }
            }
        }

        fun next(): MENUITEM {
            return values()[(ordinal + 1) % values().size]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CachedImage.clearCache()
        stopTask()
    }

}
