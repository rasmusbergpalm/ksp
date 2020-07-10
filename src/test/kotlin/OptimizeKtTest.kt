import org.junit.Assert
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class OptimizeKtTest {

    @Test
    fun newton() {
        val root = newton({ x -> sin(x) }, { x -> cos(x) }, 1e-6, 200, 2.0)
        Assert.assertEquals(root, PI, 1e-5)
    }

}
