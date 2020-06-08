package com.github.fj.springGrpc.annotation

import io.grpc.ServerInterceptor
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

/**
 * An annotation for all gRPC service classes. gRPC service classes are usually extending generated code
 * such as `XXXXGrpc.XXXXImplBase` which implements [io.grpc.BindableService] internally.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Service
annotation class GrpcService(
    vararg val interceptors: KClass<ServerInterceptor> = []
)
