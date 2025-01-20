package com.gdu.ux.core.extension

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.StyleableRes

const val INVALID_RESOURCE = -1
const val INVALID_DIMENSION = -1f
const val INVALID_STRING = ""

/**
 * Retrieve the string value for the attribute at [index].
 * Returns the found value or [defValue] if not found.
 */
fun TypedArray.getString(@StyleableRes index: Int, defValue: String): String =
        getString(index) ?: defValue

/**
 * Retrieve the string value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getString
 */
inline fun <R> TypedArray.getStringAndUse(@StyleableRes index: Int, block: (String) -> R) {
    val string = getString(index, INVALID_STRING)
    if (string != INVALID_STRING) {
        block(string)
    }
}

/**
 * Retrieve the color value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getColor
 */
inline fun <R> TypedArray.getColorAndUse(@StyleableRes index: Int, block: (Int) -> R) {
    val colorInt = getColor(index, INVALID_RESOURCE)
    if (colorInt != INVALID_RESOURCE) {
        block(colorInt)
    }
}

/**
 * Retrieve the color state list value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getColorStateList
 */
inline fun <R> TypedArray.getColorStateListAndUse(@StyleableRes index: Int, block: (ColorStateList) -> R) {
    val colorStateList = getColorStateList(index)
    colorStateList?.let { block(it) }
}

/**
 * Retrieve the dimension value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getDimension
 */
inline fun <R> TypedArray.getDimensionAndUse(@StyleableRes index: Int, block: (Float) -> R) {
    val dimension = getDimension(index, INVALID_DIMENSION)
    if (dimension != INVALID_DIMENSION) {
        block(dimension)
    }
}

/**
 * Retrieve the drawable for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getDrawable
 */
inline fun <R> TypedArray.getDrawableAndUse(@StyleableRes index: Int, block: (Drawable) -> R) {
    val drawable = getDrawable(index)
    drawable?.let { block(drawable) }
}

/**
 * Retrieve the int value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getDrawable
 */
inline fun <R> TypedArray.getIntAndUse(@StyleableRes index: Int, block: (Int) -> R) {
    val int = getInt(index, INVALID_RESOURCE)
    if (int != INVALID_RESOURCE) {
        block(int)
    }
}

/**
 * Retrieve the resource identifier for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getResourceId
 */
inline fun <R> TypedArray.getResourceIdAndUse(@StyleableRes index: Int, block: (Int) -> R) {
    val resourceId = getResourceId(index, INVALID_RESOURCE)
    if (resourceId != INVALID_RESOURCE) {
        block(resourceId)
    }
}

/**
 * Retrieve the float value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getResourceId
 */
inline fun <R> TypedArray.getFloatAndUse(@StyleableRes index: Int, block: (Float) -> R) {
    val floatValue = getFloat(index, INVALID_DIMENSION)
    if (floatValue != INVALID_DIMENSION) {
        block(floatValue)
    }
}

/**
 * Retrieve the integer value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 *
 * @see TypedArray.getResourceId
 */
inline fun <R> TypedArray.getIntegerAndUse(@StyleableRes index: Int, block: (Int) -> R) {
    val intValue = getInteger(index, INVALID_RESOURCE)
    if (intValue != INVALID_RESOURCE) {
        block(intValue)
    }
}

/**
 * Retrieve the integer value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource or the [defaultValue].
 *
 * @see TypedArray.getResourceId
 */
inline fun <R> TypedArray.getBooleanAndUse(@StyleableRes index: Int, defaultValue: Boolean, block: (Boolean) -> R) {
    val booleanValue = getBoolean(index, defaultValue)
    block(booleanValue)
}

/**
 * Retrieve the drawable array value for the attribute at [index] and executes the given [block]
 * function with the retrieved resource.
 */
inline fun <R> TypedArray.getDrawableArrayAndUse(@StyleableRes index: Int, block: (Array<Drawable?>) -> R) {
    val arrayResourceId = getResourceId(index, INVALID_RESOURCE)
    if (arrayResourceId != INVALID_RESOURCE) {
        val resourceArray = resources.obtainTypedArray(arrayResourceId)
        val drawableArray = Array(resourceArray.length()) { i ->
            resourceArray.getDrawable(i)
        }
        block(drawableArray)
        resourceArray.recycle()
    }
}


