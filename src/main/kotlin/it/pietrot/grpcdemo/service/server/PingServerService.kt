package it.pietrot.grpcdemo.service.server

import it.pietrot.grpcdemo.generated.*
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory

class PingServerService : TestRpcGrpcKt.TestRpcCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun ping(request: TestMessage): TestResponse {
        logger.info("Received request message: $request")
        return TestResponse.newBuilder()
            .setFlag(Math.random() > 0.5)
            .setDetail(request.detailList.toList().joinToString { it.toString() })
            .build()
    }

    override fun stream(requests: Flow<StreamInput>): Flow<StreamOutput> {
        return requests.map {
            val delay = Duration.ofMillis((Math.random() * 1000).toLong())
            logger.info("Responding to: ${it.request} with a delay of $delay")
            delay(delay)
            StreamOutput.newBuilder().apply { response = it.request + " - RESPONSE" }.build()
        }
    }
}
