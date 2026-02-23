package com.example.photoprintapp.activities

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        val root = window.decorView.rootView
        root.rotation = -90f
        root.post {
            val w = root.width.toFloat()
            val h = root.height.toFloat()
            root.pivotX = w / 2f
            root.pivotY = h / 2f
            root.scaleX = h / w
            root.scaleY = w / h
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val root = window.decorView.rootView
        val w = root.width.toFloat()
        val h = root.height.toFloat()
        val newX = (h - ev.y) * (w / h)
        val newY = ev.x * (h / w)
        ev.setLocation(newX, newY)
        return super.dispatchTouchEvent(ev)
    }
}