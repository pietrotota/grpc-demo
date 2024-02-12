package it.pietrot.grpcdemo.service.client

import io.grpc.ManagedChannel
import it.pietrot.grpcdemo.generated.*
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory

class PingClientService(
    private val channel: ManagedChannel,
    private val stub: TestRpcGrpcKt.TestRpcCoroutineStub =
        TestRpcGrpcKt.TestRpcCoroutineStub(channel)
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    suspend fun ping(counter: Int) {
        val delay = Duration.ofMillis((Math.random() * 1000).toLong())
        logger.info("Awaiting $delay before sending message to server")
        delay(delay)
        val testMessage =
            TestMessage.newBuilder()
                .apply {
                    flag = true
                    enumeration = Enumeration.ENUM_2
                }
                .addDetail("message with progressive id $counter")
                .build()
        logger.info("Sending request $testMessage")
        val testResponse = stub.ping(testMessage)
        logger.info(
            "Received response message flag: ${testResponse.flag}, with detail: ${testResponse.detail}"
        )
    }

    fun stream(count: Int): Flow<StreamOutput> {

        val streamResponse =
            stub.stream(
                listOf(StreamInput.newBuilder().apply { request = "Test message $count" }.build())
                    .asFlow()
            )
        logger.info("Created flow $streamResponse")
        return streamResponse
    }
}
