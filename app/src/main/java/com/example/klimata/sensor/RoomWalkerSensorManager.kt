package com.example.klimata.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class WalkedCorner(
    val position: Offset,
    val wallLengthMeters: Float,
    val cornerIndex: Int
)

data class WalkerSnapshot(
    val isTracking: Boolean = false,
    val totalSteps: Int = 0,
    val currentWallSteps: Int = 0,
    val currentWallDistanceMeters: Float = 0f,
    val currentHeadingDeg: Float = 0f,
    val currentPosition: Offset = Offset.Zero,
    val corners: List<Offset> = listOf(Offset.Zero),
    val walkedCorners: List<WalkedCorner> = emptyList(),
    val calculatedAreaM2: Int = 0
)

class RoomWalkerSensorManager(
    private val context: Context,
    val strideLengthMeters: Float = 0.70f,
    var onSnapshotChanged: (WalkerSnapshot) -> Unit = {}
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val rotationVector = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isTracking = false
    private var totalSteps = 0
    private var currentWallSteps = 0
    private var manualWallDistance = 0f
    private var currentHeadingDeg = 0f
    private var currentPosition = Offset.Zero
    private val cornersList = mutableListOf<Offset>(Offset.Zero)
    private val walkedCornersList = mutableListOf<WalkedCorner>()
    private var calculatedArea = 0

    private var lastAccelMagnitude = 9.8f
    private var lastStepTime = 0L

    fun getSnapshot(): WalkerSnapshot {
        val wallDist = if (manualWallDistance > 0f) {
            manualWallDistance
        } else {
            (currentWallSteps * strideLengthMeters * 10f).roundToInt() / 10f
        }
        return WalkerSnapshot(
            isTracking = isTracking,
            totalSteps = totalSteps,
            currentWallSteps = currentWallSteps,
            currentWallDistanceMeters = wallDist,
            currentHeadingDeg = currentHeadingDeg,
            currentPosition = currentPosition,
            corners = cornersList.toList(),
            walkedCorners = walkedCornersList.toList(),
            calculatedAreaM2 = calculatedArea
        )
    }

    private fun emit() {
        onSnapshotChanged(getSnapshot())
    }

    fun startTracking() {
        if (isTracking) return
        isTracking = true

        stepDetector?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        rotationVector?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        if (stepDetector == null) {
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        emit()
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        sensorManager?.unregisterListener(this)
        emit()
    }

    fun reset() {
        totalSteps = 0
        currentWallSteps = 0
        manualWallDistance = 0f
        currentPosition = Offset.Zero
        cornersList.clear()
        walkedCornersList.clear()
        cornersList.add(Offset.Zero)
        calculatedArea = 0
        emit()
    }

    fun adjustCurrentWall(deltaMeters: Float) {
        val currentDist = if (manualWallDistance > 0f) manualWallDistance else currentWallSteps * strideLengthMeters
        val newDist = (currentDist + deltaMeters).coerceIn(1.0f, 15.0f)
        manualWallDistance = (newDist * 10f).roundToInt() / 10f

        val rad = Math.toRadians((currentHeadingDeg - 90.0))
        val lastCorner = cornersList.lastOrNull() ?: Offset.Zero
        currentPosition = Offset(
            x = lastCorner.x + (manualWallDistance * cos(rad)).toFloat(),
            y = lastCorner.y + (manualWallDistance * sin(rad)).toFloat()
        )
        updateArea()
        emit()
    }

    fun markCorner(fallbackDistanceMeters: Float = 4.0f): WalkerSnapshot {
        if (!isTracking) {
            startTracking()
        }

        val wallDistance = when {
            manualWallDistance > 0f -> manualWallDistance
            currentWallSteps > 0 -> (currentWallSteps * strideLengthMeters * 10f).roundToInt() / 10f
            else -> fallbackDistanceMeters
        }

        // If stationary, advance along heading by wall distance
        if (currentPosition == (cornersList.lastOrNull() ?: Offset.Zero)) {
            val rad = Math.toRadians((currentHeadingDeg - 90.0))
            val last = cornersList.lastOrNull() ?: Offset.Zero
            currentPosition = Offset(
                x = last.x + (wallDistance * cos(rad)).toFloat(),
                y = last.y + (wallDistance * sin(rad)).toFloat()
            )
        }

        cornersList.add(currentPosition)
        walkedCornersList.add(
            WalkedCorner(
                position = currentPosition,
                wallLengthMeters = wallDistance,
                cornerIndex = cornersList.size - 1
            )
        )

        // Advance heading by 90 degrees for standard room layouts
        currentHeadingDeg = (currentHeadingDeg + 90f) % 360f
        currentWallSteps = 0
        manualWallDistance = 0f

        updateArea()
        val snap = getSnapshot()
        emit()
        return snap
    }

    fun finishRoom(): Int {
        if (cornersList.size >= 3) {
            updateArea()
        }
        stopTracking()
        val finalArea = calculatedArea.coerceIn(10, 80)
        emit()
        return finalArea
    }

    private fun updateArea() {
        if (cornersList.size < 3) {
            val d1 = walkedCornersList.firstOrNull()?.wallLengthMeters ?: 4.0f
            val d2 = manualWallDistance.takeIf { it > 0f } ?: (currentWallSteps * strideLengthMeters).takeIf { it > 0f } ?: 4.0f
            calculatedArea = (d1 * d2).roundToInt().coerceIn(10, 80)
            return
        }

        var areaSum = 0.0
        val poly = cornersList.toMutableList()
        poly.add(poly.first())

        for (i in 0 until poly.size - 1) {
            val p1 = poly[i]
            val p2 = poly[i + 1]
            areaSum += (p1.x * p2.y - p2.x * p1.y)
        }

        calculatedArea = abs(areaSum / 2.0).roundToInt().coerceIn(10, 100)
    }

    private fun processStep() {
        totalSteps++
        currentWallSteps++

        val rad = Math.toRadians((currentHeadingDeg - 90.0))
        val dx = (strideLengthMeters * cos(rad)).toFloat()
        val dy = (strideLengthMeters * sin(rad)).toFloat()

        currentPosition = Offset(currentPosition.x + dx, currentPosition.y + dy)
        updateArea()
        emit()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isTracking) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> processStep()
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                currentHeadingDeg = (azimuthDeg + 360f) % 360f
                emit()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                val now = System.currentTimeMillis()

                if (magnitude - lastAccelMagnitude > 2.4f && now - lastStepTime > 320L) {
                    lastStepTime = now
                    processStep()
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
