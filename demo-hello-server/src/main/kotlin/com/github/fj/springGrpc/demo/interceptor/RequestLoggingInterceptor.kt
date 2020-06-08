package com.github.fj.springGrpc.demo.interceptor

import com.github.fj.springGrpc.annotation.GrpcInterceptor
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.slf4j.LoggerFactory

/**
 * Naïve demo of intercepting all entry/exit points of RPC calls.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@GrpcInterceptor(isGlobal = true)
class RequestLoggingInterceptor : ServerInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> = object : SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
        var startedTime = 0L

        override fun onMessage(req: ReqT) {
            if (!LOG.isDebugEnabled) {
                return
            }

            startedTime = System.currentTimeMillis()
            LOG.debug(">>>> Invoking {}", call.methodDescriptor.fullMethodName)
            super.onMessage(req)
        }

        override fun onComplete() {
            if (!LOG.isDebugEnabled) {
                return
            }

            val delta = System.currentTimeMillis() - startedTime
            LOG.debug("<<<< Finished {}, elapsed {} ms", call.methodDescriptor.fullMethodName, delta)
            super.onComplete()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)
    }
}
