import org.junit.Assert
import org.junit.Test
import kotlin.test.assertTrue

class SuicideburnKtTest {

    @Test
    fun testSB() {
        val engineForce = 214_999.9844
        val fuelConsumptionRate = 68.51
        val startMass = 10628.88184
        val velocity = -20.71433646
        val position = 1359.997895
        val gravitationalParameter = 6.5138398e10
        val bodyRadius = 200_000.0

        val (tw, trajectory) = suicideBurn(
            position,
            velocity,
            engineForce,
            startMass,
            fuelConsumptionRate,
            gravitationalParameter,
            bodyRadius,
            0.0,
            1.0
        )

        assertTrue(tw > 0)
        assertTrue(trajectory.isNotEmpty())
        Assert.assertEquals(trajectory.last().altitude, 0.0, 1.0)
        Assert.assertEquals(trajectory.last().velocity, 0.0, 1.0)
    }
}