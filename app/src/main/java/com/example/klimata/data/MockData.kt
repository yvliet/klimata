package com.example.klimata.data

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

data class ACProfile(
    val roomName: String,
    val brand: String,
    val model: String,
    val capacity: String,
    val inverterType: String,
    val currentSetpoint: Int,
    val mode: String,
)

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

data class ImpactMetric(
    val title: String,
    val primaryValue: String,
    val subtitle: String,
)

data class DispatchState(
    val isAutonomous: Boolean,
    val statusLabel: String,
    val dispatchMethod: String,
)

data class RoomState(
    val id: String,
    val name: String,
    val location: String = "South Jakarta",
    val profile: ACProfile,
    val currentTemp: Int,
    val targetTemp: Int,
    val isPowerOn: Boolean = true,
    val weatherCondition: String = "Clear",
    val thermalSteps: List<ThermalStep>,
    val dispatchState: DispatchState,
    val monthlySavings: ImpactMetric,
    val avoidedCarbon: ImpactMetric,
)

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
        mode = "Eco Flow",
    )

    val thermalSteps = listOf(
        ThermalStep(
            time = "22:00",
            setpointCelsius = 24,
            label = "Pre-Cooling",
            phaseName = "Pre-Cool",
            fanMode = "High Fan",
            fanDetail = "Rapid Cool",
            outdoorTemp = 29,
            deltaLabel = "-5°C",
            isActive = true,
            isCompleted = false,
        ),
        ThermalStep(
            time = "01:00",
            setpointCelsius = 25,
            label = "Metabolic Sync",
            phaseName = "Sync",
            fanMode = "Auto Fan",
            fanDetail = "Deep Sleep",
            outdoorTemp = 27,
            deltaLabel = "-2°C",
        ),
        ThermalStep(
            time = "03:30",
            setpointCelsius = 26,
            label = "Ambient Relief",
            phaseName = "Relief",
            fanMode = "Quiet Fan",
            fanDetail = "Whisper",
            outdoorTemp = 25,
            deltaLabel = "+1°C",
        ),
        ThermalStep(
            time = "05:30",
            setpointCelsius = 0, // 0 denotes compressor bypass (ventilation mode) where setpoint payload is ignored
            label = "Thermal Coast",
            phaseName = "Coast",
            fanMode = "Circulation",
            fanDetail = "Fan Only",
            outdoorTemp = 24,
            deltaLabel = "Coast",
        ),
    )

    val savingsMetric = ImpactMetric(
        title = "Monthly Savings",
        primaryValue = "Rp 84.500",
        subtitle = "$5.40 USD • -38% kWh",
    )

    val carbonMetric = ImpactMetric(
        title = "Avoided Carbon",
        primaryValue = "34.2 kg",
        subtitle = "CO₂e offset • 1.4 trees equiv",
    )

    val dispatch = DispatchState(
        isAutonomous = true,
        statusLabel = "Autonomous Autopilot",
        dispatchMethod = "IR Blaster",
    )

    val roomList: List<String> = listOf("Master Bed", "Living Room", "Study")

    val masterBedRoom = RoomState(
        id = "room_master_bed",
        name = "Master Bed",
        location = "South Jakarta",
        profile = acProfile,
        currentTemp = 24,
        targetTemp = 24,
        isPowerOn = true,
        weatherCondition = "Clear Night",
        thermalSteps = thermalSteps,
        dispatchState = dispatch,
        monthlySavings = savingsMetric,
        avoidedCarbon = carbonMetric,
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
            mode = "Powerful Cool",
        ),
        currentTemp = 23,
        targetTemp = 23,
        isPowerOn = true,
        weatherCondition = "Partly Cloudy",
        thermalSteps = listOf(
            ThermalStep(
                time = "22:00",
                setpointCelsius = 23,
                label = "Pre-Cooling",
                phaseName = "Pre-Cool",
                fanMode = "High Fan",
                fanDetail = "Rapid Cool",
                outdoorTemp = 29,
                deltaLabel = "-6°C",
                isActive = true,
            ),
            ThermalStep(
                time = "01:00",
                setpointCelsius = 24,
                label = "Metabolic Sync",
                phaseName = "Sync",
                fanMode = "Auto Fan",
                fanDetail = "Deep Sleep",
                outdoorTemp = 27,
                deltaLabel = "-3°C",
            ),
            ThermalStep(
                time = "03:30",
                setpointCelsius = 25,
                label = "Ambient Relief",
                phaseName = "Relief",
                fanMode = "Quiet Fan",
                fanDetail = "Whisper",
                outdoorTemp = 25,
                deltaLabel = "0°C",
            ),
            ThermalStep(
                time = "05:30",
                setpointCelsius = 0,
                label = "Thermal Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 24,
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
            subtitle = "$7.15 USD • -42% kWh",
        ),
        avoidedCarbon = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = "48.6 kg",
            subtitle = "CO₂e offset • 2.1 trees equiv",
        ),
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
            mode = "Quiet Eco",
        ),
        currentTemp = 25,
        targetTemp = 25,
        isPowerOn = false,
        weatherCondition = "Clear Night",
        thermalSteps = listOf(
            ThermalStep(
                time = "22:00",
                setpointCelsius = 25,
                label = "Pre-Cooling",
                phaseName = "Pre-Cool",
                fanMode = "Auto Fan",
                fanDetail = "Balanced Cool",
                outdoorTemp = 29,
                deltaLabel = "-4°C",
                isActive = true,
            ),
            ThermalStep(
                time = "01:00",
                setpointCelsius = 26,
                label = "Metabolic Sync",
                phaseName = "Sync",
                fanMode = "Quiet Fan",
                fanDetail = "Deep Sleep",
                outdoorTemp = 27,
                deltaLabel = "-1°C",
            ),
            ThermalStep(
                time = "03:30",
                setpointCelsius = 26,
                label = "Ambient Relief",
                phaseName = "Relief",
                fanMode = "Quiet Fan",
                fanDetail = "Whisper",
                outdoorTemp = 25,
                deltaLabel = "+1°C",
            ),
            ThermalStep(
                time = "05:30",
                setpointCelsius = 0,
                label = "Thermal Coast",
                phaseName = "Coast",
                fanMode = "Circulation",
                fanDetail = "Fan Only",
                outdoorTemp = 24,
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
            subtitle = "$2.70 USD • -22% kWh",
        ),
        avoidedCarbon = ImpactMetric(
            title = "Avoided Carbon",
            primaryValue = "18.4 kg",
            subtitle = "CO₂e offset • 0.8 trees equiv",
        ),
    )

    val rooms: List<RoomState> = listOf(masterBedRoom, livingRoom, studyRoom)

    val roomProfiles: Map<String, ACProfile> = rooms.associateBy({ it.name }, { it.profile })

    fun getRoom(name: String): RoomState =
        rooms.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: masterBedRoom
}
