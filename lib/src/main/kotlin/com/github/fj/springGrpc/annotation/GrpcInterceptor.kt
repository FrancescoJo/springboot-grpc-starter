package com.github.fj.springGrpc.annotation

import org.springframework.stereotype.Component

/**
 * An annotation for all gRPC interceptor implementations. Annotated classes must implement [io.grpc.ServerInterceptor],
 * otherwise server startup would be failed.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class GrpcInterceptor(
    /**
     * Marks annotated interceptor should be work on every calls if this value is set to `true`.
     *
     * Default is `false`.
     */
    val isGlobal: Boolean = false
)
