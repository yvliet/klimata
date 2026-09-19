package com.example.klimata.data

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class AmbientWeather(
    val location: String,
    val condition: String,
    val currentOutdoorTemp: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val aqiValue: Int,
    val aqiLabel: String,
    val gridStatus: String,
)

@Immutable
data class ACProfile(
    val roomName: String,
    val brand: String,
    val model: String,
    val capacity: String,
    val inverterType: String,
    val currentSetpoint: Int,
    val mode: String,
    val irCodeSet: String? = null,
)

@Immutable
data class ThermalStep(
    val time: String,
    val setpointCelsius: Int,
    val label: String,
    val phaseName: String,
    val fanMode: String,
    val fanDetail: String,
    val outdoorTemp: Int,
    val deltaLabel: String,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
)

@Immutable
data class ImpactMetric(
    val title: String,
    val primaryValue: String,
    val subtitle: String,
)

@Immutable
data class TimeBucketedImpact(
    val weekly: ImpactMetric,
    val monthly: ImpactMetric,
    val yearly: ImpactMetric,
    val lifetime: ImpactMetric,
) {
    fun forPeriod(period: ImpactPeriod): ImpactMetric = when (period) {
        ImpactPeriod.WEEKLY -> weekly
        ImpactPeriod.MONTHLY -> monthly
        ImpactPeriod.YEARLY -> yearly
        ImpactPeriod.LIFETIME -> lifetime
    }
}

@Immutable
data class SavingsBreakdown(
    val compressorCyclingPercent: Int,
    val fanCoastingPercent: Int,
    val avgNightlyKwhSaved: Float,
    val localTariffPerKwh: String,
)

@Immutable
data class CarbonEquivalence(
    val treesEquivalent: Float,
    val drivingKmAvoided: Float,
    val ledHoursEquivalent: Float,
    val gridEmissionFactor: Float,
)

@Immutable
data class DispatchState(
    val isAutonomous: Boolean,
    val statusLabel: String,
    val dispatchMethod: String,
)

@Immutable
data class RoomState(
    val id: String,
    val name: String,
    val location: String = "South Jakarta",
    val profile: ACProfile,
    val currentTemp: Int,
    val targetTemp: Int,
    val isPowerOn: Boolean = true,
    val isEcoEnabled: Boolean = true,
    val weatherCondition: String = "Clear",
    val thermalSteps: List<ThermalStep>,
    val dispatchState: DispatchState,
    val monthlySavings: ImpactMetric,
    val avoidedCarbon: ImpactMetric,
    val savingsHistory: TimeBucketedImpact,
    val carbonHistory: TimeBucketedImpact,
    val savingsBreakdown: SavingsBreakdown,
    val carbonEquivalence: CarbonEquivalence,
    val areaSquareMeters: Int = 20,
    val ceilingHeightMeters: Float = 2.8f,
    val thermalMassLabel: String = "Medium Thermal Mass",
    val coolingLoadBtu: Int = 7000,
)

val RoomState.volumeCubicMeters: Int
    get() = (areaSquareMeters * ceilingHeightMeters).roundToInt()

object MockData {

    val weather = AmbientWeather(
        location = "South Jakarta",
        condition = "Clear Night",
        currentOutdoorTemp = 29,
        highTemp = 34,
        lowTemp = 24,
        aqiValue = 101,
        aqiLabel = "Moderate",
        gridStatus = "Grid Normal",
    )

    val acProfile = ACProfile(
        roomName = "Master Bed",
        brand = "Daikin",
        model = "FTKF25",
        capacity = "1.0 PK",
        inverterType = "Eco Inverter",
        currentSetpoint = 24,
        mode = "Cool",
    )

