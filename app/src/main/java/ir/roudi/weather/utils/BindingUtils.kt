package ir.roudi.weather.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import java.io.IOException

@BindingAdapter("imageName")
fun ImageView.setImage(fileName: String?) {
    try {
        val inputStream = context.assets.open( "$fileName.png")
        val drawable = Drawable.createFromStream(inputStream, null)
        setImageDrawable(drawable)
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}