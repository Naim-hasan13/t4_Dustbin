package com.kabir.moneytree.extrazz

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private val shakeThreshold = 2.7f
    private val minTimeBetweenShakes = 1000 // milliseconds
    private var lastShakeTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val gForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() / SensorManager.GRAVITY_EARTH
                if (gForce > shakeThreshold) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime >= minTimeBetweenShakes) {
                        lastShakeTime = currentTime
                        onShake() // Call the function on shake
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
