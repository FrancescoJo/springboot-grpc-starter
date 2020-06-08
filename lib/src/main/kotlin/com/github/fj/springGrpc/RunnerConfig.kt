package com.github.fj.springGrpc

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
internal data class RunnerConfig(
    val portNumber: Int,

    val serviceBeans: ServicesMap,

    val globalInterceptors: InterceptorsMap
)
