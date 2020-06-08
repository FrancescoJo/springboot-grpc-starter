package com.github.fj.springGrpc.demo.service

import com.github.fj.springGrpc.protocol.hello.HelloRequest
import com.github.fj.springGrpc.protocol.hello.HelloResponse
import com.github.fj.springGrpc.protocol.hello.HelloServiceGrpc
import com.github.fj.springGrpc.annotation.GrpcService
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@GrpcService
class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {
    override fun greet(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        LOG.debug("Request: name = {}", request.name)

        val reply = HelloResponse.newBuilder()
            .setMessage("Hello " + request.name)
            .build()

        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HelloServiceGrpc::class.java)
    }
}
