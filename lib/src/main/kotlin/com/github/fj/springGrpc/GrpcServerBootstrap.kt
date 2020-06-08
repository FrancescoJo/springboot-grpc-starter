package com.github.fj.springGrpc

import com.github.fj.springGrpc.annotation.GrpcInterceptor
import com.github.fj.springGrpc.annotation.GrpcService
import com.github.fj.springGrpc.config.GrpcServerConfig
import com.google.common.base.Joiner
import io.grpc.BindableService
import io.grpc.ServerInterceptor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * A bootstrap class that automatically initialised if `grpc.enabled` property is found and set to be `true`.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@ConditionalOnBean(value = [GrpcServerConfig::class], annotation = [GrpcService::class])
class GrpcServerBootstrap(
    @Inject private val context: ApplicationContext
) {
    @Bean
    internal fun grpcServerRunner(): GrpcServerRunner {
        return GrpcServerRunner(getConfig())
    }

    private fun getConfig(): RunnerConfig {
        val config = context.getBeansOfType(GrpcServerConfig::class.java).let {
            if (it.isEmpty()) {
                throw UnsupportedOperationException(
                    "No ${GrpcServerConfig::class.simpleName} was found. " +
                            "Please define a @Configuration that implements '${GrpcServerConfig::class}'."
                )
            }

            if (it.size > 1) {
                throw UnsupportedOperationException(
                    "Multiple ${GrpcServerConfig::class.simpleName} implementation were found."
                )
            }

            return@let it.values.first()
        }

        val checkedPortNo = checkPortNumber(config.portNumber)
        val interceptors = populateInterceptors().also {
            LOG.info("{} global gRPC interceptor instance(s) were found.", it.first.size)
            LOG.info("{} gRPC interceptor instance(s) were found.", it.second.size)
        }
        val checkedSvcs = populateServiceBeans(interceptors).also {
            if (it.isEmpty()) {
                LOG.warn("gRPC server is enabled but no gRPC services were found.")
                return@also
            }

            LOG.info("{} gRPC service instance(s) were found.", it.size)
        }

        return RunnerConfig(
            portNumber = checkedPortNo,
            serviceBeans = checkedSvcs,
            globalInterceptors = interceptors.first
        )
    }

    private fun populateServiceBeans(interceptorPairs: InterceptorPairs): ServicesMap {
        val svcsMap = LinkedHashMap<BindableService, List<ServerInterceptor>>()
        val failedInstances = ArrayList<Any>()

        // Grpc service bind definition
        val targetClass = BindableService::class

        context.getBeansWithAnnotation(GrpcService::class.java).values.forEach {
            if (!it::class.isSubclassOf(targetClass)) {
                failedInstances.add(it)
                return@forEach
            }

            val globalInterceptors = interceptorPairs.first
            val localInterceptors = interceptorPairs.second
            val svcInterceptors = requireNotNull(it::class.findAnnotation<GrpcService>()).interceptors

            val interceptorStack = ArrayList<ServerInterceptor>()

            svcInterceptors.forEach svcInterceptorLoop@{ klass ->
                if (globalInterceptors.containsKey(klass)) {
                    LOG.warn(
                        "Global interceptor {} is registered as service interceptor. This definition is ignored.",
                        globalInterceptors[klass]
                    )
                    return@svcInterceptorLoop
                }

                if (!localInterceptors.containsKey(klass)) {
                    return@svcInterceptorLoop
                }

                interceptorStack.add(requireNotNull(localInterceptors[klass]))
            }

            svcsMap[it as BindableService] = interceptorStack
        }

        if (failedInstances.isEmpty()) {
            return svcsMap
        }

        LOG.error("Following bean(s) is/are not a subtype of ${targetClass.simpleName}.")
        LOG.error("Read https://grpc.io/docs/tutorials/basic/java/#implementing-routeguide to fix this error.")
        throw logAndFail(targetClass, failedInstances)
    }

    private fun checkPortNumber(port: Int): Int {
        require(port in 1..65535) {
            "server.port is not configured or must be a positive 16-bit number."
        }

        if (port in 1..1024) {
            LOG.warn("server.port is configured as '{}'.", port)
            LOG.warn("It is not recommended to run with port number under 1024, because it may require a superuser access that could cause security problem(s).")
        }

        return port
    }

    private fun populateInterceptors(): InterceptorPairs {
        val globalBeans = LinkedHashMap<KClass<*>, ServerInterceptor>()
        val localBeans = LinkedHashMap<KClass<*>, ServerInterceptor>()
        val failedInstances = ArrayList<Any>()

        // Grpc service bind definition
        val targetClass = ServerInterceptor::class

        context.getBeansWithAnnotation(GrpcInterceptor::class.java).values.forEach {
            if (!it::class.isSubclassOf(targetClass)) {
                failedInstances.add(it)
                return@forEach
            }

            val isGlobal = requireNotNull(it::class.findAnnotation<GrpcInterceptor>()).isGlobal
            val container = if (isGlobal) {
                globalBeans
            } else {
                localBeans
            }

            container[it::class] = it as ServerInterceptor
        }

        if (failedInstances.isEmpty()) {
            return InterceptorPairs(globalBeans, localBeans)
        }

        LOG.error("Following bean(s) is/are not a subtype of ${targetClass.simpleName}.")
        throw logAndFail(targetClass, failedInstances)
    }

    private fun logAndFail(targetClass: KClass<*>, failedInstances: List<Any>): UnsupportedOperationException {
        val failedClasses = failedInstances.map { it::class }
        failedClasses.forEach {
            LOG.error("Not a subtype of ${targetClass}: {}", it)
        }

        throw UnsupportedOperationException(
            "Not a subtype of ${targetClass}: [${Joiner.on(",").join(failedClasses)}]"
        )
    }

    companion object {
        internal val LOG = LoggerFactory.getLogger(GrpcServerBootstrap::class.java)
    }
}

/**
 * First: global, Second: local
 */
private typealias InterceptorPairs = Pair<InterceptorsMap, InterceptorsMap>
