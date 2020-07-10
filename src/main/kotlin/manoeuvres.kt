import krpc.client.Connection
import krpc.client.Stream
import krpc.client.services.SpaceCenter
import org.javatuples.Pair
import org.javatuples.Triplet
import kotlin.math.min
import kotlin.math.pow

/**
 * Perform a suicide burn manoeuvre: fall towards the ground until the last moment then burn at ~full throttle.
 *
 * @param connection a kRPC connection
 * @param aggressiveness How aggressively to perform the suicide burn. A number between 0 and 1. The assumed throttle usage during the final burn.
 *
 * The observed throttle usage will be lower than the aggressiveness parameter due to approximations in the computations. The approximations in order of importance are
 *  * No atmosphere (+) (atmosphere induces drag)
 *  * Constant mass (+) (rocket burns fuel and becomes lighter)
 *  * Constant engine force (-) (engine output drops in denser atmosphere)
 *  * Constant gravity (-) (gravity increases the closer you are to the ground)
 *
 *  The plus and minus signs indicate whether these approximations assume the ship will accelerate faster towards the ground than it really does (+), or otherwise (-).
 *  The plus signs are "safe" approximations since the resulting solution means the ship starts burning earlier, but reduce effectiveness (average throttle). The minus signs
 *  have the opposite effect. The plus sign approximations are greater in magnitude so the solutions tend to be relatively conservative.
 */
fun suicideBurn(connection: Connection, aggressiveness: Double = 0.9) {
    assert(0.0 < aggressiveness && aggressiveness < 1.0)


    val spaceCenter = SpaceCenter.newInstance(connection)
    val vessel = spaceCenter.activeVessel
    val body = vessel.orbit.body
    val control = vessel.control
    val gravitationalParameter = body.gravitationalParameter.toDouble()
    val flight = vessel.flight(body.referenceFrame)
    val velocity: Stream<Double> = connection.addStream(flight, "getVerticalSpeed")
    val altitude: Stream<Double> = connection.addStream(flight, "getSurfaceAltitude")
    val mass: Stream<Float> = connection.addStream(vessel, "getMass")
    val radius: Stream<Float> = connection.addStream(vessel.orbit, "getRadius")
    val maxThrust: Stream<Float> = connection.addStream(vessel, "getMaxThrust")
    val boundingBox: Stream<Pair<Triplet<Double, Double, Double>, Triplet<Double, Double, Double>>> = connection.addStream(vessel, "getBoundingBox", vessel.referenceFrame)


    while (velocity.get() > 0) {
        Thread.sleep(1000)
        println("waiting for negative velocity...")
    }

    control.sas = true
    control.speedMode = SpaceCenter.SpeedMode.SURFACE
    control.sasMode = SpaceCenter.SASMode.RETROGRADE

    /**
     * Altitude is measured from CoM, but we need it from the lowest point of the vehicle.
     */
    fun altitudeFromLowestPoint(): Double {
        val bb = boundingBox.get()
        val distanceToLowestPoint = -min(bb.value0.value1, bb.value1.value1)
        return altitude.get() - distanceToLowestPoint
    }

    fun gravity(): Double {
        return -gravitationalParameter / radius.get().pow(2)
    }


    do { //Waiting loop
        Thread.sleep(20)
        val (tw, tb) = suicideBurnConstantAcceleration(gravity(), maxThrust.get() * aggressiveness / mass.get().toDouble(), velocity.get(), altitudeFromLowestPoint())
        println("$tw seconds until $tb seconds burn.")
    } while (tw > 0.1 && tw + tb > 2.0) //Reserve at least 2s for burning

    val throttles = mutableListOf<Float>()
    while (velocity.get() < -1.0) { //Burning/Throttling loop
        val a = constantAcceleration(gravity(), velocity.get(), altitudeFromLowestPoint())
        val f = mass.get() * a
        val throttle = (f / maxThrust.get()).toFloat()
        vessel.control.throttle = throttle
        throttles.add(throttle)
    }

    vessel.control.throttle = 0.0f
    println("Average throttle (effectiveness): ${throttles.average()}")
}
