package com.gdu.ux.core.extension

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.SparseLongArray
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.recyclerview.widget.RecyclerView


/**
 * Get the [String] for the given [stringRes].
 */
fun View.getString(@StringRes stringRes: Int, vararg value: Any): String = context.resources.getString(stringRes, *value)

/**
 * Get the [Drawable] for the given [drawableRes].
 */
fun View.getDrawable(@DrawableRes drawableRes: Int): Drawable = context.resources.getDrawable(drawableRes)

/**
 * The the color int for the given [colorRes].
 */
@ColorInt
fun View.getColor(@ColorRes colorRes: Int): Int = context.resources.getColor(colorRes)

/**
 * The the px size for the given [dimenRes].
 */
@Px
fun View.getDimension(@DimenRes dimenRes: Int): Float = context.resources.getDimension(dimenRes)

/**
 * Set the view [View.VISIBLE].
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Set the view [View.GONE].
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Toggle the view between [View.GONE] and [View.VISIBLE]
 */
fun View.toggleVisibility() {
    if (visibility == View.VISIBLE) hide()
    else show()
}

/**
 * Show a short length toast with the given [messageResId].
 */
fun View.showShortToast(@StringRes messageResId: Int) {
    Toast.makeText(
        context,
        messageResId,
        Toast.LENGTH_SHORT
    )
        .show()
}

/**
 * Show a long length toast with the given [messageResId].
 */
fun View.showLongToast(@StringRes messageResId: Int) {
    Toast.makeText(
        context,
        messageResId,
        Toast.LENGTH_LONG
    )
        .show()
}

/**
 * Show a short length toast with the given [String].
 */
fun View.showShortToast(message: String?) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
        .show()
}

/**
 * Show a long length toast with the given [String].
 */
fun View.showLongToast(message: String?) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    )
        .show()
}

/**
 * The [TextView]'s text color int.
 */
var TextView.textColor: Int
    @ColorInt
    get() = this.currentTextColor
    set(@ColorInt value) {
        this.setTextColor(value)
    }

/**
 * The [TextView]'s text color state list.
 */
var TextView.textColorStateList: ColorStateList?
    get() = this.textColors
    set(value) {
        this.setTextColor(value)
    }

/**
 * The [ImageView]'s drawable.
 */
var ImageView.imageDrawable: Drawable?
    get() = this.drawable
    set(value) {
        this.setImageDrawable(value)
    }

/**
 * On click listener for recycler view.
 */
fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int) -> Unit): T {
    itemView.setOnClickListener {
        event.invoke(adapterPosition)
    }
    return this
}

private const val FAST_CLICK_DURATION = 300
private val sClickTimes: SparseLongArray = SparseLongArray()

/**
 * 判断是否点击过快
 * @param viewId
 * @param duration
 * @return
 */
fun Button.isFastClick(duration: Int): Boolean {
    val prevTime: Long = sClickTimes.get(this.id)
    val now = System.currentTimeMillis()
    val isFast = now - prevTime < duration
    if (!isFast) {
        sClickTimes.put(this.id, now)
    }
    return isFast
}

/**
 * 判断是否点击过快
 * @param viewId
 * @return
 */
fun Button.isFastClick(): Boolean {
    return isFastClick(FAST_CLICK_DURATION)
}
