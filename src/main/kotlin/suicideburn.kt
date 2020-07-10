import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * Compute the time to wait before applying constant throttle*engineForce force such that altitude will be zero when velocity is zero.
 * Takes into account changing mass and gravitational pull.
 *
 * Note: not used at the moment
 */
fun suicideBurn(
    altitude: Double,
    velocity: Double,
    engineForce: Double,
    startMass: Double,
    fuelConsumptionRate: Double,
    gravitationalParameter: Double,
    bodyRadius: Double,
    desiredAltitude: Double,
    throttle: Double
): Pair<Double, List<State>> {

    fun trajectory(waitTime: Double): List<State> {
        val waitTrajectory = simulateTrajectory(
            altitude,
            velocity,
            0.0,
            startMass,
            0.0,
            gravitationalParameter,
            bodyRadius,
            { it.time < waitTime }
        )

        val waitEnd = waitTrajectory.last()
        val burnTrajectory = simulateTrajectory(
            waitEnd.altitude,
            waitEnd.velocity,
            engineForce * throttle,
            startMass,
            fuelConsumptionRate * throttle,
            gravitationalParameter,
            bodyRadius,
            { it.velocity < 0.0 }
        )

        return waitTrajectory + burnTrajectory.map { it.copy(time = it.time + waitEnd.time) }
    }

    val error = fun(waitTime: Double): Double {
        return trajectory(waitTime).last().altitude - desiredAltitude
    }

    val (init, _) = suicideBurnConstantAcceleration(
        -gravitationalParameter / (bodyRadius + altitude).pow(2),
        engineForce * throttle / startMass,
        velocity,
        altitude
    )

    val waitTime = newton(error, finiteDifference(error, 1.0 / 25), 1.0, 200, init)
    return Pair(waitTime, trajectory(waitTime))
}

/**
 * Compute the constant acceleration such that altitude will be zero when velocity is zero. Assumes everything is constant except altitude and velocity.
 */
fun constantAcceleration(
    gravity: Double,
    velocity: Double,
    altitude: Double
): Double {
    assert(gravity < 0) { "gravity must be negative" }
    return (velocity.pow(2)) / (2 * altitude) - gravity
}


/**
 * Compute the time to wait before accelerating with a constant acceleration such that altitude will be zero when velocity is zero. Assumes everything is constant except altitude and velocity.
 */
fun suicideBurnConstantAcceleration(
    gravity: Double,
    acceleration: Double,
    velocity: Double,
    altitude: Double
): Pair<Double, Double> {
    assert(gravity < 0) { "gravity must be negative" }

    val totalAcceleration = acceleration + gravity
    val qa = 1.0 / 2.0 * gravity * (1 - gravity / totalAcceleration)
    val qb = velocity * (1 - gravity / totalAcceleration)
    val qc = altitude - velocity.pow(2) / (2 * totalAcceleration)

    val twp = (-qb + sqrt(qb.pow(2) - 4 * qa * qc)) / (2 * qa)
    val twm = (-qb - sqrt(qb.pow(2) - 4 * qa * qc)) / (2 * qa)

    val tw = max(twp, twm)

    val vb = velocity + gravity * tw
    val tb = -vb / totalAcceleration

    return Pair(tw, tb)
}

