package com.example.klimata.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.klimata.data.ACProfile
import com.example.klimata.data.CarbonEquivalence
import com.example.klimata.data.DispatchState
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.MockData
import com.example.klimata.data.RoomState
import com.example.klimata.data.SavingsBreakdown
import com.example.klimata.data.ThermalStep
import com.example.klimata.data.TimeBucketedImpact
import com.example.klimata.data.network.AmbientWeatherReport
import org.json.JSONArray
import org.json.JSONObject

object KlimataPreferences {

    private const val PREFS_NAME = "klimata_engine_prefs"
    private const val KEY_ROOMS_JSON = "saved_rooms_json"
    private const val KEY_WEATHER_JSON = "cached_weather_json"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveRooms(context: Context, rooms: List<RoomState>) {
        try {
            val jsonArray = JSONArray()
            rooms.forEach { room ->
                jsonArray.put(serializeRoom(room))
            }
            getPrefs(context).edit().putString(KEY_ROOMS_JSON, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadRooms(context: Context): List<RoomState> {
        val jsonStr = getPrefs(context).getString(KEY_ROOMS_JSON, null) ?: return MockData.rooms
        return try {
            val jsonArray = JSONArray(jsonStr)
            if (jsonArray.length() == 0) return MockData.rooms

            val result = mutableListOf<RoomState>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(deserializeRoom(obj))
            }
            if (result.isEmpty()) MockData.rooms else result
        } catch (e: Exception) {
            MockData.rooms
        }
    }

    fun saveWeather(context: Context, report: AmbientWeatherReport) {
        try {
            val obj = JSONObject().apply {
                put("location", report.location)
                put("latitude", report.latitude)
                put("longitude", report.longitude)
                put("currentOutdoorTemp", report.currentOutdoorTemp)
                put("highTemp", report.highTemp)
                put("lowTemp", report.lowTemp)
                put("relativeHumidity", report.relativeHumidity)
                put("condition", report.condition)
                put("aqiValue", report.aqiValue)
                put("aqiLabel", report.aqiLabel)
                put("lastUpdatedMillis", report.lastUpdatedMillis)

                val hourlyObj = JSONObject()
                report.hourlyTemps.forEach { (hour, temp) ->
                    hourlyObj.put(hour, temp)
                }
                put("hourlyTemps", hourlyObj)
            }
            getPrefs(context).edit().putString(KEY_WEATHER_JSON, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadWeather(context: Context): AmbientWeatherReport? {
        val jsonStr = getPrefs(context).getString(KEY_WEATHER_JSON, null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            val hourlyObj = obj.optJSONObject("hourlyTemps")
            val hourlyMap = mutableMapOf<String, Int>()
            if (hourlyObj != null) {
                val keys = hourlyObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    hourlyMap[k] = hourlyObj.getInt(k)
                }
            }

            AmbientWeatherReport(
                location = obj.optString("location", "South Jakarta"),
                latitude = obj.optDouble("latitude", -6.2088),
                longitude = obj.optDouble("longitude", 106.8456),
                currentOutdoorTemp = obj.optInt("currentOutdoorTemp", 29),
                highTemp = obj.optInt("highTemp", 34),
                lowTemp = obj.optInt("lowTemp", 24),
                relativeHumidity = obj.optInt("relativeHumidity", 72),
                condition = obj.optString("condition", "Clear Night"),
                aqiValue = obj.optInt("aqiValue", 101),
                aqiLabel = obj.optString("aqiLabel", "Moderate"),
                hourlyTemps = hourlyMap,
                lastUpdatedMillis = obj.optLong("lastUpdatedMillis", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun serializeRoom(room: RoomState): JSONObject {
        return JSONObject().apply {
            put("id", room.id)
            put("name", room.name)
            put("location", room.location)
            put("currentTemp", room.currentTemp)
            put("targetTemp", room.targetTemp)
            put("isPowerOn", room.isPowerOn)
            put("isEcoEnabled", room.isEcoEnabled)
            put("weatherCondition", room.weatherCondition)
            put("areaSquareMeters", room.areaSquareMeters)
            put("ceilingHeightMeters", room.ceilingHeightMeters.toDouble())
            put("thermalMassLabel", room.thermalMassLabel)
            put("coolingLoadBtu", room.coolingLoadBtu)

            val profObj = JSONObject().apply {
                put("roomName", room.profile.roomName)
                put("brand", room.profile.brand)
                put("model", room.profile.model)
                put("capacity", room.profile.capacity)
                put("inverterType", room.profile.inverterType)
                put("currentSetpoint", room.profile.currentSetpoint)
                put("mode", room.profile.mode)
            }
            put("profile", profObj)

            val dispObj = JSONObject().apply {
                put("isAutonomous", room.dispatchState.isAutonomous)
                put("statusLabel", room.dispatchState.statusLabel)
                put("dispatchMethod", room.dispatchState.dispatchMethod)
            }
            put("dispatchState", dispObj)

            val stepsArr = JSONArray()
            room.thermalSteps.forEach { step ->
                val sObj = JSONObject().apply {
                    put("time", step.time)
                    put("setpointCelsius", step.setpointCelsius)
                    put("label", step.label)
                    put("phaseName", step.phaseName)
                    put("fanMode", step.fanMode)
                    put("fanDetail", step.fanDetail)
                    put("outdoorTemp", step.outdoorTemp)
                    put("deltaLabel", step.deltaLabel)
                    put("isActive", step.isActive)
                    put("isCompleted", step.isCompleted)
                }
                stepsArr.put(sObj)
            }
            put("thermalSteps", stepsArr)

            put("monthlySavingsPrimary", room.monthlySavings.primaryValue)
            put("monthlySavingsSubtitle", room.monthlySavings.subtitle)
            put("avoidedCarbonPrimary", room.avoidedCarbon.primaryValue)
            put("avoidedCarbonSubtitle", room.avoidedCarbon.subtitle)

            val sbObj = JSONObject().apply {
                put("compressorCyclingPercent", room.savingsBreakdown.compressorCyclingPercent)
                put("fanCoastingPercent", room.savingsBreakdown.fanCoastingPercent)
                put("avgNightlyKwhSaved", room.savingsBreakdown.avgNightlyKwhSaved.toDouble())
                put("localTariffPerKwh", room.savingsBreakdown.localTariffPerKwh)
            }
            put("savingsBreakdown", sbObj)

            val ceObj = JSONObject().apply {
                put("treesEquivalent", room.carbonEquivalence.treesEquivalent.toDouble())
                put("drivingKmAvoided", room.carbonEquivalence.drivingKmAvoided.toDouble())
                put("ledHoursEquivalent", room.carbonEquivalence.ledHoursEquivalent.toDouble())
                put("gridEmissionFactor", room.carbonEquivalence.gridEmissionFactor.toDouble())
            }
            put("carbonEquivalence", ceObj)
        }
    }

    private fun deserializeRoom(obj: JSONObject): RoomState {
        val profJson = obj.getJSONObject("profile")
        val profile = ACProfile(
            roomName = profJson.getString("roomName"),
            brand = profJson.getString("brand"),
            model = profJson.getString("model"),
            capacity = profJson.getString("capacity"),
            inverterType = profJson.getString("inverterType"),
            currentSetpoint = profJson.getInt("currentSetpoint"),
            mode = profJson.getString("mode")
        )

        val dispJson = obj.getJSONObject("dispatchState")
        val dispatch = DispatchState(
            isAutonomous = dispJson.getBoolean("isAutonomous"),
            statusLabel = dispJson.getString("statusLabel"),
            dispatchMethod = dispJson.getString("dispatchMethod")
        )

        val stepsArr = obj.getJSONArray("thermalSteps")
        val steps = mutableListOf<ThermalStep>()
        for (i in 0 until stepsArr.length()) {
            val s = stepsArr.getJSONObject(i)
            steps.add(
                ThermalStep(
                    time = s.getString("time"),
                    setpointCelsius = s.getInt("setpointCelsius"),
                    label = s.getString("label"),
                    phaseName = s.getString("phaseName"),
                    fanMode = s.getString("fanMode"),
                    fanDetail = s.getString("fanDetail"),
                    outdoorTemp = s.getInt("outdoorTemp"),
                    deltaLabel = s.getString("deltaLabel"),
                    isActive = s.optBoolean("isActive", false),
                    isCompleted = s.optBoolean("isCompleted", false)
                )
            )
        }

        val monthlySavings = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = obj.optString("monthlySavingsPrimary", "Rp 84.500"),
            subtitle = obj.optString("monthlySavingsSubtitle", "$5.40 USD / -38% kWh")
        )

        val avoidedCarbon = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = obj.optString("avoidedCarbonPrimary", "34.2 kg"),
            subtitle = obj.optString("avoidedCarbonSubtitle", "CO₂e offset")
        )

        val sbJson = obj.optJSONObject("savingsBreakdown")
        val savingsBreakdown = if (sbJson != null) {
            SavingsBreakdown(
                compressorCyclingPercent = sbJson.optInt("compressorCyclingPercent", 65),
                fanCoastingPercent = sbJson.optInt("fanCoastingPercent", 35),
                avgNightlyKwhSaved = sbJson.optDouble("avgNightlyKwhSaved", 1.85).toFloat(),
                localTariffPerKwh = sbJson.optString("localTariffPerKwh", "Rp 1.445")
            )
        } else {
            MockData.masterBedRoom.savingsBreakdown
        }

        val ceJson = obj.optJSONObject("carbonEquivalence")
        val carbonEquivalence = if (ceJson != null) {
            CarbonEquivalence(
                treesEquivalent = ceJson.optDouble("treesEquivalent", 1.4).toFloat(),
                drivingKmAvoided = ceJson.optDouble("drivingKmAvoided", 138.5).toFloat(),
                ledHoursEquivalent = ceJson.optDouble("ledHoursEquivalent", 57.2).toFloat(),
                gridEmissionFactor = ceJson.optDouble("gridEmissionFactor", 0.78).toFloat()
            )
        } else {
            MockData.masterBedRoom.carbonEquivalence
        }

        return RoomState(
            id = obj.getString("id"),
            name = obj.getString("name"),
            location = obj.optString("location", "South Jakarta"),
            profile = profile,
            currentTemp = obj.getInt("currentTemp"),
            targetTemp = obj.getInt("targetTemp"),
            isPowerOn = obj.optBoolean("isPowerOn", true),
            isEcoEnabled = obj.optBoolean("isEcoEnabled", true),
            weatherCondition = obj.optString("weatherCondition", "Clear Night"),
            thermalSteps = steps,
            dispatchState = dispatch,
            monthlySavings = monthlySavings,
            avoidedCarbon = avoidedCarbon,
            savingsHistory = MockData.masterBedRoom.savingsHistory.copy(monthly = monthlySavings),
            carbonHistory = MockData.masterBedRoom.carbonHistory.copy(monthly = avoidedCarbon),
            savingsBreakdown = savingsBreakdown,
            carbonEquivalence = carbonEquivalence,
            areaSquareMeters = obj.optInt("areaSquareMeters", 20),
            ceilingHeightMeters = obj.optDouble("ceilingHeightMeters", 2.8).toFloat(),
            thermalMassLabel = obj.optString("thermalMassLabel", "Medium Thermal Mass"),
            coolingLoadBtu = obj.optInt("coolingLoadBtu", 7000)
        )
    }
}
