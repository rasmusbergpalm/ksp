import kotlin.math.pow

data class State(
    val time: Double,
    val altitude: Double,
    val velocity: Double
)

private fun dxdt(
    state: State,
    engineForce: Double,
    startMass: Double,
    fuelConsumptionRate: Double,
    gravitationalParameter: Double,
    bodyRadius: Double
): Pair<Double, Double> {
    val (time, alt, v) = state
    val currentMass = startMass - fuelConsumptionRate * time
    val gravitationalForce = -gravitationalParameter * currentMass / (alt + bodyRadius).pow(2)
    val totalForce = engineForce + gravitationalForce
    val a = totalForce / currentMass

    return Pair(v, a)
}


fun simulateTrajectory(
    altitude: Double,
    velocity: Double,
    engineForce: Double,
    startMass: Double,
    fuelConsumptionRate: Double,
    gravitationalParameter: Double,
    bodyRadius: Double,
    condition: (State) -> Boolean,
    dt: Double = (1.0 / 1000)
): List<State> {
    var state = State(0.0, altitude, velocity)
    val trajectory = mutableListOf(state)

    while (condition(state)) {
        val (dx, dv) = dxdt(state, engineForce, startMass, fuelConsumptionRate, gravitationalParameter, bodyRadius)
        state = State(state.time + dt, state.altitude + dx * dt, state.velocity + dv * dt)
        trajectory.add(state)
    }

    return trajectory
}