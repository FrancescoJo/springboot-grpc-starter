package com.github.fj.springGrpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.CommandLineRunner

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
internal open class GrpcServerRunner(
    private val config: RunnerConfig
) : CommandLineRunner, DisposableBean {
    private lateinit var server: Server

    override fun run(vararg args: String?) {
        LOG.info("Starting gRPC server on port {}...", config.portNumber)

        this.server = ServerBuilder.forPort(config.portNumber)
            .addGlobalInterceptors(config.globalInterceptors)
            .addServices(config.serviceBeans)
            .build()

        with(server) {
            start()
            LOG.info("gRPC server started")
            awaitTermination()
        }
    }

    override fun destroy() {
        LOG.info("Stopping gRPC server on port {}...", config.portNumber)
        server.shutdown()
        LOG.info("gRPC server stopped")
    }

    private fun ServerBuilder<*>.addServices(services: ServicesMap): ServerBuilder<*> {
        services.forEach {
            LOG.trace("Adding BindableService: {}", it.key)
            addService(it.key)

            it.value.forEach { interceptor ->
                LOG.trace("  with interceptor: {}", interceptor)
            }
            ServerInterceptors.intercept(it.key, it.value)
        }
        return this
    }

    private fun ServerBuilder<*>.addGlobalInterceptors(interceptors: InterceptorsMap): ServerBuilder<*> {
        interceptors.values.forEach {
            LOG.trace("Adding global interceptor: {}", it)
            intercept(it)
        }
        return this
    }

    companion object {
        private val LOG = GrpcServerBootstrap.LOG
    }
}
