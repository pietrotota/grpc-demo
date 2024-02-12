package it.pietrot.grpcdemo

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import it.pietrot.grpcdemo.service.client.PingClientService
import it.pietrot.grpcdemo.service.server.PingServerService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

// executor service where client code will run
val clientExecutorService: ExecutorService = Executors.newFixedThreadPool(10)
val serverExecutorService: ExecutorService = Executors.newSingleThreadExecutor()
const val serverPort = 10000
private val logger = LoggerFactory.getLogger("MAIN")

fun main() {
    addPingClient()
    addStreamClient()
    startServer()
    clientExecutorService.shutdown()
    serverExecutorService.shutdown()
}

fun addPingClient() {
    repeat(3) {
        val pingClient =
            PingClientService(
                ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build()
            )
        clientExecutorService.submit {
            repeat(10) { CoroutineScope(Dispatchers.IO).launch { pingClient.ping(it) } }
        }
    }
}

fun addStreamClient() {
    val pingClient =
        PingClientService(
            ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build()
        )
    clientExecutorService.submit {
        CoroutineScope(Dispatchers.IO).launch {
            val streamResponse = pingClient.stream(10)
            streamResponse.collect { logger.info("Collected server response: $it") }
        }
    }
}

fun startServer() {
    val server = ServerBuilder.forPort(serverPort).addService(PingServerService()).build()
    serverExecutorService.submit {
        logger.info("Starting server ...")
        server.start()
        logger.info("Server started and listening on port $serverPort")
    }
    server.awaitTermination()
}
