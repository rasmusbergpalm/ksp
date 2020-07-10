import krpc.client.Connection
import krpc.client.services.KRPC


fun main() {
    Connection.newInstance("client", "192.168.0.21").use { connection ->
        val krpc = KRPC.newInstance(connection)
        println("Connected to kRPC version " + krpc.status.version)
        suicideBurn(connection, 0.9)
        println("done")
    }
}