    val thermalSteps = listOf(
        ThermalStep(
            time = "21:00",
            setpointCelsius = 24,
            label = "Pre-Cool",
            phaseName = "Pre-Cool",
            fanMode = "High Fan",
            fanDetail = "Rapid Cool",
            outdoorTemp = 30,
            deltaLabel = "-6°C",
            isActive = false,
        ),
        ThermalStep(
            time = "22:00",
            setpointCelsius = 24,
            label = "Pre-Cool",
            phaseName = "Pre-Cool",
            fanMode = "High Fan",
            fanDetail = "Rapid Cool",
            outdoorTemp = 29,
            deltaLabel = "-5°C",
            isActive = false,
        ),
        ThermalStep(
            time = "23:00",
            setpointCelsius = 24,
            label = "Pre-Cool",
            phaseName = "Pre-Cool",
            fanMode = "Auto Fan",
            fanDetail = "Equilibrate",
            outdoorTemp = 28,
            deltaLabel = "-4°C",
        ),
        ThermalStep(
            time = "00:00",
            setpointCelsius = 25,
            label = "Drift",
            phaseName = "Drift",
            fanMode = "Auto Fan",
            fanDetail = "Metabolic Sync",
            outdoorTemp = 28,
            deltaLabel = "-3°C",
        ),
        ThermalStep(
            time = "01:00",
            setpointCelsius = 25,
            label = "Drift",
            phaseName = "Drift",
            fanMode = "Auto Fan",
            fanDetail = "Deep Sleep",
            outdoorTemp = 27,
            deltaLabel = "-2°C",
        ),
        ThermalStep(
            time = "02:00",
            setpointCelsius = 25,
            label = "Drift",
            phaseName = "Drift",
            fanMode = "Quiet Fan",
            fanDetail = "Whisper",
            outdoorTemp = 26,
            deltaLabel = "-1°C",
        ),
        ThermalStep(
            time = "03:00",
            setpointCelsius = 26,
            label = "Ambient",
            phaseName = "Ambient",
            fanMode = "Quiet Fan",
            fanDetail = "Trough Sync",
            outdoorTemp = 25,
            deltaLabel = "+1°C",
        ),
        ThermalStep(
            time = "04:00",
            setpointCelsius = 26,
            label = "Ambient",
            phaseName = "Ambient",
            fanMode = "Quiet Fan",
            fanDetail = "Trough Sync",
            outdoorTemp = 25,
            deltaLabel = "+1°C",
        ),
        ThermalStep(
            time = "05:00",
            setpointCelsius = 26,
            label = "Ambient",
            phaseName = "Ambient",
            fanMode = "Quiet Fan",
            fanDetail = "Trough Sync",
            outdoorTemp = 24,
            deltaLabel = "+2°C",
        ),
        ThermalStep(
            time = "06:00",
            setpointCelsius = 0,
            label = "Coast",
            phaseName = "Coast",
            fanMode = "Circulation",
            fanDetail = "Fan Only",
            outdoorTemp = 24,
            deltaLabel = "Coast",
        ),
        ThermalStep(
            time = "07:00",
            setpointCelsius = 0,
            label = "Coast",
            phaseName = "Coast",
            fanMode = "Circulation",
            fanDetail = "Fan Only",
            outdoorTemp = 25,
            deltaLabel = "Coast",
        ),
    )

    val savingsMetric = ImpactMetric(
        title = "Monthly Savings",
        primaryValue = "Rp 84.500",
        subtitle = "$5.40 USD / -38% kWh",
    )

    val carbonMetric = ImpactMetric(
        title = "Avoided Carbon",
        primaryValue = "34.2 kg",
        subtitle = "CO₂e offset / 1.4 trees equiv",
    )

    val dispatch = DispatchState(
        isAutonomous = true,
        statusLabel = "Autonomous Autopilot",
        dispatchMethod = "IR Blaster",
    )

    val roomList: List<String> = listOf("Master Bed", "Living Room", "Study")

    val supportedWeatherConditions: List<String> = listOf("Overcast", "Partly Cloudy", "Clear Night")

