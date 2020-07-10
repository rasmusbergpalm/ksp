import kotlin.math.abs

fun newton(
    f: (Double) -> Double,
    fprime: (Double) -> Double,
    tolerance: Double,
    maxIter: Int,
    init: Double
): Double {

    var x = init
    for (i in 0 until maxIter) {
        x -= f(x) / fprime(x)
        val e = abs(f(x))
        if (e <= tolerance) {
            return x
        }
    }

    throw IllegalStateException("No solution found")
}

fun finiteDifference(fn: (Double) -> Double, eps: Double = 1e-3): (Double) -> Double {
    return { x -> (fn(x + eps) - fn(x - eps)) / (2 * eps) }
}
