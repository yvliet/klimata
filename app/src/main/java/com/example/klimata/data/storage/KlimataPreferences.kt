package com.example.klimata.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.klimata.data.AcProfile
import com.example.klimata.data.CarbonEquivalence
import com.example.klimata.data.DispatchState
import com.example.klimata.data.EnergyConfig
import com.example.klimata.data.ImpactMetric
import com.example.klimata.data.RoomState
import com.example.klimata.data.SavingsBreakdown
import com.example.klimata.data.ThermalStep
import com.example.klimata.data.TimeBucketedImpact
import com.example.klimata.data.network.AmbientWeatherReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object KlimataPreferences {

    private const val PREFS_NAME = "klimata_engine_prefs"
    private const val KEY_ROOMS_JSON = "saved_rooms_json"
    private const val KEY_WEATHER_JSON = "cached_weather_json"
    private const val KEY_ENERGY_CONFIG_JSON = "energy_config_json"
    private const val KEY_GEMINI_API_KEY = "custom_gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveGeminiApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }

    fun loadGeminiApiKey(context: Context): String? {
        return getPrefs(context).getString(KEY_GEMINI_API_KEY, null)
    }

    private const val KEY_DISABLE_OS_STATUS_BAR = "disable_os_status_bar"

    fun isStatusBarDisabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DISABLE_OS_STATUS_BAR, false)
    }

    fun setStatusBarDisabled(context: Context, disabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DISABLE_OS_STATUS_BAR, disabled).apply()
    }

    fun saveEnergyConfig(context: Context, config: EnergyConfig) {
        try {
            val obj = JSONObject().apply {
                put("countryCode", config.countryCode)
                put("currencyCode", config.currencyCode)
                put("currencySymbol", config.currencySymbol)
                put("tariffPerKwh", config.tariffPerKwh)
                put("gridEmissionFactor", config.gridEmissionFactor.toDouble())
                put("currencyRateToUsd", config.currencyRateToUsd)
            }
            getPrefs(context).edit().putString(KEY_ENERGY_CONFIG_JSON, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadEnergyConfig(context: Context): EnergyConfig {
        val jsonStr = getPrefs(context).getString(KEY_ENERGY_CONFIG_JSON, null)
            ?: return EnergyConfig.resolveForLocale()
        return try {
            val obj = JSONObject(jsonStr)
            EnergyConfig(
                countryCode = obj.optString("countryCode", "ID"),
                currencyCode = obj.optString("currencyCode", "IDR"),
                currencySymbol = obj.optString("currencySymbol", "Rp"),
                tariffPerKwh = obj.optDouble("tariffPerKwh", 1445.0),
                gridEmissionFactor = obj.optDouble("gridEmissionFactor", 0.78).toFloat(),
                currencyRateToUsd = obj.optDouble("currencyRateToUsd", 15600.0)
            )
        } catch (e: Exception) {
            EnergyConfig.resolveForLocale()
        }
    }

    suspend fun saveRoomsAsync(context: Context, rooms: List<RoomState>) = withContext(Dispatchers.IO) {
        saveRooms(context, rooms)
    }

    suspend fun loadRoomsAsync(context: Context): List<RoomState> = withContext(Dispatchers.IO) {
        loadRooms(context)
    }

    suspend fun saveWeatherAsync(context: Context, report: AmbientWeatherReport) = withContext(Dispatchers.IO) {
        saveWeather(context, report)
    }

    suspend fun loadWeatherAsync(context: Context): AmbientWeatherReport? = withContext(Dispatchers.IO) {
        loadWeather(context)
    }

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
        val jsonStr = getPrefs(context).getString(KEY_ROOMS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            if (jsonArray.length() == 0) return emptyList()

            val result = mutableListOf<RoomState>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(deserializeRoom(obj))
            }
            result
        } catch (e: Exception) {
            emptyList()
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
                put("gridStatus", report.gridStatus)
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
                gridStatus = obj.optString("gridStatus", "Grid Normal"),
                lastUpdatedMillis = obj.optLong("lastUpdatedMillis", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun serializeTimeBucketedImpact(impact: TimeBucketedImpact): JSONObject {
        return JSONObject().apply {
            put("weeklyPrimary", impact.weekly.primaryValue)
            put("weeklySub", impact.weekly.subtitle)
            put("monthlyPrimary", impact.monthly.primaryValue)
            put("monthlySub", impact.monthly.subtitle)
            put("yearlyPrimary", impact.yearly.primaryValue)
            put("yearlySub", impact.yearly.subtitle)
            put("lifetimePrimary", impact.lifetime.primaryValue)
            put("lifetimeSub", impact.lifetime.subtitle)
        }
    }

    private fun deserializeTimeBucketedImpact(
        obj: JSONObject?,
        titlePrefix: String,
        defaultPrimary: String,
        defaultSub: String
    ): TimeBucketedImpact {
        if (obj == null) {
            val metric = ImpactMetric(title = titlePrefix, primaryValue = defaultPrimary, subtitle = defaultSub)
            return TimeBucketedImpact(weekly = metric, monthly = metric, yearly = metric, lifetime = metric)
        }
        return TimeBucketedImpact(
            weekly = ImpactMetric(title = "$titlePrefix (Weekly)", primaryValue = obj.optString("weeklyPrimary", defaultPrimary), subtitle = obj.optString("weeklySub", defaultSub)),
            monthly = ImpactMetric(title = "$titlePrefix (Monthly)", primaryValue = obj.optString("monthlyPrimary", defaultPrimary), subtitle = obj.optString("monthlySub", defaultSub)),
            yearly = ImpactMetric(title = "$titlePrefix (Yearly)", primaryValue = obj.optString("yearlyPrimary", defaultPrimary), subtitle = obj.optString("yearlySub", defaultSub)),
            lifetime = ImpactMetric(title = "$titlePrefix (Lifetime)", primaryValue = obj.optString("lifetimePrimary", defaultPrimary), subtitle = obj.optString("lifetimeSub", defaultSub))
        )
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
                put("irCodeSet", room.profile.irCodeSet)
                put("fanSpeed", room.profile.fanSpeed)
                put("swing", room.profile.swing)
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

            put("savingsHistory", serializeTimeBucketedImpact(room.savingsHistory))
            put("carbonHistory", serializeTimeBucketedImpact(room.carbonHistory))

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
        val profile = AcProfile(
            roomName = profJson.getString("roomName"),
            brand = profJson.getString("brand"),
            model = profJson.getString("model"),
            capacity = profJson.getString("capacity"),
            inverterType = profJson.getString("inverterType"),
            currentSetpoint = profJson.getInt("currentSetpoint"),
            mode = profJson.getString("mode"),
            irCodeSet = profJson.optString("irCodeSet").takeIf { !it.isNullOrBlank() && it != "null" },
            fanSpeed = profJson.optString("fanSpeed", "Auto").ifBlank { "Auto" },
            swing = profJson.optBoolean("swing", false)
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

        val savingsHistory = deserializeTimeBucketedImpact(
            obj.optJSONObject("savingsHistory"),
            titlePrefix = "Cost Reduction",
            defaultPrimary = monthlySavings.primaryValue,
            defaultSub = monthlySavings.subtitle
        )

        val carbonHistory = deserializeTimeBucketedImpact(
            obj.optJSONObject("carbonHistory"),
            titlePrefix = "Emissions Avoided",
            defaultPrimary = avoidedCarbon.primaryValue,
            defaultSub = avoidedCarbon.subtitle
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
            SavingsBreakdown(
                compressorCyclingPercent = 65,
                fanCoastingPercent = 35,
                avgNightlyKwhSaved = 1.85f,
                localTariffPerKwh = "Rp 1.445"
            )
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
            CarbonEquivalence(
                treesEquivalent = 1.4f,
                drivingKmAvoided = 138.5f,
                ledHoursEquivalent = 57.2f,
                gridEmissionFactor = 0.78f
            )
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
            savingsHistory = savingsHistory,
            carbonHistory = carbonHistory,
            savingsBreakdown = savingsBreakdown,
            carbonEquivalence = carbonEquivalence,
            areaSquareMeters = obj.optInt("areaSquareMeters", 20),
            ceilingHeightMeters = obj.optDouble("ceilingHeightMeters", 2.8).toFloat(),
            thermalMassLabel = obj.optString("thermalMassLabel", "Medium Thermal Mass"),
            coolingLoadBtu = obj.optInt("coolingLoadBtu", 7000)
        )
    }
}
