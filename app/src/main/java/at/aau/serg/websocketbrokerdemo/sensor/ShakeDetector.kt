package at.aau.serg.websocketbrokerdemo.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import at.aau.serg.websocketbrokerdemo.logging.DebugLog
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
        if (sensorManager != null) {
            DebugLog.d("ShakeDetector") { "start() ignoriert - bereits registriert" }
            return
        }
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (manager == null) {
            DebugLog.e("ShakeDetector") { "start() FEHLER: kein SensorManager" }
            return
        }
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            DebugLog.e("ShakeDetector") { "start() FEHLER: kein Accelerometer" }
            return
        }
        sensorManager = manager
        accelerometer = sensor
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        DebugLog.d("ShakeDetector") { "start() - Accelerometer registriert" }
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

        if (gForce < SHAKE_THRESHOLD_G) {
            if (gForce > 1.5f) {
                DebugLog.d("ShakeDetector") {
                    "gForce=$gForce (zu schwach, Schwelle=$SHAKE_THRESHOLD_G)"
                }
            }
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastShakeAt < DEBOUNCE_MS) return
        lastShakeAt = now
        DebugLog.d("ShakeDetector") { "SHAKE erkannt! gForce=$gForce" }
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
