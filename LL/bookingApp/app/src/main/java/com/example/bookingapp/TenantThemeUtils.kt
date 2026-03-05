package com.example.bookingapp

import android.app.Activity
import android.view.View
import android.graphics.drawable.GradientDrawable

object TenantThemeUtils {

    fun applyTenantBackground(activity: Activity, tenantType: String) {
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)

        when (tenantType.toLowerCase()) {
            "lashes" -> {
                // Lashes: Solid color #2D2E30
                rootView.setBackgroundColor(android.graphics.Color.parseColor("#2D2E30"))
            }
            "nails" -> {
                // Nails: Gradient F3D06A to F59358
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        android.graphics.Color.parseColor("#F3D06A"),
                        android.graphics.Color.parseColor("#F59358")
                    )
                )
                rootView.background = gradient
            }
            "hair" -> {
                // Hair: Black
                rootView.setBackgroundColor(android.graphics.Color.parseColor("#000000"))
            }
            else -> {
                // Default background
                rootView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
            }
        }
    }

    fun getTenantType(activity: Activity): String {
        return activity.intent.getStringExtra("TENANT_TYPE") ?: ""
    }
}