    val masterBedRoom = RoomState(
        id = "room_master_bed",
        name = "Master Bed",
        location = "South Jakarta",
        profile = acProfile,
        currentTemp = 24,
        targetTemp = 24,
        isPowerOn = true,
        weatherCondition = "Overcast",
        thermalSteps = thermalSteps,
        dispatchState = dispatch,
        monthlySavings = savingsMetric,
        avoidedCarbon = carbonMetric,
        savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Weekly Savings", "Rp 19.600", "$1.25 USD / -38% kWh"),
            monthly = savingsMetric,
            yearly = ImpactMetric("Yearly Savings", "Rp 1.014.000", "$64.80 USD / -38% kWh"),
            lifetime = ImpactMetric("Lifetime Savings", "Rp 676.000", "$43.20 USD / 8 months active"),
        ),
        carbonHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Avoided Carbon", "8.0 kg", "CO₂e offset / 0.3 trees equiv"),
            monthly = carbonMetric,
            yearly = ImpactMetric("Avoided Carbon", "410.4 kg", "CO₂e offset / 16.8 trees equiv"),
            lifetime = ImpactMetric("Avoided Carbon", "273.6 kg", "CO₂e offset / 11.2 trees equiv"),
        ),
        savingsBreakdown = SavingsBreakdown(
            compressorCyclingPercent = 64,
            fanCoastingPercent = 36,
            avgNightlyKwhSaved = 1.85f,
            localTariffPerKwh = "Rp 1.445",
        ),
        carbonEquivalence = CarbonEquivalence(
            treesEquivalent = 1.4f,
            drivingKmAvoided = 138.5f,
            ledHoursEquivalent = 57.2f,
            gridEmissionFactor = 0.78f,
        ),
        areaSquareMeters = 20,
        ceilingHeightMeters = 2.8f,
        thermalMassLabel = "Medium Thermal Mass",
        coolingLoadBtu = 7000,
    )

    val livingRoom = RoomState(
        id = "room_living",
        name = "Living Room",
        location = "South Jakarta",
        profile = ACProfile(
            roomName = "Living Room",
            brand = "Panasonic",
            model = "CS-XU18XKH",
            capacity = "2.0 PK",
            inverterType = "Inverter Aero",
            currentSetpoint = 23,
            mode = "Cool",
        ),
        currentTemp = 23,
        targetTemp = 23,
        isPowerOn = true,
        weatherCondition = "Partly Cloudy",
        thermalSteps = listOf(
            ThermalStep(
                time = "21:00",
                setpointCelsius = 23,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "High Fan",
                fanDetail = "Rapid Cool",
                outdoorTemp = 30,
                deltaLabel = "-7°C",
                isActive = false,
            ),
            ThermalStep(
                time = "22:00",
                setpointCelsius = 23,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "High Fan",
                fanDetail = "Rapid Cool",
                outdoorTemp = 29,
                deltaLabel = "-6°C",
                isActive = true,
            ),
            ThermalStep(
                time = "23:00",
                setpointCelsius = 23,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Equilibrate",
                outdoorTemp = 28,
                deltaLabel = "-5°C",
            ),
            ThermalStep(
                time = "00:00",
                setpointCelsius = 24,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Auto Fan",
                fanDetail = "Metabolic Sync",
                outdoorTemp = 28,
                deltaLabel = "-4°C",
            ),
            ThermalStep(
                time = "01:00",
                setpointCelsius = 24,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Auto Fan",
                fanDetail = "Deep Sleep",
                outdoorTemp = 27,
                deltaLabel = "-3°C",
            ),
            ThermalStep(
                time = "02:00",
                setpointCelsius = 24,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Quiet Fan",
                fanDetail = "Whisper",
                outdoorTemp = 26,
                deltaLabel = "-2°C",
            ),
            ThermalStep(
                time = "03:00",
                setpointCelsius = 25,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "0°C",
            ),
            ThermalStep(
                time = "04:00",
                setpointCelsius = 25,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "0°C",
            ),
            ThermalStep(
                time = "05:00",
                setpointCelsius = 25,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 24,
                deltaLabel = "+1°C",
            ),
            ThermalStep(
                time = "06:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 24,
                deltaLabel = "Coast",
            ),
            ThermalStep(
                time = "07:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 25,
                deltaLabel = "Coast",
            ),
        ),
        dispatchState = DispatchState(
            isAutonomous = true,
            statusLabel = "Tuya Cloud Sync",
            dispatchMethod = "Tuya Wi-Fi Hub",
        ),
        monthlySavings = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = "Rp 112.000",
            subtitle = "$7.15 USD / -42% kWh",
        ),
        avoidedCarbon = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = "48.6 kg",
            subtitle = "CO₂e offset / 2.1 trees equiv",
        ),
        savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Weekly Savings", "Rp 26.000", "$1.65 USD / -42% kWh"),
            monthly = ImpactMetric("Monthly Savings", "Rp 112.000", "$7.15 USD / -42% kWh"),
            yearly = ImpactMetric("Yearly Savings", "Rp 1.344.000", "$85.80 USD / -42% kWh"),
            lifetime = ImpactMetric("Lifetime Savings", "Rp 896.000", "$57.20 USD / 8 months active"),
        ),
        carbonHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Avoided Carbon", "11.3 kg", "CO₂e offset / 0.5 trees equiv"),
            monthly = ImpactMetric("Avoided Carbon", "48.6 kg", "CO₂e offset / 2.1 trees equiv"),
            yearly = ImpactMetric("Avoided Carbon", "583.2 kg", "CO₂e offset / 25.2 trees equiv"),
            lifetime = ImpactMetric("Avoided Carbon", "388.8 kg", "CO₂e offset / 16.8 trees equiv"),
        ),
        savingsBreakdown = SavingsBreakdown(
            compressorCyclingPercent = 70,
            fanCoastingPercent = 30,
            avgNightlyKwhSaved = 2.45f,
            localTariffPerKwh = "Rp 1.445",
        ),
        carbonEquivalence = CarbonEquivalence(
            treesEquivalent = 2.1f,
            drivingKmAvoided = 196.8f,
            ledHoursEquivalent = 81.0f,
            gridEmissionFactor = 0.78f,
        ),
        areaSquareMeters = 35,
        ceilingHeightMeters = 3.0f,
        thermalMassLabel = "High Thermal Inertia",
        coolingLoadBtu = 15000,
    )

    val studyRoom = RoomState(
        id = "room_study",
        name = "Study",
        location = "South Jakarta",
        profile = ACProfile(
            roomName = "Study",
            brand = "Mitsubishi Electric",
            model = "MSY-GR13VF",
            capacity = "1.5 PK",
            inverterType = "Mr. Slim Inverter",
            currentSetpoint = 25,
            mode = "Auto",
        ),
        currentTemp = 25,
        targetTemp = 25,
        isPowerOn = false,
        weatherCondition = "Clear Night",
        thermalSteps = listOf(
            ThermalStep(
                time = "21:00",
                setpointCelsius = 25,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Balanced Cool",
                outdoorTemp = 30,
                deltaLabel = "-5°C",
                isActive = false,
            ),
            ThermalStep(
                time = "22:00",
                setpointCelsius = 25,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Balanced Cool",
                outdoorTemp = 29,
                deltaLabel = "-4°C",
                isActive = true,
            ),
            ThermalStep(
                time = "23:00",
                setpointCelsius = 25,
                label = "Pre-Cool",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Balanced Cool",
                outdoorTemp = 28,
                deltaLabel = "-3°C",
            ),
            ThermalStep(
                time = "00:00",
                setpointCelsius = 26,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Quiet Fan",
                fanDetail = "Metabolic Sync",
                outdoorTemp = 28,
                deltaLabel = "-2°C",
            ),
            ThermalStep(
                time = "01:00",
                setpointCelsius = 26,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Quiet Fan",
                fanDetail = "Deep Sleep",
                outdoorTemp = 27,
                deltaLabel = "-1°C",
            ),
            ThermalStep(
                time = "02:00",
                setpointCelsius = 26,
                label = "Drift",
                phaseName = "Drift",
                fanMode = "Quiet Fan",
                fanDetail = "Whisper",
                outdoorTemp = 26,
                deltaLabel = "0°C",
            ),
            ThermalStep(
                time = "03:00",
                setpointCelsius = 26,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "+1°C",
            ),
            ThermalStep(
                time = "04:00",
                setpointCelsius = 26,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 25,
                deltaLabel = "+1°C",
            ),
            ThermalStep(
                time = "05:00",
                setpointCelsius = 26,
                label = "Ambient",
                phaseName = "Ambient",
                fanMode = "Quiet Fan",
                fanDetail = "Trough Sync",
                outdoorTemp = 24,
                deltaLabel = "+2°C",
            ),
            ThermalStep(
                time = "06:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 24,
                deltaLabel = "Coast",
            ),
            ThermalStep(
                time = "07:00",
                setpointCelsius = 0,
                label = "Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 25,
                deltaLabel = "Coast",
            ),
        ),
        dispatchState = DispatchState(
            isAutonomous = false,
            statusLabel = "Standby (Off)",
            dispatchMethod = "Home Assistant",
        ),
        monthlySavings = ImpactMetric(
            title = "Monthly Savings",
            primaryValue = "Rp 42.800",
            subtitle = "$2.70 USD / -22% kWh",
        ),
        avoidedCarbon = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = "18.4 kg",
            subtitle = "CO₂e offset / 0.8 trees equiv",
        ),
        savingsHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Weekly Savings", "Rp 9.900", "$0.63 USD / -22% kWh"),
            monthly = ImpactMetric("Monthly Savings", "Rp 42.800", "$2.70 USD / -22% kWh"),
            yearly = ImpactMetric("Yearly Savings", "Rp 513.600", "$32.40 USD / -22% kWh"),
            lifetime = ImpactMetric("Lifetime Savings", "Rp 342.400", "$21.60 USD / 8 months active"),
        ),
        carbonHistory = TimeBucketedImpact(
            weekly = ImpactMetric("Avoided Carbon", "4.3 kg", "CO₂e offset / 0.2 trees equiv"),
            monthly = ImpactMetric("Avoided Carbon", "18.4 kg", "CO₂e offset / 0.8 trees equiv"),
            yearly = ImpactMetric("Avoided Carbon", "220.8 kg", "CO₂e offset / 9.6 trees equiv"),
            lifetime = ImpactMetric("Avoided Carbon", "147.2 kg", "CO₂e offset / 6.4 trees equiv"),
        ),
        savingsBreakdown = SavingsBreakdown(
            compressorCyclingPercent = 58,
            fanCoastingPercent = 42,
            avgNightlyKwhSaved = 0.95f,
            localTariffPerKwh = "Rp 1.445",
        ),
        carbonEquivalence = CarbonEquivalence(
            treesEquivalent = 0.8f,
            drivingKmAvoided = 74.5f,
            ledHoursEquivalent = 30.6f,
            gridEmissionFactor = 0.78f,
        ),
        areaSquareMeters = 14,
        ceilingHeightMeters = 2.7f,
        thermalMassLabel = "Low Thermal Mass",
        coolingLoadBtu = 4800,
    )

    val rooms: List<RoomState> = listOf(masterBedRoom, livingRoom, studyRoom)

    val roomProfiles: Map<String, ACProfile> = rooms.associateBy({ it.name }, { it.profile })

    fun getRoom(name: String): RoomState =
        rooms.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: masterBedRoom
}
