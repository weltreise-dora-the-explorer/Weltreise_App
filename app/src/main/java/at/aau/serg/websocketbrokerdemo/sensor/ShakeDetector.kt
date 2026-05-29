package at.aau.serg.websocketbrokerdemo.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Erkennt ein Schuetteln des Geraets ueber den Beschleunigungssensor.
 * Triggert [onShake], wenn die spuerbare g-Force [SHAKE_THRESHOLD_G] uebersteigt
 * und seit dem letzten Trigger mindestens [DEBOUNCE_MS] vergangen sind.
 *
 * Lebenszyklus: in onResume [start] aufrufen, in onPause [stop].
 */
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeAt: Long = 0L

    fun start(context: Context) {
        if (sensorManager != null) return
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager = manager
        accelerometer = sensor
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        accelerometer = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        if (values.size < 3) return
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH

        if (gForce < SHAKE_THRESHOLD_G) return

        val now = System.currentTimeMillis()
        if (now - lastShakeAt < DEBOUNCE_MS) return
        lastShakeAt = now
        onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not used
    }

    companion object {
        private const val SHAKE_THRESHOLD_G: Float = 2.7f
        private const val DEBOUNCE_MS: Long = 800L
    }
}
