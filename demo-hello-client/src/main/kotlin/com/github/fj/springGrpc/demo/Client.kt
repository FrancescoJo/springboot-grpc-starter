package com.github.fj.springGrpc.demo

import com.github.fj.springGrpc.protocol.hello.HelloRequest
import com.github.fj.springGrpc.protocol.hello.HelloResponse
import com.github.fj.springGrpc.protocol.hello.HelloServiceGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
fun main(args: Array<String>) {
    ClientApplication().run()
}

class ClientApplication {
    fun run() {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50031)
            .usePlaintext()
            .build()

        val stub = HelloServiceGrpc.newBlockingStub(channel)
        val request = HelloRequest.newBuilder().setName("Example").build()
        val response: HelloResponse

        try {
            response = stub.greet(request)
        } catch (e: StatusRuntimeException) {
            if (e.status.code == Status.Code.UNAVAILABLE) {
                println("Connection is unavailable")
            }

            e.printStackTrace()
            return
        }
        println("Response.message: " + response.message)
    }
}
