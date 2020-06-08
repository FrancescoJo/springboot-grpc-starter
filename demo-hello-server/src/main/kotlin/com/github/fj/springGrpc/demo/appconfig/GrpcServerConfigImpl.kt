package com.github.fj.springGrpc.demo.appconfig

import com.github.fj.springGrpc.config.GrpcServerConfig
import org.springframework.context.annotation.Configuration

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
@Configuration
class GrpcServerConfigImpl : GrpcServerConfig {
    override val portNumber: Int
        get() = 50031
}
